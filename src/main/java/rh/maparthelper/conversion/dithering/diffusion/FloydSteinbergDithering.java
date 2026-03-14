package rh.maparthelper.conversion.dithering.diffusion;

import rh.maparthelper.conversion.dithering.ConversionContext;

public class FloydSteinbergDithering extends DiffusionDithering {
    private static final float[] KERNEL = createKernelFromWeights(7, 3, 5, 1);
    private static final int[] OFFSET_X = new int[]{1, -1, 0, 1};
    private static final int[] OFFSET_Y = new int[]{0, 1, 1, 1};

    public FloydSteinbergDithering(ConversionContext context) {
        super(context, KERNEL, OFFSET_X, OFFSET_Y);
    }
}
