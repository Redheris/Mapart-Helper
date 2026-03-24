package rh.maparthelper.event;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
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
import rh.maparthelper.command.ClientCommandsContext;
import rh.maparthelper.util.ParticleUtils;

public class MapartSelectionHandler {

    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            assert client.level != null;
            Vec3 selectedPos = ClientCommandsContext.getSelectedPos();
            if (selectedPos == null) return;

            Direction direction = ClientCommandsContext.getSelectedDirection();
            Vec3 pos1 = ClientCommandsContext.getSelectedPos1();
            Vec3 pos2 = ClientCommandsContext.getSelectedPos2();

            int selectionColor = MapartHelper.commonConfig.selectionColor;

            if (pos1 != null) {
                pos1 = pos1.relative(direction, 0.05);
                ParticleUtils.spawnParticle(client.level, new DustParticleOptions(~((~selectionColor >> 1) & 0x7F7F7F7F), 1.0f), pos1);
            }
            if (pos2 != null) {
                pos2 = pos2.relative(direction, 0.05);
                ParticleUtils.spawnParticle(client.level, new DustParticleOptions((selectionColor >> 1) & 0x7F7F7F7F, 1.0f), pos2);
            }
            if (pos1 != null && pos2 != null) {
                ParticleUtils.drawSelectionBox(client.level, pos1, pos2, direction, 0.08);
            }
        });

        AttackBlockCallback.EVENT.register((player, level, hand, pos, direction) -> {
            if (ClientCommandsContext.isNotSelectingFramesArea() || !level.isClientSide())
                return InteractionResult.PASS;

            // Offset from center to the item frame's box
            Vec3 currentPos = pos.getCenter().relative(direction, 0.53);
            selectPosition(player, currentPos, direction, false);
            return InteractionResult.FAIL;
        });
        AttackEntityCallback.EVENT.register((player, level, hand, entity, hitResult) -> {
            if (ClientCommandsContext.isNotSelectingFramesArea())
                return InteractionResult.PASS;

            if (entity instanceof ItemFrame mapFrame) {
                BlockPos blockPos = mapFrame.blockPosition().relative(mapFrame.getNearestViewDirection().getOpposite());
                assert Minecraft.getInstance().gameMode != null;
                Vec3 currentPos = blockPos.getCenter().relative(mapFrame.getNearestViewDirection(), 0.53);
                selectPosition(player, currentPos, mapFrame.getNearestViewDirection(), false);
            }

            return InteractionResult.FAIL;
        });

        UseBlockCallback.EVENT.register((player, level, hand, hitResult) -> {
            if (ClientCommandsContext.isNotSelectingFramesArea() || !level.isClientSide())
                return InteractionResult.PASS;

            Vec3 currentPos = hitResult.getBlockPos().getCenter().relative(hitResult.getDirection(), 0.53);
            selectPosition(player, currentPos, hitResult.getDirection(), true);

            return InteractionResult.FAIL;
        });
        UseEntityCallback.EVENT.register((player, level, hand, entity, hitResult) -> {
            if (ClientCommandsContext.isNotSelectingFramesArea())
                return InteractionResult.PASS;

            if (entity instanceof ItemFrame mapFrame) {
                BlockPos blockPos = mapFrame.blockPosition().relative(mapFrame.getNearestViewDirection().getOpposite());
                assert Minecraft.getInstance().gameMode != null;
                Vec3 currentPos = blockPos.getCenter().relative(mapFrame.getNearestViewDirection(), 0.53);
                selectPosition(player, currentPos, mapFrame.getNearestViewDirection(), true);
            }

            return InteractionResult.FAIL;
        });
    }

    private static void selectPosition(Player player, Vec3 pos, Direction direction, boolean secondPos) {
        if ((secondPos || ClientCommandsContext.getSelectedPos2() == null) && (!secondPos || ClientCommandsContext.getSelectedPos1() == null)) {
            ClientCommandsContext.setSelectedDirection(direction);
            if (secondPos)
                ClientCommandsContext.setSelectedPos2(pos);
            else
                ClientCommandsContext.setSelectedPos1(pos);
            return;
        }

        if (!direction.equals(ClientCommandsContext.getSelectedDirection())) {
            player.displayClientMessage(Component.translatable("maparthelper.selection_not_flat").withColor(CommonColors.SOFT_RED), true);
            return;
        }

        Vec3 selectedPos = secondPos ? ClientCommandsContext.getSelectedPos1() : ClientCommandsContext.getSelectedPos2();

        boolean isFlat = switch (ClientCommandsContext.getSelectedDirection().getAxis()) {
            case Direction.Axis.X -> pos.x == selectedPos.x;
            case Direction.Axis.Y -> pos.y == selectedPos.y;
            case Direction.Axis.Z -> pos.z == selectedPos.z;
        };

        if (!isFlat) {
            player.displayClientMessage(Component.translatable("maparthelper.selection_not_flat").withColor(CommonColors.SOFT_RED), true);
            return;
        }

        int flag = secondPos ? ClientCommandsContext.setSelectedPos2(pos) : ClientCommandsContext.setSelectedPos1(pos);

        if (flag == -1)
            player.displayClientMessage(Component.translatable("maparthelper.too_many_maps").withStyle(ChatFormatting.RED), true);
        else
            player.displayClientMessage(Component.translatable("maparthelper.selecting_succeeded").withColor(CommonColors.GREEN), true);
    }
}
