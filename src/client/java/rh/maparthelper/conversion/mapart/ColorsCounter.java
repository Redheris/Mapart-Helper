package rh.maparthelper.conversion.mapart;

import java.util.Arrays;

public class ColorsCounter {
    private int[] counter;

    public ColorsCounter() {
        this.counter = new int[63];
    }

    public ColorsCounter(ColorsCounter counter) {
        this.counter = Arrays.copyOf(counter.counter, 63);
    }

    public void increment(int colorId) {
        this.counter[colorId - 1]++;
    }

    public int get(int colorId) {
        return this.counter[colorId - 1];
    }

    public void clear() {
        this.counter = new int[63];
    }
}
