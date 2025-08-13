package rh.maparthelper.config.palette;

import net.minecraft.block.MapColor;
import rh.maparthelper.MapartHelper;
import rh.maparthelper.conversion.colors.ColorUtils;
import rh.maparthelper.conversion.colors.MapColorEntry;

import java.util.HashMap;
import java.util.Map;

public class PaletteColors {
    static final Map<Integer, MapColorEntry> argbMapColors = new HashMap<>();
    static final Map<Integer, MapColorEntry> cachedClosestColors = new HashMap<>();

    public static MapColorEntry getMapColorEntryByARGB(int argb) {
        if (argb == 0) return MapColorEntry.CLEAR;
        return argbMapColors.get(argb);
    }

    private static MapColorEntry getClosestColor3D(int argb) {
        MapColor closestColor = MapColor.CLEAR;
        MapColor.Brightness closeBrightness = MapColor.Brightness.NORMAL;
        double minDist = Integer.MAX_VALUE;

        int[] rgbOriginal = new int[0];
        int[] rgbClosest = new int[0];
        if (MapartHelper.config.conversionSettings.useDithering())
            rgbOriginal = ColorUtils.getRGB(argb);

        for (MapColor color : PaletteConfigManager.presetsConfig.getCurrentPresetColors()) {
            for (int brightId = 0; brightId < 3; brightId++) {
                MapColor.Brightness brightness;
                brightness = color == MapColor.WATER_BLUE ? MapColor.Brightness.NORMAL : MapColor.Brightness.validateAndGet(brightId);
                int current = color.getRenderColor(brightness);
                if (current == argb) return new MapColorEntry(color, brightness, new int[]{0, 0, 0});

                double dist = ColorUtils.colorDistance(argb, current, MapartHelper.config.conversionSettings.useLAB);
                if (dist < minDist) {
                    minDist = dist;
                    closestColor = color;
                    closeBrightness = brightness;
                    if (MapartHelper.config.conversionSettings.useDithering())
                        rgbClosest = ColorUtils.getRGB(current);
                }

                if (color == MapColor.WATER_BLUE) break;
            }
        }

        if (MapartHelper.config.conversionSettings.useDithering()) {
            int[] distError = new int[]{
                    rgbOriginal[0] - rgbClosest[0],
                    rgbOriginal[1] - rgbClosest[1],
                    rgbOriginal[2] - rgbClosest[2]
            };
            return new MapColorEntry(closestColor, closeBrightness, distError);
        }

        return new MapColorEntry(closestColor, closeBrightness);
    }

    private static MapColorEntry getClosestColor2D(int argb) {
        MapColor closest = MapColor.CLEAR;
        double minDist = Integer.MAX_VALUE;

        int[] rgbOriginal = new int[0];
        int[] rgbClosest = new int[0];
        if (MapartHelper.config.conversionSettings.useDithering())
            rgbOriginal = ColorUtils.getRGB(argb);

        for (MapColor color : PaletteConfigManager.presetsConfig.getCurrentPresetColors()) {
            int current = color.getRenderColor(MapColor.Brightness.NORMAL);
            if (current == argb) return new MapColorEntry(color, MapColor.Brightness.NORMAL, new int[]{0, 0, 0});

            double dist = ColorUtils.colorDistance(argb, current, MapartHelper.config.conversionSettings.useLAB);
            if (dist < minDist) {
                minDist = dist;
                closest = color;
                if (MapartHelper.config.conversionSettings.useDithering())
                    rgbClosest = ColorUtils.getRGB(current);
            }
        }

        if (MapartHelper.config.conversionSettings.useDithering()) {
            int[] distError = new int[]{
                    rgbOriginal[0] - rgbClosest[0],
                    rgbOriginal[1] - rgbClosest[1],
                    rgbOriginal[2] - rgbClosest[2]
            };
            return new MapColorEntry(closest, MapColor.Brightness.NORMAL, distError);
        }

        return new MapColorEntry(closest, MapColor.Brightness.NORMAL);
    }

    public static MapColorEntry getClosestColor(int argb, boolean use3D) {
        if (((argb >> 24) & 0xFF) < 80) return MapColorEntry.CLEAR;
        if (use3D)
            return cachedClosestColors.computeIfAbsent(argb, PaletteColors::getClosestColor3D);
        return cachedClosestColors.computeIfAbsent(argb, PaletteColors::getClosestColor2D);
    }

    public static void clearColorCache(){
        cachedClosestColors.clear();
        ColorUtils.clearRgb2LabCache();
    }
}
