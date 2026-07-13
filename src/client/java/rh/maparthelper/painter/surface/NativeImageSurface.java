package rh.maparthelper.painter.surface;

import com.mojang.blaze3d.platform.NativeImage;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.system.MemoryUtil;
import rh.maparthelper.mixin.NativeImageAccessor;

import java.nio.IntBuffer;

public class NativeImageSurface extends AbstractSurface {
    private final NativeImage image;
    private final IntBuffer buffer;

    public NativeImageSurface(@NotNull NativeImage image, boolean writable) {
        super(writable);
        this.image = image;
        NativeImageAccessor accessor = (NativeImageAccessor) (Object) image;
        long pointer = accessor.maparthelper$getPointer();
        long capacity = accessor.maparthelper$getCapacity();
        this.buffer = MemoryUtil.memIntBuffer(pointer, (int) capacity);
    }

    public NativeImageSurface(@NotNull NativeImage image) {
        this(image, true);
    }

    @Override
    public int getWidth() {
        return image.getWidth();
    }

    @Override
    public int getHeight() {
        return image.getHeight();
    }

    @Override
    public int getPixelUnsafe(int x, int y) {
        return buffer.get(x + y * getWidth());
    }

    @Override
    public boolean setPixelUnsafe(int x, int y, int color) {
        if (getPixelUnsafe(x, y) == color) return false;

        buffer.put(x + y * getWidth(), color);

        return true;
    }
}
