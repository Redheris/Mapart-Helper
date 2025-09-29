package rh.maparthelper.conversion;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.texture.TextureManager;

import java.awt.image.BufferedImage;

public class NativeImageUtils {

    public static void updateMapartImageTexture(NativeImage image) {
        CurrentConversionSettings.guiMapartImage = new NativeImageBackedTexture(
                () -> "mapart_gui_texture",
                image
        );
        TextureManager textureManager = MinecraftClient.getInstance().getTextureManager();
        textureManager.registerTexture(CurrentConversionSettings.guiMapartId, CurrentConversionSettings.guiMapartImage);
    }

    public static int[][] divideMapartByMaps(ConvertedMapartImage mapart) {
        if (CurrentConversionSettings.guiMapartImage == null || CurrentConversionSettings.guiMapartImage.getImage() == null)
            return null;
        int width = mapart.getWidth();
        int height = mapart.getHeight();
        int imageWidth = width * 128;

        int[] pixels = mapart.image.copyPixelsArgb();
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

    public static NativeImage convertBufferedImageToNativeImage(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();

        NativeImage nativeImage = new NativeImage(width, height, false);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int argb = image.getRGB(x, y);
                nativeImage.setColorArgb(x, y, argb);
            }
        }

        return nativeImage;
    }
}
