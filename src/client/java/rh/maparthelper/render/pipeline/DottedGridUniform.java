package rh.maparthelper.render.pipeline;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import com.mojang.blaze3d.systems.RenderSystem;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;

import static rh.maparthelper.colors.ColorUtils.intToVec4;

public class DottedGridUniform {
    public static final int SIZE = new Std140SizeCalculator()
            .putIVec2()
            .putIVec2()
            .putIVec2()
            .putVec4()
            .putVec4()
            .get();
    public static final GpuBuffer BUFFER = RenderSystem.getDevice().createBuffer(() -> "DottedGrid", 136, SIZE);

    public static void set(int screenWidth, int screenHeight,
                           int scaledWidth, int scaledHeight,
                           int x0, int y0,
                           int color1, int color2
    ) {
        try (MemoryStack memoryStack = MemoryStack.stackPush()) {
            ByteBuffer byteBuffer = Std140Builder.onStack(memoryStack, SIZE)
                    .putIVec2(screenWidth, screenHeight)
                    .putIVec2(scaledWidth, scaledHeight)
                    .putIVec2(x0, y0)
                    .putVec4(intToVec4(color1))
                    .putVec4(intToVec4(color2))
                    .get();
            RenderSystem.getDevice().createCommandEncoder().writeToBuffer(BUFFER.slice(), byteBuffer);
        }
    }
}
