package rh.maparthelper.conversion.staircases;

import net.minecraft.block.MapColor;
import rh.maparthelper.config.palette.PaletteColors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Valley3DStaircase implements IMapartStaircase {

    @Override
    public List<List<Integer>> getStaircase(int[][] colors) {
        int height = colors.length + 1;
        int width = colors[0].length;

        List<List<Integer>> highBrightnessStairs = new ArrayList<>();
        for (int i = 0; i < width; i++) {
            highBrightnessStairs.add(new ArrayList<>());
        }

        List<List<Integer>> staircase = new ArrayList<>();
        for (int z = 0; z < height; z++) {
            List<Integer> row = new ArrayList<>(Collections.nCopies(width, 0));
            staircase.add(row);
        }

        for (int x = 0; x < width; x++) {
            for (int z = height - 2; z >= 0; z--) {
                if (z > 1 && colors[z - 1][x] == 0) continue;
                while (z > 0 && getBrightness(colors, x, z) != MapColor.Brightness.HIGH) {
                    staircase.get(z).set(x, staircase.get(z + 1).get(x) + getHeightShift(colors, x, z));
                    z--;
                }

                staircase.get(z).set(x, staircase.get(z + 1).get(x) + getHeightShift(colors, x, z));
                if (z == 0 || colors[z - 1][x] == 0) continue;

                highBrightnessStairs.get(x).add(z - 1);
                while (z > 0 && getBrightness(colors, x, z) == MapColor.Brightness.HIGH) {
                    z--;
                }
                highBrightnessStairs.get(x).add(z + 1);
            }
        }

        for (int x = 0; x < highBrightnessStairs.size(); x++) {
            List<Integer> col = highBrightnessStairs.get(x).reversed();
            for (int i = 0; i < col.size(); i += 2) {
                int z0 = col.get(i);
                int z1 = col.get(i + 1);
                int aboveHeight = z0 == 0 ? 1 : staircase.get(z0 - 1).get(x) + 1;
                if (z0 > 1 && colors[z0 - 2][x] == 0)
                    aboveHeight = 0;
                for (int z = z0; z <= z1; z++) {
                    staircase.get(z).set(x, aboveHeight++);
                }
                aboveHeight--;

                if (aboveHeight >= staircase.get(z1 + 1).get(x)) {
                    staircase.get(++z1).set(x, ++aboveHeight);
                }
                while (z1 < staircase.size() - 1 && colors[z1][x] != 0 && getBrightness(colors, x, z1 + 1) == MapColor.Brightness.NORMAL) {
                    staircase.get(++z1).set(x, aboveHeight);
                }
            }
        }
        int z = height - 1;
        for (int x = 0; x < width; x++) {
            if (getBrightness(colors, x, z) == MapColor.Brightness.HIGH)
                staircase.get(z).set(x, staircase.get(z - 1).get(x) + 1);
        }

        return staircase;
    }

    private static MapColor.Brightness getBrightness(int[][] colors, int x, int z) {
        return PaletteColors.getMapColorEntryByARGB(colors[z - 1][x]).brightness();
    }

    private static int getHeightShift(int[][] colors, int x, int z) {
        if (z == colors.length) return 0;
        MapColor.Brightness brightness = getBrightness(colors, x, z + 1);
        if (brightness == MapColor.Brightness.LOW) return 1;
        return 0;
    }
}
