package rh.maparthelper.conversion.staircases;

import net.minecraft.block.MapColor;
import rh.maparthelper.config.palette.PaletteColors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Waves3DStaircase implements IMapartStaircase {

    @Override
    public List<List<Integer>> getStaircase(int[][] colors) {
        int height = colors.length + 1;
        int width = colors[0].length;

        int[] negativeHeights = new int[width];
        Arrays.fill(negativeHeights, 0);

        List<List<Integer>> staircase = new ArrayList<>();
        for (int z = 0; z < height; z++) {
            List<Integer> row = new ArrayList<>(Collections.nCopies(width, 0));
            staircase.add(row);
        }

        for (int x = 0; x < width; x++) {
            for (int z = height - 2; z >= 0; z--) {
                if (z > 0 && colors[z - 1][x] == 0) continue;
                int y = staircase.get(z + 1).get(x) + getHeightAlt(colors, x, z);
                staircase.get(z).set(x, y);
                negativeHeights[x] = Math.min(negativeHeights[x], y);
            }
        }

        for (int z = 0; z < height; z++) {
            for (int x = 0; x < width; x++) {
                if (z == 0 && colors[z][x] == 0)
                    staircase.get(z).set(x, 0);
                else
                    staircase.get(z).set(x, staircase.get(z).get(x) - negativeHeights[x]);
            }
        }

        return staircase;
    }

    private static int getHeightAlt(int[][] colors, int x, int z) {
        if (z == colors.length) return 0;
        MapColor.Brightness brightness = PaletteColors.getMapColorEntryByARGB(colors[z][x]).brightness();
        if (brightness == MapColor.Brightness.LOW) return 1;
        if (brightness == MapColor.Brightness.HIGH) return -1;
        return 0;
    }
}
