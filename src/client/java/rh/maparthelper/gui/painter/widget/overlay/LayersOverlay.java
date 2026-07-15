package rh.maparthelper.gui.painter.widget.overlay;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.layouts.SpacerElement;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.CommonColors;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector4i;
import rh.maparthelper.MapartHelper;
import rh.maparthelper.gui.widget.DecorativeButtonWidget;
import rh.maparthelper.gui.widget.NativeImageViewWidget;
import rh.maparthelper.gui.widget.layout.AdjScrollableLayoutWidget;
import rh.maparthelper.gui.widget.layout.OverlayLayout;
import rh.maparthelper.painter.PainterProject;
import rh.maparthelper.painter.layer.DynamicTextureLayer;
import rh.maparthelper.painter.layer.LayerManager;
import rh.maparthelper.painter.surface.NativeImageSurface;
import rh.maparthelper.state.fullscreen_view.NativeImageViewState;
import rh.maparthelper.util.RenderUtils;

import java.util.List;

//? >=1.21.10
//import net.minecraft.client.input.MouseButtonEvent;

public class LayersOverlay extends OverlayLayout {
    private static final Identifier NEW_LAYER_ICON = MapartHelper.identifier(
            "textures/gui/icons/painter/new_layer.png"
    );
    private static final Identifier REMOVE_LAYER_ICON = MapartHelper.identifier(
            "textures/gui/icons/painter/remove_layer.png"
    );
    private static final Identifier REMOVE_LAYER_DISABLED_ICON = MapartHelper.identifier(
            "textures/gui/icons/painter/remove_layer_disabled.png"
    );
    private static final Identifier DUPLICATE_LAYER_ICON = MapartHelper.identifier(
            "textures/gui/icons/painter/copy_layer.png"
    );
    private static final Identifier MERGE_LAYER_DOWN_ICON = MapartHelper.identifier(
            "textures/gui/icons/painter/merge_down_layer.png"
    );
    private static final Identifier MERGE_LAYER_DOWN_DISABLED_ICON = MapartHelper.identifier(
            "textures/gui/icons/painter/merge_down_layer_disabled.png"
    );
    private static final Identifier MOVE_LAYER_UP_ICON = MapartHelper.identifier(
            "textures/gui/icons/painter/move_up_layer.png"
    );
    private static final Identifier MOVE_LAYER_UP_DISABLED_ICON = MapartHelper.identifier(
            "textures/gui/icons/painter/move_up_layer_disabled.png"
    );
    private static final Identifier MOVE_LAYER_DOWN_ICON = MapartHelper.identifier(
            "textures/gui/icons/painter/move_down_layer.png"
    );
    private static final Identifier MOVE_LAYER_DOWN_DISABLED_ICON = MapartHelper.identifier(
            "textures/gui/icons/painter/move_down_layer_disabled.png"
    );
    private static final Identifier LAYER_PROPERTIES_ICON = MapartHelper.identifier(
            "textures/gui/icons/painter/layer_properties.png"
    );

    private DecorativeButtonWidget removeLayerBtn;
    private DecorativeButtonWidget mergeLayerDownBtn;
    private DecorativeButtonWidget moveLayerUpBtn;
    private DecorativeButtonWidget moveLayerDownBtn;

    private final Screen screen;
    private final PainterProject<NativeImageSurface, DynamicTextureLayer> painterProject;
    private final LayerManager<NativeImageSurface, DynamicTextureLayer> layerManager;
    private final LayerPropertiesOverlay layerPropertiesOverlay;
    private final int maxHeight;

    public LayersOverlay(@NotNull Screen screen, @NotNull PainterProject<NativeImageSurface, DynamicTextureLayer> painterProject,
                         LayerPropertiesOverlay layerPropertiesOverlay, int y, int margin
    ) {
        super(true, false);

        this.screen = screen;
        this.painterProject = painterProject;
        this.layerManager = painterProject.getLayerManager();
        this.layerPropertiesOverlay = layerPropertiesOverlay;
        this.maxHeight = Math.min(250, screen.height - y - margin);

        AdjScrollableLayoutWidget layout = initContent();
        lazyInitLayout(layout);
        this.setPosition(screen.width - layout.getWidth() - margin, y);
    }

