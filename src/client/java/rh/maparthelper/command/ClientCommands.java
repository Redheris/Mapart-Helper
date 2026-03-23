package rh.maparthelper.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.ChatFormatting;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
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
                    player.displayClientMessage(Component.empty(), false);
                    player.displayClientMessage(
                            Component.translatable("maparthelper.commands_list_1")
                                    .withStyle(ChatFormatting.BOLD, ChatFormatting.DARK_AQUA),
                            false
                    );
                    for (int i = 2; i < 8; i++) {
                        player.displayClientMessage(Component.translatable("maparthelper.commands_list_" + i), false);
                    }
                    player.displayClientMessage(Component.empty(), false);
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
                            Component status;
                            if (ClientCommandsContext.showMapartStartPos)
                                status = Component.translatable("maparthelper.beams_on").withStyle(ChatFormatting.GREEN);
                            else
                                status = Component.translatable("maparthelper.beams_off").withStyle(ChatFormatting.RED);
                            ctx.getSource().getPlayer().displayClientMessage(
                                    Component.translatable("maparthelper.beams_change_status", status)
                                            .withStyle(ChatFormatting.DARK_AQUA),
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
                                ctx.getSource().getPlayer().displayClientMessage(Component.translatable(
                                        "maparthelper.blocks_palette_generated").withStyle(ChatFormatting.GREEN),
                                        true
                                );
                                return 1;
                            }))
                        .then(literal("update")
                            .executes(ctx -> {
                                PaletteConfigManager.updateCompletePalette();
                                PaletteConfigManager.readPresetsConfigFile();
                                ctx.getSource().getPlayer().displayClientMessage(Component.translatable(
                                                "maparthelper.presets_config_updated").withStyle(ChatFormatting.GREEN),
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
        assert ctx.getSource().getEntity() instanceof LocalPlayer;

        LocalPlayer player = (LocalPlayer) ctx.getSource().getEntity();
        ItemStack itemStack = player.getMainHandItem();

        if (!(itemStack.getItem() instanceof MapItem))
            itemStack = player.getOffhandItem();
        if (!(itemStack.getItem() instanceof MapItem)) {
            player.displayClientMessage(Component.translatable(
                            "maparthelper.is_holding_filled_map").withStyle(ChatFormatting.RED),
                    true);
            return 0;
        }

        MapItemSavedData mapState = MapItem.getSavedData(itemStack, player.level());
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
        assert ctx.getSource().getEntity() instanceof LocalPlayer;

        LocalPlayer player = (LocalPlayer) ctx.getSource().getEntity();
        byte[] mapColors = MapartToFile.getMapColorsFromItemFrame();

        if (mapColors == null) {
            player.displayClientMessage(Component.translatable(
                            "maparthelper.is_looking_at_frame_with_map").withStyle(ChatFormatting.RED),
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
            ctx.getSource().sendFeedback(Component.translatable("maparthelper.selection_required").withStyle(ChatFormatting.RED));
            return 0;
        }

        String filename = StringArgumentType.getString(ctx, "filename");
        LocalPlayer player = (LocalPlayer)ctx.getSource().getEntity();

        MapartToFile.saveImageFromItemFramesArea(player, player.level(), filename);

        return 1;
    }

    // Save image from the selected area of item frames
    private static int selectFrameArea(CommandContext<FabricClientCommandSource> ctx) {
        assert ctx.getSource().getEntity() instanceof LocalPlayer;
        LocalPlayer player = (LocalPlayer) ctx.getSource().getEntity();

        if (ClientCommandsContext.selectedPos1 != null || ClientCommandsContext.isSelectingFramesArea) {
            ClientCommandsContext.resetSelection();
            player.displayClientMessage(Component.translatable("maparthelper.selecting_stopped").withStyle(ChatFormatting.DARK_AQUA), true);
            return 0;
        }
        ClientCommandsContext.isSelectingFramesArea = true;

        player.displayClientMessage(Component.translatable("maparthelper.pos_selecting").withStyle(ChatFormatting.DARK_AQUA), false);
        player.displayClientMessage(Component.translatable("maparthelper.stop_selecting").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC), false);
        return 1;
    }
}
