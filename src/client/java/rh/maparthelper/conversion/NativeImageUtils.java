package rh.maparthelper.conversion;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.texture.TextureManager;

import java.awt.image.BufferedImage;

public class NativeImageUtils {

    public static void updateMapartImageTexture(BufferedImage bufferedImage) {
        NativeImage image = convertBufferedImageToNativeImage(bufferedImage);
        CurrentConversionSettings.guiMapartImage = new NativeImageBackedTexture(
                "mapart_gui_texture",
                bufferedImage.getWidth(),
                bufferedImage.getHeight(),
                false);
        CurrentConversionSettings.guiMapartImage.setImage(image);
        CurrentConversionSettings.guiMapartImage.upload();

        TextureManager textureManager = MinecraftClient.getInstance().getTextureManager();
        textureManager.registerTexture(CurrentConversionSettings.guiMapartId, CurrentConversionSettings.guiMapartImage);
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
