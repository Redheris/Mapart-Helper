package rh.maparthelper.palette;

import net.minecraft.world.level.material.MapColor;
import rh.maparthelper.MapartHelper;
import rh.maparthelper.colors.ColorUtils;
import rh.maparthelper.colors.DitherEntry;
import rh.maparthelper.colors.MapColorEntry;
import rh.maparthelper.config.palette.PaletteConfigManager;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class PaletteColors {
    private static final Map<Integer, MapColorEntry> argbMapColors = new HashMap<>();
    private static final Map<Integer, DitherEntry> cachedClosestColors = new ConcurrentHashMap<>();
    private static final int[] mapRenderColors = new int[256];
    static final Set<MapColor> excludingColors = new HashSet<>();

    public static void initMapColorsCache() {
        for (int colorId = 1; colorId < 64; colorId++) {
            MapColor mapColor = MapColor.byId(colorId);
            if (mapColor == MapColor.NONE) continue;
            for (MapColor.Brightness brightness : MapColor.Brightness.values()) {
                int argb = mapColor.calculateARGBColor(brightness);
                MapColorEntry entry = new MapColorEntry(mapColor, brightness);
                PaletteColors.argbMapColors.put(argb, entry);
                byte colorByte = (byte) ((mapColor.id << 2) | brightness.id);
                PaletteColors.mapRenderColors[colorByte + 128] = argb;
            }
        }
    }

    public static MapColorEntry getMapColorEntryByARGB(int argb) {
        if (argb == 0) return MapColorEntry.CLEAR;
        return argbMapColors.get(argb);
    }

    public static int getMapRenderColor(byte colorByte) {
        return mapRenderColors[colorByte + 128];
    }

    public static int getMapRenderColor(byte colorByte, MapColor.Brightness brightness) {
        colorByte = (byte) ((colorByte & ~3) | brightness.id);
        return mapRenderColors[colorByte + 128];
    }

    private static DitherEntry findClosestColorUnconditional(int argb) {
        byte closestColorByte = 0;
        byte secondClosestColorByte = 0;
        double minDist = Integer.MAX_VALUE;
        double secondMinDist = Integer.MAX_VALUE;

        int[] rgbOriginal = ColorUtils.getRGB(argb);
        int[] rgbClosest = new int[0];

        for (int colorByteId = 4; colorByteId < 255; colorByteId++) {
            int renderColor = mapRenderColors[colorByteId];
            if (renderColor == 0) continue;
            byte colorByte = (byte) (colorByteId - 128);
            if (renderColor == argb) return new DitherEntry(colorByte);

            double dist = ColorUtils.colorDistance(argb, renderColor, MapartHelper.conversionConfig().useLAB());
            if (dist < minDist) {
                secondMinDist = minDist;
                minDist = dist;
                secondClosestColorByte = closestColorByte;
                closestColorByte = colorByte;
                rgbClosest = ColorUtils.getRGB(renderColor);
            } else if (dist < secondMinDist && colorByte != closestColorByte) {
                secondMinDist = dist;
                secondClosestColorByte = colorByte;
            }
        }

        int errorRed = rgbOriginal[0] - rgbClosest[0];
        int errorGreen = rgbOriginal[1] - rgbClosest[1];
        int errorBlue = rgbOriginal[2] - rgbClosest[2];
        return new DitherEntry(
                closestColorByte, secondClosestColorByte,
                (float) (minDist / (minDist + secondMinDist)),
                errorRed, errorGreen, errorBlue
        );
    }

    private static DitherEntry getClosestColor3D(int argb) {
        byte closestColorByte = 0;
        byte secondClosestColorByte = 0;
        double minDist = Integer.MAX_VALUE;
        double secondMinDist = Integer.MAX_VALUE;

        int[] rgbOriginal = ColorUtils.getRGB(argb);
        int[] rgbClosest = new int[0];

        for (MapColor mapColor : PaletteConfigManager.presetsConfig.getCurrentPresetColors()) {
            if (excludingColors.contains(mapColor)) continue;

            for (int brightness = 0; brightness < 3; brightness++) {
                if (mapColor == MapColor.WATER && brightness != 2) continue; // Allow only HIGH for water color
                byte colorByte = (byte) ((mapColor.id << 2) | brightness);

                int renderColor = mapRenderColors[colorByte + 128];
                if (renderColor == argb) return new DitherEntry(colorByte);

                double dist = ColorUtils.colorDistance(argb, renderColor, MapartHelper.conversionConfig().useLAB());
                if (dist < minDist) {
                    secondMinDist = minDist;
                    minDist = dist;
                    secondClosestColorByte = closestColorByte;
                    closestColorByte = colorByte;
                    rgbClosest = ColorUtils.getRGB(renderColor);
                } else if (dist < secondMinDist && colorByte != closestColorByte) {
                    secondMinDist = dist;
                    secondClosestColorByte = colorByte;
                }
            }
        }

        if (secondClosestColorByte == 0) {
            secondClosestColorByte = closestColorByte;
            secondMinDist = minDist;
        }

        int errorRed = rgbOriginal[0] - rgbClosest[0];
        int errorGreen = rgbOriginal[1] - rgbClosest[1];
        int errorBlue = rgbOriginal[2] - rgbClosest[2];
        return new DitherEntry(
                closestColorByte, secondClosestColorByte,
                (float) (minDist / (minDist + secondMinDist)),
                errorRed, errorGreen, errorBlue
        );
    }

    private static DitherEntry getClosestColor2D(int argb) {
        byte closestColorByte = 0;
        byte secondClosestColorByte = 0;
        double minDist = Integer.MAX_VALUE;
        double secondMinDist = Integer.MAX_VALUE;

        int[] rgbOriginal = ColorUtils.getRGB(argb);
        int[] rgbClosest = new int[0];

        for (MapColor mapColor : PaletteConfigManager.presetsConfig.getCurrentPresetColors()) {
            if (excludingColors.contains(mapColor)) continue;

            int brightness = mapColor == MapColor.WATER ? 2 : 1; // Normal brightness for water color is HIGH(2)
            byte colorByte = (byte) ((mapColor.id << 2) | brightness);

            int renderColor = MapColor.getColorFromPackedId(colorByte);
            if (renderColor == argb) return new DitherEntry(colorByte);

            double dist = ColorUtils.colorDistance(argb, renderColor, MapartHelper.conversionConfig().useLAB());
            if (dist < minDist) {
                secondMinDist = minDist;
                minDist = dist;
                secondClosestColorByte = closestColorByte;
                closestColorByte = colorByte;
                rgbClosest = ColorUtils.getRGB(renderColor);
            } else if (dist < secondMinDist && colorByte != closestColorByte) {
                secondMinDist = dist;
                secondClosestColorByte = colorByte;
            }
        }

        if (secondClosestColorByte == 0) {
            secondClosestColorByte = closestColorByte;
            secondMinDist = minDist;
        }

        int errorRed = rgbOriginal[0] - rgbClosest[0];
        int errorGreen = rgbOriginal[1] - rgbClosest[1];
        int errorBlue = rgbOriginal[2] - rgbClosest[2];
        return new DitherEntry(
                closestColorByte, secondClosestColorByte,
                (float) (minDist / (minDist + secondMinDist)),
                errorRed, errorGreen, errorBlue
        );
    }

    public static DitherEntry getClosestColor(int argb, boolean use3D) {
        if (((argb >> 24) & 0xFF) < 80) return DitherEntry.CLEAR;
        if (use3D)
            return cachedClosestColors.computeIfAbsent(argb, PaletteColors::getClosestColor3D);
        return cachedClosestColors.computeIfAbsent(argb, PaletteColors::getClosestColor2D);
    }

    public static DitherEntry getClosestColorUnconditional(int argb) {
        if (((argb >> 24) & 0xFF) < 80) return DitherEntry.CLEAR;
        return cachedClosestColors.computeIfAbsent(argb, PaletteColors::findClosestColorUnconditional);
    }

    public static int excludingColorsAmount() {
        return excludingColors.size();
    }

    public static boolean addExcludingColors(Set<MapColor> excludingColors) {
        clearColorCache();
        return PaletteColors.excludingColors.addAll(excludingColors);
    }

    public static void clearExcludingColors() {
        excludingColors.clear();
        clearColorCache();
    }

    public static void clearColorCache() {
        cachedClosestColors.clear();
        ColorUtils.clearRgb2LabCache();
    }
}
