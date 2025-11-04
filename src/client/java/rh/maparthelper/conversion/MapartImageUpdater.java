package rh.maparthelper.conversion;

import net.minecraft.block.MapColor;
import rh.maparthelper.config.palette.PaletteColors;
import rh.maparthelper.conversion.mapart.ConvertedMapartImage;
import rh.maparthelper.conversion.mapart.ProcessingMapartImage;

import java.nio.file.Path;
import java.util.Set;

public class MapartImageUpdater {
    static double moveDx = 0;
    static double moveDy = 0;
    static double scale = 0;

    public static void readAndUpdateMapartImage(ConvertedMapartImage mapart, Path path) {
        ProcessingMapartImage processingMapart = new ProcessingMapartImage(mapart);
        MapartImageConverter.readAndUpdateMapartImage(mapart, processingMapart, path, ImageChangeResult.NEED_RESCALE);
        mapart.reset(false);
    }

    public static void updateMapart(ConvertedMapartImage mapart) {
        if (!mapart.isReset())
            readAndUpdateMapartImage(mapart, mapart.getImagePath());
    }

    public static void changeCroppingMode(ConvertedMapartImage mapart, CroppingMode cropMode) {
        if (mapart.isReset()) return;
        ProcessingMapartImage processingMapart = new ProcessingMapartImage(mapart);
        if (!mapart.isReset() && cropMode == CroppingMode.USER_CROP)
            processingMapart.autoCropOriginalImage();
        MapartImageConverter.readAndUpdateMapartImage(mapart, processingMapart, mapart.getImagePath(), ImageChangeResult.NEED_RESCALE);
    }

    public static void resizeMapartImage(ConvertedMapartImage mapart, int width, int height) {
        mapart.setMapartSize(width, height);
        if (mapart.isReset()) return;
        ProcessingMapartImage processingMapart = new ProcessingMapartImage(mapart);
        if (processingMapart.getOriginal() != null) {
            processingMapart.autoCropOriginalImage();
        }
        MapartImageConverter.readAndUpdateMapartImage(mapart, processingMapart, mapart.getImagePath(), ImageChangeResult.NEED_RESCALE);
    }

    public static void scaleToPoint(ConvertedMapartImage mapart, double pointX, double pointY, double scale) {
        if (mapart.isReset()) return;
        MapartImageUpdater.scale += scale;
        ProcessingMapartImage processingMapart = new ProcessingMapartImage(mapart);
        processingMapart.scaleToPoint(pointX, pointY, MapartImageUpdater.scale);
        MapartImageConverter.readAndUpdateMapartImage(mapart, processingMapart, mapart.getImagePath(), ImageChangeResult.NEED_RESCALE);
    }

    public static void scaleToCenter(ConvertedMapartImage mapart, double scale) {
        if (mapart.isReset()) return;
        MapartImageUpdater.scale += scale;
        ProcessingMapartImage processingMapart = new ProcessingMapartImage(mapart);
        processingMapart.scaleToCenter(MapartImageUpdater.scale);
        MapartImageConverter.readAndUpdateMapartImage(mapart, processingMapart, mapart.getImagePath(), ImageChangeResult.NEED_RESCALE);
    }

    public static void moveCroppingFrameOrMapartImage(ConvertedMapartImage mapart, double dx, double dy, boolean withMouse) {
        if (mapart.isReset()) return;
        moveDx += dx;
        moveDy += dy;
        ProcessingMapartImage processingMapart = new ProcessingMapartImage(mapart);
        int type = withMouse ? -1 : 1;
        ImageChangeResult imageChangeResult = processingMapart.moveCroppingFrame((int) moveDx, (int) moveDy, type);
        MapartImageConverter.readAndUpdateMapartImage(mapart, processingMapart, mapart.getImagePath(), imageChangeResult);
    }

    public static void centerCroppingFrameAndMapartImage(ConvertedMapartImage mapart) {
        if (mapart.isReset()) return;
        ProcessingMapartImage processingMapart = new ProcessingMapartImage(mapart);
        processingMapart.centerImage();
        MapartImageConverter.readAndUpdateMapartImage(mapart, processingMapart, mapart.getImagePath(), ImageChangeResult.NEED_RESCALE);
    }

    public static void fitImageBySide(ConvertedMapartImage mapart, int side) {
        if (mapart.isReset()) return;
        ProcessingMapartImage processingMapart = new ProcessingMapartImage(mapart);
        processingMapart.fitBySide(side);
        MapartImageConverter.readAndUpdateMapartImage(mapart, processingMapart, mapart.getImagePath(), ImageChangeResult.NEED_RESCALE);
    }

    public static void removeColorsFromMapart(ConvertedMapartImage mapart, Set<MapColor> excludingColors) {
        if (mapart.isReset()) return;
        ProcessingMapartImage processingMapart = new ProcessingMapartImage(mapart);
        if (PaletteColors.addExcludingColors(excludingColors))
            MapartImageConverter.readAndUpdateMapartImage(mapart, processingMapart, mapart.getImagePath(), ImageChangeResult.NEED_RESCALE);
    }

    public static void revertRemovingColors(ConvertedMapartImage mapart) {
        if (mapart.isReset()) return;
        ProcessingMapartImage processingMapart = new ProcessingMapartImage(mapart);
        PaletteColors.clearExcludingColors();
        MapartImageConverter.readAndUpdateMapartImage(mapart, processingMapart, mapart.getImagePath(), ImageChangeResult.NEED_RESCALE);
    }
}
