package rh.maparthelper.conversion;

import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;
import rh.maparthelper.MapartHelper;

import java.nio.file.Path;

public class CurrentConversionSettings {
    // GUI image
    public static Path imagePath;
    public static NativeImageBackedTexture guiMapartImage;
    public static Identifier guiMapartId = Identifier.of(MapartHelper.MOD_ID, "mapart_gui_texture");

    // Map settings
    public static String mapartName = "New mapart";
    private static int width = 1;
    private static int height = 1;

    // Cropping settings
    public static CroppingMode cropMode = CroppingMode.AUTO_CROP;
    public static int croppingFrameX = 0;
    public static int croppingFrameY = 0;
    public static int croppingFrameWidth = 1;
    public static int croppingFrameHeight = 1;

    // Preview settings
    public static boolean doShowGrid = false;

    // Image preprocessing
    public static float brightness = 1.0f;
    public static float contrast = 0.0f;
    public static float saturation = 1.0f;

    public static void resetMapart() {
        imagePath = null;
        guiMapartImage = null;
        MapartImageConverter.lastImagePath = null;
        MapartImageConverter.lastImage = null;
    }

    public static int getWidth() {
        return width;
    }

    public static boolean setWidth(int newWidth) {
        if (width == newWidth) return false;

        width = newWidth;
        guiMapartImage = null;
        if (MapartImageConverter.lastImage != null) {
            centerCroppingSize(MapartImageConverter.lastImage.getWidth(), MapartImageConverter.lastImage.getHeight());
        }
        return true;
    }

    public static int getHeight() {
        return height;
    }

    public static boolean setHeight(int newHeight) {
        if (height == newHeight) return false;

        height = newHeight;
        guiMapartImage = null;
        if (MapartImageConverter.lastImage != null) {
            centerCroppingSize(MapartImageConverter.lastImage.getWidth(), MapartImageConverter.lastImage.getHeight());
        }
        return true;
    }

    public static void centerCroppingSize(int imageWidth, int imageHeight) {
        double mapartAspect = (double) width / height;
        double imageAspect = (double) imageWidth / imageHeight;

        int frameWidth, frameHeight;
        int frameX, frameY;

        if (imageAspect > mapartAspect) {
            frameHeight = imageHeight;
            frameY = 0;
            frameWidth = (int) (frameHeight * mapartAspect);
            frameX = (imageWidth - frameWidth) / 2;
        } else {
            frameWidth = imageWidth;
            frameX = 0;
            frameHeight = (int) (frameWidth / mapartAspect);
            frameY = (imageHeight - frameHeight) / 2;
        }
        CurrentConversionSettings.croppingFrameX = frameX;
        CurrentConversionSettings.croppingFrameY = frameY;
        CurrentConversionSettings.croppingFrameWidth = frameWidth;
        CurrentConversionSettings.croppingFrameHeight = frameHeight;
    }

    public static boolean isMapartConverted() {
        return !MapartHelper.conversionSettings.showOriginalImage && guiMapartImage != null;
    }
}
