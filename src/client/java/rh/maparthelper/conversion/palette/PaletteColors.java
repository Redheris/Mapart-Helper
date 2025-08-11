package rh.maparthelper.conversion.palette;

import net.minecraft.block.MapColor;
import rh.maparthelper.MapartHelper;
import rh.maparthelper.conversion.colors.ColorUtils;
import rh.maparthelper.conversion.palette.config.PaletteConfigManager;

import java.util.HashMap;
import java.util.Map;

import static rh.maparthelper.conversion.palette.PaletteGenerator.argbMapColors;

public class PaletteColors {
    static final Map<Integer, MapColorEntry> cachedClosestColors = new HashMap<>();

    public static MapColorEntry getMapColorEntryByARGB(int argb) {
        if (argb == 0) return MapColorEntry.CLEAR;
        return argbMapColors.get(argb);
    }

    private static MapColorEntry getClosestColor3D(int argb) {
        MapColorEntry closest = new MapColorEntry(MapColor.CLEAR, MapColor.Brightness.NORMAL);
        double minDist = Integer.MAX_VALUE;

        for (MapColor color : PaletteConfigManager.palettePresetsConfig.getCurrentPresetColors()) {
            for (int brightId = 0; brightId < 3; brightId++) {
                MapColor.Brightness brightness;
                brightness = color == MapColor.WATER_BLUE ? MapColor.Brightness.NORMAL : MapColor.Brightness.validateAndGet(brightId);
                int current = color.getRenderColor(brightness);

                if (current == argb) return new MapColorEntry(color, brightness);

                double dist = ColorUtils.colorDistance(argb, current, MapartHelper.config.conversionSettings.useLAB);
                if (dist < minDist) {
                    minDist = dist;
                    closest = new MapColorEntry(color, brightness);
                }

                if (color == MapColor.WATER_BLUE) break;
            }
        }

        return closest;
    }

    private static MapColorEntry getClosestColor2D(int argb) {
        MapColor closest = MapColor.CLEAR;
        double minDist = Integer.MAX_VALUE;

        for (MapColor color : PaletteConfigManager.palettePresetsConfig.getCurrentPresetColors()) {
            int current = color.getRenderColor(MapColor.Brightness.NORMAL);
            if (current == argb) return new MapColorEntry(color, MapColor.Brightness.NORMAL);
            double dist = ColorUtils.colorDistance(argb, current, MapartHelper.config.conversionSettings.useLAB);
            if (dist < minDist) {
                minDist = dist;
                closest = color;
            }
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
