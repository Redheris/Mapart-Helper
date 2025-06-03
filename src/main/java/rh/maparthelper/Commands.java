package rh.maparthelper;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.entity.Entity;
import rh.maparthelper.conversion.BlocksPalette;

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
                    .then(literal("generate")
                        .executes(ctx -> {
                            // Regenerates blocks palette to correspond to the configs and game's blocks list
                            BlocksPalette.initColors();
                            return 1;
                        }))
                    .then(literal("place").then(argument("height", IntegerArgumentType.integer())
                        .executes(ctx -> {
                            // Some kind of debug feature to place and see the complete blocks palette in the world
                            if (ctx.getSource().getEntity() == null || ctx.getSource().getWorld() == null)
                                return 0;

                            int y = IntegerArgumentType.getInteger(ctx, "height");
                            Entity source = ctx.getSource().getEntity();

                            BlocksPalette.placeBlocksFromPalette(source.getWorld(), source.getBlockX(), y, source.getBlockZ());
                            return 1;
                        })))
                )
            ));
    }
}
