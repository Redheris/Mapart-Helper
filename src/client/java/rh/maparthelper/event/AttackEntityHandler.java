package rh.maparthelper.event;

import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import rh.maparthelper.SessionVariables;

public class AttackEntityHandler {

    public static void init() {
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (!SessionVariables.isSelectingFramesArea)
                return ActionResult.PASS;

            if (entity instanceof ItemFrameEntity mapFrame) {
                BlockPos blockPos = mapFrame.getBlockPos().offset(mapFrame.getFacing().getOpposite());
                assert MinecraftClient.getInstance().interactionManager != null;
                MinecraftClient.getInstance().interactionManager.attackBlock(blockPos, mapFrame.getFacing());
            }

            return ActionResult.FAIL;
        });
    }
}
