package rh.maparthelper.conversion.dithering.diffusion;

import rh.maparthelper.conversion.dithering.ConversionContext;

public class JarvisJudiceNinkeDithering extends DiffusionDithering {
    private static final float[] KERNEL = createKernelFromWeights(
            7, 5,
            3, 5, 7, 5, 3,
            1, 3, 5, 3, 1
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

    public JarvisJudiceNinkeDithering(ConversionContext context) {
        super(context, KERNEL, OFFSET_X, OFFSET_Y);
    }
}
