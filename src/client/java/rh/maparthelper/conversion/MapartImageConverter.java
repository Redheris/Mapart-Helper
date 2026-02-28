package rh.maparthelper.conversion;

import com.google.common.util.concurrent.AtomicDouble;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import net.minecraft.block.MapColor;
import net.minecraft.client.MinecraftClient;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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
import java.io.IOException;
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
    @Nullable
    private static Future<?> currentConvertingFuture;
    @Nullable
    private static UpdateMapartRunnable currentRunnable;
    private static int[] topLineBright;
    private static int[] topLineCorrect;

    public static void readAndUpdateMapartImage(MapartProcessing processingMapart, Path path, ImageChangeResult imageChangeResult) {
        if (currentRunnable != null && isConverting()) {
            if (currentRunnable.imageChangeResult.priority > imageChangeResult.priority)
                imageChangeResult = currentRunnable.imageChangeResult;
        }
        currentRunnable = createUpdateMapartRunnable(processingMapart, path, imageChangeResult);

        if (currentConvertingFuture != null)
            currentConvertingFuture.cancel(true);
        currentConvertingFuture = convertingExecutor.submit(new FutureTask<>(currentRunnable, null));
    }

    private static @NotNull UpdateMapartRunnable createUpdateMapartRunnable(MapartProcessing processingMapart, Path path, ImageChangeResult imageChangeResult) {
        boolean logExecutionTime = MapartHelper.commonConfig.mapartEditor.logConversionTime;
        if (!processingMapart.isReset() && path.equals(processingMapart.getImagePath()))
            return new UpdateMapartRunnable(processingMapart, null, logExecutionTime, imageChangeResult);
        return new UpdateMapartRunnable(processingMapart, path, logExecutionTime, imageChangeResult);
    }

    public static boolean isConverting() {
        return currentConvertingFuture != null && !currentConvertingFuture.isDone();
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
    private static BufferedImage convertToBlocksPalette(AbstractMapart mapart, BufferedImage image,
                                                        int bgColor, int bgMapColorId, boolean use3D) {
        BufferedImage converted = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);

        int width = converted.getWidth();
        int[] originalPixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
        int[] resultPixels = ((DataBufferInt) converted.getRaster().getDataBuffer()).getData();
        double progressStep = 1.0 / originalPixels.length;

        topLineCorrect = new int[width];
        topLineBright = new int[width];

        int[] errorsArray = new int[0];
        DitheringAlgorithms ditherAlg = MapartHelper.conversionSettings.ditheringAlgorithm;
        boolean useDithering = ditherAlg != DitheringAlgorithms.NONE;
        if (useDithering)
            errorsArray = new int[ditherAlg.rowsNumber * width * 3];

        for (int y = 0; y < converted.getHeight(); y++) {
            for (int x = 0; x < width; x++) {
                ColorsCounter colorsCounter = mapart.getColorsCounterFor(x / 128, y / 128);
                if (colorsCounter == null || Thread.currentThread().isInterrupted()) {
                    PaletteColors.clearColorCache();
                    mapart.clearColorCounters();
                    return null;
                }
                int argb = originalPixels[x + y * width];
                if (argb == 0 && bgColor != 0) {
                    resultPixels[x + y * width] = bgColor;
                    colorsCounter.increment(bgMapColorId);
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
                    newArgb = bgColor;
                } else {
                    if (useDithering)
                        ditherAlg.spreadDiffusionError(errorsArray, width, x, color.distError());
                    if (y > 0 && resultPixels[x + (y - 1) * width] == 0)
                        newArgb = color.mapColor().getRenderColor(MapColor.Brightness.HIGH);
                    else {
                        if (use3D)
                            newArgb = color.getRenderColor();
                        else
                            newArgb = color.mapColor().getRenderColor(MapColor.Brightness.NORMAL);
                    }
                    colorsCounter.increment(color.mapColor().id);
                }
                if (y == mapart.getInsertionY() && x >= mapart.getInsertionX()) {
                    topLineBright[x - mapart.getInsertionX()] = newArgb;
                    topLineCorrect[x - mapart.getInsertionX()] = color.getRenderColor();
                }
                resultPixels[x + y * width] = newArgb;
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

    private static void swapTopLine(AbstractMapart mapart, BufferedImage image) {
        int lineY = mapart.getInsertionY();
        int imageWidth = image.getWidth();
        int insertionX = mapart.getInsertionX();
        int[] pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
        if (lineY > 0)
            System.arraycopy(topLineBright, 0, pixels, lineY * imageWidth + insertionX, imageWidth - insertionX);
        else
            System.arraycopy(topLineCorrect, 0, pixels, lineY * imageWidth + insertionX, imageWidth - insertionX);
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

    private static class UpdateMapartRunnable implements Runnable {
        public final ImageChangeResult imageChangeResult;
        private final MapartProcessing mapart;
        private final Path newImagePath;
        private final boolean logExecutionTime;

        private final boolean showOriginalImage = MapartHelper.conversionSettings.showOriginalImage;
        private final int bgColor = MapartHelper.conversionSettings.getBackgroundRenderColor();
        private final int bgMapColorId = MapartHelper.conversionSettings.getBackgroundColor().mapColor().id;
        private final boolean use3D = MapartHelper.conversionSettings.use3D();

        public UpdateMapartRunnable(MapartProcessing mapart, Path path, boolean logExecutionTime, ImageChangeResult imageChangeResult) {
            this.mapart = mapart;
            this.newImagePath = path;
            this.logExecutionTime = logExecutionTime;
            if (imageChangeResult == ImageChangeResult.ONLY_TOP_LINE && (showOriginalImage || bgMapColorId != 0))
                this.imageChangeResult = ImageChangeResult.SIMPLE;
            else
                this.imageChangeResult = imageChangeResult;
        }

        @Override
        public void run() {
            synchronized (mapart) {
                try {
                    long startTime = System.currentTimeMillis();
                    conversionProgress = new AtomicDouble(0.0);

                    if (newImagePath != null) {
                        BufferedImage original = ImageIO.read(newImagePath.toFile());
                        mapart.setOriginal(original);
                        mapart.setImagePath(newImagePath);
                        mapart.autoCropOriginalImage();
                    }
                    if (Thread.currentThread().isInterrupted()) return;

                    BufferedImage processingImage = cropAndScaleToMapSize(mapart, imageChangeResult == ImageChangeResult.NEED_FULL_UPDATE);
                    if (Thread.currentThread().isInterrupted()) return;

                    if (imageChangeResult == ImageChangeResult.NEED_FULL_UPDATE) {
                        processingImage = preprocessImage(processingImage);
                        if (Thread.currentThread().isInterrupted()) return;

                        PaletteColors.clearColorCache();
                        mapart.clearColorCounters();
                        if (!showOriginalImage) {
                            if (PaletteConfigManager.presetsConfig.shouldConvertWithCurrentPreset())
                                processingImage = convertToBlocksPalette(mapart, processingImage, bgColor, bgMapColorId, use3D);
                            else
                                processingImage = new BufferedImage(processingImage.getWidth(), processingImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
                        }
                    } else if (imageChangeResult == ImageChangeResult.ONLY_TOP_LINE) {
                        swapTopLine(mapart, processingImage);
                    }
                    if (processingImage == null || Thread.currentThread().isInterrupted()) return;

                    if (imageChangeResult != ImageChangeResult.SIMPLE && CurrentConversionSettings.cropMode == CroppingMode.USER_CROP) {
                        assert mapart.getScaledImage() != null;
                        mapart.setScaledImage(processingImage.getSubimage(
                                mapart.getInsertionX(), mapart.getInsertionY(),
                                mapart.getScaledImage().getWidth(), mapart.getScaledImage().getHeight())
                        );
                        if (Thread.currentThread().isInterrupted()) return;
                    }

                    if (showOriginalImage || imageChangeResult != ImageChangeResult.NEED_FULL_UPDATE) {
                        mapart.setNativeImage(NativeImageUtils.convertBufferedImageToNativeImage(
                                processingImage,
                                bgColor,
                                CurrentConversionSettings.doShowTranslucent
                        ));
                    } else {
                        mapart.setNativeImage(NativeImageUtils.convertBufferedImageToNativeImage(processingImage));
                    }
                    if (Thread.currentThread().isInterrupted()) return;

                    MinecraftClient.getInstance().execute(() -> NativeImageUtils.updateMapartImageTexture(mapart.getNativeImage()));

                    if (logExecutionTime) {
                        double timeLeft = (System.currentTimeMillis() - startTime) / 1000.0;
                        MapartHelper.LOGGER.info("Image preprocessing and conversion took {} seconds", timeLeft);
                    }
                } catch (IOException e) {
                    CurrentConversionSettings.resetMapart();
                    MapartHelper.LOGGER.error("Can't read image file \"{}\". Check path and file's extension. ", newImagePath, e);
                } catch (Exception e) {
                    CurrentConversionSettings.resetMapart();
                    MapartHelper.LOGGER.error("Unexpected error occurred while reading and converting an imag: ", e);
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
