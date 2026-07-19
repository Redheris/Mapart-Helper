package rh.maparthelper;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.SpecialGuiElementRegistry;
import rh.maparthelper.command.ClientCommands;
import rh.maparthelper.command.MapartToFile;
import rh.maparthelper.event.ModEventsHandler;
import rh.maparthelper.render.ScaledItemGuiElementRenderer;

public class MapartHelperClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        MapartToFile.initializeSavesDir();

        ClientCommands.registerCommands();
        ModEventsHandler.registerAll();

        SpecialGuiElementRegistry.register(ctx -> new ScaledItemGuiElementRenderer(
                //? if <26.1 {
                ctx.vertexConsumers()
                //?} else if 26.1
                //ctx.bufferSource()
        ));
    }
}