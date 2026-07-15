package rh.maparthelper.gui.painter.widget.overlay;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.CommonColors;
import org.jetbrains.annotations.NotNull;
import rh.maparthelper.MapartHelper;
import rh.maparthelper.gui.widget.input.AdjEditBox;
import rh.maparthelper.gui.widget.layout.AdjScrollableLayoutWidget;
import rh.maparthelper.gui.widget.layout.OverlayLayout;
import rh.maparthelper.painter.PainterProject;
import rh.maparthelper.painter.layer.DynamicTextureLayer;
import rh.maparthelper.painter.layer.Layer;
import rh.maparthelper.painter.surface.NativeImageSurface;

import java.util.function.Consumer;
import java.util.function.Supplier;

//? >=1.21.10
//import net.minecraft.client.input.MouseButtonEvent;

public class LayerPropertiesOverlay extends OverlayLayout {
    private final Screen screen;
    private Layer<?> layer;
    private String layerName;
    private float layerAlpha;

    private AdjEditBox labelNameField;
    private SliderOption opacitySlider;

    public LayerPropertiesOverlay(Screen screen,
                                  @NotNull PainterProject<NativeImageSurface, DynamicTextureLayer> painterProject) {
        super(false, false);
        this.screen = screen;
        this.layer = painterProject.getLayerManager().getSelectedLayer();
        this.layerName = layer.getName();
        this.layerAlpha = layer.getAlpha();
        lazyInitLayout(initContent());
        setVisible(false);
        setPosition((screen.width - getWidth()) / 2, (screen.height - getHeight()) / 2);
    }

    private AdjScrollableLayoutWidget initContent() {
        // TODO: Localize
        LinearLayout content = LinearLayout.vertical().spacing(-8);
        content.defaultCellSetting().padding(4);
        content.addChild(labelWidget(
                        Component.literal("Layer properties").withColor(CommonColors.LIGHTER_GRAY)),
                content.newCellSettings().paddingBottom(10)
        );

        content.addChild(labelWidget(Component.literal("Name:")));
        labelNameField = new AdjEditBox(
                screen.getFont(),
                100, 16,
                layer.getName(), false
        );
        labelNameField.setValueConsumer(name -> layer.setName(name));
        content.addChild(labelNameField, content.newCellSettings().paddingBottom(8));

        content.addChild(labelWidget(Component.literal("Opacity:")));
        opacitySlider = new SliderOption(
                100, 16,
                layer.getAlpha(),
                d -> layer.setAlpha(d.floatValue())
        );
        content.addChild(opacitySlider, content.newCellSettings().paddingBottom(8));
        LinearLayout visibilitySetting = LinearLayout.horizontal().spacing(2);
        visibilitySetting.defaultCellSetting().alignVerticallyMiddle();
        visibilitySetting.addChild(labelWidget(
                Component.literal("Visible:")
        ));
        visibilitySetting.addChild(new TickBoxOption(
                () -> layer.isVisible(),
                b -> layer.setVisible(b)
        ));
        content.addChild(visibilitySetting);

        content.arrangeElements();

        AdjScrollableLayoutWidget scrollable = new AdjScrollableLayoutWidget(content, content.getHeight());
        scrollable.setWidth(content.getWidth());
        scrollable.setBackgroundColor(0xAA_343434);
        scrollable.setOutlineColor(ARGB.color(0.3f, -1));
        scrollable.setScrollBarWidth(0);
        return scrollable;
    }

    private StringWidget labelWidget(Component text) {
        return new StringWidget(text, screen.getFont());
    }

    public void setLayer(Layer<?> layer) {
        this.layer = layer;
        updateValues();
    }

    public void updateValues() {
        this.layerName = layer.getName();
        this.layerAlpha = layer.getAlpha();
        labelNameField.setValue(layerName);
        opacitySlider.setValue(layerAlpha);
    }

    private static class SliderOption extends AbstractSliderButton {
        private final Consumer<Double> setter;

        public SliderOption(int width, int height, float initValue, Consumer<Double> setter) {
            super(0, 0, width, height,
                    Component.literal(String.format("%d%%", (int) (initValue * 100))), initValue);
            this.setter = setter;
        }

        protected void setValue(double value) {
            super.setValue(value);
        }

        @Override
        protected void updateMessage() {
            setMessage(Component.literal(String.format("%d%%", (int) (value * 100))));
        }

        @Override
        protected void applyValue() {
            setter.accept(value);
        }
    }

    private static class TickBoxOption extends AbstractWidget {
        private static final Identifier ENABLE_ICON = MapartHelper.identifier(
                "textures/gui/icons/tickbox_enable.png"
        );
        private static final Identifier DISABLE_ICON = MapartHelper.identifier(
                "textures/gui/icons/tickbox_disable.png"
        );

        private final Supplier<Boolean> getter;
        private final Consumer<Boolean> setter;

        public TickBoxOption(Supplier<Boolean> getter, Consumer<Boolean> setter) {
            super(0, 0, 16, 16, Component.empty());
            this.getter = getter;
            this.setter = setter;
        }

        //~ gui_rendering
        @Override
        protected void renderWidget(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            //? >=1.21.11
            //this.handleCursor(graphics);
            graphics.blit(
                    RenderPipelines.GUI_TEXTURED,
                    getter.get() ? ENABLE_ICON : DISABLE_ICON,
                    getX(), getY(),
                    0.0f, 0.0f,
                    16, 16,
                    16, 16
            );
        }
        //~ !gui_rendering

        //~ widget_events
        @Override
        public void onClick(double mouseX, double mouseY) {
            setter.accept(!getter.get());
        }
        //~ !widget_events

        @Override
        protected void updateWidgetNarration(@NotNull NarrationElementOutput narrationElementOutput) {}
    }
}
