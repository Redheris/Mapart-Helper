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
    public static int cropMode = MapartImageConverter.AUTO_CROP;
    public static int croppingFrameX = 0;
    public static int croppingFrameY = 0;
    public static int croppingFrameWidth = 0;
    public static int croppingFrameHeight = 0;

    // Preview settings
    public static boolean doShowGrid = false;
    public static double previewScale = 1.0;

    // Image preprocessing
    public static float brightness = 1.0f;
    public static float contrast = 0.0f;
    public static float saturation = 1.0f;

    public static void resetMapart() {
        imagePath = null;
        guiMapartImage = null;
        cropMode = MapartImageConverter.AUTO_CROP;
        croppingFrameX = 0;
        croppingFrameY = 0;
        croppingFrameWidth = 0;
        croppingFrameHeight = 0;
        doShowGrid = false;
        previewScale = 1.0;
        brightness = 1.0f;
        contrast = 0.0f;
        saturation = 1.0f;
    }

    public static void setSize(int newWidth, int newHeight) {
        if (width != newWidth || height != newHeight)
            guiMapartImage = null;
        width = newWidth;
        height = newHeight;
    }

    public static int getWidth() {
        return width;
    }

    public static void setWidth(int newWidth) {
        if (width != newWidth)
            guiMapartImage = null;
        width = newWidth;
    }

    public static int getHeight() {
        return height;
    }

    public static void setHeight(int newHeight) {
        if (height != newHeight)
            guiMapartImage = null;
        height = newHeight;
    }
}
