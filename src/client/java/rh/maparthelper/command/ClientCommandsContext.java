package rh.maparthelper.command;

import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

public class ClientCommandsContext {
    static boolean isSelectingFramesArea = false;
    static Direction selectedDirection;
    static Vec3d selectedPos1;
    static Vec3d selectedPos2;
    static int selectionHeight;
    static int selectionWidth;

    static boolean showMapartStartPos = false;

    static List<ItemFrameEntity> fakeItemFrames = new ArrayList<>();
    static long fakeFramesBornTime = 0;

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

    public static void setSelectedDirection(Direction selectedDirection) {
        ClientCommandsContext.selectedDirection = selectedDirection;
    }

    public static boolean isNotSelectingFramesArea() {
        return !isSelectingFramesArea;
    }

    public static Vec3d getSelectedPos1() {
        return selectedPos1;
    }

    public static Vec3d getSelectedPos2() {
        return selectedPos2;
    }

    public static Direction getSelectedDirection() {
        return selectedDirection;
    }

    public static boolean showMapartStartPos() {
        return showMapartStartPos;
    }

    public static boolean showFakeItemFrames() {
        return !fakeItemFrames.isEmpty();
    }

    public static long getFakeFramesBornTime() {
        return fakeFramesBornTime;
    }

    @SuppressWarnings("DuplicateExpressions")
    private static int checkSize(Vec3d pos1, Vec3d pos2) {
        if (pos1 == null || pos2 == null)
            return 0;
        switch (ClientCommandsContext.selectedDirection.getAxis()) {
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
