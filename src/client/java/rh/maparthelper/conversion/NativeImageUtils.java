package rh.maparthelper.conversion;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import org.jetbrains.annotations.NotNull;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

public class NativeImageUtils {
    private static final Object mapartTextureUploadLock = new Object();

    public static void updateMapartImageTexture(NativeImage image) {
        synchronized (mapartTextureUploadLock) {
            DynamicTexture backedTexture = new DynamicTexture(
                    CurrentConversionSettings.guiMapartId::getPath,
                    image
            );
            TextureManager textureManager = Minecraft.getInstance().getTextureManager();
            textureManager.register(CurrentConversionSettings.guiMapartId, backedTexture);
            CurrentConversionSettings.guiMapartImage = backedTexture;
        }
    }

    public static int[][] divideImageByMaps(@NotNull NativeImage image) {
        int imageWidth = image.getWidth();
        int mapsWidth = imageWidth / 128;
        int mapsHeight = image.getHeight() / 128;

        int[] pixels = image.getPixels();
        int[][] maps = new int[mapsWidth * mapsHeight][];
        for (int i = 0; i < maps.length; i++) {
            maps[i] = new int[16384];
        }

        for (int x = 0; x < mapsWidth; x++) {
            for (int y = 0; y < mapsHeight; y++) {
                for (int i = 0; i < 128; i++) {
                    int rowStart = x * 128 + (y * 128 + i) * imageWidth;
                    System.arraycopy(pixels, rowStart, maps[x + y * mapsWidth], i * 128, 128);
                }
            }
        }
        return maps;
    }

    public static NativeImage convertBufferedImageToNativeImage(@NotNull BufferedImage image, int bgColor, boolean useTranslucent) {
        int width = image.getWidth();
        int height = image.getHeight();

        int[] pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
        NativeImage nativeImage = new NativeImage(width, height, false);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int argb = pixels[x + y * width];

                if (useTranslucent) {
                    if (argb == 0) continue;
                    nativeImage.setPixel(x, y, argb);
                } else {
                    if (((argb >> 24) & 0xFF) < 80)
                        nativeImage.setPixel(x, y, bgColor);
                    else
                        nativeImage.setPixel(x, y, argb | 0xFF000000);
                }
            }
        }

        return nativeImage;
    }

    public static NativeImage convertBufferedImageToNativeImage(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();

        int[] pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
        NativeImage nativeImage = new NativeImage(width, height, false);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                nativeImage.setPixel(x, y, pixels[x + y * width]);
            }
        }

        return nativeImage;
    }
}
