package rh.maparthelper.conversion.staircases;

import rh.maparthelper.config.palette.PaletteColors;

import java.util.Arrays;
import java.util.List;

public interface IMapartStaircase {
    List<List<Integer>> getStaircase(int[][] colors);

    default int[][] getBrightnesses(int[][] colors) {
        return Arrays.stream(colors)
                .map(ints -> Arrays.stream(ints)
                        .map(color -> PaletteColors.getMapColorEntryByARGB(color).brightness().id).toArray()
                ).toArray(int[][]::new);
    }
}
