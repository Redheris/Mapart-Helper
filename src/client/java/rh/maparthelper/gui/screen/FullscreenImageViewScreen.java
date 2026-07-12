package rh.maparthelper.gui.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.layouts.SpacerElement;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import org.jetbrains.annotations.NotNull;
import rh.maparthelper.MapartHelper;
import rh.maparthelper.conversion.CurrentConversionSettings;
import rh.maparthelper.gui.widget.DecorativeButtonWidget;
import rh.maparthelper.gui.widget.NativeImageViewWidget;

//? >=1.21.10 {
/*import net.minecraft.client.input.MouseButtonEvent;
*///?}

public class FullscreenImageViewScreen extends ScreenAdapted {
    private static final Identifier RESET_OFFSET_ICON = MapartHelper.identifier("textures/gui/icons/fit_image_center.png");
    private static final Identifier MAP_GRID_ICON = MapartHelper.identifier("textures/gui/icons/grid.png");
    private static final Identifier PIXEL_GRID_ICON = MapartHelper.identifier("textures/gui/icons/fine_grid.png");

    private NativeImageViewWidget imageViewWidget;
    private StringWidget pixelPosLabel;

    public FullscreenImageViewScreen() {
        super(null, Component.literal("Full size mapart view"));
//        ActiveModScreenManager.getInstance().setActiveModScreen(ActiveModScreenManager.ModScreen.FULLSCREEN_VIEW);
    }

    protected NativeImageViewWidget initImageViewWidget() {
        return new NativeImageViewWidget(
                CurrentConversionSettings.guiMapartImage, CurrentConversionSettings.guiMapartId,
                0, 30, width, height - 30
        );
    }

    @Override
    protected void initContent() {
        this.imageViewWidget = initImageViewWidget();
        this.addRenderableWidget(imageViewWidget);

        GridLayout header = new GridLayout(0, 0);
        GridLayout.RowHelper headerAdder = header.createRowHelper(3);
        headerAdder.addChild(SpacerElement.width(width), 3);
        headerAdder.addChild(SpacerElement.width(width / 3));
        headerAdder.addChild(SpacerElement.width(width / 3));
        headerAdder.addChild(SpacerElement.width(width / 3));
        header.defaultCellSetting().alignVerticallyMiddle();

        LinearLayout headerLeft = headerLeft();
        LinearLayout headerCenter = headerCenter();
        LinearLayout headerRight = headerRight();

        headerAdder.addChild(headerLeft, header.newCellSettings().alignHorizontallyLeft());
        headerAdder.addChild(headerCenter, header.newCellSettings().alignHorizontallyCenter());
        headerAdder.addChild(headerRight, header.newCellSettings().alignHorizontallyRight());
        header.arrangeElements();
        header.visitWidgets(this::addRenderableWidget);
    }

    protected LinearLayout headerLeft() {
        LinearLayout headerLeft = LinearLayout.horizontal().spacing(2);
        headerLeft.addChild(Button.builder(
                        Component.translatable("maparthelper.gui.close"),
                        btn -> Minecraft.getInstance().setScreen(new MapartEditorScreen())
                ).size(60, 20).build()
        );

        return headerLeft;
    }

    protected LinearLayout headerCenter() {
        LinearLayout headerCenter = LinearLayout.horizontal().spacing(2);
        headerCenter.addChild(SpacerElement.height(30));
        headerCenter.defaultCellSetting().alignVerticallyMiddle().alignHorizontallyRight();

        DecorativeButtonWidget showPixelGridBtn = DecorativeButtonWidget.builderSimpleTexture(
                PIXEL_GRID_ICON,
                btn -> {
                    imageViewWidget.setShowPixelGrid(!imageViewWidget.isShowPixelGrid());
                    btn.setTextureColor(imageViewWidget.isShowPixelGrid() ? 0xFF_55ffff : -1);
                }
        ).size(16, 16).build();
        showPixelGridBtn.setTextureColor(imageViewWidget.isShowPixelGrid() ? 0xFF_55ffff : -1);
        showPixelGridBtn.setTooltip(Tooltip.create(Component.translatable("maparthelper.gui.fullscreen_view.show_pixel_grid")));
        headerCenter.addChild(showPixelGridBtn);

        DecorativeButtonWidget showMapGridBtn = DecorativeButtonWidget.builderSimpleTexture(
                MAP_GRID_ICON,
                btn -> {
                    imageViewWidget.setShowMapGrid(!imageViewWidget.isShowMapGrid());
                    btn.setTextureColor(imageViewWidget.isShowMapGrid() ? 0xFF_55ffff : -1);
                }
        ).size(16, 16).build();
        showMapGridBtn.setTextureColor(imageViewWidget.isShowMapGrid() ? 0xFF_55ffff : -1);
        showMapGridBtn.setTooltip(Tooltip.create(Component.translatable("maparthelper.gui.fullscreen_view.show_map_grid")));
        headerCenter.addChild(showMapGridBtn);

        DecorativeButtonWidget resetOffsetBtn = DecorativeButtonWidget.builderSimpleTexture(
                RESET_OFFSET_ICON,
                btn -> imageViewWidget.resetScaleAndOffset()
        ).size(16, 16).build();
        resetOffsetBtn.setTooltip(Tooltip.create(Component.translatable("maparthelper.gui.fullscreen_view.reset_scale_and_offset")));

        pixelPosLabel = new StringWidget(80, 9, Component.literal("(0, 0)"), font);
        //? <= 1.21.8
        pixelPosLabel.alignLeft();

        headerCenter.addChild(resetOffsetBtn);
        headerCenter.addChild(pixelPosLabel);

        return headerCenter;
    }

    protected LinearLayout headerRight() {
        LinearLayout headerRight = LinearLayout.horizontal().spacing(2);
        headerRight.defaultCellSetting().alignVerticallyMiddle().alignHorizontallyRight();

        return headerRight;
    }

    private void updatePixelPosLabel() {
        pixelPosLabel.setMessage(Component.literal(imageViewWidget.pixelPosString()));
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        super.mouseMoved(mouseX, mouseY);
        imageViewWidget.mouseMoved(mouseX, mouseY);
        updatePixelPosLabel();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        boolean result = super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
        updatePixelPosLabel();
        return result;
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
        if (!Minecraft.getInstance().isWindowActive()) {
            pixelPosLabel.setMessage(Component.literal(imageViewWidget.pixelPosString()));
        }
        graphics.fill(0, 0, width, 30, ARGB.color(0.7f, 0xFF222222));
        graphics.hLine(0, width, 29, ARGB.color(0.3f, -1));
        super.render(graphics, mouseX, mouseY, partialTick);
    }
    //~ !gui_rendering
}
