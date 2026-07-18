package rh.maparthelper.gui.painter.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.CommonColors;
import org.jetbrains.annotations.NotNull;
import rh.maparthelper.MapartHelper;
import rh.maparthelper.gui.painter.widget.overlay.SelectionModesDropdown;
import rh.maparthelper.gui.widget.DecorativeButtonWidget;
import rh.maparthelper.gui.widget.input.IntegerFieldWidget;
import rh.maparthelper.painter.drawing.DrawingEngine;
import rh.maparthelper.painter.drawing.tool.PainterTool;
import rh.maparthelper.painter.drawing.tool.ToolSettingsProvider;
import rh.maparthelper.painter.drawing.tool.settings.*;

import java.util.List;
import java.util.function.Consumer;

public class ToolSettingsLayoutBuilder {
    private static final Identifier LOCAL_FILL_ICON = MapartHelper.identifier(
            "textures/gui/icons/painter/flood_fill.png"
    );
    private static final Identifier GLOBAL_FILL_ICON = MapartHelper.identifier(
            "textures/gui/icons/painter/global_flood_fill.png"
    );
    private static final Identifier FIGURE_CIRCLE_ICON = MapartHelper.identifier(
            "textures/gui/icons/painter/figure_circle.png"
    );
    private static final Identifier FIGURE_RECTANGLE_ICON = MapartHelper.identifier(
            "textures/gui/icons/painter/figure_rectangle.png"
    );

    private final ToolSettingsProvider settingsProvider = ToolSettingsProvider.getInstance();
    private final SettingsBackground background = new SettingsBackground();

    private int minX;
    private int minY;
    private int maxX;
    private int maxY;
    private final List<AbstractWidget> selectionSettings;
    private final List<AbstractWidget> brushSettings;
    private final List<AbstractWidget> floodFillSettings;
    private final List<AbstractWidget> patternSettings;
    private SelectionModesDropdown selectionModesDropdown;

    public ToolSettingsLayoutBuilder(Screen screen) {
        this.selectionSettings = initSelectionSettingsWidgets(screen);
        this.brushSettings = initBrushSettingsWidgets();
        this.floodFillSettings = initFloodFillSettingsWidgets();
        this.patternSettings = initPatternSettingsWidgets();
    }

    public Renderable getBackground() {
        return background;
    }

    public SelectionModesDropdown getSelectionModesDropdown() {
        return selectionModesDropdown;
    }

    public void visitWidgets(Consumer<AbstractWidget> widgetVisitor) {
        selectionSettings.forEach(widgetVisitor);
        brushSettings.forEach(widgetVisitor);
        floodFillSettings.forEach(widgetVisitor);
        patternSettings.forEach(widgetVisitor);
    }

    public void arrangeWidget(Screen screen, DrawingEngine<?> drawingEngine) {
        visitWidgets(w -> w.visible = false);
        LinearLayout layout = LinearLayout.horizontal().spacing(2);
        layout.defaultCellSetting().alignHorizontallyCenter().alignVerticallyMiddle().paddingVertical(2);

        PainterTool selectedTool = drawingEngine.getSelectedTool();
        if (selectedTool instanceof SelectionBehavior) {
            addWidgetsToLayout(layout, selectionSettings);
        }
        if (selectedTool instanceof FloodFillBehavior) {
            addWidgetsToLayout(layout, floodFillSettings);
        }
        if (selectedTool instanceof BrushBehavior) {
            addWidgetsToLayout(layout, brushSettings);
        }

        layout.setY(30);
        layout.arrangeElements();
        layout.setX((screen.width - layout.getWidth()) / 2);
        minX = layout.getX();
        minY = layout.getY();
        maxX = minX + layout.getWidth();
        maxY = minY + layout.getHeight();
    }

    private void addWidgetsToLayout(LinearLayout layout, List<AbstractWidget> widgets) {
        for (var widget : widgets) {
            layout.addChild(widget);
            widget.visible = true;
        }
    }

    private List<AbstractWidget> initSelectionSettingsWidgets(Screen screen) {
        SelectionToolSettings selectionSettings = settingsProvider.SELECTION;
        selectionModesDropdown = new SelectionModesDropdown(screen, selectionSettings.getMode());
        selectionModesDropdown.setOverlayXOffset(-1);
        selectionModesDropdown.setOverlayYOffset(3);
        return List.of(selectionModesDropdown);
    }

