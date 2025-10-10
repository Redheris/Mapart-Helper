package rh.maparthelper.conversion;

import rh.maparthelper.conversion.mapart.ConvertedMapartImage;
import rh.maparthelper.conversion.mapart.ProcessingMapartImage;

import java.nio.file.Path;

public class MapartImageUpdater {

    public static void readAndUpdateMapartImage(ConvertedMapartImage mapart, Path path, boolean rescale) {
        ProcessingMapartImage processingMapart = new ProcessingMapartImage(mapart);
        MapartImageConverter.readAndUpdateMapartImage(mapart, processingMapart, path, rescale);
    }

    public static void updateMapart(ConvertedMapartImage mapart, boolean rescale) {
        if (mapart.getImagePath() != null)
            readAndUpdateMapartImage(mapart, mapart.getImagePath(), rescale);
    }

    public static void updateMapart(ConvertedMapartImage mapart) {
        updateMapart(mapart, true);
    }

    public static void resizeAndUpdateMapart(ConvertedMapartImage mapart, int width, int height) {
        ProcessingMapartImage processingMapart = new ProcessingMapartImage(mapart);
        processingMapart.setWidth(width);
        processingMapart.setHeight(height);
        if (processingMapart.getOriginal() != null) {
            processingMapart.centerCroppingFrame();
        }
        MapartImageConverter.readAndUpdateMapartImage(mapart, processingMapart, mapart.getImagePath(), true);
    }

    public static void scaleToPointAndUpdateMapart(ConvertedMapartImage mapart, double pointX, double pointY, double scale) {
        ProcessingMapartImage processingMapart = new ProcessingMapartImage(mapart);
        processingMapart.scaleToPoint(pointX, pointY, scale);
        MapartImageConverter.readAndUpdateMapartImage(mapart, processingMapart, mapart.getImagePath(), true);
    }

    public static void scaleToCenterAndUpdateMapart(ConvertedMapartImage mapart, double scale) {
        ProcessingMapartImage processingMapart = new ProcessingMapartImage(mapart);
        processingMapart.scaleToCenter(scale);
        MapartImageConverter.readAndUpdateMapartImage(mapart, processingMapart, mapart.getImagePath(), true);
    }

    public static void moveAndUpdateMapart(ConvertedMapartImage mapart, int dx, int dy) {
        ProcessingMapartImage processingMapart = new ProcessingMapartImage(mapart);
        boolean needRescale = processingMapart.moveCroppingFrame(dx, dy);
        MapartImageConverter.readAndUpdateMapartImage(mapart, processingMapart, mapart.getImagePath(), needRescale);
    }
}
