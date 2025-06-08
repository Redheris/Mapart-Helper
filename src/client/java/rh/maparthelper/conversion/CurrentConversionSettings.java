package rh.maparthelper.conversion;

import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;
import rh.maparthelper.MapartHelper;

public class CurrentConversionSettings {
    // GUI image
    public static NativeImageBackedTexture guiMapartImage = new NativeImageBackedTexture("mapart_gui_texture", 128, 128, true);
    public static Identifier guiMapartId = Identifier.of(MapartHelper.MOD_ID, "mapart_gui_texture");

    // Map settings
    public static String mapartName = "New mapart";
    public static int width = 1;
    public static int height = 1;

    // Crop settings
    public static double conversionScale = 1.0;
    public static int xShift = 0;
    public static int yShift = 0;

    // Preview settings
    public static boolean doShowGrid = false;
    public static double previewScale = 1.0;

    // Image preprocessing
    public static double brightness = 0.5;
    public static double saturation = 0.5;
    public static double contrast = 0.5;
}
