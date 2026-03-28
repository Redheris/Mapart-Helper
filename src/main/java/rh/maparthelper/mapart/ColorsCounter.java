package rh.maparthelper.mapart;

import net.minecraft.world.level.material.MapColor;
import rh.maparthelper.config.palette.PaletteConfigManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class ColorsCounter {
    private int[] counter;

    public ColorsCounter() {
        this.counter = new int[63];
    }

    public ColorsCounter(ColorsCounter counter) {
        this.counter = Arrays.copyOf(counter.counter, 63);
    }

    public static ColorsCounter intersect(ColorsCounter[] subCounters) {
        if (subCounters == null || subCounters.length == 0) return new ColorsCounter();

        ColorsCounter newCounter = new ColorsCounter(subCounters[0]);
        for (int i = 1; i < subCounters.length; i++) {
            ColorsCounter subCounter = subCounters[i];
            for (int color = 0; color < 63; color++) {
                newCounter.counter[color] = Math.max(newCounter.counter[color], subCounter.counter[color]);
            }
        }
        return newCounter;
    }

    public static ColorsCounter sum(ColorsCounter[] subCounters) {
        if (subCounters == null || subCounters.length == 0) return new ColorsCounter();

        ColorsCounter newCounter = new ColorsCounter(subCounters[0]);
        for (int i = 1; i < subCounters.length; i++) {
            ColorsCounter subCounter = subCounters[i];
            for (int color = 0; color < 63; color++) {
                newCounter.counter[color] += subCounter.counter[color];
            }
        }
        return newCounter;
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

    public MapColorCount[] getColorCounts(boolean ascending) {
        Comparator<MapColorCount> cmp = Comparator.comparingInt(MapColorCount::amount);
        List<MapColorCount> counts = new ArrayList<>();

        for (MapColor color : PaletteConfigManager.presetsConfig.getCurrentPresetColors()) {
            int amount = counter[color.id - 1];
            if (amount > 0)
                counts.add(new MapColorCount(color.id, amount));
        }

        counts.sort(ascending ? cmp : cmp.reversed());
        return counts.toArray(MapColorCount[]::new);
    }

    public record MapColorCount(int id, int amount) {
    }
}
