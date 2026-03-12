package rh.maparthelper.conversion;

import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;
import rh.maparthelper.MapartHelper;
import rh.maparthelper.config.palette.PaletteColors;
import rh.maparthelper.mapart.MapartProcessing;

public class CurrentConversionSettings {
    // GUI image
    public final static MapartProcessing mapart = new MapartProcessing();
    public static NativeImageBackedTexture guiMapartImage;
    public final static Identifier guiMapartId = Identifier.of(MapartHelper.MOD_ID, "mapart_gui_texture");

    public static CroppingMode cropMode = CroppingMode.AUTO_CROP;
    public static boolean doShowGrid = false;
    public static boolean doShowTranslucent = false;
    public static boolean doShowManualCroppingButtons = true;

    // Image preprocessing
    public static float brightness = 1.0f;
    public static float contrast = 0.0f;
    public static float saturation = 1.0f;

    public static void resetMapart() {
        MapartImageConverter.cancelConverting();
        synchronized (mapart) {
            PaletteColors.clearExcludingColors();
            PaletteColors.clearColorCache();
            guiMapartImage = null;
            mapart.setReset(true);
        }
    }

    public static int getMapartWidth() {
        return mapart.getWidth();
    }

    public static int getMapartHeight() {
        return mapart.getHeight();
    }

    public static boolean isMapartConverted() {
        return !MapartHelper.conversionSettings.isShowOriginalImage() && guiMapartImage != null;
    }
}
