package rh.maparthelper.conversion;

import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;
import rh.maparthelper.MapartHelper;
import rh.maparthelper.mapart.MapartProcessing;
import rh.maparthelper.palette.PaletteColors;

public class CurrentConversionSettings {
    // GUI image
    public final static MapartProcessing mapart = new MapartProcessing();
    public static DynamicTexture guiMapartImage;
    public final static Identifier guiMapartId = Identifier.fromNamespaceAndPath(MapartHelper.MOD_ID, "mapart_gui_texture");

    public static CroppingMode cropMode = CroppingMode.AUTO_CROP;
    public static boolean doShowGrid = false;
    public static boolean doShowTranslucent = false;
    public static boolean doShowCroppingControls = true;

    // Image preprocessing
    public static float brightness = 1.0f;
    public static float contrast = 0.0f;
    public static float saturation = 1.0f;

    // Error propagation weights
    public static float redPropagation = 1.0f;
    public static float greenPropagation = 1.0f;
    public static float bluePropagation = 1.0f;

    public static void resetMapart() {
        MapartImageConverter.cancelConverting();
        synchronized (mapart) {
            PaletteColors.clearExcludingColors();
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
        return !MapartHelper.conversionConfig().isShowOriginalImage() && guiMapartImage != null;
    }
}
