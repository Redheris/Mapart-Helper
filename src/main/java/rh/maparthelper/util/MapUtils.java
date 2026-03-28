package rh.maparthelper.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.joml.Vector2i;
import rh.maparthelper.config.palette.PaletteConfigManager;
import rh.maparthelper.config.palette.PaletteGenerator;

import java.util.List;
import java.util.Map;

public class MapUtils {

    public static Vector2i getMapAreaStartPos(int x, int z) {
        int posX = (Math.floorDiv(x + 64, 128) * 128) - 64;
        int posZ = (Math.floorDiv(z + 64, 128) * 128) - 64;
        return new Vector2i(posX, posZ);
    }

    public static void placeBlocksFromPalette(Level world, int playerX, int y, int playerZ) {
        Vector2i startPos = MapUtils.getMapAreaStartPos(playerX, playerZ);
        int startX = startPos.x;
        int startZ = startPos.y;

        Map<Integer, List<Block>> palette = PaletteConfigManager.completePalette.palette;

        int maxLen = palette.values().stream().mapToInt(List::size).max().orElse(0);

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(startX, y, startZ);
        for (int x = 0; x < maxLen; x++) {
            for (int z = -1; z < palette.size(); z++) {
                world.setBlockAndUpdate(pos.offset(x, 0, z), Blocks.GRASS_BLOCK.defaultBlockState());
            }
        }

        pos.set(startX, y, startZ);
        for (int color : palette.keySet()) {
            for (Block block : palette.get(color)) {
                world.setBlock(pos, PaletteGenerator.getDefaultPaletteState(block), 816);
                pos = pos.move(Direction.EAST);
                if (pos.getX() == startX + 128) {
                    pos = pos.move(Direction.WEST, 128);
                    pos = pos.move(Direction.SOUTH);
                }
            }
            pos = pos.move(Direction.WEST, pos.getX() - startX);
            pos = pos.move(Direction.SOUTH);
        }
    }
}
