package rh.maparthelper.command;

import net.minecraft.world.entity.decoration.ItemFrame;

import java.util.ArrayList;
import java.util.List;

public class ClientCommandsContext {
    static boolean showMapartStartPos = false;

    static List<ItemFrame> fakeItemFrames = new ArrayList<>();
    static long fakeFramesBornTime = 0;

    public static boolean showMapartStartPos() {
        return showMapartStartPos;
    }

    public static boolean showFakeItemFrames() {
        return !fakeItemFrames.isEmpty();
    }

    public static long getFakeFramesBornTime() {
        return fakeFramesBornTime;
    }
}
