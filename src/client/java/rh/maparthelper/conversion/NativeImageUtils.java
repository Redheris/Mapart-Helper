package rh.maparthelper.conversion;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.texture.TextureManager;
import rh.maparthelper.colors.MapColorEntry;
import rh.maparthelper.conversion.mapart.ConvertedMapartImage;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

public class NativeImageUtils {

    public synchronized static void updateMapartImageTexture(NativeImage image) {
        NativeImageBackedTexture backedTexture = new NativeImageBackedTexture(
                () -> "mapart_gui_texture",
                image
        );
        TextureManager textureManager = MinecraftClient.getInstance().getTextureManager();
        textureManager.registerTexture(CurrentConversionSettings.guiMapartId, backedTexture);
        CurrentConversionSettings.guiMapartImage = backedTexture;
    }

    public static int[][] divideMapartByMaps(ConvertedMapartImage mapart) {
        if (CurrentConversionSettings.guiMapartImage == null || CurrentConversionSettings.guiMapartImage.getImage() == null)
            return null;
        int width = mapart.getWidth();
        int height = mapart.getHeight();
        int imageWidth = width * 128;

        int[] pixels = mapart.getNativeImage().copyPixelsArgb();
        int[][] maps = new int[width * height][];
        for (int i = 0; i < maps.length; i++) {
            maps[i] = new int[16384];
        }

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                for (int i = 0; i < 128; i++) {
                    int rowStart = x * 128 + (y * 128 + i) * imageWidth;
                    System.arraycopy(pixels, rowStart, maps[x + y * width], i * 128, 128);
                }
            }
        }
        return maps;
    }

    public static NativeImage convertBufferedImageToNativeImage(BufferedImage image, MapColorEntry bgColor, boolean useTransparent) {
        int width = image.getWidth();
        int height = image.getHeight();

        int[] pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
        NativeImage nativeImage = new NativeImage(width, height, false);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int argb = pixels[x + y * width];

                if (useTransparent ? argb == 0 : ((argb >> 24) & 0xFF) < 80) {
                    nativeImage.setColorArgb(x, y, bgColor.getRenderColor());
                } else {
                    nativeImage.setColorArgb(x, y, useTransparent ? argb : argb | 0xFF000000);
                }

                if (useTransparent) {
                    if (argb == 0) continue;
                    nativeImage.setColorArgb(x, y, argb);
                } else {
                    if (((argb >> 24) & 0xFF) < 80)
                        nativeImage.setColorArgb(x, y, bgColor.getRenderColor());
                    else
                        nativeImage.setColorArgb(x, y, argb | 0xFF000000);
                }
            }
        }

        return nativeImage;
    }
}