    public void rebuildContent() {
        replaceLayout(screen, initContent());
    }

    private AdjScrollableLayoutWidget initContent() {
        int overlayWidth = 128;

        LinearLayout content = LinearLayout.vertical();
        content.defaultCellSetting().alignHorizontallyCenter();
        content.addChild(SpacerElement.width(overlayWidth));

        LinearLayout buttons = createButtonsLayout(screen, painterProject);

        LinearLayout layersList = LinearLayout.vertical();
        layersList.defaultCellSetting().alignHorizontallyCenter();

        var layerManager = painterProject.getLayerManager();
        List<DynamicTextureLayer> layers = layerManager.getLayers();

        NativeImageSurface surface = layerManager.getSelectedLayer().getSurface();
        int imageWidth = surface.getWidth();
        int imageHeight = surface.getHeight();

        for (int i = layers.size() - 1; i >= 0; i--) {
            layersList.addChild(
                    new LayerPreview(
                            layerManager, layers.get(i),
                            overlayWidth, 50,
                            46, 46,
                            imageWidth, imageHeight
                    ),
                    layersList.newCellSettings().paddingHorizontal(2)
            );
        }
        layersList.arrangeElements();
        if (layersList.getHeight() < maxHeight - buttons.getHeight()) {
            layersList.addChild(SpacerElement.height(maxHeight - buttons.getHeight() - layersList.getHeight()));
        }

        AdjScrollableLayoutWidget layersScrollable = new AdjScrollableLayoutWidget(layersList, maxHeight - buttons.getHeight());
        layersScrollable.setScrollBarWidth(4);
        layersScrollable.arrangeElements();

        content.addChild(layersScrollable);
        content.addChild(buttons);

        AdjScrollableLayoutWidget scrollable = new AdjScrollableLayoutWidget(content, maxHeight);
        scrollable.setBackgroundColor(0xAA_343434);
        scrollable.setOutlineColor(ARGB.color(0.3f, -1));
        scrollable.setScrollBarWidth(0);
        scrollable.arrangeElements();

        return scrollable;
    }

