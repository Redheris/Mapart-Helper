package rh.maparthelper.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import rh.maparthelper.palette.PaletteDataManager;
import rh.maparthelper.state.FramesAreaSelectionState;
import rh.maparthelper.util.CompatUtils;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

@Environment(EnvType.CLIENT)
public class ClientCommands {

    public static void registerCommands() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
            dispatcher.register(literal("mart")
                .executes(ctx -> {
                    var player = ctx.getSource().getPlayer();
                    CompatUtils.sendMessage(player, Component.empty(), false);
                    CompatUtils.sendMessage(player,
                            Component.translatable("maparthelper.commands_list_1")
                                    .withStyle(ChatFormatting.BOLD, ChatFormatting.DARK_AQUA),
                            false
                    );
                    for (int i = 2; i < 8; i++) {
                        CompatUtils.sendMessage(player, Component.translatable("maparthelper.commands_list_" + i), false);
                    }
                    CompatUtils.sendMessage(player, Component.empty(), false);
                    return 1;
                })
                .then(literal("save")
                    .then(literal("hand")
                        .executes(ClientCommands::saveMapartFromHand)
                            .then(argument("mapart-name", StringArgumentType.string())
                                    .executes(ClientCommands::saveMapartFromHand)))
                    .then(literal("frame")
                        .executes(ClientCommands::saveMapartFromFrame)
                            .then(argument("mapart-name", StringArgumentType.string())
                                    .executes(ClientCommands::saveMapartFromFrame)))
                        .then(literal("selection")
                                .executes(ClientCommands::saveMapartFromFramesArea)
                                .then(argument("mapart-name", StringArgumentType.string())
                                        .executes(ClientCommands::saveMapartFromFramesArea)
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
                            CompatUtils.sendMessage(ctx.getSource().getPlayer(),
                                    Component.translatable("maparthelper.beams_change_status", status)
                                            .withStyle(ChatFormatting.DARK_AQUA),
                                    true);
                            return 1;
                        })
                )
                .then(literal("palette")
                        .then(literal("regenerate")
                            .executes(ctx -> {
                                // Regenerates blocks palette to correspond to the configs and game's blocks list
                                PaletteDataManager.getInstance().updatePaletteGameVersion(true);
                                CompatUtils.sendMessage(ctx.getSource().getPlayer(), Component.translatable(
                                        "maparthelper.blocks_palette_generated").withStyle(ChatFormatting.GREEN),
                                        true
                                );
                                return 1;
                            }))
                        .then(literal("update")
                            .executes(ctx -> {
                                PaletteDataManager.getInstance().updatePaletteAndPresets();
                                CompatUtils.sendMessage(ctx.getSource().getPlayer(), Component.translatable(
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
    private static int saveMapartFromHand(CommandContext<FabricClientCommandSource> ctx) {
        Player player = (Player) ctx.getSource().getEntity();

        try {
            String mapartName = StringArgumentType.getString(ctx, "mapart-name");
            MapartToFile.saveMapImageFromHand(player, mapartName);
        } catch (IllegalArgumentException e) {
            MapartToFile.saveMapImageFromHand(player, null);
        }

        return 1;
    }

    // Save image from the item frames the player is looking at
    private static int saveMapartFromFrame(CommandContext<FabricClientCommandSource> ctx) {
        Player player = (Player) ctx.getSource().getEntity();

        try {
            String mapartName = StringArgumentType.getString(ctx, "mapart-name");
            MapartToFile.saveMapImageFromItemFrame(player, mapartName);
        } catch (IllegalArgumentException e) {
            MapartToFile.saveMapImageFromItemFrame(player, null);
        }

        return 1;
    }

    private static int saveMapartFromFramesArea(CommandContext<FabricClientCommandSource> ctx) {
        Player player = (Player) ctx.getSource().getEntity();
        FramesAreaSelectionState selectionState = FramesAreaSelectionState.getInstance();

        try {
            String mapartName = StringArgumentType.getString(ctx, "mapart-name");
            if (selectionState.getSelectedPos1() == null || selectionState.getSelectedPos2() == null) {
                ctx.getSource().sendFeedback(Component.translatable("map_frames_selection.selection_required").withStyle(ChatFormatting.RED));
                return 0;
            }
            MapartToFile.saveImageFromItemFramesArea(player, mapartName);
        } catch (IllegalArgumentException e) {
            selectionState.selectFramesArea(player);
        }

        return 1;
    }
}
