package rh.maparthelper.conversion.dithering;

import java.util.Arrays;

public enum DitheringAlgorithms {
    NONE(new int[0], new int[0], new int[0]),
    FLOYD_STEINBERG(
            new int[]{7, 3, 5, 1},
            new int[]{1, -1, 0, 1},
            new int[]{0, 1, 1, 1}
    ),
    ATKINSON(
            new int[]{1, 1, 1, 1, 1, 1},
            new int[]{1, 2, -1, 0, 1, 0},
            new int[]{0, 0, 1, 1, 1, 2}
    ),
    JJN(
            new int[]{
                    7, 5,
                    3, 5, 7, 5, 3,
                    1, 3, 5, 3, 1
            },
            new int[]{
                    1, 2,
                    -2, -1, 0, 1, 2,
                    -2, -1, 0, 1, 2
            },
            new int[]{
                    0, 0,
                    1, 1, 1, 1, 1,
                    2, 2, 2, 2, 2
            }
    ),
    STUCKI(
            new int[]{
                    8, 4,
                    2, 4, 8, 4, 2,
                    1, 2, 4, 2, 1
            },
            new int[]{
                    1, 2,
                    -2, -1, 0, 1, 2,
                    -2, -1, 0, 1, 2
            },
            new int[]{
                    0, 0,
                    1, 1, 1, 1, 1,
                    2, 2, 2, 2, 2
            }
    ),
    BURKES(
            new int[]{
                    8, 4,
                    2, 4, 8, 4, 2
            },
            new int[]{
                    1, 2,
                    -2, -1, 0, 1, 2
            },
            new int[]{
                    0, 0,
                    1, 1, 1, 1, 1
            }
    ),
    SIERRA(
            new int[]{
                    4, 3,
                    1, 2, 3, 2, 1
            },
            new int[]{
                    1, 2,
                    -2, -1, 0, 1, 2
            },
            new int[]{
                    0, 0,
                    1, 1, 1, 1, 1
            }
    ),
    SIERRA_LITE(
            new int[]{2, 1, 1},
            new int[]{1, -1, 0},
            new int[]{0, 1, 1}
    ),
    SIERRA_2ROW(
            new int[]{
                    5, 3,
                    2, 4, 5, 4, 2,
                    2, 3, 2
            },
            new int[]{
                    1, 2,
                    -2, -1, 0, 1, 2,
                    -1, 0, 1
            },
            new int[]{
                    0, 0,
                    1, 1, 1, 1, 1,
                    2, 2, 2
            }
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
     * Then weights will be {7, 3F, 5, 1} (/16 will apply as sum of the weights) and two other arrays will be coordinate offsets with X at (0,0)
     * @param weights Array of algorithm's weights for diffusion error propagation
     * @param offsetX Array of horizontal (x) coordinate offsets
     * @param offsetY Array of vertical (y) coordinate offsets
     */
    DitheringAlgorithms(int[] weights, int[] offsetX, int[] offsetY) {
        int sum = Arrays.stream(weights).sum();
        this.kernel = new float[weights.length];
        for (int i = 0; i < weights.length; i++) {
            this.kernel[i] = (float) weights[i] / sum;
        }
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
