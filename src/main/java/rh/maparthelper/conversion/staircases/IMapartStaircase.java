package rh.maparthelper.conversion.staircases;

import net.minecraft.world.level.material.MapColor;
import rh.maparthelper.colors.MapColorEntry;
import rh.maparthelper.palette.PaletteColors;

import java.util.Arrays;
import java.util.List;

public interface IMapartStaircase {
    List<List<Integer>> getStaircase(int[][] colors);

    default int[][] getBrightnesses(int[][] colors) {
        return Arrays.stream(colors)
                .map(ints -> Arrays.stream(ints)
                        .map(color -> {
                            MapColorEntry mapColor = PaletteColors.getMapColorEntryByARGB(color);
                            return mapColor.mapColor() == MapColor.WATER ? 1 : mapColor.brightness().id;
                        })
                        .toArray()
                ).toArray(int[][]::new);
    }
}
