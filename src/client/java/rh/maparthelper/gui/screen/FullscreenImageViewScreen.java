package rh.maparthelper.gui.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.layouts.SpacerElement;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import org.jetbrains.annotations.NotNull;
import rh.maparthelper.MapartHelper;
import rh.maparthelper.conversion.CurrentConversionSettings;
import rh.maparthelper.gui.widget.DecorativeButtonWidget;
import rh.maparthelper.gui.widget.NativeImageViewWidget;
import rh.maparthelper.mapart.MapartProcessing;

//? >=1.21.10 {
/*import net.minecraft.client.input.MouseButtonEvent;
*///?}

public class FullscreenImageViewScreen extends ScreenAdapted {
    private static final Identifier RESET_OFFSET_TEXTURE = Identifier.fromNamespaceAndPath(MapartHelper.MOD_ID, "textures/gui/icons/fit_image_center.png");

    protected final MapartProcessing mapart = CurrentConversionSettings.mapart;

    private NativeImageViewWidget imageViewWidget;
    private StringWidget pixelPosLabel;

    protected FullscreenImageViewScreen(Screen parent) {
        super(parent, Component.literal("Full size mapart view"));
    }

    @Override
    protected void initContent() {
        LinearLayout header = LinearLayout.horizontal().spacing(2);
        header.addChild(SpacerElement.height(30));
        header.defaultCellSetting().alignVerticallyMiddle();

        DecorativeButtonWidget resetOffsetBtn = DecorativeButtonWidget.builder(
                RESET_OFFSET_TEXTURE,
                btn -> imageViewWidget.resetScaleAndOffset()
        ).size(16, 16).build();
        resetOffsetBtn.setTooltip(Tooltip.create(Component.translatable("maparthelper.gui.fullscreen_view.reset_scale_and_offset")));

        pixelPosLabel = new StringWidget(80, 9, Component.literal("(0, 0)"), font);
        //? <= 1.21.8
        pixelPosLabel.alignLeft();

        header.addChild(resetOffsetBtn);
        header.addChild(pixelPosLabel);
        header.arrangeElements();
        header.visitWidgets(this::addRenderableWidget);

        imageViewWidget = new NativeImageViewWidget(
                CurrentConversionSettings.guiMapartImage, CurrentConversionSettings.guiMapartId,
                0, 30, width, height - 30
        );

        this.addRenderableWidget(imageViewWidget);
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        super.mouseMoved(mouseX, mouseY);
        if (pixelPosLabel != null) {
            pixelPosLabel.setMessage(Component.literal(imageViewWidget.pixelPosString()));
        }
    }

    //~ widget_events
    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        return this.getFocused() != null && this.getFocused().mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }
    //~ !widget_events

    //~ gui_rendering
    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(0, 0, width, 30, ARGB.color(0.7f, 0xFF222222));
        graphics.hLine(0, width, 29, ARGB.color(0.3f, -1));
        super.render(graphics, mouseX, mouseY, partialTick);
    }
    //~ !gui_rendering
}
