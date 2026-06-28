package rh.maparthelper.render.pipeline;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import com.mojang.blaze3d.systems.RenderSystem;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;

public class PainterToolAreaUniform {
    public static final int SIZE = new Std140SizeCalculator()
            .putVec2()  // ScaledMaskSize
            .putInt()   // MarchingAnts
            .get();
    public static final GpuBuffer BUFFER = RenderSystem.getDevice().createBuffer(() -> "PainterToolArea", 136, SIZE);

    public static void set(float scaledAreaWidth, float scaledAreaHeight, boolean marchingAnts) {
        try (MemoryStack memoryStack = MemoryStack.stackPush()) {
            ByteBuffer byteBuffer = Std140Builder.onStack(memoryStack, SIZE)
                    .putVec2(scaledAreaWidth, scaledAreaHeight)
                    .putInt(marchingAnts ? 1 : 0)
                    .get();
            RenderSystem.getDevice().createCommandEncoder().writeToBuffer(BUFFER.slice(), byteBuffer);
        }
    }
}
