package rh.maparthelper.conversion.dithering;

import java.util.Arrays;

public enum DitheringAlgorithms {
    NONE(new float[0], new int[0], new int[0]),
    FLOYD_STEINBERG(
            new float[]{7F/16, 3F/16, 5F/16, 1F/16},
            new int[]{1, -1, 0, 1},
            new int[]{0, 1, 1, 1}
    ),
    ATKINSON(
            new float[]{1F/8, 1F/8, 1F/8, 1F/8, 1F/8, 1F/8},
            new int[]{1, 2, -1, 0, 1, 0},
            new int[]{0, 0, 1, 1, 1, 2}
    );


    private final float[] kernel;
    private final int[] offsetX;
    private final int[] offsetY;
    public final int rowsNumber;

    /**
     * Describes error diffusion dithering algorithm by three arrays representing rules of error propagation
     * E.g. Floyd-Steinberg has next rule:<br>
     * (1/16)<br>
     * --X 7<br>
     * 3 5 1<br>
     * Then kernel will be {7F/16, 3F/16, 5F/16, 1F/16} and two other arrays will be coordinate offsets with X at (0,0)
     * @param kernel Array of algorithm's coefficients for diffusion error propagation
     * @param offsetX Array of horizontal (x) coordinate offsets
     * @param offsetY Array of vertical (y) coordinate offsets
     */
    DitheringAlgorithms(float[] kernel, int[] offsetX, int[] offsetY) {
        this.kernel = kernel;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.rowsNumber = Arrays.stream(offsetY).max().orElse(0) + 1;
    }

    /**
     *
     * @param errorsArray Array of the stored errors with 3*width size for storing as int[]{r1,g1,b1, r2,b2,g2, ...}
     * @param width Width of the image
     * @param x X coordinate of the pixel
     * @param errorRGB Value of the distance error as int[]{r, g, b}
     */
    public void spreadDiffusionError(int[] errorsArray, int width, int x, int[] errorRGB) {
        for (int i = 0; i < kernel.length; i++) {
            int nx = x + offsetX[i];
            int ny = offsetY[i];
            putError(errorsArray, width, nx, ny,
                    errorRGB[0] * kernel[i],
                    errorRGB[1] * kernel[i],
                    errorRGB[2] * kernel[i]
            );
        }
    }

    private void putError(int[] errorsArray, int width, int x, int y, float r, float g, float b) {
        if (x < 0 || x >= width || y < 0 || y >= rowsNumber) return;
        int ind = (y * width + x) * 3;
        errorsArray[ind] += Math.round(r);
        errorsArray[ind + 1] += Math.round(g);
        errorsArray[ind + 2] += Math.round(b);
    }
}
