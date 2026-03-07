package rh.maparthelper.config.palette;

import net.minecraft.block.MapColor;
import rh.maparthelper.MapartHelper;
import rh.maparthelper.colors.ColorUtils;
import rh.maparthelper.colors.MapColorEntry;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PaletteColors {
    static final Map<Integer, MapColorEntry> argbMapColors = new HashMap<>();
    static final Map<Integer, MapColorEntry> cachedClosestColors = new HashMap<>();
    static final Set<MapColor> excludingColors = new HashSet<>();

    public static MapColorEntry getMapColorEntryByARGB(int argb) {
        if (argb == 0) return MapColorEntry.CLEAR;
        return argbMapColors.get(argb);
    }

    private static MapColorEntry getClosestColor3D(int argb) {
        MapColor closestColor = MapColor.CLEAR;
        MapColor.Brightness closestBrightness = MapColor.Brightness.NORMAL;
        double minDist = Integer.MAX_VALUE;

        int[] rgbOriginal = ColorUtils.getRGB(argb);
        int[] rgbClosest = new int[0];

        for (MapColor color : PaletteConfigManager.presetsConfig.getCurrentPresetColors()) {
            if (excludingColors.contains(color)) continue;
            for (int brightId = 0; brightId < 3; brightId++) {
                MapColor.Brightness brightness;
                brightness = color == MapColor.WATER_BLUE ? MapColor.Brightness.NORMAL : MapColor.Brightness.validateAndGet(brightId);
                int current = color.getRenderColor(brightness);
                if (current == argb) return new MapColorEntry(color, brightness);

                double dist = ColorUtils.colorDistance(argb, current, MapartHelper.conversionSettings.useLAB());
                if (dist < minDist) {
                    minDist = dist;
                    closestColor = color;
                    closestBrightness = brightness;
                    rgbClosest = ColorUtils.getRGB(current);
                }

                if (color == MapColor.WATER_BLUE) break;
            }
        }

        int errorRed = rgbOriginal[0] - rgbClosest[0];
        int errorGreen = rgbOriginal[1] - rgbClosest[1];
        int errorBlue = rgbOriginal[2] - rgbClosest[2];
        return new MapColorEntry(closestColor, closestBrightness, errorRed, errorGreen, errorBlue);
    }

    private static MapColorEntry getClosestColor2D(int argb) {
        MapColor closestColor = MapColor.CLEAR;
        double minDist = Integer.MAX_VALUE;

        int[] rgbOriginal = ColorUtils.getRGB(argb);
        int[] rgbClosest = new int[0];

        for (MapColor color : PaletteConfigManager.presetsConfig.getCurrentPresetColors()) {
            if (excludingColors.contains(color)) continue;
            int current = color.getRenderColor(MapColor.Brightness.NORMAL);
            if (current == argb) return new MapColorEntry(color, MapColor.Brightness.NORMAL);

            double dist = ColorUtils.colorDistance(argb, current, MapartHelper.conversionSettings.useLAB());
            if (dist < minDist) {
                minDist = dist;
                closestColor = color;
                rgbClosest = ColorUtils.getRGB(current);
            }
        }

        int errorRed = rgbOriginal[0] - rgbClosest[0];
        int errorGreen = rgbOriginal[1] - rgbClosest[1];
        int errorBlue = rgbOriginal[2] - rgbClosest[2];
        return new MapColorEntry(closestColor, MapColor.Brightness.NORMAL, errorRed, errorGreen, errorBlue);
    }

    public static MapColorEntry getClosestColor(int argb, boolean use3D) {
        if (((argb >> 24) & 0xFF) < 80) return MapColorEntry.CLEAR;
        if (use3D)
            return cachedClosestColors.computeIfAbsent(argb, PaletteColors::getClosestColor3D);
        return cachedClosestColors.computeIfAbsent(argb, PaletteColors::getClosestColor2D);
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
