package rh.maparthelper.conversion;

import net.minecraft.block.MapColor;
import rh.maparthelper.config.palette.PaletteColors;
import rh.maparthelper.mapart.ProcessingMapartImage;

import java.nio.file.Path;
import java.util.Set;

public class MapartImageUpdater {
    static double moveDx = 0;
    static double moveDy = 0;
    static double scale = 0;

    public static void readAndUpdateMapartImage(ProcessingMapartImage mapart, Path path) {
        MapartImageConverter.readAndUpdateMapartImage(mapart, path, ImageChangeResult.NEED_RESCALE);
        mapart.setReset(false);
    }

    public static void updateMapart(ProcessingMapartImage mapart) {
        if (!mapart.isReset())
            readAndUpdateMapartImage(mapart, mapart.getImagePath());
    }

    public static void changeCroppingMode(ProcessingMapartImage mapart, CroppingMode cropMode) {
        if (mapart.isReset()) return;
        if (!mapart.isReset() && cropMode == CroppingMode.USER_CROP)
            mapart.autoCropOriginalImage();
        MapartImageConverter.readAndUpdateMapartImage(mapart, mapart.getImagePath(), ImageChangeResult.NEED_RESCALE);
    }

    public static void resizeMapartImage(ProcessingMapartImage mapart, int width, int height) {
        mapart.setMapartSize(width, height);
        if (mapart.isReset()) return;
        if (mapart.getOriginal() != null) {
            mapart.autoCropOriginalImage();
        }
        MapartImageConverter.readAndUpdateMapartImage(mapart, mapart.getImagePath(), ImageChangeResult.NEED_RESCALE);
    }

    public static void scaleToPoint(ProcessingMapartImage mapart, double pointX, double pointY, double scale) {
        if (mapart.isReset()) return;
        MapartImageUpdater.scale += scale;
        mapart.scaleToPoint(pointX, pointY, MapartImageUpdater.scale);
        MapartImageConverter.readAndUpdateMapartImage(mapart, mapart.getImagePath(), ImageChangeResult.NEED_RESCALE);
    }

    public static void scaleToCenter(ProcessingMapartImage mapart, double scale) {
        if (mapart.isReset()) return;
        MapartImageUpdater.scale += scale;
        mapart.scaleToCenter(MapartImageUpdater.scale);
        MapartImageConverter.readAndUpdateMapartImage(mapart, mapart.getImagePath(), ImageChangeResult.NEED_RESCALE);
    }

    // TODO: replace delta-changes with set-changes
    public static void moveCroppingFrameOrMapartImage(ProcessingMapartImage mapart, double dx, double dy, boolean withMouse) {
        if (mapart.isReset()) return;
        moveDx += dx;
        moveDy += dy;
        int type = withMouse ? -1 : 1;
        ImageChangeResult imageChangeResult = mapart.moveCroppingFrame((int) moveDx, (int) moveDy, type);
        MapartImageConverter.readAndUpdateMapartImage(mapart, mapart.getImagePath(), imageChangeResult);
    }

    public static void centerCroppingFrameAndMapartImage(ProcessingMapartImage mapart) {
        if (mapart.isReset()) return;
        mapart.centerImage();
        MapartImageConverter.readAndUpdateMapartImage(mapart, mapart.getImagePath(), ImageChangeResult.NEED_RESCALE);
    }

    public static void fitImageBySide(ProcessingMapartImage mapart, int side) {
        if (mapart.isReset()) return;
        mapart.fitBySide(side);
        MapartImageConverter.readAndUpdateMapartImage(mapart, mapart.getImagePath(), ImageChangeResult.NEED_RESCALE);
    }

    public static void excludeColorsFromMapart(ProcessingMapartImage mapart, Set<MapColor> excludingColors) {
        if (mapart.isReset()) return;
        if (PaletteColors.addExcludingColors(excludingColors))
            MapartImageConverter.readAndUpdateMapartImage(mapart, mapart.getImagePath(), ImageChangeResult.NEED_RESCALE);
    }

    public static void revertExcludingColors(ProcessingMapartImage mapart) {
        if (mapart.isReset()) return;
        PaletteColors.clearExcludingColors();
        MapartImageConverter.readAndUpdateMapartImage(mapart, mapart.getImagePath(), ImageChangeResult.NEED_RESCALE);
    }
}
