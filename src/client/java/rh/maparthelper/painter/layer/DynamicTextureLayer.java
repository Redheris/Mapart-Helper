package rh.maparthelper.painter.layer;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
import rh.maparthelper.painter.surface.NativeImageSurface;

import java.awt.*;

public class DynamicTextureLayer extends Layer<NativeImageSurface> {
    @NotNull
    private final DynamicTexture texture;
    @NotNull
    private final Identifier textureId;

    public DynamicTextureLayer(@NotNull DynamicTexture texture, @NotNull Identifier textureId) {
        super(fromDynamicTexture(texture));
        this.texture = texture;
        this.textureId = textureId;
    }

    public @NotNull DynamicTexture getTexture() {
        return texture;
    }

    public @NotNull Identifier getTextureId() {
        return textureId;
    }

    private static NativeImageSurface fromDynamicTexture(DynamicTexture texture) {
        if (texture == null) {
            throw new IllegalStateException("Layer's dynamic texture must not be null");
        }
        //? if <26.2 {
        if (texture.getPixels() == null) {
            throw new IllegalStateException("Pixel surface must not be null");
        }
        //?}
        return new NativeImageSurface(texture.getPixels());
    }

    @Override
    protected void uploadDirtyArea(Rectangle dirtyArea) {
        if (isDirty()) {
            //? if >=26.2 {
            /*NativeImage dirtySubimage = new NativeImage(dirtyArea.width, dirtyArea.height, false);
            texture.getPixels().copyRect(
                    dirtySubimage,
                    dirtyArea.x, dirtyArea.y,
                    0, 0,
                    dirtyArea.width, dirtyArea.height,
                    false, false
            );
            RenderSystem.getDevice().createCommandEncoder().writeToTexture(
                    texture.getTexture(),
                    dirtySubimage,
                    0,
                    0,
                    dirtyArea.x,
                    dirtyArea.y
            );
            *///?} else {
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
            //?}
        }
    }

    @Override
    public void dispose() {
        TextureManager textureManager = Minecraft.getInstance().getTextureManager();
        textureManager.release(textureId);
    }
}
