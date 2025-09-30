package rh.maparthelper.conversion;

import com.google.common.util.concurrent.AtomicDouble;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import net.minecraft.block.MapColor;
import net.minecraft.client.MinecraftClient;
import rh.maparthelper.MapartHelper;
import rh.maparthelper.colors.ColorUtils;
import rh.maparthelper.colors.MapColorEntry;
import rh.maparthelper.config.palette.PaletteColors;
import rh.maparthelper.config.palette.PaletteConfigManager;
import rh.maparthelper.conversion.dithering.DitheringAlgorithms;
import rh.maparthelper.gui.MapartEditorScreen;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

public class MapartImageConverter {
    private static volatile AtomicDouble conversionProgress = new AtomicDouble(0.0);

    private static final ExecutorService convertingExecutor = Executors.newSingleThreadExecutor(
            new ThreadFactoryBuilder().setNameFormat(MapartHelper.MOD_NAME + "/Image Converter")
                    .build()
    );
    private static Future<?> currentConvertingFuture;

    public static void readAndUpdateMapartImage(ConvertedMapartImage mapart, Path path) {
        FutureTask<Void> future;
        boolean logExecutionTime = MapartHelper.commonConfig.logConversionTime;
        if (path.equals(mapart.imagePath))
            future = new FutureTask<>(new ConvertImageFileRunnable(mapart, null, logExecutionTime), null);
        else {
            future = new FutureTask<>(new ConvertImageFileRunnable(mapart, path, logExecutionTime), null);
        }

        // A very crutch to make moving the cropping frame faster
        // TODO: Replace this dozen-threads-per-time crutch with caching the image and shifting within it
        if (!MapartHelper.conversionSettings.showOriginalImage && currentConvertingFuture != null)
            currentConvertingFuture.cancel(true);
        currentConvertingFuture = convertingExecutor.submit(future);
    }

    public static void updateMapart(ConvertedMapartImage mapart) {
        if (mapart.imagePath != null)
            readAndUpdateMapartImage(mapart, mapart.imagePath);
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

    /**
     * Computes new image with the original pixels adapted to the current blocks palette colors
     **/
    private static void convertToBlocksPalette(BufferedImage image, boolean use3D, ConvertedMapartImage.ColorsCounter colorsCounter) {
        int width = image.getWidth();
        int[] pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
        double progressStep = 1.0 / pixels.length;

        int[] errorsArray = new int[0];
        DitheringAlgorithms ditherAlg = MapartHelper.conversionSettings.ditheringAlgorithm;
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

    public static BufferedImage cropAndScaleImage(BufferedImage image, ConvertedMapartImage.CroppingFrame frame, int mapartWidth, int mapartHeight) {
        BufferedImage subimage = image.getSubimage(frame.getX(), frame.getY(), frame.getWidth(), frame.getHeight());
        return scaleImage(subimage, mapartWidth, mapartHeight);
    }

    private static BufferedImage cropAndScaleToMapSize(BufferedImage image, ConvertedMapartImage mapart) {
        int mapartWidth = mapart.getWidth() * 128;
        int mapartHeight = mapart.getHeight() * 128;
        return switch (CurrentConversionSettings.cropMode) {
            case NO_CROP: yield scaleImage(image, mapartWidth, mapartHeight);
            case AUTO_CROP: mapart.centerCroppingFrame();
            case USER_CROP: {
                yield cropAndScaleImage(image, mapart.croppingFrame, mapartWidth, mapartHeight);
            }
        };
    }

    private static class ConvertImageFileRunnable implements Runnable {
        private final ConvertedMapartImage mapart;
        private final Path newImagePath;
        private final boolean logExecutionTime;

        public ConvertImageFileRunnable(ConvertedMapartImage mapart, Path path, boolean logExecutionTime) {
            this.mapart = mapart;
            this.newImagePath = path;
            this.logExecutionTime = logExecutionTime;
        }

        @Override
        public void run() {
            try {
                long startTime = System.currentTimeMillis();
                conversionProgress = new AtomicDouble(0.0);
                if (newImagePath != null) {
                    mapart.imagePath = null;
                    mapart.original = ImageIO.read(newImagePath.toFile());
                    mapart.imagePath = newImagePath;
                    mapart.centerCroppingFrame();
                }
                BufferedImage bufferedImage = mapart.original;
                if (Thread.currentThread().isInterrupted()) return;

                bufferedImage = cropAndScaleToMapSize(bufferedImage, mapart);
                if (Thread.currentThread().isInterrupted()) return;

                bufferedImage = preprocessImage(bufferedImage);
                if (Thread.currentThread().isInterrupted()) return;

                PaletteColors.clearColorCache();
                mapart.colorsCounter.clear();
                if (!MapartHelper.conversionSettings.showOriginalImage) {
                    if (PaletteConfigManager.presetsConfig.getCurrentPresetColors().isEmpty())
                        bufferedImage = new BufferedImage(bufferedImage.getWidth(), bufferedImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
                    else
                        convertToBlocksPalette(bufferedImage, MapartHelper.conversionSettings.use3D(), mapart.colorsCounter);
                }
                if (Thread.currentThread().isInterrupted()) return;

                mapart.image = NativeImageUtils.convertBufferedImageToNativeImage(bufferedImage);
                if (Thread.currentThread().isInterrupted()) return;

                MinecraftClient.getInstance().execute(() -> NativeImageUtils.updateMapartImageTexture(mapart.image));

                if (logExecutionTime) {
                    double timeLeft = (System.currentTimeMillis() - startTime) / 1000.0;
                    MapartHelper.LOGGER.info("Image preprocessing and conversion took {} seconds", timeLeft);
                }

            } catch (Exception e) {
                CurrentConversionSettings.resetMapart();
                MapartHelper.LOGGER.error("Error occurred while reading and converting an image: ", e);
                throw new RuntimeException(e);
            } finally {
                MinecraftClient.getInstance().execute(() -> {
                    if (MinecraftClient.getInstance().currentScreen instanceof MapartEditorScreen editorScreen) {
                        editorScreen.updateMaterialList();
                        editorScreen.updateMapartOutputButtons();
                    }
                });
            }
        }
    }
}
