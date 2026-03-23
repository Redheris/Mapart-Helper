package rh.maparthelper.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.CommonColors;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.saveddata.maps.MapId;
import rh.maparthelper.server.MapCreator;
import rh.maparthelper.util.MapUtils;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;


public class ServerCommands {
    public static void registerCommands() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
            dispatcher.register(literal("mart-creative")
                .then(literal("palette")
                    .then(literal("place").then(argument("height", IntegerArgumentType.integer())
                        .executes(ctx -> {
                            // Some kind of debug feature to place and see the complete blocks palette in the world
                            if (ctx.getSource().getEntity() == null)
                                return 0;

                            int y = IntegerArgumentType.getInteger(ctx, "height");
                            Entity source = ctx.getSource().getEntity();

                            MapUtils.placeBlocksFromPalette(source.level(), source.getBlockX(), y, source.getBlockZ());
                            return 1;
                        })))
                        .then(literal("give-map-palette")
                            .executes(ctx -> {
                                ServerPlayer player = ctx.getSource().getPlayer();
                                assert player != null;

                                byte[] colors = new byte[16384];
                                for (int id = 1; id < 64; id++) {
                                    MapColor color = MapColor.byId(id);
                                    if (color == MapColor.NONE) break;
                                    for (int i = 0; i < 2; i++) {
                                        int idx = (id - 1) * 2 + i;
                                        colors[idx] = color.getPackedId(MapColor.Brightness.HIGH);
                                        colors[idx + 128] = color.getPackedId(MapColor.Brightness.NORMAL);
                                        colors[idx + 256] = color.getPackedId(MapColor.Brightness.LOW);
                                        colors[idx + 384] = color.getPackedId(MapColor.Brightness.LOWEST);
                                    }
                                }

                                MapId mapId;
                                mapId = MapCreator.createMap(colors, player.level());

                                ItemStack itemStack = MapCreator.getMapItemStack(
                                        mapId,
                                        Component.literal("Palette").withColor(CommonColors.YELLOW)
                                );
                                player.addItem(itemStack);

                                return 1;
                            }))
                )
            ));
    }
}
