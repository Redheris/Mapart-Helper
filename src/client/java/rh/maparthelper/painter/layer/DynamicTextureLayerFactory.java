package rh.maparthelper.painter.layer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import rh.maparthelper.MapartHelper;
import rh.maparthelper.painter.surface.NativeImageSurface;

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

    @Override
    public DynamicTextureLayer copy(DynamicTextureLayer origin) {
        String layerUUID = UUID.randomUUID().toString();

        Identifier textureId = MapartHelper.identifier("painter_layer_" + layerUUID);
        DynamicTexture texture = new DynamicTexture(textureId::getPath, origin.texture.getPixels());
        Minecraft.getInstance().getTextureManager().register(textureId, texture);

        DynamicTextureLayer newLayer = new DynamicTextureLayer(texture, textureId);
        newLayer.setAlpha(origin.getAlpha());
        newLayer.setVisible(origin.isVisible());
        newLayer.setName(origin.getName());

        return newLayer;
    }

    @Override
    public DynamicTextureLayer merge(DynamicTextureLayer layerAbove, DynamicTextureLayer layerBelow) {
        DynamicTextureLayer layerMerged = copy(layerBelow);
        NativeImageSurface surfaceMerged = layerMerged.getSurface();
        NativeImageSurface surfaceAbove = layerAbove.getSurface();

        int width = surfaceMerged.getWidth();
        int height = surfaceMerged.getHeight();

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int colorAbove = surfaceAbove.getPixel(x, y);
                if (colorAbove == 0) continue;
                surfaceMerged.setPixel(x, y, colorAbove);
            }
        }
        layerMerged.texture.upload();

        return layerMerged;
    }
}
