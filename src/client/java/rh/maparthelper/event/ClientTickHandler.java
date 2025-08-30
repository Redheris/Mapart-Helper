package rh.maparthelper.event;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BeaconBlockEntityRenderer;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector2i;
import org.lwjgl.glfw.GLFW;
import rh.maparthelper.command.ClientCommandsContext;
import rh.maparthelper.util.MapUtils;
import rh.maparthelper.MapartHelper;
import rh.maparthelper.gui.MapartEditorScreen;

public class ClientTickHandler {
    public static void init() {
        KeyBinding openScreen = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.maparthelper.openScreen",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_Y,
                KeyBinding.UI_CATEGORY
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openScreen.wasPressed()) {
                client.setScreen(new MapartEditorScreen());
            }
            if (client.world != null && !ClientCommandsContext.fakeItemFrames.isEmpty()) {
                long liveTime = client.world.getTime() - ClientCommandsContext.fakeFramesBornTime;
                if (liveTime >= MapartHelper.config.commonConfiguration.fakeItemFramesLiveTime) {
                    ClientCommandsContext.removeFakeItemFrames(client.world);
                }
            }
        });

        WorldRenderEvents.AFTER_ENTITIES.register(context -> {
            if (!ClientCommandsContext.showMapartStartPos) return;
            MatrixStack matrices = context.matrixStack();
            VertexConsumerProvider vertexConsumers = context.consumers();

            if (matrices == null || vertexConsumers == null)
                return;

            Vec3d pos = context.camera().getPos();
            for (int x = -1; x < 2; x++) {
                for (int y = -1; y < 2; y++) {
                    Vector2i mapPos = MapUtils.getMapAreaStartPos((int)pos.x + x * 128, (int)pos.z + y * 128);
                    if (x == 0 && y == 0 && Math.abs(pos.x - mapPos.x - 0.5) <= 0.4 && Math.abs(pos.z - mapPos.y - 0.5) <= 0.4) continue;

                    matrices.push();
                    matrices.translate(mapPos.x - pos.x, context.world().getBottomY() - pos.y, mapPos.y - pos.z);
                    BeaconBlockEntityRenderer.renderBeam(
                            matrices, vertexConsumers,
                            BeaconBlockEntityRenderer.BEAM_TEXTURE,
                            0, 1, 0, 0, context.world().getHeight(),
                            MapartHelper.config.commonConfiguration.selectionColor,
                            0.2F, 0.0F
                    );
                    matrices.pop();
                }
            }
        });
    }
}
