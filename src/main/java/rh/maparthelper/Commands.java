package rh.maparthelper;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.block.MapColor;
import net.minecraft.entity.Entity;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.map.MapState;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import rh.maparthelper.config.palette.PaletteConfigManager;

import java.util.Set;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;


public class Commands {
    public static void registerCommands() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
            dispatcher.register(literal("mart-server")
                .executes(ctx -> {
                    // Commands list
                    return 1;
                })
                .then(literal("palette")
                    .then(literal("place").then(argument("height", IntegerArgumentType.integer())
                        .executes(ctx -> {
                            // Some kind of debug feature to place and see the complete blocks palette in the world
                            if (ctx.getSource().getEntity() == null || ctx.getSource().getWorld() == null)
                                return 0;

                            int y = IntegerArgumentType.getInteger(ctx, "height");
                            Entity source = ctx.getSource().getEntity();

                            MapUtils.placeBlocksFromPalette(source.getWorld(), source.getBlockX(), y, source.getBlockZ());
                            return 1;
                        })))
                        .then(literal("give-map-palette")
                            .executes(ctx -> {
                                ServerPlayerEntity player = ctx.getSource().getPlayer();
                                assert player != null;

                                ItemStack itemStack = player.getMainHandStack();
                                if (!(itemStack.getItem() instanceof FilledMapItem))
                                    itemStack = player.getOffHandStack();
                                if (!(itemStack.getItem() instanceof FilledMapItem)) {
                                    player.sendMessage(
                                            Text.translatable("maparthelper.is_holding_filled_map").formatted(Formatting.RED),
                                            true
                                    );
                                    return 0;
                                }

                                MapState mapState = FilledMapItem.getMapState(itemStack, player.getWorld());
                                assert mapState != null;
                                for (int i = 0; i < 128; i++) {
                                    for (int j = 0; j < 128; j++) {
                                        mapState.setColor(i, j, MapColor.CLEAR.getRenderColorByte(MapColor.Brightness.NORMAL));
                                    }
                                }

                                Set<MapColor> paletteColors = PaletteConfigManager.palettePresetsConfig.getCurrentPresetColors();
                                var it = paletteColors.iterator();
                                for (int x = 0; x < paletteColors.size(); x++) {
                                    MapColor color = it.next();
                                    for (int i = 0; i < 2; i++) {
                                        mapState.setColor(x * 2 + i, 0, color.getRenderColorByte(MapColor.Brightness.HIGH));
                                        mapState.setColor(x * 2 + i, 1, color.getRenderColorByte(MapColor.Brightness.NORMAL));
                                        mapState.setColor(x * 2 + i, 2, color.getRenderColorByte(MapColor.Brightness.LOW));
                                    }
                                }

                                return 1;
                            }))
                )
            ));
    }
}
