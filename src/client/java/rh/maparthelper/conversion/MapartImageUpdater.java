package rh.maparthelper.conversion;

import rh.maparthelper.conversion.mapart.ConvertedMapartImage;
import rh.maparthelper.conversion.mapart.ProcessingMapartImage;

import java.nio.file.Path;

public class MapartImageUpdater {
    static double moveDx = 0;
    static double moveDy = 0;
    static double scale = 0;

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
        MapartImageUpdater.scale += scale;
        ProcessingMapartImage processingMapart = new ProcessingMapartImage(mapart);
        processingMapart.scaleToPoint(pointX, pointY, MapartImageUpdater.scale);
        MapartImageConverter.readAndUpdateMapartImage(mapart, processingMapart, mapart.getImagePath(), true);
    }

    public static void scaleToCenter(ConvertedMapartImage mapart, double scale) {
        if (mapart.isReset()) return;
        MapartImageUpdater.scale += scale;
        ProcessingMapartImage processingMapart = new ProcessingMapartImage(mapart);
        processingMapart.scaleToCenter(MapartImageUpdater.scale);
        MapartImageConverter.readAndUpdateMapartImage(mapart, processingMapart, mapart.getImagePath(), true);
    }

    public static void moveCroppingFrameOrMapartImage(ConvertedMapartImage mapart, double dx, double dy) {
        if (mapart.isReset()) return;
        moveDx += dx;
        moveDy += dy;
        ProcessingMapartImage processingMapart = new ProcessingMapartImage(mapart);
        boolean needRescale = processingMapart.moveCroppingFrame((int) moveDx, (int) moveDy);
        MapartImageConverter.readAndUpdateMapartImage(mapart, processingMapart, mapart.getImagePath(), needRescale);
    }
}
