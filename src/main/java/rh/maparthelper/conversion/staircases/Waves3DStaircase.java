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

        List<List<Integer>> negativeHeights = new ArrayList<>();

        List<List<Integer>> staircase = new ArrayList<>();
        for (int z = 0; z < height; z++) {
            List<Integer> row = new ArrayList<>(Collections.nCopies(width, 0));
            staircase.add(row);
        }

        for (int x = 0; x < width; x++) {
            // Waves logic to modularly fix negative heights
            negativeHeights.add(x, new ArrayList<>());
            negativeHeights.get(x).add(0);
            int waveNumber = 0;

            for (int z = 1; z < height; z++) {
                if (colors[z - 1][x] == 0) continue;
                if (z > 1 && colors[z - 2][x] == 0) {
                    waveNumber++; // Increase wave number if air blocks finished
                    negativeHeights.get(x).add(0);
                }
                int y = staircase.get(z - 1).get(x);
                if (z == 1 || colors[z - 2][x] != 0)
                    y += getHeightAltFromNorthern(brights, x, z);

                staircase.get(z).set(x, y);
                negativeHeights.get(x).set(waveNumber, Math.min(negativeHeights.get(x).get(waveNumber), y));
            }
        }

        // Ascending blocks with negative heights, including aux blocks
        for (int x = 0; x < width; x++) {
            int waveNumber = 0;
            for (int z = 0; z < height; z++) {
                if (z > 0 && colors[z - 1][x] == 0) continue;
                if (z > 1 && colors[z - 2][x] == 0) waveNumber++;
                staircase.get(z).set(x, staircase.get(z).get(x) - negativeHeights.get(x).get(waveNumber));
            }
        }

        return staircase;
    }

    // What the height diff comparing to [z - 1][x]
    private static int getHeightAltFromNorthern(int[][] brights, int x, int staircaseZ) {
        if (staircaseZ == 0) return 0;
        int brightness = brights[staircaseZ - 1][x];
        if (brightness == 0) return -1; // LOW
        if (brightness == 2) return 1; // HIGH
        return 0; // NORMAL
    }
}
