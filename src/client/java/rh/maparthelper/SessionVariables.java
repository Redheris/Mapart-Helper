package rh.maparthelper;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.MapIdComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.map.MapState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import rh.maparthelper.conversion.colors.MapColorEntry;
import rh.maparthelper.config.palette.PaletteColors;

import java.util.ArrayList;
import java.util.List;

public class SessionVariables {
    public static boolean isSelectingFramesArea = false;
    public static Direction selectedDirection;
    public static Vec3d selectedPos1;
    public static Vec3d selectedPos2;
    public static int selectionHeight;
    public static int selectionWidth;

    public static boolean showMapartStartPos = false;

    public static List<ItemFrameEntity> fakeItemFrames = new ArrayList<>();
    public static long fakeFramesBornTime = 0;

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

    public static void removeFakeItemFrames(ClientWorld world) {
        for (ItemFrameEntity itemFrame : SessionVariables.fakeItemFrames)
            world.removeEntity(itemFrame.getId(), Entity.RemovalReason.DISCARDED);
        fakeItemFrames.clear();
        fakeFramesBornTime = 0;
    }

    public static void addFakeItemFrame(int[] map, ClientPlayerEntity player) {
        MapState mapState = MapState.of((byte) 1, false, null);
        mapState.colors = new byte[map.length];
        boolean use3D = MapartHelperClient.conversionConfig.use3D();
        boolean useDithering = MapartHelperClient.conversionConfig.useDithering();
        for (int i = 0; i < map.length; i++) {
            MapColorEntry color = PaletteColors.getClosestColor(map[i], use3D, useDithering);
            mapState.colors[i] = color.mapColor().getRenderColorByte(color.brightness());
        }

        ItemStack mapItem = new ItemStack(Items.FILLED_MAP);
        MapIdComponent mapId = new MapIdComponent(-1 - fakeItemFrames.size());
        mapItem.set(DataComponentTypes.MAP_ID, mapId);

        ItemFrameEntity itemFrame = new ItemFrameEntity(player.clientWorld, player.getBlockPos(), player.getMovementDirection().getOpposite());
        itemFrame.setHeldItemStack(mapItem);
        itemFrame.setInvisible(true);

        player.clientWorld.putClientsideMapState(mapId, mapState);

        fakeItemFrames.add(itemFrame);
    }

    public static void showFakeFrames(ClientPlayerEntity player, int width, int height) {
        BlockPos.Mutable pos = player.getBlockPos().mutableCopy();
        Direction direction = player.getMovementDirection();
        if (direction.getDirection() == Direction.AxisDirection.NEGATIVE)
            pos.move(direction);
        else
            pos.move(direction, 2);
        if (height == 1)
            pos.move(Direction.UP, 2);
        else
            pos.move(Direction.UP, height);

        Direction LEFT = Direction.WEST;
        switch (direction) {
            case EAST -> LEFT = Direction.NORTH;
            case SOUTH -> LEFT = Direction.EAST;
            case WEST -> LEFT = Direction.SOUTH;
        }
        pos.move(LEFT, (int)(width / 2.0));

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                ItemFrameEntity itemFrame = fakeItemFrames.get(x + y * width);
                double posX = LEFT.getOffsetX() * 0.5;
                double posZ = LEFT.getOffsetZ() * 0.5;
                if (LEFT.getDirection() == Direction.AxisDirection.NEGATIVE) {
                    posX = pos.getX() - posX;
                    posZ = pos.getZ() - posZ;
                } else {
                    posX = pos.getX() + posX;
                    posZ = pos.getZ() + posZ;
                }
                posX -= direction.getOffsetX() * 0.03;
                posZ -= direction.getOffsetZ() * 0.03;

                itemFrame.setPos(posX, pos.getY() - 0.5, posZ);
                player.clientWorld.addEntity(itemFrame);
                pos.move(LEFT.getOpposite());
            }
            pos.move(LEFT, width);
            pos.move(Direction.DOWN);
        }
        fakeFramesBornTime = player.clientWorld.getTime();
    }
}
