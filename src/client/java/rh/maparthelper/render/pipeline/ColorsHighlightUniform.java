package rh.maparthelper.render.pipeline;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import com.mojang.blaze3d.systems.RenderSystem;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;

import static rh.maparthelper.colors.ColorUtils.intToVec4;

public class ColorsHighlightUniform {
    public static final int SIZE = new Std140SizeCalculator()
            .putVec4()
            .putVec4()
            .putVec4()
            .putVec4()
            .get();
    public static final GpuBuffer BUFFER = RenderSystem.getDevice().createBuffer(() -> "ColorsHighlight", 136, SIZE);

    public static void set(int color1, int color2, int color3, int colorHighlight) {
        try (MemoryStack memoryStack = MemoryStack.stackPush()) {
            ByteBuffer byteBuffer = Std140Builder.onStack(memoryStack, SIZE)
                    .putVec4(intToVec4(color1))
                    .putVec4(intToVec4(color2))
                    .putVec4(intToVec4(color3))
                    .putVec4(intToVec4(colorHighlight))
                    .get();
            RenderSystem.getDevice().createCommandEncoder().writeToBuffer(BUFFER.slice(), byteBuffer);
        }
    }
}
