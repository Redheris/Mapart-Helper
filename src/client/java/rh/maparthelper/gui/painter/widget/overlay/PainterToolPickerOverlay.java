package rh.maparthelper.gui.painter.widget.overlay;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import org.jetbrains.annotations.NotNull;
import rh.maparthelper.MapartHelper;
import rh.maparthelper.gui.painter.PainterScreen;
import rh.maparthelper.gui.painter.cursor.PainterCursorManager;
import rh.maparthelper.gui.widget.DecorativeButtonWidget;
import rh.maparthelper.gui.widget.layout.AdjScrollableLayoutWidget;
import rh.maparthelper.gui.widget.layout.OverlayLayout;
import rh.maparthelper.painter.PainterProject;
import rh.maparthelper.painter.drawing.DrawingEngine;
import rh.maparthelper.painter.drawing.tool.*;
import rh.maparthelper.painter.layer.DynamicTextureLayer;
import rh.maparthelper.painter.layer.LayerManager;
import rh.maparthelper.painter.surface.NativeImageSurface;
import rh.maparthelper.render.pipeline.PainterSelectionUniform;

public class PainterToolPickerOverlay extends OverlayLayout {
    private final PainterScreen screen;

    public PainterToolPickerOverlay(PainterScreen screen, PainterProject<NativeImageSurface, DynamicTextureLayer> painterProject, int x, int y) {
        super(true, false);
        this.screen = screen;
        lazyInitLayout(initContent(painterProject));
        this.setPosition(x, y);
    }

    private AdjScrollableLayoutWidget initContent(PainterProject<NativeImageSurface, DynamicTextureLayer> painterProject) {
        ToolSettingsProvider settingsProvider = ToolSettingsProvider.getInstance();
        DrawingEngine<NativeImageSurface> drawingEngine = painterProject.getDrawingEngine();
        LayerManager<NativeImageSurface, DynamicTextureLayer> layerManager = painterProject.getLayerManager();

        GridLayout toolsGrid = new GridLayout().spacing(-2);
        toolsGrid.defaultCellSetting().alignVerticallyMiddle().alignHorizontallyCenter().padding(2);

        toolsGrid.addChild(
                createToolSelectWidget(
                        drawingEngine,
                        new RectangleSelectionTool(settingsProvider.SELECTION, drawingEngine.selection),
                        MapartHelper.identifier("textures/gui/icons/painter/selection_rectangle.png"),
                        Component.literal("Rectangle selection")
                ),
                0, 0
        );
        toolsGrid.addChild(
                createToolSelectWidget(
                        drawingEngine,
                        new MagicWandTool<>(
                                settingsProvider.FLOOD_FILL, settingsProvider.SELECTION,
                                drawingEngine.selection, layerManager
                        ),
                        MapartHelper.identifier("textures/gui/icons/painter/selection_magic_wand.png"),
                        Component.literal("Magic wand")
                ),
                1, 0
        );
        toolsGrid.addChild(
                createToolSelectWidget(
                        drawingEngine,
                        new SelectionBrushTool(
                                settingsProvider.BRUSH, settingsProvider.SELECTION,
                                drawingEngine.selection
                        ),
                        MapartHelper.identifier("textures/gui/icons/painter/selection_brush.png"),
                        Component.literal("Selection brush")
                ),
                2, 0
        );
        toolsGrid.addChild(
                createToolSelectWidget(
                        drawingEngine,
                        new EyedropperTool(layerManager, drawingEngine),
                        MapartHelper.identifier("textures/gui/icons/painter/eyedropper.png"),
                        Component.literal("Eyedropper")
                ),
                3, 0
        );
        toolsGrid.addChild(
                createToolSelectWidget(
                        drawingEngine,
                        new HandTool(),
                        MapartHelper.identifier("textures/gui/icons/painter/hand.png"),
                        Component.literal("Hand")
                ),
                0, 1
        );
        toolsGrid.addChild(
                createToolSelectWidget(
                        drawingEngine,
                        new FloodFillTool<>(settingsProvider.FLOOD_FILL, layerManager, drawingEngine.selection),
                        MapartHelper.identifier("textures/gui/icons/painter/flood_fill.png"),
                        Component.literal("Flood fill")
                ),
                1, 1
        );
        toolsGrid.addChild(
                createToolSelectWidget(
                        drawingEngine,
                        new BrushTool<>(
                                settingsProvider.BRUSH, layerManager, drawingEngine.selection
                        ),
                        MapartHelper.identifier("textures/gui/icons/painter/brush.png"),
                        Component.literal("Brush")
                ),
                2, 1
        );
        toolsGrid.addChild(
                createToolSelectWidget(
                        drawingEngine,
                        new EraserTool<>(
                                settingsProvider.BRUSH, layerManager, drawingEngine.selection
                        ),
                        MapartHelper.identifier("textures/gui/icons/painter/eraser.png"),
                        Component.literal("Eraser")
                ),
                3, 1
        );
        //TODO: Temporarily disabled because pattern setting is postponed for now
//        toolsGrid.addChild(
//                createToolSelectWidget(
//                        drawingEngine,
//                        new PatternBrushTool<>(
//                                settingsProvider.PATTERN, settingsProvider.BRUSH_TOOL,
//                                layerManager, drawingEngine.selection
//                        ),
//                        MapartHelper.identifier("textures/gui/icons/painter/pattern_brush.png"),
//                        Component.literal("Pattern brush")
//                ),
//                4, 1
//        );

        AdjScrollableLayoutWidget scrollable = new AdjScrollableLayoutWidget(toolsGrid, 200);
        scrollable.setBackgroundColor(0xAA_343434);
        scrollable.setOutlineColor(ARGB.color(0.3f, -1));
        scrollable.setScrollBarWidth(0);
        scrollable.arrangeElements();

        return scrollable;
    }

    private DecorativeButtonWidget createToolSelectWidget(
            DrawingEngine<?> drawingEngine, PainterTool tool, Identifier icon, Component tooltip
    ) {
        var widget = new ToolButton(drawingEngine, tool, icon);
        widget.setTooltip(Tooltip.create(tooltip));
        return widget;
    }

    private class ToolButton extends DecorativeButtonWidget {
        private final DrawingEngine<?> drawingEngine;
        private final PainterTool tool;

        public ToolButton(DrawingEngine<?> drawingEngine, PainterTool tool, Identifier icon) {
            super(
                    false,
                    new WidgetSprites(icon, icon),
                    0, 0, 16, 16,
                    btn -> {
                        drawingEngine.setSelectedTool(tool);
                        PainterSelectionUniform.set(tool instanceof AbstractSelectionTool);
                        PainterCursorManager.getInstance().updateCursorAreaUniform();
                        screen.rebuildToolSettingsLayout();
                    }
            );
            this.drawingEngine = drawingEngine;
            this.tool = tool;
        }

        //~ render_button_contents
        @Override
        protected void renderWidget(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            if (tool.getClass().equals(drawingEngine.getSelectedTool().getClass())) {
                graphics.fill(
                        getX() - 1,
                        getY() - 1,
                        getRight() + 1,
                        getBottom() + 1,
                        ARGB.color(0.5f, 0x6666ff)
                );
            }
            super.renderWidget(graphics, mouseX, mouseY, partialTick);
        }
        //~ !render_button_contents
    }
}
