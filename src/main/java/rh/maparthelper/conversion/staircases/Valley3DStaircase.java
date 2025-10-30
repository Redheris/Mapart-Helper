package rh.maparthelper.conversion.staircases;

import net.minecraft.block.MapColor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Valley3DStaircase implements IMapartStaircase {

    @Override
    public List<List<Integer>> getStaircase(int[][] colors) {
        int[][] brights = getBrightnesses(colors);
        int height = colors.length + 1;
        int width = colors[0].length;

        List<List<Integer>> staircase = new ArrayList<>();
        for (int z = 0; z < height; z++) {
            List<Integer> row = new ArrayList<>(Collections.nCopies(width, 0));
            staircase.add(row);
        }

        for (int column = 0; column < width; column++) {
            applyValleyToColumn(colors, brights, staircase, column);
        }

        return staircase;
    }

    static void applyValleyToColumn(int[][] colors, int[][] brights, List<List<Integer>> staircase, int col) {
        final int LOW = MapColor.Brightness.LOW.id;
        final int HIGH = MapColor.Brightness.HIGH.id;
        int row = staircase.size() - 1;

        // Forward pass (ascending stairs)
        while (row > 0) {
            int prev = brights[row - 1][col];
            row--;
            if (prev == LOW) { // Structuring an ascending stairs
                while (row >= 0 && prev != HIGH) {
                    if (row > 0 && colors[row - 1][col] == 0) {
                        row--;
                        break;
                    }
                    int prevH = staircase.get(row + 1).get(col);
                    if (prev == LOW) {
                        staircase.get(row).set(col, prevH + 1);
                    } else {
                        staircase.get(row).set(col, prevH);
                    }
                    if (row == 0) break;
                    prev = brights[row - 1][col];
                    row--;
                }
            }
        }

        // Backward pass (descending stairs)
        row = 1;
        while (row < staircase.size()) {
            int cur = brights[row - 1][col];
            int next = row < brights.length ? brights[row][col] : -1;
            if (cur == HIGH && staircase.get(row).get(col) == 0) { // Structuring a descending stairs
                while (row < staircase.size() && next != LOW && staircase.get(row).get(col) == 0) {
                    if (row > 0 && colors[row - 1][col] == 0) {
                        row++;
                        continue;
                    }
                    int prevH = staircase.get(row - 1).get(col);
                    if (cur == HIGH && (row == 1 || colors[row - 2][col] != 0)) {
                        staircase.get(row).set(col, prevH + 1);
                    } else {
                        staircase.get(row).set(col, prevH);
                    }
                    if (row >= brights.length) break;
                    row++;
                    cur = brights[row - 1][col];
                    next = row < brights.length ? brights[row][col] : -1;
                }

                // Adjustment of boundary blocks
                if (cur != LOW && row < staircase.size() - 1) {
                    if (cur == HIGH) {
                        staircase.get(row).set(col,
                                Math.max(staircase.get(row).get(col), staircase.get(row - 1).get(col) + 1)
                        );
                    }
                    while (next == 1) {
                        row++;
                        staircase.get(row).set(col, staircase.get(row - 1).get(col));
                        next = brights[row][col];
                    }
                }
            }
            row++;
        }
    }
}
