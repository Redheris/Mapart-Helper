package rh.maparthelper.maps;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.world.level.material.MapColor;
import rh.maparthelper.MapartHelper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class MapartImageData {
    private static final ExecutorService imageBuilder = Executors.newSingleThreadExecutor(
            new ThreadFactoryBuilder().setNameFormat("Mapart Gallery/Image Builder").build()
    );

    public static final MapartImageData INVALID = new MapartImageData(null);
    public final int imageWidth;
    public final int imageHeight;
    public byte[] colors;

    public MapartImageData(int imageWidth, int imageHeight, byte[] colors) {
        this.colors = colors;
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
    }

    public MapartImageData(byte[] colors) {
        this(128, 128, colors);
    }

    public void buildNativeImage(Consumer<NativeImage> imageConsumer) {
        imageBuilder.execute(() -> {
            try {
                NativeImage image = new NativeImage(imageWidth, imageHeight, false);
                for (int i = 0; i < colors.length; i++) {
                    image.setPixel(
                            i % imageWidth,
                            i / imageWidth,
                            MapColor.getColorFromPackedId(colors[i])
                    );
                }
                imageConsumer.andThen(NativeImage::close).accept(image);
            } catch (Exception e) {
                MapartHelper.LOGGER.error("An error occurred during execution on the mapart image: {}", e.toString());
                throw new RuntimeException(e);
            }
        });
    }
}
