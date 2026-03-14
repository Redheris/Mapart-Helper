package rh.maparthelper.conversion.dithering.diffusion;

import rh.maparthelper.conversion.dithering.ConversionContext;

public class AtkinsonDithering extends DiffusionDithering {
    private static final float[] KERNEL = createKernelFromWeights(1, 1, 1, 1, 1, 1);
    private static final int[] OFFSET_X = new int[]{1, 2, -1, 0, 1, 0};
    private static final int[] OFFSET_Y = new int[]{0, 0, 1, 1, 1, 2};

    public AtkinsonDithering(ConversionContext context) {
        super(context, KERNEL, OFFSET_X, OFFSET_Y);
    }
}
