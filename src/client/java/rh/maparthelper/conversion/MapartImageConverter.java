package rh.maparthelper.conversion;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.MapColor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import rh.maparthelper.MapartHelper;
import rh.maparthelper.MapartHelperClient;
import rh.maparthelper.conversion.colors.ColorUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.nio.file.Path;
import java.util.concurrent.*;

public class MapartImageConverter {
    public static final int NO_CROP = -1;
    public static final int AUTO_CROP = 0;
    public static final int USER_CROP = 1;

    public static BufferedImage lastImage;
    public static Path lastImagePath;

    private final static Path TEMP_ARTS_DIR = FabricLoader.getInstance().getGameDir().resolve("saved_maps").resolve("temp");

    private static final ExecutorService convertingExecutor = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setNameFormat("Mart Helper Image-%d").build());
    private static Future<?> currentConvertingFuture;

    public static void readAndUpdateMapartImage(Path path) {
        FutureTask<Void> future;
        if (path.equals(lastImagePath))
            future = new FutureTask<>(new ConvertImageFileRunnable(null, false), null);
        else {
            lastImagePath = path;
            future = new FutureTask<>(new ConvertImageFileRunnable(path, false), null);
        }

        if (currentConvertingFuture != null)
            currentConvertingFuture.cancel(true);
        currentConvertingFuture = convertingExecutor.submit(future);
    }

    public static void updateMapart() {
        if (CurrentConversionSettings.imagePath != null)
            readAndUpdateMapartImage(CurrentConversionSettings.imagePath);
    }

    private static BufferedImage preprocessImage(BufferedImage image) {
        float brightness = CurrentConversionSettings.brightness;
        float contrast = CurrentConversionSettings.contrast;
        float saturation = CurrentConversionSettings.saturation;

        return ColorUtils.preprocessImage(image, brightness, contrast, saturation);
    }

    private static void saveMapartImage(Path imagePath) {
        try {
            NativeImageBackedTexture mapartTexture = CurrentConversionSettings.guiMapartImage;
            if (mapartTexture != null && mapartTexture.getImage() != null)
                mapartTexture.getImage().writeTo(imagePath);
        } catch (Exception e) {
            MapartHelper.LOGGER.error("Error occurred while saving an image: {}", e.toString());
            throw new RuntimeException(e);
        }
    }

    public static void saveMapartImage(String filename) {
        saveMapartImage(TEMP_ARTS_DIR.getParent().resolve(filename + ".png"));
    }

    /**
     * Computes new image with the original pixels adapted to the current blocks palette colors
     **/
    public static void convertToBlocksPalette(BufferedImage image, boolean use3D, boolean logExecutionTime) {
        long startTime = System.currentTimeMillis();

        int[] pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();

        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                if (Thread.currentThread().isInterrupted()) {
                    BlocksPalette.clearColorCache();
                    return;
                }
                int argb = pixels[x + y * image.getWidth()];
                if (argb == 0) continue;
                int newArgb;
                BlocksPalette.MapColorEntry color = BlocksPalette.getClosestColor(argb, use3D);
                if (y > 0 && pixels[x + (y - 1) * image.getWidth()] == 0)
                    newArgb = color.mapColor().getRenderColor(MapColor.Brightness.HIGH);
                else
                    newArgb = color.mapColor().getRenderColor(color.brightness());
                pixels[x + y * image.getWidth()] = newArgb;
            }
        }

        BlocksPalette.clearColorCache();

        if (logExecutionTime) {
            double timeLeft = (System.currentTimeMillis() - startTime) / 1000.0;
            MapartHelper.LOGGER.info("[{}] Colors conversion took {} seconds", use3D ? "3D" : "2D", timeLeft);
        }
    }

    public static BufferedImage scaleImage(BufferedImage image, int width, int height) {
        boolean scaleUp = width > image.getWidth() || height > image.getHeight();
        if (scaleUp) {
            BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = resized.createGraphics();

            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR); // или BICUBIC
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.drawImage(image, 0, 0, width, height, null);
            g2.dispose();

            return resized;
        } else {
            Image scaled = image.getScaledInstance(width, height, Image.SCALE_SMOOTH);
            image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

            Graphics2D g2d = image.createGraphics();
            g2d.drawImage(scaled, 0, 0, null);
            g2d.dispose();

            return image;
        }
    }

    public static BufferedImage cropAndScaleImage(BufferedImage image, int frameX, int frameY, int frameWidth, int frameHeight, int mapartWidth, int mapartHeight) {
        BufferedImage subimage = image.getSubimage(frameX, frameY, frameWidth, frameHeight);
        return scaleImage(subimage, mapartWidth, mapartHeight);
    }

    public static BufferedImage autoCropAndScale(BufferedImage image, int mapartWidth, int mapartHeight) {
        int imageWidth = image.getWidth();
        int imageHeight = image.getHeight();

        CurrentConversionSettings.centerCroppingSize(imageWidth, imageHeight);
        int frameX = CurrentConversionSettings.croppingFrameX;
        int frameY = CurrentConversionSettings.croppingFrameY;
        int frameWidth = CurrentConversionSettings.croppingFrameWidth;
        int frameHeight = CurrentConversionSettings.croppingFrameHeight;

        return cropAndScaleImage(image, frameX, frameY, frameWidth, frameHeight, mapartWidth, mapartHeight);
    }

    private static BufferedImage cropAndScaleToMapSize(BufferedImage image) {
        int mapartWidth = CurrentConversionSettings.getWidth() * 128;
        int mapartHeight = CurrentConversionSettings.getHeight() * 128;

        return switch (CurrentConversionSettings.cropMode) {
            case NO_CROP -> scaleImage(image, mapartWidth, mapartHeight);
            case AUTO_CROP -> autoCropAndScale(image, mapartWidth, mapartHeight);
            case USER_CROP -> {
                int frameX = CurrentConversionSettings.croppingFrameX;
                int frameY = CurrentConversionSettings.croppingFrameY;
                int frameWidth = CurrentConversionSettings.croppingFrameWidth;
                int frameHeight = CurrentConversionSettings.croppingFrameHeight;
                yield cropAndScaleImage(image, frameX, frameY, frameWidth, frameHeight, mapartWidth, mapartHeight);
            }
            default -> throw new IllegalArgumentException("Invalid cropping mode");
        };
    }

    private static class ConvertImageFileRunnable implements Runnable {
        private final Path imagePath;
        private final boolean logExecutionTime;

        public ConvertImageFileRunnable(Path path, boolean logExecutionTime) {
            this.imagePath = path;
            this.logExecutionTime = logExecutionTime;
        }

        @Override
        public void run() {
            try {
                long startTime = System.currentTimeMillis();
                if (imagePath != null) {
                    lastImage = ImageIO.read(imagePath.toFile());
                    CurrentConversionSettings.centerCroppingSize(lastImage.getWidth(), lastImage.getHeight());
                }
                BufferedImage bufferedImage = lastImage;
                if (Thread.currentThread().isInterrupted()) return;

                bufferedImage = cropAndScaleToMapSize(bufferedImage);
                if (Thread.currentThread().isInterrupted()) return;

                bufferedImage = preprocessImage(bufferedImage);
                if (Thread.currentThread().isInterrupted()) return;

                convertToBlocksPalette(bufferedImage, MapartHelperClient.conversionConfig.use3D(), logExecutionTime);
                if (Thread.currentThread().isInterrupted()) return;

                NativeImage image = NativeImageUtils.convertBufferedImageToNativeImage(bufferedImage);
                if (Thread.currentThread().isInterrupted()) return;

                MinecraftClient.getInstance().execute(() ->
                        NativeImageUtils.updateMapartImageTexture(image)
                );

                if (logExecutionTime) {
                    double timeLeft = (System.currentTimeMillis() - startTime) / 1000.0;
                    MapartHelper.LOGGER.info("Image preprocessing and conversion took {} seconds", timeLeft);
                }

            } catch (Exception e) {
                CurrentConversionSettings.guiMapartImage = null;
                CurrentConversionSettings.imagePath = null;
                lastImage = null;
                lastImagePath = null;
                MapartHelper.LOGGER.error("Error occurred while reading and converting an image: ", e);
                throw new RuntimeException(e);
            }
        }
    }
}
