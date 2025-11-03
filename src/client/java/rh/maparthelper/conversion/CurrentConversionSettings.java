package rh.maparthelper.conversion;

import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;
import rh.maparthelper.MapartHelper;
import rh.maparthelper.config.palette.PaletteColors;
import rh.maparthelper.conversion.mapart.ConvertedMapartImage;

public class CurrentConversionSettings {
    // GUI image
    public final static ConvertedMapartImage mapart = new ConvertedMapartImage();
    public static NativeImageBackedTexture guiMapartImage;
    public final static Identifier guiMapartId = Identifier.of(MapartHelper.MOD_ID, "mapart_gui_texture");

    public static CroppingMode cropMode = CroppingMode.AUTO_CROP;
    public static boolean doShowGrid = false;
    public static boolean doShowTransparent = false;
    public static boolean doShowManualCroppingButtons = true;

    // Image preprocessing
    public static float brightness = 1.0f;
    public static float contrast = 0.0f;
    public static float saturation = 1.0f;

    public static void resetMapart() {
        PaletteColors.clearIgnoringColors();
        guiMapartImage = null;
        mapart.reset();
    }

    public static int getMapartWidth() {
        return mapart.getWidth();
    }

    public static int getMapartHeight() {
        return mapart.getHeight();
    }

    public static boolean isMapartConverted() {
        return !MapartHelper.conversionSettings.showOriginalImage && guiMapartImage != null;
    }
}
