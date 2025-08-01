package rh.maparthelper.conversion.staircases;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// TODO: replace this crap by simply checking for 2D
public class Flat2DStaircase implements IMapartStaircase {

    @Override
    public List<List<Integer>> getStaircase(int[][] colors) {
        int height = colors.length + 1;
        int width = colors[0].length;

        List<List<Integer>> staircase = new ArrayList<>();
        for (int y = 0; y < height; y++) {
            List<Integer> row = new ArrayList<>(Collections.nCopies(width, 0));
            staircase.add(row);
        }
        return staircase;
    }
}
