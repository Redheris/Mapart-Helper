package rh.maparthelper.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.block.MapColor;
import net.minecraft.component.type.MapIdComponent;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import rh.maparthelper.server.MapCreator;
import rh.maparthelper.util.MapUtils;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;


public class ServerCommands {
    public static void registerCommands() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
            dispatcher.register(literal("mart-creative")
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

                                byte[] colors = new byte[16384];
                                for (int id = 1; id < 64; id++) {
                                    MapColor color = MapColor.get(id);
                                    if (color == MapColor.CLEAR) break;
                                    for (int i = 0; i < 2; i++) {
                                        int idx = (id - 1) * 2 + i;
                                        colors[idx] = color.getRenderColorByte(MapColor.Brightness.HIGH);
                                        colors[idx + 128] = color.getRenderColorByte(MapColor.Brightness.NORMAL);
                                        colors[idx + 256] = color.getRenderColorByte(MapColor.Brightness.LOW);
                                        colors[idx + 384] = color.getRenderColorByte(MapColor.Brightness.LOWEST);
                                    }
                                }

                                MapIdComponent mapId;
                                mapId = MapCreator.createMap(colors, player.getWorld());

                                ItemStack itemStack = MapCreator.getMapItemStack(
                                        mapId,
                                        Text.literal("Palette").withColor(Colors.YELLOW)
                                );
                                player.giveItemStack(itemStack);

                                return 1;
                            }))
                )
            ));
    }
}
