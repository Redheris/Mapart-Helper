package rh.maparthelper.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.map.MapState;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import rh.maparthelper.config.palette.PaletteConfigManager;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

@Environment(EnvType.CLIENT)
public class ClientCommands {

    public static void registerCommands() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
            dispatcher.register(literal("mart")
                .executes(ctx -> {
                    var player = ctx.getSource().getPlayer();
                    player.sendMessage(Text.empty(), false);
                    player.sendMessage(Text.translatable("maparthelper.commands_list_1").formatted(Formatting.BOLD, Formatting.DARK_AQUA), false);
                    for (int i = 2; i < 8; i++) {
                        player.sendMessage(Text.translatable("maparthelper.commands_list_" + i), false);
                    }
                    player.sendMessage(Text.empty(), false);
                    return 1;
                })
                .then(literal("save")
                    .then(literal("hand")
                        .executes(ClientCommands::saveMapFromHand)
                            .then(argument("filename", StringArgumentType.string())
                                    .executes(ClientCommands::saveMapFromHand)))
                    .then(literal("frame")
                        .executes(ClientCommands::saveMapFromFrame)
                            .then(argument("filename", StringArgumentType.string())
                                    .executes(ClientCommands::saveMapFromFrame)))
                    .then(literal("selection")
                        .executes(ClientCommands::selectFrameArea)
                            .then(argument("filename", StringArgumentType.string())
                                    .executes(ClientCommands::saveMapFromFramesArea)
                            )
                    )
                )
                .then(literal("beams")
                        .executes(ctx -> {
                            ClientCommandsContext.showMapartStartPos = !ClientCommandsContext.showMapartStartPos;
                            Text status;
                            if (ClientCommandsContext.showMapartStartPos)
                                status = Text.translatable("maparthelper.beams_on").formatted(Formatting.GREEN);
                            else
                                status = Text.translatable("maparthelper.beams_off").formatted(Formatting.RED);
                            ctx.getSource().getPlayer().sendMessage(
                                    Text.translatable("maparthelper.beams_change_status", status)
                                            .formatted(Formatting.DARK_AQUA),
                                    true
                            );
                            return 1;
                        })
                )
                .then(literal("palette")
                        .then(literal("regenerate")
                            .executes(ctx -> {
                                // Regenerates blocks palette to correspond to the configs and game's blocks list
                                PaletteConfigManager.regenerateCompletePalette();
                                ctx.getSource().getPlayer().sendMessage(Text.translatable(
                                        "maparthelper.blocks_palette_generated").formatted(Formatting.GREEN),
                                        true
                                );
                                return 1;
                            }))
                        .then(literal("update")
                            .executes(ctx -> {
                                PaletteConfigManager.updateCompletePalette();
                                PaletteConfigManager.readPresetsConfigFile();
                                ctx.getSource().getPlayer().sendMessage(Text.translatable(
                                                "maparthelper.presets_config_updated").formatted(Formatting.GREEN),
                                        true
                                );
                                return 1;
                            })
                        )
                )
            ));
    }

    // Save image from the held FilledMapItem
    private static int saveMapFromHand(CommandContext<FabricClientCommandSource> ctx) {
        assert ctx.getSource().getEntity() instanceof ClientPlayerEntity;

        ClientPlayerEntity player = (ClientPlayerEntity) ctx.getSource().getEntity();
        ItemStack itemStack = player.getMainHandStack();

        if (!(itemStack.getItem() instanceof FilledMapItem))
            itemStack = player.getOffHandStack();
        if (!(itemStack.getItem() instanceof FilledMapItem)) {
            player.sendMessage(Text.translatable(
                            "maparthelper.is_holding_filled_map").formatted(Formatting.RED),
                    true);
            return 0;
        }

        MapState mapState = FilledMapItem.getMapState(itemStack, player.getEntityWorld());
        assert mapState != null;
        byte[] mapColors = mapState.colors.clone();

        try {
            String filename = StringArgumentType.getString(ctx, "filename");
            MapartToFile.saveImageFromMapColors(player, mapColors, filename);
        } catch (IllegalArgumentException e) {
            MapartToFile.saveImageFromMapColors(player, mapColors);
        }

        return 1;
    }

    // Save image from the item frames the player is looking at
    private static int saveMapFromFrame(CommandContext<FabricClientCommandSource> ctx) {
        assert ctx.getSource().getEntity() instanceof ClientPlayerEntity;

        ClientPlayerEntity player = (ClientPlayerEntity) ctx.getSource().getEntity();
        byte[] mapColors = MapartToFile.getMapColorsFromItemFrame();

        if (mapColors == null) {
            player.sendMessage(Text.translatable(
                            "maparthelper.is_looking_at_frame_with_map").formatted(Formatting.RED),
                    true);
            return 0;
        }

        try {
            String filename = StringArgumentType.getString(ctx, "filename");
            MapartToFile.saveImageFromMapColors(player, mapColors, filename);
        } catch (IllegalArgumentException e) {
            MapartToFile.saveImageFromMapColors(player, mapColors);
        }

        return 1;
    }

    private static int saveMapFromFramesArea(CommandContext<FabricClientCommandSource> ctx) {
        if (ClientCommandsContext.selectedPos2 == null || ClientCommandsContext.selectedPos1 == null) {
            ctx.getSource().sendFeedback(Text.translatable("maparthelper.selection_required").formatted(Formatting.RED));
            return 0;
        }

        String filename = StringArgumentType.getString(ctx, "filename");
        ClientPlayerEntity player = (ClientPlayerEntity)ctx.getSource().getEntity();

        MapartToFile.saveImageFromItemFramesArea(player, player.getEntityWorld(), filename);

        return 1;
    }

    // Save image from the selected area of item frames
    private static int selectFrameArea(CommandContext<FabricClientCommandSource> ctx) {
        assert ctx.getSource().getEntity() instanceof ClientPlayerEntity;
        ClientPlayerEntity player = (ClientPlayerEntity) ctx.getSource().getEntity();

        if (ClientCommandsContext.selectedPos1 != null || ClientCommandsContext.isSelectingFramesArea) {
            ClientCommandsContext.resetSelection();
            player.sendMessage(Text.translatable("maparthelper.selecting_stopped").formatted(Formatting.DARK_AQUA), true);
            return 0;
        }
        ClientCommandsContext.isSelectingFramesArea = true;

        player.sendMessage(Text.translatable("maparthelper.pos_selecting").formatted(Formatting.DARK_AQUA), false);
        player.sendMessage(Text.translatable("maparthelper.stop_selecting").formatted(Formatting.GRAY, Formatting.ITALIC), false);
        return 1;
    }
}
