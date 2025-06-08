package rh.maparthelper.conversion;

import net.minecraft.client.texture.NativeImage;

import java.awt.image.BufferedImage;

public class NativeImageUtils {

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
