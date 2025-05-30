package rh.maparthelper;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import rh.maparthelper.conversion.BlocksPalette;

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
                    .then(literal("blank")
                        .executes(ctx -> {
                            // May be temporary testing code... But also may be some kind of debug feature to see
                            // a complete blocks palette in-world
//                                            ((ServerPlayerEntity)ctx.getSource().getEntity()).sendMessage(Text.literal(String.valueOf(ctx.getSource().getEntity().getWorld().isClient())), false);
                            if (ctx.getSource().getEntity() == null || ctx.getSource().getWorld() == null)
                                return 0;
                            Entity source = ctx.getSource().getEntity();
                            ctx.getSource().sendFeedback(() -> Text.literal(String.valueOf(source.getWorld().isClient())), false);
                            BlocksPalette.setBlocksFromPalette(source.getWorld());
                            System.out.println("Set completed");
                            return 1;
                        }))
                )
            ));
    }
}