    private LinearLayout createButtonsLayout(Screen screen, PainterProject<NativeImageSurface, DynamicTextureLayer> painterProject) {
        LayerManager<NativeImageSurface, DynamicTextureLayer> layerManager = painterProject.getLayerManager();
        LinearLayout buttons = LinearLayout.horizontal().spacing(2);

        var createNewLayerBtn = DecorativeButtonWidget.builderSimpleTexture(
                NEW_LAYER_ICON,
                btn -> {
                    NativeImageSurface surface = layerManager.getSelectedLayer().getSurface();
                    layerManager.createEmptyLayer(
                            surface.getWidth(),
                            surface.getHeight()
                    );
                    replaceLayout(screen, initContent());
                    if (layerPropertiesOverlay.isVisible()) {
                        layerPropertiesOverlay.setLayer(layerManager.getSelectedLayer());
                    }
                    updateButtonsActiveState();
                }
        ).size(16, 16).build();
        removeLayerBtn = DecorativeButtonWidget.builder(
                REMOVE_LAYER_ICON,
                REMOVE_LAYER_DISABLED_ICON,
                btn -> {
                    DynamicTextureLayer layer = layerManager.getSelectedLayer();
                    layerManager.removeLayer(layer);
                    replaceLayout(screen, initContent());
                    if (layerPropertiesOverlay.isVisible()) {
                        layerPropertiesOverlay.setLayer(layerManager.getSelectedLayer());
                    }
                    updateButtonsActiveState();
                }
        ).size(16, 16).build();
        var duplicateLayerBtn = DecorativeButtonWidget.builderSimpleTexture(
                DUPLICATE_LAYER_ICON,
                btn -> {
                    layerManager.copyLayer(layerManager.getSelectedLayer());
                    replaceLayout(screen, initContent());
                    if (layerPropertiesOverlay.isVisible()) {
                        layerPropertiesOverlay.setLayer(layerManager.getSelectedLayer());
                    }
                    updateButtonsActiveState();
                }
        ).size(16, 16).build();
        mergeLayerDownBtn = DecorativeButtonWidget.builder(
                MERGE_LAYER_DOWN_ICON,
                MERGE_LAYER_DOWN_DISABLED_ICON,
                btn -> {
                    DynamicTextureLayer layer = layerManager.getSelectedLayer();
                    layerManager.mergeLayerWithBelow(layer);
                    replaceLayout(screen, initContent());
                    if (layerPropertiesOverlay.isVisible()) {
                        layerPropertiesOverlay.setLayer(layerManager.getSelectedLayer());
                    }
                    updateButtonsActiveState();
                }
        ).size(16, 16).build();
        moveLayerUpBtn = DecorativeButtonWidget.builder(
                MOVE_LAYER_UP_ICON,
                MOVE_LAYER_UP_DISABLED_ICON,
                btn -> {
                    layerManager.moveLayerUp(layerManager.getSelectedLayer());
                    replaceLayout(screen, initContent());
                    updateButtonsActiveState();
                }
        ).size(16, 16).build();
        moveLayerDownBtn = DecorativeButtonWidget.builder(
                MOVE_LAYER_DOWN_ICON,
                MOVE_LAYER_DOWN_DISABLED_ICON,
                btn -> {
                    layerManager.moveLayerDown(layerManager.getSelectedLayer());
                    replaceLayout(screen, initContent());
                    updateButtonsActiveState();
                }
        ).size(16, 16).build();
        var layerPropertiesBtn = DecorativeButtonWidget.builderSimpleTexture(
                LAYER_PROPERTIES_ICON,
                btn -> {
                    DynamicTextureLayer layer = layerManager.getSelectedLayer();
                    layerPropertiesOverlay.setLayer(layer);
                    layerPropertiesOverlay.setPosition(
                            getX() - layerPropertiesOverlay.getWidth() - 1,
                            getY() + (height - layerPropertiesOverlay.getHeight()) / 2
                    );
                    layerPropertiesOverlay.setVisible(!layerPropertiesOverlay.isVisible());
                }
        ).size(16, 16).build();

        // TODO: Localize
        createNewLayerBtn.setTooltip(Tooltip.create(
                Component.literal("Create new layer")
        ));
        removeLayerBtn.setTooltip(Tooltip.create(
                Component.literal("Remove layer")
        ));
        duplicateLayerBtn.setTooltip(Tooltip.create(
                Component.literal("Duplicate layer")
        ));
        mergeLayerDownBtn.setTooltip(Tooltip.create(
                Component.literal("Merge layer down")
        ));
        moveLayerUpBtn.setTooltip(Tooltip.create(
                Component.literal("Move layer up")
        ));
        moveLayerDownBtn.setTooltip(Tooltip.create(
                Component.literal("Move layer down")
        ));
        layerPropertiesBtn.setTooltip(Tooltip.create(
                Component.literal("Layer properties")
        ));

        buttons.addChild(createNewLayerBtn);
        buttons.addChild(removeLayerBtn);
        buttons.addChild(duplicateLayerBtn);
        buttons.addChild(mergeLayerDownBtn);
        buttons.addChild(moveLayerUpBtn);
        buttons.addChild(moveLayerDownBtn);
        buttons.addChild(layerPropertiesBtn);
        buttons.arrangeElements();

        updateButtonsActiveState();
        return buttons;
    }

