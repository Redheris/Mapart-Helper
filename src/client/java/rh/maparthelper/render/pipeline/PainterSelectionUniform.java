package rh.maparthelper.render.pipeline;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import com.mojang.blaze3d.systems.RenderSystem;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;

import static rh.maparthelper.colors.ColorUtils.intToVec4;

public class PainterSelectionUniform {
    public static final int SIZE = new Std140SizeCalculator()
            .putIVec2()
            .putVec4()
            .putInt()
            .get();
    public static final GpuBuffer BUFFER = RenderSystem.getDevice().createBuffer(() -> "PainterSelection", 136, SIZE);

    public static void set(int scaledWidth, int scaledHeight, int selectionFillColor, boolean fillSelection) {
        try (MemoryStack memoryStack = MemoryStack.stackPush()) {
            ByteBuffer byteBuffer = Std140Builder.onStack(memoryStack, SIZE)
                    .putIVec2(scaledWidth, scaledHeight)
                    .putVec4(intToVec4(selectionFillColor))
                    .putInt(fillSelection ? 1 : 0)
                    .get();
            RenderSystem.getDevice().createCommandEncoder().writeToBuffer(BUFFER.slice(), byteBuffer);
        }
    }
}
