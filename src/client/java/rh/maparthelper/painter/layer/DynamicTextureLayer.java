package rh.maparthelper.painter.layer;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
import rh.maparthelper.painter.surface.NativeImageSurface;

import java.awt.*;

public class DynamicTextureLayer extends Layer<NativeImageSurface> {
    private final DynamicTexture texture;
    private final Identifier textureId;

    public DynamicTextureLayer(@NotNull DynamicTexture texture, @NotNull Identifier textureId) {
        super(fromDynamicTexture(texture));
        this.texture = texture;
        this.textureId = textureId;
    }

    public Identifier getTextureId() {
        return textureId;
    }

    private static NativeImageSurface fromDynamicTexture(DynamicTexture texture) {
        if (texture == null) {
            throw new IllegalStateException("Layer's dynamic texture must not be null");
        } else if (texture.getPixels() == null) {
            throw new IllegalStateException("Pixel surface must not be null");
        }
        return new NativeImageSurface(texture.getPixels());
    }

    @Override
    protected void uploadDirtyArea(Rectangle dirtyArea) {
        if (isDirty()) {
            RenderSystem.getDevice().createCommandEncoder().writeToTexture(
                    texture.getTexture(),
                    texture.getPixels(),
                    0,
                    0,
                    dirtyArea.x,
                    dirtyArea.y,
                    dirtyArea.width,
                    dirtyArea.height,
                    dirtyArea.x,
                    dirtyArea.y
            );
        }
    }
}
