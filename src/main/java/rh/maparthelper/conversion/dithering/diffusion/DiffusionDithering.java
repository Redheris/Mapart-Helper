package rh.maparthelper.conversion.dithering.diffusion;

import net.minecraft.block.MapColor;
import rh.maparthelper.colors.ColorUtils;
import rh.maparthelper.colors.DitherEntry;
import rh.maparthelper.config.palette.PaletteColors;
import rh.maparthelper.conversion.dithering.ColorConverter;
import rh.maparthelper.conversion.dithering.ConversionContext;
import rh.maparthelper.mapart.ColorsCounter;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.Arrays;

public abstract class DiffusionDithering extends ColorConverter {
    protected final float[] kernel;
    protected final int[] offsetX;
    protected final int[] offsetY;
    public final int rowsNumber;

    /// Array of the stored errors with 3*width size for storing as int[]{r1,g1,b1, r2,b2,g2, ...}
    private int[] errorsArray;

    /**
     * Describes error diffusion dithering algorithm by three arrays representing rules of error propagation
     * E.g. Floyd-Steinberg has next rule:<br>
     * (1/16)<br>
     * --X 7<br>
     * 3 5 1<br>
     * Then weights will be {7/16F, 3/16F, 5/16F, 1/16F} and two other arrays will be coordinate offsets with X at (0,0)
     *
     * @param kernel  Array of algorithm's weights for diffusion error propagation
     * @param offsetX Array of horizontal coordinate offsets
     * @param offsetY Array of vertical coordinate offsets
     */
    public DiffusionDithering(ConversionContext context, float[] kernel, int[] offsetX, int[] offsetY) {
        super(context);

        this.kernel = kernel;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.rowsNumber = Arrays.stream(offsetY).max().orElse(0) + 1;
    }

    @Override
    public BufferedImage convertColors(boolean useUnobtainableColors) {
        BufferedImage convertedImage = new BufferedImage(original.getWidth(), original.getHeight(), BufferedImage.TYPE_INT_ARGB);

        int width = convertedImage.getWidth();
        int[] originalPixels = ((DataBufferInt) original.getRaster().getDataBuffer()).getData();
        int[] resultPixels = ((DataBufferInt) convertedImage.getRaster().getDataBuffer()).getData();
        double progressStep = 1.0 / originalPixels.length;
        this.errorsArray = new int[rowsNumber * width * 3];

        for (int y = 0; y < convertedImage.getHeight(); y++) {
            for (int x = 0; x < width; x++) {
                ColorsCounter colorsCounter = mapart.getColorsCounterFor(x / 128, y / 128);
                if (colorsCounter == null || Thread.currentThread().isInterrupted()) {
                    mapart.clearColorCounters();
                    return null;
                }
                int argb = originalPixels[x + y * width];
                if (argb == 0 && backgroundColor != 0) {
                    resultPixels[x + y * width] = backgroundColor;
                    colorsCounter.increment(backgroundMapColorId);
                    continue;
                }

                int ind = x * 3;
                int[] argb0 = ColorUtils.getARGB(argb);
                argb0[1] = Math.clamp(argb0[1] + errorsArray[ind], 0, 255);
                argb0[2] = Math.clamp(argb0[2] + errorsArray[ind + 1], 0, 255);
                argb0[3] = Math.clamp(argb0[3] + errorsArray[ind + 2], 0, 255);
                argb = ColorUtils.getARGB(argb0);

                DitherEntry mapColors = useUnobtainableColors ? PaletteColors.getClosestColorUnconditional(argb) : PaletteColors.getClosestColor(argb, use3D);
                MapColor closestColor = mapColors.getFirstMapColor();

                int newArgb;
                if (mapColors == DitherEntry.CLEAR) {
                    newArgb = backgroundColor;
                } else {
                    spreadDiffusionError(width, x, mapColors.errorRed(), mapColors.errorGreen(), mapColors.errorBlue());
                    if (y > 0 && resultPixels[x + (y - 1) * width] == 0)
                        newArgb = PaletteColors.getMapRenderColor(mapColors.colorByte1(),MapColor.Brightness.HIGH);
                    else
                        newArgb = PaletteColors.getMapRenderColor(mapColors.colorByte1());
                    colorsCounter.increment(closestColor.id);
                }
                if (!useUnobtainableColors && y == mapart.getInsertionY() && x >= mapart.getInsertionX()) {
                    topLineBright[x - mapart.getInsertionX()] = newArgb;
                    topLineCorrect[x - mapart.getInsertionX()] = PaletteColors.getMapRenderColor(mapColors.colorByte1());
                }
                resultPixels[x + y * width] = newArgb;
                progress.addAndGet(progressStep);
            }

            for (int row = 1; row < rowsNumber; row++) {
                System.arraycopy(errorsArray, row * width * 3, errorsArray, (row - 1) * width * 3, width * 3);
            }
            Arrays.fill(errorsArray, (rowsNumber - 1) * width * 3, rowsNumber * width * 3, 0);
        }

        return convertedImage;
    }

    /**
     * @param width      Width of the image
     * @param x          X coordinate of the pixel
     * @param errorRed   Value of the distance error for Red
     * @param errorGreen Value of the distance error for Green
     * @param errorBlue  Value of the distance error for Blue
     */
    private void spreadDiffusionError(int width, int x, int errorRed, int errorGreen, int errorBlue) {
        for (int i = 0; i < kernel.length; i++) {
            int nx = x + offsetX[i];
            int ny = offsetY[i];
            putError(width, nx, ny,
                    errorRed * kernel[i],
                    errorGreen * kernel[i],
                    errorBlue * kernel[i]
            );
        }
    }

    private void putError(int width, int x, int y, float r, float g, float b) {
        if (x < 0 || x >= width || y < 0 || y >= rowsNumber) return;
        int ind = (y * width + x) * 3;
        errorsArray[ind] += Math.round(r);
        errorsArray[ind + 1] += Math.round(g);
        errorsArray[ind + 2] += Math.round(b);
    }

    protected static float[] createKernelFromWeights(int... weights) {
        int sum = Arrays.stream(weights).sum();
        float[] kernel = new float[weights.length];
        for (int i = 0; i < weights.length; i++) {
            kernel[i] = (float) weights[i] / sum;
        }
        return kernel;
    }
}
