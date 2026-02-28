package rh.maparthelper.conversion;

import net.minecraft.block.MapColor;
import rh.maparthelper.config.palette.PaletteColors;
import rh.maparthelper.mapart.MapartProcessing;

import java.nio.file.Path;
import java.util.Set;

public class MapartImageUpdater {
    public static void readAndUpdateMapartImage(MapartProcessing mapart, Path path) {
        MapartImageConverter.readAndUpdateMapartImage(mapart, path, ImageChangeResult.NEED_FULL_UPDATE);
        mapart.setReset(false);
    }

    public static void updateMapart(MapartProcessing mapart) {
        if (!mapart.isReset())
            readAndUpdateMapartImage(mapart, mapart.getImagePath());
    }

    public static void changeCroppingMode(MapartProcessing mapart, CroppingMode cropMode) {
        if (mapart.isReset()) return;
        if (!mapart.isReset() && cropMode == CroppingMode.USER_CROP)
            mapart.autoCropOriginalImage();
        MapartImageConverter.readAndUpdateMapartImage(mapart, mapart.getImagePath(), ImageChangeResult.NEED_FULL_UPDATE);
    }

    public static void resizeMapartImage(MapartProcessing mapart, int width, int height) {
        mapart.setMapartSize(width, height);
        if (mapart.isReset()) return;
        if (mapart.getOriginal() != null) {
            mapart.autoCropOriginalImage();
        }
        MapartImageConverter.readAndUpdateMapartImage(mapart, mapart.getImagePath(), ImageChangeResult.NEED_FULL_UPDATE);
    }

    public static void scaleToPoint(MapartProcessing mapart, double pointX, double pointY, double scale) {
        if (mapart.isReset()) return;
        mapart.scaleToPoint(pointX, pointY, scale);
        MapartImageConverter.readAndUpdateMapartImage(mapart, mapart.getImagePath(), ImageChangeResult.NEED_FULL_UPDATE);
    }

    public static void scaleToCenter(MapartProcessing mapart, double scale) {
        if (mapart.isReset()) return;
        mapart.scaleToCenter(scale);
        MapartImageConverter.readAndUpdateMapartImage(mapart, mapart.getImagePath(), ImageChangeResult.NEED_FULL_UPDATE);
    }

    public static void moveCroppingFrameOrMapartImage(MapartProcessing mapart, int dx, int dy, boolean withMouse) {
        if (mapart.isReset()) return;
        int type = withMouse ? -1 : 1;
        if (dx != 0 || dy != 0) {
            ImageChangeResult imageChangeResult = mapart.moveCroppingFrame(dx, dy, type);
            MapartImageConverter.readAndUpdateMapartImage(mapart, mapart.getImagePath(), imageChangeResult);
        }
    }

    public static void centerCroppingFrameAndMapartImage(MapartProcessing mapart) {
        if (mapart.isReset()) return;
        mapart.centerImage();
        MapartImageConverter.readAndUpdateMapartImage(mapart, mapart.getImagePath(), ImageChangeResult.NEED_FULL_UPDATE);
    }

    public static void fitImageBySide(MapartProcessing mapart, int side) {
        if (mapart.isReset()) return;
        mapart.fitBySide(side);
        MapartImageConverter.readAndUpdateMapartImage(mapart, mapart.getImagePath(), ImageChangeResult.NEED_FULL_UPDATE);
    }

    public static void excludeColorsFromMapart(MapartProcessing mapart, Set<MapColor> excludingColors) {
        if (mapart.isReset()) return;
        if (PaletteColors.addExcludingColors(excludingColors))
            MapartImageConverter.readAndUpdateMapartImage(mapart, mapart.getImagePath(), ImageChangeResult.NEED_FULL_UPDATE);
    }

    public static void revertExcludingColors(MapartProcessing mapart) {
        if (mapart.isReset()) return;
        PaletteColors.clearExcludingColors();
        MapartImageConverter.readAndUpdateMapartImage(mapart, mapart.getImagePath(), ImageChangeResult.NEED_FULL_UPDATE);
    }
}
