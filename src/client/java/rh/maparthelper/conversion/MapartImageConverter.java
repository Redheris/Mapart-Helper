package rh.maparthelper.conversion;

import com.google.common.util.concurrent.AtomicDouble;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.MapColor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import rh.maparthelper.MapartHelper;
import rh.maparthelper.MapartHelperClient;
import rh.maparthelper.config.palette.PaletteColors;
import rh.maparthelper.conversion.colors.ColorUtils;
import rh.maparthelper.conversion.colors.MapColorEntry;
import rh.maparthelper.conversion.dithering.DitheringAlgorithms;
import rh.maparthelper.gui.MapartEditorScreen;
import rh.maparthelper.util.Utils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

public class MapartImageConverter {
    public static BufferedImage lastImage;
    public static Path lastImagePath;
    private static final ColorsCounter colorsCounter = new ColorsCounter();
    private static volatile AtomicDouble conversionProgress = new AtomicDouble(0.0);

    private final static Path SAVED_MAPS_DIR = FabricLoader.getInstance().getGameDir().resolve("saved_maps");

    private static final ExecutorService convertingExecutor = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setNameFormat("Mart Helper Image-%d").build());
    private static Future<?> currentConvertingFuture;

    public static void readAndUpdateMapartImage(Path path) {
        FutureTask<Void> future;
        if (path.equals(lastImagePath))
            future = new FutureTask<>(new ConvertImageFileRunnable(null, false), null);
        else {
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

    public static MapColorCount[] getColorsCounter() {
        MapColorCount[] countsSorted = new MapColorCount[63];
        for (int id = 1; id < 64; id++) {
            countsSorted[id - 1] = new MapColorCount(id, colorsCounter.get(id));
        }
        Arrays.sort(countsSorted, Comparator.comparingInt(MapColorCount::amount).reversed());
        return countsSorted;
    }

    public static boolean isConverting() {
        double d = conversionProgress.get();
        return d > 0.0 && d < 1.0;
    }

    public static double getConversionProgress() {
        return conversionProgress.get();
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
            MapartHelper.LOGGER.error("Error occurred while saving the image to \"{}\"", imagePath, e);
            throw new RuntimeException(e);
        }
    }

    public static void saveMapartImage(String filename) {
        filename = Utils.makeUniqueFilename(SAVED_MAPS_DIR, filename, "png");
        saveMapartImage(SAVED_MAPS_DIR.resolve(filename));
    }

    /**
     * Computes new image with the original pixels adapted to the current blocks palette colors
     **/
    public static void convertToBlocksPalette(BufferedImage image, boolean use3D, boolean logExecutionTime) {
        long startTime = System.currentTimeMillis();
        PaletteColors.clearColorCache();
        colorsCounter.clear();

        int width = image.getWidth();
        int[] pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
        double progressStep = 1.0 / pixels.length;

        int[] errorsArray = new int[0];
        DitheringAlgorithms ditherAlg = MapartHelper.config.conversionSettings.ditheringAlgorithm;
        boolean useDithering = ditherAlg != DitheringAlgorithms.NONE;
        if (useDithering)
            errorsArray = new int[ditherAlg.rowsNumber * width * 3];

        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < width; x++) {
                if (Thread.currentThread().isInterrupted()) {
                    PaletteColors.clearColorCache();
                    colorsCounter.clear();
                    return;
                }
                int argb = pixels[x + y * width];
                if (argb == 0) continue;
                if (useDithering) {
                    int ind = x * 3;
                    int[] argb0 = ColorUtils.getARGB(argb);
                    argb0[1] = Math.clamp(argb0[1] + errorsArray[ind], 0, 255);
                    argb0[2] = Math.clamp(argb0[2] + errorsArray[ind + 1], 0, 255);
                    argb0[3] = Math.clamp(argb0[3] + errorsArray[ind + 2], 0, 255);
                    argb = ColorUtils.getARGB(argb0);
                }
                int newArgb;
                MapColorEntry color = PaletteColors.getClosestColor(argb, use3D, useDithering);
                if (useDithering)
                    ditherAlg.spreadDiffusionError(errorsArray, width, x, color.distError());
                if (y > 0 && pixels[x + (y - 1) * width] == 0)
                    newArgb = color.mapColor().getRenderColor(MapColor.Brightness.HIGH);
                else
                    newArgb = color.mapColor().getRenderColor(color.brightness());
                pixels[x + y * width] = newArgb;
                if (color != MapColorEntry.CLEAR) {
                    colorsCounter.increment(color.mapColor().id);
                }
                conversionProgress.addAndGet(progressStep);
            }
            if (useDithering) {
                for (int row = 1; row < ditherAlg.rowsNumber; row++) {
                    System.arraycopy(errorsArray, row * width * 3, errorsArray, (row - 1) * width * 3, width * 3);
                }
                Arrays.fill(errorsArray, (ditherAlg.rowsNumber - 1) * width * 3, ditherAlg.rowsNumber * width * 3, 0);
            }
        }

        if (logExecutionTime) {
            double timeLeft = (System.currentTimeMillis() - startTime) / 1000.0;
            MapartHelper.LOGGER.info("[{}] Colors conversion took {} seconds", use3D ? "3D" : "2D", timeLeft);
        }

        conversionProgress.set(1.0);
        PaletteColors.clearColorCache();
    }

    public static BufferedImage scaleImage(BufferedImage image, int width, int height) {
        boolean scaleUp = width > image.getWidth() || height > image.getHeight();
        if (scaleUp) {
            BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = resized.createGraphics();

            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
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
                conversionProgress = new AtomicDouble(0.0);
                if (imagePath != null) {
                    lastImagePath = null;
                    BufferedImage readImage = ImageIO.read(imagePath.toFile());
                    CurrentConversionSettings.centerCroppingSize(readImage.getWidth(), readImage.getHeight());
                    lastImage = readImage;
                    lastImagePath = imagePath;
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

                MinecraftClient.getInstance().execute(() -> {
                    if (MinecraftClient.getInstance().currentScreen instanceof MapartEditorScreen editorScreen)
                        editorScreen.updateMaterialList();
                    NativeImageUtils.updateMapartImageTexture(image);
                });

                if (logExecutionTime) {
                    double timeLeft = (System.currentTimeMillis() - startTime) / 1000.0;
                    MapartHelper.LOGGER.info("Image preprocessing and conversion took {} seconds", timeLeft);
                }

            } catch (Exception e) {
                CurrentConversionSettings.imagePath = null;
                MapartHelper.LOGGER.error("Error occurred while reading and converting an image: ", e);
                throw new RuntimeException(e);
            }
        }
    }

    public record MapColorCount(int id, int amount) {
    }

    private static class ColorsCounter {
        private int[] counter = new int[63];

        void increment(int colorId) {
            this.counter[colorId - 1]++;
        }

        int get(int colorId) {
            return this.counter[colorId - 1];
        }

        void clear() {
            this.counter = new int[63];
        }
    }
}
