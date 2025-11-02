package rh.maparthelper.event;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BeaconBlockEntityRenderer;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.joml.Vector2i;
import org.lwjgl.glfw.GLFW;
import rh.maparthelper.MapartHelper;
import rh.maparthelper.command.ClientCommandsContext;
import rh.maparthelper.command.FakeMapsPreview;
import rh.maparthelper.gui.MapartEditorScreen;
import rh.maparthelper.util.MapUtils;

public class ClientTickHandler {
    public static void init() {
        KeyBinding openScreen = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.maparthelper.openScreen",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_Y,
                KeyBinding.Category.MISC
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openScreen.wasPressed()) {
                client.setScreen(new MapartEditorScreen());
            }
            if (client.world != null && ClientCommandsContext.showFakeItemFrames()) {
                long liveTime = client.world.getTime() - ClientCommandsContext.getFakeFramesBornTime();
                if (liveTime >= MapartHelper.commonConfig.fakeItemFramesLiveTime) {
                    FakeMapsPreview.removeFakeItemFrames(client.world);
                }
            }
        });

        WorldRenderEvents.AFTER_ENTITIES.register(context -> {
            if (!ClientCommandsContext.showMapartStartPos()) return;
            MatrixStack matrices = context.matrices();
            VertexConsumerProvider vertexConsumers = context.consumers();
            World world = MinecraftClient.getInstance().world;
            if (matrices == null || vertexConsumers == null || world == null)
                return;

            Vec3d cameraPos = context.worldState().cameraRenderState.pos;
            for (int x = -1; x < 2; x++) {
                for (int y = -1; y < 2; y++) {
                    Vector2i mapPos = MapUtils.getMapAreaStartPos((int) cameraPos.x + x * 128, (int) cameraPos.z + y * 128);
                    if (x == 0 && y == 0 && Math.abs(cameraPos.x - mapPos.x - 0.5) <= 0.4 && Math.abs(cameraPos.z - mapPos.y - 0.5) <= 0.4) continue;

                    matrices.push();
                    matrices.translate(mapPos.x - cameraPos.x, world.getBottomY() - cameraPos.y, mapPos.y - cameraPos.z);
                    BeaconBlockEntityRenderer.renderBeam(
                            matrices, context.commandQueue(),
                            BeaconBlockEntityRenderer.BEAM_TEXTURE,
                            1, 1, 0, BeaconBlockEntityRenderer.MAX_BEAM_HEIGHT,
                            MapartHelper.commonConfig.selectionColor,
                            0.2F, 0.0F
                    );
                    matrices.pop();
                }
            }
        });
    }
}