    private List<AbstractWidget> initBrushSettingsWidgets() {
        BrushToolSettings brushSettings = settingsProvider.BRUSH;

        Component brushShapeLabel = Component.translatable("maparthelper.gui.mapart_painter.tools_settings.brush_shape")
                .withColor(CommonColors.LIGHT_GRAY);

        StringWidget thicknessLabel = new StringWidget(
                Component.translatable("maparthelper.gui.mapart_painter.tools_settings.brush_thickness"),
                Minecraft.getInstance().font
        );
        var thicknessField = new IntegerFieldWidget(
                Minecraft.getInstance().font, 35, 16, brushSettings.getThickness(), 1, 100
        );
        thicknessField.setIntegerValueConsumer(brushSettings::setThickness);

        Component circleShape = Component.translatable("maparthelper.gui.mapart_painter.tools_settings.brush_shape.circle")
                .withColor(-1);
        Component rectangleShape = Component.translatable("maparthelper.gui.mapart_painter.tools_settings.brush_shape.square")
                .withColor(-1);
        var brushShapeBtn = DecorativeButtonWidget.builderSimpleTexture(
                brushSettings.isCircleShape() ? FIGURE_CIRCLE_ICON : FIGURE_RECTANGLE_ICON,
                btn -> {
                    brushSettings.setCircleShape(!brushSettings.isCircleShape());
                    Identifier icon = brushSettings.isCircleShape() ? FIGURE_CIRCLE_ICON : FIGURE_RECTANGLE_ICON;
                    btn.setCustomSprites(new WidgetSprites(icon, icon));
                    btn.setTooltip(Tooltip.create(
                            brushShapeLabel.copy().append(brushSettings.isCircleShape() ? circleShape : rectangleShape))
                    );
                }
        ).size(16, 16).textColorActive(-1).build();
        brushShapeBtn.setTooltip(Tooltip.create(
                brushShapeLabel.copy().append(brushSettings.isCircleShape() ? circleShape : rectangleShape))
        );

        return List.of(thicknessLabel, thicknessField, brushShapeBtn);
    }

    private List<AbstractWidget> initFloodFillSettingsWidgets() {
        FloodFillSettings floodFillSettings = settingsProvider.FLOOD_FILL;

        Component fillModeLabel = Component.translatable("maparthelper.gui.mapart_painter.tools_settings.fill_mode")
                .withColor(CommonColors.LIGHT_GRAY);

        Component localMode = Component.translatable("maparthelper.gui.mapart_painter.tools_settings.fill_mode.local")
                .withColor(-1);
        Component globalMode = Component.translatable("maparthelper.gui.mapart_painter.tools_settings.fill_mode.global")
                .withColor(-1);
        var globalFillBtn = DecorativeButtonWidget.builderSimpleTexture(
                floodFillSettings.isGlobalFill() ? GLOBAL_FILL_ICON : LOCAL_FILL_ICON,
                btn -> {
                    floodFillSettings.setGlobalFill(!floodFillSettings.isGlobalFill());
                    Identifier icon = floodFillSettings.isGlobalFill() ? GLOBAL_FILL_ICON : LOCAL_FILL_ICON;
                    btn.setCustomSprites(new WidgetSprites(icon, icon));
                    btn.setTooltip(Tooltip.create(
                            fillModeLabel.copy().append(floodFillSettings.isGlobalFill() ? globalMode : localMode))
                    );
                }
        ).size(16, 16).textColorActive(-1).build();
        globalFillBtn.setTooltip(Tooltip.create(
                fillModeLabel.copy().append(floodFillSettings.isGlobalFill() ? globalMode : localMode))
        );

        var toleranceSlider = new SliderOption(
                100, 16,
                Component.translatable("maparthelper.gui.mapart_painter.tools_settings.tolerance"),
                floodFillSettings.getTolerance(),
                d -> floodFillSettings.setTolerance(d.floatValue())
        );

        return List.of(globalFillBtn, toleranceSlider);
    }

    private List<AbstractWidget> initPatternSettingsWidgets() {
        return List.of();
    }


    private static class SliderOption extends AbstractSliderButton {
        private final Consumer<Double> setter;
        private final Component fieldName;

        public SliderOption(int width, int height, Component fieldName, double initValue, Consumer<Double> setter) {
            super(0, 0, width, height, fieldName.copy().append(": ").append(String.format("%d%%", (int) (initValue * 100))), initValue);
            this.fieldName = fieldName;
            this.setter = setter;
        }

        @Override
        protected void updateMessage() {
            setMessage(fieldName.copy().append(": ").append(String.format("%d%%", (int) (value * 100))));
        }

        @Override
        protected void applyValue() {
            setter.accept(value);
        }
    }

    private class SettingsBackground implements Renderable {
        private SettingsBackground() {}

        //~ gui_rendering
        @Override
        public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            graphics.fill(minX - 2, minY, maxX + 2, maxY, ARGB.color(0.5f, 0));
        }
        //~ !gui_rendering
    }
}
