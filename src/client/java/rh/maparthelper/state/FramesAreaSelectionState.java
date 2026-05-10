package rh.maparthelper.state;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import rh.maparthelper.util.CompatUtils;

public class FramesAreaSelectionState {
    private static final FramesAreaSelectionState INSTANCE = new FramesAreaSelectionState();

    private boolean isSelectingFramesArea = false;
    private Direction selectedDirection;
    private Vec3 selectedPos1;
    private Vec3 selectedPos2;

    public static FramesAreaSelectionState getInstance() {
        return INSTANCE;
    }

    private FramesAreaSelectionState() {
    }

    public void selectFramesArea(Player player) {
        if (selectedPos1 != null || isSelectingFramesArea) {
            resetSelection();
            CompatUtils.sendMessage(player, Component.translatable("map_frames_selection.selection_stopped").withStyle(ChatFormatting.DARK_AQUA), true);
            return;
        }
        isSelectingFramesArea = true;

        CompatUtils.sendMessage(player, Component.translatable("map_frames_selection.tip_selecting_pos").withStyle(ChatFormatting.DARK_AQUA), false);
        CompatUtils.sendMessage(player, Component.translatable("map_frames_selection.tip_stop_selection").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC), false);
    }

    public Vec3 getSelectedPos() {
        if (selectedPos1 != null)
            return selectedPos1;
        if (selectedPos2 != null)
            return selectedPos2;
        return null;
    }

    public int setSelectedPos1(Vec3 pos) {
        int flag = checkSize(pos, selectedPos2);
        if (flag >= 0)
            selectedPos1 = pos;
        return flag;
    }

    public int setSelectedPos2(Vec3 pos) {
        int flag = checkSize(selectedPos1, pos);
        if (flag >= 0)
            selectedPos2 = pos;
        return flag;
    }

    public void setSelectedDirection(Direction selectedDirection) {
        this.selectedDirection = selectedDirection;
    }

    public boolean isNotSelectingFramesArea() {
        return !isSelectingFramesArea;
    }

    public Vec3 getSelectedPos1() {
        return selectedPos1;
    }

    public Vec3 getSelectedPos2() {
        return selectedPos2;
    }

    public Direction getSelectedDirection() {
        return selectedDirection;
    }

    @SuppressWarnings("DuplicateExpressions")
    private int checkSize(Vec3 pos1, Vec3 pos2) {
        if (pos1 == null || pos2 == null)
            return 0;

        int selectionWidth = 0;
        int selectionHeight = 0;

        switch (selectedDirection.getAxis()) {
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
        if (size > 1500) {
            return -1;
        }

        return 1;
    }

    public void resetSelection() {
        isSelectingFramesArea = false;
        selectedDirection = null;
        selectedPos1 = null;
        selectedPos2 = null;
    }
}
