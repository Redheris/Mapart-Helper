package rh.maparthelper.event;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BeaconRenderer;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector2i;
import org.lwjgl.glfw.GLFW;
import rh.maparthelper.MapartHelper;
import rh.maparthelper.command.ClientCommandsContext;
import rh.maparthelper.command.FakeMapsPreview;
import rh.maparthelper.gui.screen.MapartEditorScreen;
import rh.maparthelper.util.MapUtils;

public class ClientTickHandler {
    public static void init() {
        KeyMapping openScreen = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.maparthelper.openScreen",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_Y,
                KeyMapping.CATEGORY_INTERFACE
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openScreen.consumeClick()) {
                client.setScreen(new MapartEditorScreen());
            }
            if (client.level != null && ClientCommandsContext.showFakeItemFrames()) {
                long liveTime = client.level.getGameTime() - ClientCommandsContext.getFakeFramesBornTime();
                if (liveTime >= MapartHelper.commonConfig.fakeItemFramesLiveTime) {
                    FakeMapsPreview.removeFakeItemFrames(client.level);
                }
            }
        });

        WorldRenderEvents.AFTER_ENTITIES.register(context -> {
            if (!ClientCommandsContext.showMapartStartPos()) return;
            PoseStack poseStack = context.matrixStack();
            MultiBufferSource vertexConsumers = context.consumers();

            if (poseStack == null || vertexConsumers == null)
                return;

            Vec3 pos = context.camera().getPosition();
            for (int x = -1; x < 2; x++) {
                for (int y = -1; y < 2; y++) {
                    Vector2i mapPos = MapUtils.getMapAreaStartPos((int) pos.x + x * 128, (int) pos.z + y * 128);
                    if (x == 0 && y == 0 && Math.abs(pos.x - mapPos.x - 0.5) <= 0.4 && Math.abs(pos.z - mapPos.y - 0.5) <= 0.4)
                        continue;

                    poseStack.pushPose();
                    poseStack.translate(mapPos.x - pos.x, context.world().getMinY() - pos.y, mapPos.y - pos.z);
                    BeaconRenderer.renderBeaconBeam(
                            poseStack, vertexConsumers,
                            BeaconRenderer.BEAM_LOCATION,
                            0, 1, 0, 0, context.world().getHeight(),
                            MapartHelper.commonConfig.selectionColor,
                            0.2F, 0.0F
                    );
                    poseStack.popPose();
                }
            }
        });
    }
}
