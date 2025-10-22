package rh.maparthelper.conversion.staircases;

import java.util.List;

public enum StaircaseStyles {
    FLAT_2D(null),
    VALLEY_3D(new Valley3DStaircase()),
    WAVES_3D(new Waves3DStaircase());
//    SMOOTH_3D(new Smooth3DStaircase()); // Postponed for now

    private final IMapartStaircase staircase;

    StaircaseStyles(IMapartStaircase staircase) {
        this.staircase = staircase;
    }

    public List<List<Integer>> getStaircase(int[][] colors) {
        return staircase.getStaircase(colors);
    }
}
