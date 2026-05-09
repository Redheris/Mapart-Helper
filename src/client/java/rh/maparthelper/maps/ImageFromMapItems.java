package rh.maparthelper.maps;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import rh.maparthelper.state.FramesAreaSelectionState;

import java.util.Comparator;
import java.util.List;

public class ImageFromMapItems {
    public static MapartImageData getMapartFromItem(ItemStack itemStack, Level level) {
        MapItemSavedData mapState = MapItem.getSavedData(itemStack, level);
        MapId mapIdComponent = itemStack.get(DataComponents.MAP_ID);

        if (mapState == null || mapIdComponent == null) return MapartImageData.INVALID;
        return new MapartImageData(mapState.colors.clone());
    }

    public static MapartImageData getMapartFromItemFrame(ItemFrame itemFrame, Level level, Direction viewDirection) {
        ItemStack itemStack = itemFrame.getItem();
        MapItemSavedData mapState = MapItem.getSavedData(itemStack, level);
        MapId mapIdComponent = itemStack.get(DataComponents.MAP_ID);

        if (mapState == null || mapIdComponent == null) return MapartImageData.INVALID;
        MapartImageData mapData = new MapartImageData(mapState.colors.clone());

        int mapRotation = getMapRotation(itemFrame, viewDirection);
        rotateMap(mapData, mapRotation);

        return mapData;
    }

    public static MapartImageData getMapartFromItemFramesArea(Level level, Direction viewDirection)
            throws SelectionIsEmptyException, SelectionNotFullException {
        FramesAreaSelectionState selectionState = FramesAreaSelectionState.getInstance();
        Vec3 pos1 = selectionState.getSelectedPos1();
        Vec3 pos2 = selectionState.getSelectedPos2();

        AABB area = new AABB(pos1, pos2);
        List<ItemFrame> itemFrames = level.getEntities(EntityType.ITEM_FRAME, area, ItemFrame::hasFramedMap);
        itemFrames.addAll(level.getEntities(EntityType.GLOW_ITEM_FRAME, area, ItemFrame::hasFramedMap));

        if (itemFrames.isEmpty()) {
            throw new SelectionIsEmptyException();
        }

        Direction areaDirection = selectionState.getSelectedDirection();
        Direction down = switch (areaDirection) {
            case DOWN -> viewDirection;
            case UP -> viewDirection.getOpposite();
            default -> Direction.DOWN;
        };
        Direction right = down.getCounterClockWise(areaDirection.getAxis());
        if (areaDirection.getAxisDirection() == Direction.AxisDirection.NEGATIVE)
            right = right.getOpposite();

        int width = Math.abs(comparingBlockPosAlignDirection(right).compare(BlockPos.containing(pos1), BlockPos.containing(pos2))) + 1;
        int height = Math.abs(comparingBlockPosAlignDirection(down).compare(BlockPos.containing(pos1), BlockPos.containing(pos2))) + 1;
        int size = width * height;

        if (itemFrames.size() != size) {
            throw new SelectionNotFullException();
        }

        List<MapartImageData> mapsData = itemFrames.stream()
                .sorted(comparingFramesPos(right, down))
                .map(itemFrame -> getMapartFromItemFrame(itemFrame, level, viewDirection))
                .toList();

        int imageWidth = width * 128;
        byte[] colors = new byte[imageWidth * height * 128];
        for (int ind = 0; ind < mapsData.size(); ind++) {
            MapartImageData mapData = mapsData.get(ind);
            int x0 = 128 * (ind % width);
            int y0 = 128 * (ind / width);

            for (int j = 0; j < 16384; j++) {
                int resultX = x0 + j % 128;
                int resultY = y0 + j / 128;
                colors[resultY * imageWidth + resultX] = mapData.colors[j];
            }
        }
        return new MapartImageData(imageWidth, height * 128, colors);
    }

    private static Comparator<ItemFrame> comparingFramesPos(Direction right, Direction down) {
        return (if1, if2) -> {
            BlockPos pos1 = if1.blockPosition();
            BlockPos pos2 = if2.blockPosition();
            int cmpHeight = comparingBlockPosAlignDirection(down).compare(pos1, pos2);
            if (cmpHeight != 0) return cmpHeight;
            return comparingBlockPosAlignDirection(right).compare(pos1, pos2);
        };
    }

    private static Comparator<BlockPos> comparingBlockPosAlignDirection(Direction direction) {
        return (pos1, pos2) -> {
            Direction.Axis axis = direction.getAxis();
            boolean negative = direction.getAxisDirection() == Direction.AxisDirection.NEGATIVE;
            int v1 = pos1.get(axis);
            int v2 = pos2.get(axis);
            return negative ? v2 - v1 : v1 - v2;
        };
    }

    public static int getMapRotation(ItemFrame itemFrame, Direction viewDirection) {
        int mapRotation = itemFrame.getRotation();
        Direction facingDirection = itemFrame.getNearestViewDirection();
        if (facingDirection.getAxis() == Direction.Axis.Y) {
            if (facingDirection == Direction.DOWN) {
                mapRotation += switch (viewDirection) {
                    case EAST -> 1;
                    case SOUTH -> 2;
                    case WEST -> 3;
                    default -> 0;
                };
            } else {
                mapRotation += switch (viewDirection) {
                    case WEST -> 1;
                    case SOUTH -> 2;
                    case EAST -> 3;
                    default -> 0;
                };
            }
        }
        return mapRotation;
    }

    public static void rotateMap(MapartImageData map, int rotation) {
        byte[] rotated = new byte[128 * 128];

        for (int y = 0; y < 128; y++) {
            for (int x = 0; x < 128; x++) {
                int fromIndex = y * 128 + x;
                int toIndex;

                switch (rotation % 4) {
                    case 0 -> toIndex = y * 128 + x;
                    case 1 -> toIndex = x * 128 + (127 - y);
                    case 2 -> toIndex = (127 - y) * 128 + (127 - x);
                    case 3 -> toIndex = (127 - x) * 128 + y;
                    default -> throw new IllegalArgumentException("Invalid rotation");
                }

                rotated[toIndex] = map.colors[fromIndex];
            }
        }

        map.colors = rotated;
    }
}
