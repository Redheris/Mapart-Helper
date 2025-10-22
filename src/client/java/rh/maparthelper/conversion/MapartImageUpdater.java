package rh.maparthelper.conversion;

import rh.maparthelper.conversion.mapart.ConvertedMapartImage;
import rh.maparthelper.conversion.mapart.ProcessingMapartImage;

import java.nio.file.Path;

public class MapartImageUpdater {

    public static void readAndUpdateMapartImage(ConvertedMapartImage mapart, Path path, boolean rescale) {
        ProcessingMapartImage processingMapart = new ProcessingMapartImage(mapart);
        MapartImageConverter.readAndUpdateMapartImage(mapart, processingMapart, path, rescale);
        mapart.reset(false);
    }

    public static void updateMapart(ConvertedMapartImage mapart) {
        if (!mapart.isReset())
            readAndUpdateMapartImage(mapart, mapart.getImagePath(), true);
    }

    public static void changeCroppingMode(ConvertedMapartImage mapart, CroppingMode cropMode) {
        if (mapart.isReset()) return;
        ProcessingMapartImage processingMapart = new ProcessingMapartImage(mapart);
        if (!mapart.isReset() && cropMode == CroppingMode.USER_CROP)
            processingMapart.centerCroppingFrame();
        MapartImageConverter.readAndUpdateMapartImage(mapart, processingMapart, mapart.getImagePath(), true);
    }

    public static void resizeMapartImage(ConvertedMapartImage mapart, int width, int height) {
        if (mapart.isReset()) return;
        ProcessingMapartImage processingMapart = new ProcessingMapartImage(mapart);
        processingMapart.setWidth(width);
        processingMapart.setHeight(height);
        if (processingMapart.getOriginal() != null) {
            processingMapart.centerCroppingFrame();
        }
        MapartImageConverter.readAndUpdateMapartImage(mapart, processingMapart, mapart.getImagePath(), true);
    }

    public static void scaleToPoint(ConvertedMapartImage mapart, double pointX, double pointY, double scale) {
        if (mapart.isReset()) return;
        ProcessingMapartImage processingMapart = new ProcessingMapartImage(mapart);
        processingMapart.scaleToPoint(pointX, pointY, scale);
        MapartImageConverter.readAndUpdateMapartImage(mapart, processingMapart, mapart.getImagePath(), true);
    }

    public static void scaleToCenter(ConvertedMapartImage mapart, double scale) {
        if (mapart.isReset()) return;
        ProcessingMapartImage processingMapart = new ProcessingMapartImage(mapart);
        processingMapart.scaleToCenter(scale);
        MapartImageConverter.readAndUpdateMapartImage(mapart, processingMapart, mapart.getImagePath(), true);
    }

    public static void moveCroppingFrameOrMapartImage(ConvertedMapartImage mapart, int dx, int dy) {
        if (mapart.isReset()) return;
        ProcessingMapartImage processingMapart = new ProcessingMapartImage(mapart);
        boolean needRescale = processingMapart.moveCroppingFrame(dx, dy);
        MapartImageConverter.readAndUpdateMapartImage(mapart, processingMapart, mapart.getImagePath(), needRescale);
    }
}
