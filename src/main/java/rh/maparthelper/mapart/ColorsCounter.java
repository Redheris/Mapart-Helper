package rh.maparthelper.mapart;

import net.minecraft.world.level.material.MapColor;
import rh.maparthelper.palette.PaletteDataManager;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicIntegerArray;

public class ColorsCounter {
    private AtomicIntegerArray counter;

    public ColorsCounter() {
        this.counter = new AtomicIntegerArray(63);
    }

    public ColorsCounter(ColorsCounter counter) {
        this.counter = new AtomicIntegerArray(63);

        for (int i = 0; i < counter.counter.length(); i++) {
            this.counter.set(i, counter.counter.get(i));
        }
    }

    public static ColorsCounter intersect(ColorsCounter[] subCounters) {
        if (subCounters == null || subCounters.length == 0) return new ColorsCounter();

        ColorsCounter newCounter = new ColorsCounter(subCounters[0]);
        for (int i = 1; i < subCounters.length; i++) {
            ColorsCounter subCounter = subCounters[i];
            for (int color = 0; color < 63; color++) {
                newCounter.counter.set(color, Math.max(newCounter.counter.get(color), subCounter.counter.get(color)));
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
                newCounter.counter.addAndGet(color, subCounter.counter.get(color));
            }
        }
        return newCounter;
    }

    public void increment(int colorId) {
        this.counter.incrementAndGet(colorId - 1);
    }

    public int get(int colorId) {
        return this.counter.get(colorId - 1);
    }

    public void clear() {
        this.counter = new AtomicIntegerArray(63);
    }

    public MapColorCount[] getColorCounts(boolean ascending) {
        Comparator<MapColorCount> cmp = Comparator.comparingInt(MapColorCount::amount);
        List<MapColorCount> counts = new ArrayList<>();

        Set<MapColor> colors = PaletteDataManager.getInstance().getPresetsHandler().getSelectedPreset().getMapColors();
        for (MapColor color : colors) {
            int amount = counter.get(color.id - 1);
            if (amount > 0)
                counts.add(new MapColorCount(color.id, amount));
        }

        counts.sort(ascending ? cmp : cmp.reversed());
        return counts.toArray(MapColorCount[]::new);
    }

    public record MapColorCount(int id, int amount) {
    }
}
