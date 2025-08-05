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
    public static int width = 1;
    public static int height = 1;

    // Cropping settings
    public static int cropMode = MapartImageConverter.AUTO_CROP;
    public static int croppingFrameX = 0;
    public static int croppingFrameY = 0;
    public static int croppingFrameWidth = 0;
    public static int croppingFrameHeight = 0;

    // Preview settings
    public static boolean doShowGrid = false;
    public static double previewScale = 0.5;

    // Image preprocessing
    public static float brightness = 1.0f;
    public static float contrast = 0.0f;
    public static float saturation = 1.0f;
}
