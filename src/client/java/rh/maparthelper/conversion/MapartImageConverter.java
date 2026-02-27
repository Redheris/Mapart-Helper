package rh.maparthelper.conversion;

import com.google.common.util.concurrent.AtomicDouble;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import net.minecraft.block.MapColor;
import net.minecraft.client.MinecraftClient;
import org.jetbrains.annotations.NotNull;
import rh.maparthelper.MapartHelper;
import rh.maparthelper.colors.ColorUtils;
import rh.maparthelper.colors.MapColorEntry;
import rh.maparthelper.config.palette.PaletteColors;
import rh.maparthelper.config.palette.PaletteConfigManager;
import rh.maparthelper.conversion.dithering.DitheringAlgorithms;
import rh.maparthelper.gui.MapartEditorScreen;
import rh.maparthelper.mapart.AbstractMapart;
import rh.maparthelper.mapart.ColorsCounter;
import rh.maparthelper.mapart.MapartProcessing;

import javax.imageio.ImageIO;
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

    public static void readAndUpdateMapartImage(MapartProcessing processingMapart, Path path, ImageChangeResult imageChangeResult) {
        FutureTask<Void> future = getVoidFutureTask(processingMapart, path, imageChangeResult);

        if (currentConvertingFuture != null)
            currentConvertingFuture.cancel(true);
        currentConvertingFuture = convertingExecutor.submit(future);
    }

    private static @NotNull FutureTask<Void> getVoidFutureTask(MapartProcessing processingMapart, Path path, ImageChangeResult imageChangeResult) {
        FutureTask<Void> future;
        boolean logExecutionTime = MapartHelper.commonConfig.mapartEditor.logConversionTime;
        if (!processingMapart.isReset() && path.equals(processingMapart.getImagePath()))
            future = new FutureTask<>(new ConvertImageFileRunnable(processingMapart, null, logExecutionTime, imageChangeResult), null);
        else {
            future = new FutureTask<>(new ConvertImageFileRunnable(processingMapart, path, logExecutionTime, imageChangeResult), null);
        }
        return future;
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
    private static BufferedImage convertToBlocksPalette(AbstractMapart mapart, BufferedImage image, MapColorEntry bgColor, boolean use3D) {
        BufferedImage converted = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);

        int width = converted.getWidth();
        int[] originalPixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
        int[] resultPixels = ((DataBufferInt) converted.getRaster().getDataBuffer()).getData();
        double progressStep = 1.0 / originalPixels.length;

        int[] errorsArray = new int[0];
        DitheringAlgorithms ditherAlg = MapartHelper.conversionSettings.ditheringAlgorithm;
        boolean useDithering = ditherAlg != DitheringAlgorithms.NONE;
        if (useDithering)
            errorsArray = new int[ditherAlg.rowsNumber * width * 3];

        for (int y = 0; y < converted.getHeight(); y++) {
            for (int x = 0; x < width; x++) {
                ColorsCounter colorsCounter = mapart.getColorsCounterFor(x / 128, y / 128);
                if (Thread.currentThread().isInterrupted()) {
                    PaletteColors.clearColorCache();
                    mapart.clearColorCounters();
                    return null;
                }
                int argb = originalPixels[x + y * width];
                if (argb == 0 && bgColor == MapColorEntry.CLEAR) {
                    continue;
                }
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
                if (color == MapColorEntry.CLEAR) {
                    color = bgColor;
                }
                if (useDithering && color != MapColorEntry.CLEAR)
                    ditherAlg.spreadDiffusionError(errorsArray, width, x, color.distError());
                if (y > 0 && originalPixels[x + (y - 1) * width] == 0)
                    newArgb = color.mapColor().getRenderColor(MapColor.Brightness.HIGH);
                else {
                    if (use3D)
                        newArgb = color.getRenderColor();
                    else
                        newArgb = color.mapColor().getRenderColor(MapColor.Brightness.NORMAL);
                }
                resultPixels[x + y * width] = newArgb;
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
        return converted;
    }

    private static BufferedImage cropAndScaleToMapSize(MapartProcessing mapart, boolean rescale) {
        int mapartWidth = mapart.getWidth() * 128;
        int mapartHeight = mapart.getHeight() * 128;
        if (!rescale) {
            return MapartImageResizer.placeOnMapartCanvas(mapart, mapartWidth, mapartHeight);
        }
        return switch (CurrentConversionSettings.cropMode) {
            case NO_CROP:
                yield MapartImageResizer.scaleImage(mapart, mapartWidth, mapartHeight);
            case AUTO_CROP, FIT:
                if (CurrentConversionSettings.cropMode == CroppingMode.FIT)
                    mapart.fitOriginalCroppingFrame();
                else
                    mapart.autoCropOriginalImage();
            case USER_CROP:
                yield MapartImageResizer.adjustToMapartSize(mapart);
        };
    }

    private static class ConvertImageFileRunnable implements Runnable {
        private final MapartProcessing mapart;
        private final Path newImagePath;
        private final boolean logExecutionTime;
        private final ImageChangeResult imageChangeResult;

        public ConvertImageFileRunnable(MapartProcessing mapart, Path path, boolean logExecutionTime, ImageChangeResult imageChangeResult) {
            this.mapart = mapart;
            this.newImagePath = path;
            this.logExecutionTime = logExecutionTime;
            this.imageChangeResult = imageChangeResult;
        }

        @Override
        public void run() {
            synchronized (CurrentConversionSettings.mapart) {
                try {
                    long startTime = System.currentTimeMillis();
                    conversionProgress = new AtomicDouble(0.0);

                    boolean showOriginalImage = MapartHelper.conversionSettings.showOriginalImage;
                    MapColorEntry bgColor = MapartHelper.conversionSettings.backgroundColor;

                    if (newImagePath != null) {
                        mapart.setImagePath(null);
                        mapart.setOriginal(ImageIO.read(newImagePath.toFile()));
                        mapart.setImagePath(newImagePath);
                        mapart.autoCropOriginalImage();
                    }
                    if (Thread.currentThread().isInterrupted()) return;

                    boolean needReconvertingColors = imageChangeResult != ImageChangeResult.SIMPLE
                            && (imageChangeResult == ImageChangeResult.NEED_RESCALE || bgColor == MapColorEntry.CLEAR && !showOriginalImage);

                    BufferedImage bufferedImage = cropAndScaleToMapSize(mapart, needReconvertingColors);
                    if (Thread.currentThread().isInterrupted()) return;

                    if (imageChangeResult != ImageChangeResult.SIMPLE) {
                        if (needReconvertingColors)
                            bufferedImage = preprocessImage(bufferedImage);
                        if (Thread.currentThread().isInterrupted()) return;

                        PaletteColors.clearColorCache();
                        mapart.clearColorCounters();
                        if (!showOriginalImage) {
                            if (!PaletteConfigManager.presetsConfig.shouldConvertWithCurrentPreset())
                                bufferedImage = new BufferedImage(bufferedImage.getWidth(), bufferedImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
                            else
                                bufferedImage = convertToBlocksPalette(mapart, bufferedImage, bgColor, MapartHelper.conversionSettings.use3D());
                        }
                        if (bufferedImage == null) return;
                        if (CurrentConversionSettings.cropMode == CroppingMode.USER_CROP) {
                            mapart.setScaledImage(bufferedImage.getSubimage(
                                    mapart.getInsertionX(), mapart.getInsertionY(),
                                    mapart.getScaledImage().getWidth(), mapart.getScaledImage().getHeight())
                            );
                        }
                        if (Thread.currentThread().isInterrupted()) return;
                    }

                    mapart.setNativeImage(NativeImageUtils.convertBufferedImageToNativeImage(
                            bufferedImage,
                            bgColor,
                            CurrentConversionSettings.doShowTransparent)
                    );
                    if (Thread.currentThread().isInterrupted()) return;
                    MinecraftClient.getInstance().execute(() -> NativeImageUtils.updateMapartImageTexture(mapart.getNativeImage()));

                    MapartImageUpdater.scale = 0;
                    MapartImageUpdater.moveDx = 0;
                    MapartImageUpdater.moveDy = 0;

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
}