    private void updateButtonsActiveState() {
        DynamicTextureLayer layer = layerManager.getSelectedLayer();
        boolean isTopLayer = layerManager.isTopLayer(layer);
        boolean isBottomLayer = layerManager.isBottomLayer(layer);

        removeLayerBtn.active = !isTopLayer || !isBottomLayer;
        mergeLayerDownBtn.active = !isBottomLayer;
        moveLayerUpBtn.active = !isTopLayer;
        moveLayerDownBtn.active = !isBottomLayer;
    }

    private class LayerPreview extends AbstractWidget {
        private final LayerManager<NativeImageSurface, DynamicTextureLayer> layerManager;
        private final DynamicTextureLayer layer;
        private final int maxTextureWidth;
        private final int maxTextureHeight;
        private final int fittedImageWidth;
        private final int fittedImageHeight;
        private final int fittedImageXOffset;
        private final int fittedImageYOffset;

        public LayerPreview(LayerManager<NativeImageSurface, DynamicTextureLayer> layerManager, DynamicTextureLayer layer,
                            int width, int height, int maxTextureWidth, int maxTextureHeight, int imageWidth, int imageHeight
        ) {
            super(0, 0, width, height, Component.empty());
            this.layerManager = layerManager;
            this.layer = layer;
            this.maxTextureWidth = maxTextureWidth;
            this.maxTextureHeight = maxTextureHeight;

            Vector4i fitted = NativeImageViewState.fitImage(maxTextureWidth, maxTextureHeight, imageWidth, imageHeight);
            this.fittedImageWidth = fitted.x();
            this.fittedImageHeight = fitted.y();
            this.fittedImageXOffset = fitted.z();
            this.fittedImageYOffset = fitted.w();
        }

        //~ gui_rendering
        @Override
        protected void renderWidget(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            if (layerManager.getSelectedLayer() == layer) {
                graphics.fill(
                        getX() - 60, getY(),
                        getRight() + 60, getBottom(),
                        ARGB.color(0.4f * alpha, 0x5555ff)
                );
            }
            int heightOffset = (height - maxTextureHeight) / 2;
            graphics.blit(
                    RenderPipelines.GUI_TEXTURED,
                    NativeImageViewWidget.TRANSPARENT_TEXTURE,
                    getX() + fittedImageXOffset, getY() + fittedImageYOffset + heightOffset,
                    0, 0,
                    fittedImageWidth, fittedImageHeight,
                    fittedImageWidth / 4, fittedImageHeight / 4
            );
            graphics.blit(
                    RenderPipelines.GUI_TEXTURED,
                    layer.getTextureId(),
                    getX() + fittedImageXOffset, getY() + fittedImageYOffset + heightOffset,
                    0, 0,
                    fittedImageWidth, fittedImageHeight,
                    fittedImageWidth, fittedImageHeight
            );
            RenderUtils.renderOutline(
                    graphics,
                    getX() + fittedImageXOffset - 1, getY() + fittedImageYOffset + heightOffset - 1,
                    fittedImageWidth + 2, fittedImageHeight + 2,
                    ARGB.color(alpha, CommonColors.GRAY)
            );

            graphics.enableScissor(getX(), getY(), getRight(), getBottom());
            //~ if >=26.1 'drawWordWrap' -> 'textWithWordWrap' >> '('
            graphics.drawWordWrap(
                    Minecraft.getInstance().font, FormattedText.of(layer.getName()),
                    getX() + maxTextureWidth + 2, getY() + height / 3 - 4,
                    width - maxTextureWidth - 2,
                    layer.isVisible() ? -1 : 0xffa0a0a0
            );
            graphics.disableScissor();

        }
        //~ !gui_rendering

        //~ widget_events
        @Override
        public void onClick(double mouseX, double mouseY) {
            layerManager.setSelectedLayer(layer);
            if (layerPropertiesOverlay.isVisible()) {
                layerPropertiesOverlay.setLayer(layer);
            }
            updateButtonsActiveState();
        }
        //~ !widget_events

        @Override
        protected void updateWidgetNarration(@NotNull NarrationElementOutput narrationElementOutput) {}
    }
}
