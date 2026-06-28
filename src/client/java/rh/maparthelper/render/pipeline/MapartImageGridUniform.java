package rh.maparthelper.render.pipeline;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import com.mojang.blaze3d.systems.RenderSystem;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;

import static rh.maparthelper.colors.ColorUtils.intToVec4;

public class MapartImageGridUniform {
    public static final int SIZE = new Std140SizeCalculator()
            .putIVec2() // ScreenSize
            .putVec2()  // ScaledSize
            .putVec2()  // StartPos
            .putVec4()  // ColorMapGridLine
            .putInt()   // PixelsGrid
            .putInt()   // MapsGrid
            .get();
    public static final GpuBuffer BUFFER = RenderSystem.getDevice().createBuffer(() -> "MapartImageGrid", 136, SIZE);

    public static void set(int screenWidth, int screenHeight,
                           float scaledWidth, float scaledHeight,
                           float x0, float y0,
                           int colorMap,
                           boolean showPixelGrid, boolean showMapGrid
    ) {
        try (MemoryStack memoryStack = MemoryStack.stackPush()) {
            ByteBuffer byteBuffer = Std140Builder.onStack(memoryStack, SIZE)
                    .putIVec2(screenWidth, screenHeight)
                    .putVec2(scaledWidth, scaledHeight)
                    .putVec2(x0, y0)
                    .putVec4(intToVec4(colorMap))
                    .putInt(showPixelGrid ? 1 : 0)
                    .putInt(showMapGrid ? 1 : 0)
                    .get();
            RenderSystem.getDevice().createCommandEncoder().writeToBuffer(BUFFER.slice(), byteBuffer);
        }
    }
}
