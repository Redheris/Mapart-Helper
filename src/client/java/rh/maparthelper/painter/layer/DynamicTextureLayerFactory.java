package rh.maparthelper.painter.layer;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
import rh.maparthelper.MapartHelper;
import rh.maparthelper.painter.surface.NativeImageSurface;

import java.util.List;
import java.util.UUID;

public class DynamicTextureLayerFactory implements LayerFactory<NativeImageSurface, DynamicTextureLayer> {

    @Override
    public DynamicTextureLayer createEmpty(int width, int height, int layerNumber) {
        String layerUUID = UUID.randomUUID().toString();

        Identifier textureId = MapartHelper.identifier("painter_layer_" + layerUUID);
        DynamicTexture texture = new DynamicTexture(textureId.getPath(), width, height, true);
        Minecraft.getInstance().getTextureManager().register(textureId, texture);
        texture.upload();

        var newLayer = new DynamicTextureLayer(texture, textureId);
        newLayer.setName(
                Component.translatable("maparthelper.gui.mapart_painter.layer").getString() + " " + layerNumber
        );

        return newLayer;
    }

    public DynamicTextureLayer createFromImage(@NotNull NativeImage nativeImage, String layerName) {
        String layerUUID = UUID.randomUUID().toString();

        NativeImage layerImage = new NativeImage(nativeImage.getWidth(), nativeImage.getHeight(), true);
        layerImage.copyFrom(nativeImage);

        Identifier textureId = MapartHelper.identifier("painter_layer_" + layerUUID);
        DynamicTexture texture = new DynamicTexture(textureId::getPath, layerImage);
        Minecraft.getInstance().getTextureManager().register(textureId, texture);

        DynamicTextureLayer layer = new DynamicTextureLayer(texture, textureId);
        layer.setName(layerName);

        return layer;
    }

    @Override
    public DynamicTextureLayer copy(@NotNull DynamicTextureLayer origin) {
        String layerUUID = UUID.randomUUID().toString();

        NativeImage originImage = origin.getTexture().getPixels();
        if (originImage == null) throw new IllegalStateException("Native image must not be null");
        int width = originImage.getWidth();
        int height = originImage.getHeight();

        NativeImage newImage = new NativeImage(width, height, true);
        newImage.copyFrom(originImage);

        Identifier textureId = MapartHelper.identifier("painter_layer_" + layerUUID);
        DynamicTexture texture = new DynamicTexture(textureId::getPath, newImage);
        Minecraft.getInstance().getTextureManager().register(textureId, texture);

        DynamicTextureLayer newLayer = new DynamicTextureLayer(texture, textureId);
        newLayer.setAlpha(origin.getAlpha());
        newLayer.setVisible(origin.isVisible());
        newLayer.setName(origin.getName());

        return newLayer;
    }

    @Override
    public DynamicTextureLayer merge(@NotNull DynamicTextureLayer layerAbove, DynamicTextureLayer layerBelow) {
        DynamicTextureLayer layerMerged = copy(layerAbove);
        mergeLayerDown(layerMerged, layerBelow);
        layerMerged.getTexture().upload();
        return layerMerged;
    }

    @Override
    public DynamicTextureLayer flattenLayers(@NotNull List<DynamicTextureLayer> layers) {
        DynamicTextureLayer layerMerged = copy(layers.getLast());
        for (int i = layers.size() - 1; i >= 0; i--) {
            mergeLayerDown(layerMerged, layers.get(i));
        }
        layerMerged.getTexture().upload();
        return layerMerged;
    }

    private void mergeLayerDown(@NotNull DynamicTextureLayer layer, @NotNull DynamicTextureLayer layerBelow) {
        NativeImageSurface surfaceAbove = layer.getSurface();
        NativeImageSurface surfaceBelow = layerBelow.getSurface();

        for (int x = 0; x < surfaceAbove.getWidth(); x++) {
            for (int y = 0; y < surfaceAbove.getHeight(); y++) {
                int colorAbove = surfaceAbove.getPixel(x, y);
                if (colorAbove == 0) {
                    surfaceAbove.setPixel(x, y, surfaceBelow.getPixel(x, y));
                }
            }
        }
    }
}
