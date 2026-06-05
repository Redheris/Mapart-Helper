package rh.maparthelper.event;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.util.CommonColors;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import rh.maparthelper.MapartHelper;
import rh.maparthelper.state.FramesAreaSelectionState;
import rh.maparthelper.util.ParticleUtils;
import rh.maparthelper.util.CompatUtils;

public class MapartSelectionHandler {

    public static void init() {
        FramesAreaSelectionState selectionState = FramesAreaSelectionState.getInstance();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.level == null) return;

            Vec3 selectedPos = selectionState.getSelectedPos();
            if (selectedPos == null) return;

            Direction direction = selectionState.getSelectedDirection();
            Vec3 pos1 = selectionState.getSelectedPos1();
            Vec3 pos2 = selectionState.getSelectedPos2();

            int selectionColor = MapartHelper.commonConfig().selectionColor.getRGB();

            if (pos1 != null) {
                pos1 = pos1.relative(direction, 0.05);
                ParticleUtils.spawnParticle(client.level, new DustParticleOptions(~((~selectionColor >> 1) & 0x7F7F7F7F), 1.0f), pos1);
            }
            if (pos2 != null) {
                pos2 = pos2.relative(direction, 0.05);
                ParticleUtils.spawnParticle(client.level, new DustParticleOptions((selectionColor >> 1) & 0x7F7F7F7F, 1.0f), pos2);
            }
            if (pos1 != null && pos2 != null) {
                ParticleUtils.drawSelectionBox(client.level, pos1, pos2, direction);
            }
        });

        AttackBlockCallback.EVENT.register((player, level, hand, pos, direction) -> {
            if (selectionState.isNotSelectingFramesArea() || !level.isClientSide())
                return InteractionResult.PASS;

            // Offset from center to the item frame's box
            Vec3 currentPos = pos.getCenter().relative(direction, 0.53);
            selectPosition(selectionState, player, currentPos, direction, false);
            return InteractionResult.FAIL;
        });
        AttackEntityCallback.EVENT.register((player, level, hand, entity, hitResult) -> {
            if (selectionState.isNotSelectingFramesArea())
                return InteractionResult.PASS;

            if (entity instanceof ItemFrame mapFrame) {
                BlockPos blockPos = mapFrame.blockPosition().relative(mapFrame.getNearestViewDirection().getOpposite());
                Vec3 currentPos = blockPos.getCenter().relative(mapFrame.getNearestViewDirection(), 0.53);
                selectPosition(selectionState, player, currentPos, mapFrame.getNearestViewDirection(), false);
            }

            return InteractionResult.FAIL;
        });

        UseBlockCallback.EVENT.register((player, level, hand, hitResult) -> {
            if (selectionState.isNotSelectingFramesArea() || !level.isClientSide())
                return InteractionResult.PASS;

            Vec3 currentPos = hitResult.getBlockPos().getCenter().relative(hitResult.getDirection(), 0.53);
            selectPosition(selectionState, player, currentPos, hitResult.getDirection(), true);

            return InteractionResult.FAIL;
        });
        UseEntityCallback.EVENT.register((player, level, hand, entity, hitResult) -> {
            if (selectionState.isNotSelectingFramesArea())
                return InteractionResult.PASS;

            if (entity instanceof ItemFrame itemFrame) {
                BlockPos blockPos = itemFrame.blockPosition().relative(itemFrame.getNearestViewDirection().getOpposite());
                Vec3 currentPos = blockPos.getCenter().relative(itemFrame.getNearestViewDirection(), 0.53);
                selectPosition(selectionState, player, currentPos, itemFrame.getNearestViewDirection(), true);
            }

            return InteractionResult.FAIL;
        });
    }

    private static void selectPosition(FramesAreaSelectionState selectionState, Player player, Vec3 pos, Direction direction, boolean secondPos) {
        if ((secondPos || selectionState.getSelectedPos2() == null) && (!secondPos || selectionState.getSelectedPos1() == null)) {
            selectionState.setSelectedDirection(direction);
            if (secondPos)
                selectionState.setSelectedPos2(pos);
            else
                selectionState.setSelectedPos1(pos);
            return;
        }

        if (!direction.equals(selectionState.getSelectedDirection())) {
            CompatUtils.sendMessage(player, Component.translatable("map_frames_selection.selection_not_flat").withColor(CommonColors.SOFT_RED), true);
            return;
        }

        Vec3 selectedPos = secondPos ? selectionState.getSelectedPos1() : selectionState.getSelectedPos2();

        boolean isFlat = switch (selectionState.getSelectedDirection().getAxis()) {
            case Direction.Axis.X -> pos.x == selectedPos.x;
            case Direction.Axis.Y -> pos.y == selectedPos.y;
            case Direction.Axis.Z -> pos.z == selectedPos.z;
        };

        if (!isFlat) {
            CompatUtils.sendMessage(player, Component.translatable("map_frames_selection.selection_not_flat").withColor(CommonColors.SOFT_RED), true);
            return;
        }

        int flag = secondPos ? selectionState.setSelectedPos2(pos) : selectionState.setSelectedPos1(pos);

        if (flag == -1)
            CompatUtils.sendMessage(player, Component.translatable("map_frames_selection.too_many_maps").withStyle(ChatFormatting.RED), true);
        else
            CompatUtils.sendMessage(player, Component.translatable("map_frames_selection.selection_succeeded").withColor(CommonColors.GREEN), true);
    }
}
