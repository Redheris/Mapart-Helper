package rh.maparthelper.event;

import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Colors;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import rh.maparthelper.SessionVariables;

public class AttackBlockHandler {

    public static void init() {
        AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {
            if (!SessionVariables.isSelectingFramesArea || !player.getWorld().isClient())
                return ActionResult.PASS;

            // Offset from center to the item frame's box
            Vec3d currentPos = pos.toCenterPos().offset(direction, 0.53);

            if (SessionVariables.selectedPos1 == null) {
                SessionVariables.selectedDirection = direction;
                SessionVariables.setSelectedPos1(currentPos);
                return ActionResult.FAIL;
            }

            if (!direction.equals(SessionVariables.selectedDirection)) {
                player.sendMessage(Text.translatable("maparthelper.selection_not_flat").withColor(Colors.LIGHT_RED), true);
                return ActionResult.FAIL;
            }

            boolean isFlat = switch (SessionVariables.selectedDirection.getAxis()) {
                case Direction.Axis.X -> currentPos.x == SessionVariables.selectedPos1.x;
                case Direction.Axis.Y -> currentPos.y == SessionVariables.selectedPos1.y;
                case Direction.Axis.Z -> currentPos.z == SessionVariables.selectedPos1.z;
            };

            if (!isFlat) {
                player.sendMessage(Text.translatable("maparthelper.selection_not_flat").withColor(Colors.LIGHT_RED), true);
                return ActionResult.FAIL;
            }

            int flag = SessionVariables.setSelectedPos2(currentPos);

            if (flag == -1)
                player.sendMessage(Text.translatable("maparthelper.too_many_maps").formatted(Formatting.RED), true);
            else
                player.sendMessage(Text.translatable("maparthelper.selecting_succeeded").withColor(Colors.GREEN), true);

            return ActionResult.FAIL;
        });
    }
}
