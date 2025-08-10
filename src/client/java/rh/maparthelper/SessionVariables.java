package rh.maparthelper;

import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class SessionVariables {
    public static boolean isSelectingFramesArea = false;
    public static Direction selectedDirection;
    public static Vec3d selectedPos1;
    public static Vec3d selectedPos2;
    public static int selectionHeight;
    public static int selectionWidth;

    public static boolean showMapartStartPos = false;

    public static Vec3d getSelectedPos() {
        if (selectedPos1 != null)
            return selectedPos1;
        if (selectedPos2 != null)
            return selectedPos2;
        return null;
    }

    public static int setSelectedPos1(Vec3d pos) {
        int flag = checkSize(pos, selectedPos2);
        if (flag >= 0)
            selectedPos1 = pos;
        return flag;
    }

    public static int setSelectedPos2(Vec3d pos) {
        int flag = checkSize(selectedPos1, pos);
        if (flag >= 0)
            selectedPos2 = pos;
        return flag;
    }

    @SuppressWarnings("DuplicateExpressions")
    private static int checkSize(Vec3d pos1, Vec3d pos2) {
        if (pos1 == null || pos2 == null)
            return 0;
        switch (SessionVariables.selectedDirection.getAxis()) {
            case X -> {
                selectionWidth = (int) Math.abs(pos1.getZ() - pos2.getZ()) + 1;
                selectionHeight = (int) Math.abs(pos1.getY() - pos2.getY()) + 1;
            }
            case Y -> {
                selectionWidth = (int) Math.abs(pos1.getX() - pos2.getX()) + 1;
                selectionHeight = (int) Math.abs(pos1.getZ() - pos2.getZ()) + 1;
            }
            case Z -> {
                selectionWidth = (int) Math.abs(pos1.getX() - pos2.getX()) + 1;
                selectionHeight = (int) Math.abs(pos1.getY() - pos2.getY()) + 1;
            }
        }
        int size = selectionWidth * selectionHeight;

        if (size <= 0) {
            throw new IllegalArgumentException("Invalid area");
        }
        if (size > 300) {
            return -1;
        }

        return 1;
    }

    public static void resetSelection() {
        isSelectingFramesArea = false;
        selectedDirection = null;
        selectedPos1 = null;
        selectedPos2 = null;
        selectionHeight = 0;
        selectionWidth = 0;
    }
}
