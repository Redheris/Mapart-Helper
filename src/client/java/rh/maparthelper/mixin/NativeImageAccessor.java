package rh.maparthelper.mixin;

import com.mojang.blaze3d.platform.NativeImage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.nio.channels.WritableByteChannel;

@Mixin(NativeImage.class)
public interface NativeImageAccessor {
    @Invoker("writeToChannel")
    boolean maparthelper$writeToChannel(WritableByteChannel channel);
    @Accessor("pixels")
    long maparthelper$getPointer();
    @Accessor("size")
    long maparthelper$getCapacity();
}
