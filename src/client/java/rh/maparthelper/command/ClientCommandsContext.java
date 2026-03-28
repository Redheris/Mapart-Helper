package rh.maparthelper.command;

import net.minecraft.core.Direction;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class ClientCommandsContext {
    static boolean isSelectingFramesArea = false;
    static Direction selectedDirection;
    static Vec3 selectedPos1;
    static Vec3 selectedPos2;
    static int selectionHeight;
    static int selectionWidth;

    static boolean showMapartStartPos = false;

    static List<ItemFrame> fakeItemFrames = new ArrayList<>();
    static long fakeFramesBornTime = 0;

    public static Vec3 getSelectedPos() {
        if (selectedPos1 != null)
            return selectedPos1;
        if (selectedPos2 != null)
            return selectedPos2;
        return null;
    }

    public static int setSelectedPos1(Vec3 pos) {
        int flag = checkSize(pos, selectedPos2);
        if (flag >= 0)
            selectedPos1 = pos;
        return flag;
    }

    public static int setSelectedPos2(Vec3 pos) {
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

    public static Vec3 getSelectedPos1() {
        return selectedPos1;
    }

    public static Vec3 getSelectedPos2() {
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
    private static int checkSize(Vec3 pos1, Vec3 pos2) {
        if (pos1 == null || pos2 == null)
            return 0;
        switch (ClientCommandsContext.selectedDirection.getAxis()) {
            case X -> {
                selectionWidth = (int) Math.abs(pos1.z() - pos2.z()) + 1;
                selectionHeight = (int) Math.abs(pos1.y() - pos2.y()) + 1;
            }
            case Y -> {
                selectionWidth = (int) Math.abs(pos1.x() - pos2.x()) + 1;
                selectionHeight = (int) Math.abs(pos1.z() - pos2.z()) + 1;
            }
            case Z -> {
                selectionWidth = (int) Math.abs(pos1.x() - pos2.x()) + 1;
                selectionHeight = (int) Math.abs(pos1.y() - pos2.y()) + 1;
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
