package rh.maparthelper.conversion.staircases;

import java.util.List;

public enum StaircaseStyles {
    FLAT_2D(null),
    WAVES_3D(new Waves3DStaircase()),
    STAIRS_3D(null),
    SMART_3D(null);

    final IMapartStaircase staircase;

    StaircaseStyles(IMapartStaircase staircase) {
        this.staircase = staircase;
    }

    public List<List<Integer>> getStaircase(int[][] colors) {
        return staircase.getStaircase(colors);
    }
}
