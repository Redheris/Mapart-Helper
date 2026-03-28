package rh.maparthelper.conversion;

import com.google.common.util.concurrent.AtomicDouble;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import rh.maparthelper.MapartHelper;
import rh.maparthelper.colors.ColorUtils;
import rh.maparthelper.config.palette.PaletteColors;
import rh.maparthelper.config.palette.PaletteConfigManager;
import rh.maparthelper.conversion.dithering.ColorConverter;
import rh.maparthelper.gui.screen.MapartEditorScreen;
import rh.maparthelper.mapart.AbstractMapart;
import rh.maparthelper.mapart.MapartProcessing;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.IOException;
import java.nio.file.Path;
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

        cancelConverting();
        currentConvertingFuture = convertingExecutor.submit(new FutureTask<>(currentRunnable, null));
    }

    public static void cancelConverting() {
        conversionProgress.set(0);
        if (currentConvertingFuture != null)
            currentConvertingFuture.cancel(true);
    }

    private static @NotNull UpdateMapartRunnable createUpdateMapartRunnable(MapartProcessing processingMapart, Path path, ImageChangeResult imageChangeResult) {
        boolean logExecutionTime = MapartHelper.commonConfig().logConversionTime;
        if (!processingMapart.isReset() && path.equals(processingMapart.getImagePath()))
            return new UpdateMapartRunnable(processingMapart, null, logExecutionTime, imageChangeResult);
        return new UpdateMapartRunnable(processingMapart, path, logExecutionTime, imageChangeResult);
    }

    public static boolean isConverting() {
        if (currentConvertingFuture == null || currentRunnable == null) return false;
        return !currentConvertingFuture.isCancelled() && currentRunnable.isUpdating();
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
                                                        int bgColor, int bgMapColorId, boolean use3D, boolean useUnobtainable) {
        topLineBright = new int[image.getWidth()];
        topLineCorrect = new int[image.getWidth()];
        ColorConverter colorConverter = MapartHelper.conversionConfig().getColorConverter().createColorConverter(
                mapart,
                image,
                use3D,
                bgColor,
                bgMapColorId,
                topLineBright,
                topLineCorrect,
                conversionProgress
        );
        return colorConverter.convertColors(useUnobtainable);
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

        private final boolean showOriginalImage = MapartHelper.conversionConfig().isShowOriginalImage();
        private final int bgColor = MapartHelper.conversionConfig().getBackgroundRenderColor();
        private final int bgMapColorId = MapartHelper.conversionConfig().getBackgroundColor().mapColor().id;
        private final boolean use3D = MapartHelper.conversionConfig().use3D();
        private final boolean useUnobtainable = MapartHelper.conversionConfig().useUnobtainable();
        private final int colorsCacheLiveTimeMs = MapartHelper.commonConfig().colorsCacheLiveTimeMs;

        private boolean isUpdating = true;

        public UpdateMapartRunnable(MapartProcessing mapart, Path path, boolean logExecutionTime, ImageChangeResult imageChangeResult) {
            this.mapart = mapart;
            this.newImagePath = path;
            this.logExecutionTime = logExecutionTime;
            if (imageChangeResult == ImageChangeResult.ONLY_TOP_LINE && (showOriginalImage || bgColor != 0))
                this.imageChangeResult = ImageChangeResult.SIMPLE;
            else
                this.imageChangeResult = imageChangeResult;
        }

        public boolean isUpdating() {
            return isUpdating;
        }

        @Override
        public void run() {
            synchronized (mapart) {
                try {
                    long startTime = System.currentTimeMillis();
                    isUpdating = true;
                    conversionProgress = new AtomicDouble(0.0);

                    if (newImagePath != null) {
                        BufferedImage original = ImageIO.read(newImagePath.toFile());
                        if (original == null) throw new IllegalArgumentException();
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

                        mapart.clearColorCounters();
                        if (!showOriginalImage) {
                            if (PaletteConfigManager.presetsConfig.shouldConvertWithCurrentPreset())
                                processingImage = convertToBlocksPalette(mapart, processingImage, bgColor, bgMapColorId, use3D, useUnobtainable);
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

                    Minecraft.getInstance().execute(() -> {
                        NativeImageUtils.updateMapartImageTexture(mapart.getNativeImage());
                        isUpdating = false;
                        conversionProgress.set(1.0);
                    });

                    if (logExecutionTime) {
                        double timeLeft = (System.currentTimeMillis() - startTime) / 1000.0;
                        MapartHelper.LOGGER.info("Image preprocessing and conversion took {} seconds", timeLeft);
                    }
                } catch (IOException | IllegalArgumentException e) {
                    CurrentConversionSettings.resetMapart();
                    MapartHelper.LOGGER.error("Can't read image file \"{}\". Check path and file's extension. ", newImagePath, e);
                } catch (Exception e) {
                    CurrentConversionSettings.resetMapart();
                    MapartHelper.LOGGER.error("Unexpected error occurred while reading and converting an imag: ", e);
                    throw new RuntimeException(e);
                } finally {
                    Minecraft.getInstance().execute(() -> {
                        if (Minecraft.getInstance().screen instanceof MapartEditorScreen editorScreen) {
                            editorScreen.updateMaterialList();
                            editorScreen.updateMapartOutputButtons();
                        }
                    });
                }
                try {
                    Thread.sleep(colorsCacheLiveTimeMs);
                    PaletteColors.clearColorCache();
                } catch (InterruptedException ignored) {

                }
            }
        }
    }
}
