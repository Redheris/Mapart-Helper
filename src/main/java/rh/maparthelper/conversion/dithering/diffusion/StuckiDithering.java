package rh.maparthelper.conversion.dithering.diffusion;

import rh.maparthelper.conversion.dithering.ConversionContext;

public class StuckiDithering extends DiffusionDithering {
    private static final float[] KERNEL = createKernelFromWeights(
            8, 4,
            2, 4, 8, 4, 2,
            1, 2, 4, 2, 1
    );
    private static final int[] OFFSET_X = new int[]{
            1, 2,
            -2, -1, 0, 1, 2,
            -2, -1, 0, 1, 2
    };
    private static final int[] OFFSET_Y = new int[]{
            0, 0,
            1, 1, 1, 1, 1,
            2, 2, 2, 2, 2
    };

    public StuckiDithering(ConversionContext context) {
        super(context, KERNEL, OFFSET_X, OFFSET_Y);
    }
}
