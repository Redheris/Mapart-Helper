package rh.maparthelper.conversion.staircases;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Waves3DStaircase implements IMapartStaircase {

    @Override
    public List<List<Integer>> getStaircase(int[][] colors) {
        int[][] brights = getBrightnesses(colors);
        int height = colors.length + 1;
        int width = colors[0].length;

        int[] negativeHeights = new int[width];

        List<List<Integer>> staircase = new ArrayList<>();
        for (int z = 0; z < height; z++) {
            List<Integer> row = new ArrayList<>(Collections.nCopies(width, 0));
            staircase.add(row);
        }

        for (int x = 0; x < width; x++) {
            for (int z = height - 2; z >= 0; z--) {
                if (z > 0 && colors[z - 1][x] == 0) continue;
                int y = staircase.get(z + 1).get(x) + getHeightAlt(brights, x, z);
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

    private static int getHeightAlt(int[][] brights, int x, int z) {
        if (z == brights.length) return 0;
        int brightness = brights[z][x];
        if (brightness == 0) return 1;
        if (brightness == 2) return -1;
        return 0;
    }
}
