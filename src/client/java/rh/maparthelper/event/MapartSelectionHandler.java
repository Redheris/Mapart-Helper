package rh.maparthelper.event;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Colors;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import rh.maparthelper.MapartHelper;
import rh.maparthelper.command.ClientCommandsContext;

public class MapartSelectionHandler {

    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            assert client.world != null;
            Vec3d selectedPos = ClientCommandsContext.getSelectedPos();
            if (selectedPos == null) return;

            Direction direction = ClientCommandsContext.selectedDirection;
            Vec3d pos1 = ClientCommandsContext.selectedPos1;
            Vec3d pos2 = ClientCommandsContext.selectedPos2;

            int selectionColor = MapartHelper.config.commonConfiguration.selectionColor;

            if (pos1 != null) {
                pos1 = pos1.offset(direction, 0.05);
                ParticleUtils.spawnParticle(client.world, new DustParticleEffect(~((~selectionColor >> 1) & 0x7F7F7F7F), 1.0f), pos1);
            }
            if (pos2 != null) {
                pos2 = pos2.offset(direction, 0.05);
                ParticleUtils.spawnParticle(client.world, new DustParticleEffect((selectionColor >> 1) & 0x7F7F7F7F, 1.0f), pos2);
            }
            if (pos1 != null && pos2 != null) {
                ParticleUtils.drawSelectionBox(client.world, pos1, pos2, direction, 0.08);
            }
        });


        AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {
            if (!ClientCommandsContext.isSelectingFramesArea || !player.getWorld().isClient())
                return ActionResult.PASS;

            // Offset from center to the item frame's box
            Vec3d currentPos = pos.toCenterPos().offset(direction, 0.53);
            selectPosition(player, currentPos, direction, false);
            return ActionResult.FAIL;
        });
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (!ClientCommandsContext.isSelectingFramesArea)
                return ActionResult.PASS;

            if (entity instanceof ItemFrameEntity mapFrame) {
                BlockPos blockPos = mapFrame.getBlockPos().offset(mapFrame.getFacing().getOpposite());
                assert MinecraftClient.getInstance().interactionManager != null;
                Vec3d currentPos = blockPos.toCenterPos().offset(mapFrame.getFacing(), 0.53);
                selectPosition(player, currentPos, mapFrame.getFacing(), false);
            }

            return ActionResult.FAIL;
        });

        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (!ClientCommandsContext.isSelectingFramesArea || !player.getWorld().isClient())
                return ActionResult.PASS;

            Vec3d currentPos = hitResult.getBlockPos().toCenterPos().offset(hitResult.getSide(), 0.53);
            selectPosition(player, currentPos, hitResult.getSide(), true);

            return ActionResult.FAIL;
        });
        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (!ClientCommandsContext.isSelectingFramesArea)
                return ActionResult.PASS;

            if (entity instanceof ItemFrameEntity mapFrame) {
                BlockPos blockPos = mapFrame.getBlockPos().offset(mapFrame.getFacing().getOpposite());
                assert MinecraftClient.getInstance().interactionManager != null;
                Vec3d currentPos = blockPos.toCenterPos().offset(mapFrame.getFacing(), 0.53);
                selectPosition(player, currentPos, mapFrame.getFacing(), true);
            }

            return ActionResult.FAIL;
        });
    }

    private static void selectPosition(PlayerEntity player, Vec3d pos, Direction direction, boolean secondPos) {
        if ((secondPos || ClientCommandsContext.selectedPos2 == null) && (!secondPos || ClientCommandsContext.selectedPos1 == null)) {
            ClientCommandsContext.selectedDirection = direction;
            if (secondPos)
                ClientCommandsContext.setSelectedPos2(pos);
            else
                ClientCommandsContext.setSelectedPos1(pos);
            return;
        }

        if (!direction.equals(ClientCommandsContext.selectedDirection)) {
            player.sendMessage(Text.translatable("maparthelper.selection_not_flat").withColor(Colors.LIGHT_RED), true);
            return;
        }

        Vec3d selectedPos = secondPos ? ClientCommandsContext.selectedPos1 : ClientCommandsContext.selectedPos2;

        boolean isFlat = switch (ClientCommandsContext.selectedDirection.getAxis()) {
            case Direction.Axis.X -> pos.x == selectedPos.x;
            case Direction.Axis.Y -> pos.y == selectedPos.y;
            case Direction.Axis.Z -> pos.z == selectedPos.z;
        };

        if (!isFlat) {
            player.sendMessage(Text.translatable("maparthelper.selection_not_flat").withColor(Colors.LIGHT_RED), true);
            return;
        }

        int flag = secondPos ? ClientCommandsContext.setSelectedPos2(pos) : ClientCommandsContext.setSelectedPos1(pos);

        if (flag == -1)
            player.sendMessage(Text.translatable("maparthelper.too_many_maps").formatted(Formatting.RED), true);
        else
            player.sendMessage(Text.translatable("maparthelper.selecting_succeeded").withColor(Colors.GREEN), true);
    }
}
