package rh.maparthelper.gui.painter.widget.overlay;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.MapColor;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3x2fStack;
import rh.maparthelper.MapartHelper;
import rh.maparthelper.colors.MapColorEntry;
import rh.maparthelper.colors.MapColors;
import rh.maparthelper.gui.widget.NativeImageViewWidget;
import rh.maparthelper.gui.widget.layout.AdjScrollableLayoutWidget;
import rh.maparthelper.gui.widget.layout.OverlayLayout;
import rh.maparthelper.painter.PainterProject;
import rh.maparthelper.painter.drawing.DrawingEngine;
import rh.maparthelper.palette.PaletteColors;
import rh.maparthelper.palette.PaletteDataManager;
import rh.maparthelper.palette.RegisteredPalettePreset;
import rh.maparthelper.state.painter.MapartPainterState;
import rh.maparthelper.util.CompatUtils;
import rh.maparthelper.util.RenderUtils;

import java.time.Duration;
import java.util.Collection;
import java.util.List;

//? >=1.21.10
//import net.minecraft.client.input.MouseButtonEvent;

public class ColorPickerOverlay extends OverlayLayout {
    private final Screen screen;
    private boolean selectingSecondary;
    private ColorPaletteEntry selectedColorEntry;
    private ColorPaletteEntry hoveredColorEntry;
    private RegisteredPalettePreset preset;
    private ColorPickerPaletteState colorPickerPaletteState;

    public ColorPickerOverlay(Screen screen, @NotNull PainterProject<?, ?> painterProject, int x) {
        super(true, false);
        this.screen = screen;
        var layout = initContent(painterProject);
        lazyInitLayout(layout);
        this.setPosition(x, screen.height - layout.getHeight() - 5);
    }

    @Override
    protected void replaceLayout(Screen screen, AdjScrollableLayoutWidget newLayout) {
        super.replaceLayout(screen, newLayout);
        this.setY(screen.height - newLayout.getHeight() - 5);
    }

    private AdjScrollableLayoutWidget initContent(PainterProject<?, ?> painterProject) {
        LinearLayout pickerContent = LinearLayout.horizontal().spacing(-2);
        pickerContent.defaultCellSetting().alignVerticallyTop().padding(4);

        this.preset = PaletteDataManager.getInstance().getPresetsHandler().getSelectedPreset();

        MapartPainterState.ColorPickerState colorPickerState = MapartPainterState.getInstance().getColorPickerState();

        LinearLayout mainButtons = LinearLayout.vertical().spacing(10);
        mainButtons.addChild(new SelectedColorsView(painterProject.getDrawingEngine(), 30, 30));
        var modeSwitchButton = Button.builder(
                paletteStateComponent(colorPickerState),
                btn -> {
                    var states = MapartPainterState.ColorPickerState.values();
                    int unobtainableId = MapartPainterState.ColorPickerState.UNOBTAINABLE_4COLORS.ordinal();
                    boolean backward = CompatUtils.hasShiftDown();
                    int nextId = Math.floorMod(colorPickerState.ordinal() + (backward ? -1 : 1), states.length);
                    if (!MapartHelper.commonConfig().displayUnobtainableMode && nextId == unobtainableId) {
                        nextId = backward ? states.length - 2 : 0;
                    }
                    MapartPainterState.getInstance().setColorPickerState(states[nextId]);
                    this.replaceLayout(screen, initContent(painterProject));
                }
        ).size(50, 20).build();
        if (colorPickerState == MapartPainterState.ColorPickerState.PRESET) {
            modeSwitchButton.setTooltipDelay(Duration.ofMillis(400));
            modeSwitchButton.setTooltip(Tooltip.create(
                    Component.literal("Palette is based on your selected preset and staircase style")
            ));
        }
        mainButtons.addChild(modeSwitchButton);

        colorPickerPaletteState = paletteState(colorPickerState);
        int huesCount = colorPickerPaletteState.huesCount;
        GridLayout palette = new GridLayout();
        GridLayout.RowHelper paletteAdder = palette.createRowHelper(huesCount == 1 ? 15 : huesCount == 3 ? 18 : 20);

        updateSelectedPaletteEntry();

        for (MapColor mapColor : colorPickerPaletteState.mapColors) {
            if (mapColor == MapColor.NONE) continue;
            addColorHues(painterProject, paletteAdder, mapColor, huesCount);
        }

        pickerContent.addChild(mainButtons, pickerContent.newCellSettings().alignVerticallyBottom());
        pickerContent.addChild(palette);

        AdjScrollableLayoutWidget scrollable = new AdjScrollableLayoutWidget(pickerContent, 150);
        scrollable.setBackgroundColor(0xAA_343434);
        scrollable.setOutlineColor(ARGB.color(0.3f, -1));
        scrollable.setScrollBarWidth(0);
        scrollable.arrangeElements();

        return scrollable;
    }

    private static Component paletteStateComponent(MapartPainterState.ColorPickerState colorPickerState) {
        // TODO: Localize
        return switch (colorPickerState) {
            case PRESET -> Component.literal("Preset");
            case FLAT_1COLOR -> Component.literal("2D");
            case STAIRCASE_3COLORS -> Component.literal("3D");
            case UNOBTAINABLE_4COLORS -> Component.literal("Full").withStyle(ChatFormatting.GOLD);
        };
    }

    private ColorPickerPaletteState paletteState(MapartPainterState.ColorPickerState colorPickerState) {
        if (colorPickerState != MapartPainterState.ColorPickerState.PRESET) {
            return new ColorPickerPaletteState(colorPickerState.huesCount, List.of(MapColors.mapColors()));
        }
        int huesCount = switch (MapartHelper.conversionConfig().getStaircaseStyle()) {
            case FLAT_2D -> 1;
            case WAVES_3D, VALLEY_3D -> 3;
            case UNOBTAINABLE -> 4;
        };
        return new ColorPickerPaletteState(huesCount, preset.getMapColors());
    }

    private void addColorHues(PainterProject<?, ?> painterProject, GridLayout.RowHelper adder, MapColor mapColor, int huesCount) {
        int size = huesCount == 1 ? 10 : 7;

        if (huesCount > 3) {
            adder.addChild(new ColorPaletteEntry(
                    size, size,
                    painterProject.getDrawingEngine(),
                    new MapColorEntry(mapColor, MapColor.Brightness.LOWEST)
            ));
        }
        if (huesCount > 1) {
            adder.addChild(new ColorPaletteEntry(
                    size, size,
                    painterProject.getDrawingEngine(),
                    new MapColorEntry(mapColor, MapColor.Brightness.LOW)
            ));
        }
        adder.addChild(new ColorPaletteEntry(
                size, size,
                painterProject.getDrawingEngine(),
                new MapColorEntry(mapColor, MapColor.Brightness.NORMAL)
        ));
        if (huesCount > 1) {
            adder.addChild(new ColorPaletteEntry(
                    size, size,
                    painterProject.getDrawingEngine(),
                    new MapColorEntry(mapColor, MapColor.Brightness.HIGH)
            ));
        }
    }

    @Override
    public void renderOverlay(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        selectedColorEntry = null;
        hoveredColorEntry = null;
        super.renderOverlay(graphics, mouseX, mouseY, partialTick);
        if (selectedColorEntry != null) {
            RenderUtils.renderOutline(
                    graphics,
                    selectedColorEntry.getX(),
                    selectedColorEntry.getY(),
                    selectedColorEntry.getWidth(),
                    selectedColorEntry.getHeight(),
                    ARGB.color(selectedColorEntry.getAlpha(), -1)
            );
            RenderUtils.renderOutline(
                    graphics,
                    selectedColorEntry.getX() - 1,
                    selectedColorEntry.getY() - 1,
                    selectedColorEntry.getWidth() + 2,
                    selectedColorEntry.getHeight() + 2,
                    ARGB.color(selectedColorEntry.getAlpha(), 0)
            );
        }
        if (hoveredColorEntry != null) {
            Block block = preset.getBlockOfMapColor(hoveredColorEntry.mapColor);
            // TODO: Localize
            Component text = block != null ? block.getName() : Component.literal("Not in the preset");

            int xOffset = block == null ? 0 : 16;
            int width = xOffset + screen.getFont().width(text);
            graphics.fill(
                    mouseX + 8, mouseY - 10,
                    mouseX + 14 + width, mouseY - 8 + 18,
                    ARGB.color(0.7f, 0)
            );
            //~ if >=26.1 'drawString' -> 'text' >> '('
            graphics.drawString(screen.getFont(), text, mouseX + xOffset + 12, mouseY - 4, -1);
            if (block != null) {
                //~ if >=26.1 'renderItem' -> 'item' >> '('
                graphics.renderItem(block.asItem().getDefaultInstance(), mouseX + 10, mouseY - 8);
            }
        }
    }

    private void updateSelectedPaletteEntry() {
        if (selectedColorEntry != null) {
            MapColorEntry selectedMapColorEntry = PaletteColors.getMapColorEntryByARGB(selectedColorEntry.color);
            if (!colorPickerPaletteState.mapColors.contains(selectedMapColorEntry.mapColor()))
                selectedColorEntry = null;
            else if (colorPickerPaletteState.huesCount == 1 && selectedMapColorEntry.brightness() != MapColor.Brightness.NORMAL)
                selectedColorEntry = null;
            else if (colorPickerPaletteState.huesCount < 4 && selectedMapColorEntry.brightness() == MapColor.Brightness.LOWEST)
                selectedColorEntry = null;
        }
    }

    public class SelectedColorsView extends AbstractWidget {
        private final DrawingEngine<?> drawingEngine;

        public SelectedColorsView(DrawingEngine<?> drawingEngine, int width, int height) {
            super(0, 0, width, height, Component.empty());
            this.drawingEngine = drawingEngine;
        }

        //~ gui_rendering
        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            int mainColor = drawingEngine.getMainColor();
            int secondaryColor = drawingEngine.getSecondaryColor();

            int edgeX = width / 3;
            int edgeY = height / 3;

            if (ARGB.alpha(secondaryColor) < 1) {
                graphics.blit(
                        RenderPipelines.GUI_TEXTURED,
                        NativeImageViewWidget.TRANSPARENT_TEXTURE,
                        getX() + edgeX, getY() + edgeY,
                        0.0f, 0.0f,
                        2 * edgeX, 2 * edgeY,
                        2 * edgeX, 2 * edgeY,
                        ARGB.white(0.7f * alpha)
                );
            }
            graphics.fill(
                    getX() + edgeX, getBottom() - edgeY,
                    getRight(), getBottom(),
                    ARGB.color(ARGB.alphaFloat(secondaryColor) * alpha, secondaryColor)
            );
            graphics.fill(
                    getRight() - edgeX, getY() + edgeY,
                    getRight(), getBottom() - edgeY,
                    ARGB.color(ARGB.alphaFloat(secondaryColor) * alpha, secondaryColor)
            );
            graphics.hLine(getRight() - edgeX, getRight(), getY() + edgeY - 1, ARGB.white(alpha));
            graphics.hLine(getX() + edgeX - 1, getRight(), getBottom(), ARGB.white(alpha));
            graphics.vLine(getX() + edgeX - 1, getBottom() - edgeY - 1, getBottom(), ARGB.white(alpha));
            graphics.vLine(getRight(), getY() + edgeY - 1, getBottom(), ARGB.white(alpha));

            if (ARGB.alpha(mainColor) < 1) {
                graphics.blit(
                        RenderPipelines.GUI_TEXTURED,
                        NativeImageViewWidget.TRANSPARENT_TEXTURE,
                        getX(), getY(),
                        0.0f, 0.0f,
                        2 * edgeX, 2 * edgeY,
                        2 * edgeX, 2 * edgeY,
                        ARGB.white(0.7f * alpha)
                );
            }
            graphics.fill(
                    getX(), getY(),
                    getRight() - edgeX, getBottom() - edgeY,
                    ARGB.color(ARGB.alphaFloat(mainColor) * alpha, mainColor)
            );
            RenderUtils.renderOutline(graphics, getX() - 1, getY() - 1, 2 * edgeX + 2, 2 * edgeY + 2, ARGB.white(alpha));

            Matrix3x2fStack poseStack = graphics.pose();
            poseStack.pushMatrix();

            int rectWidth = width - edgeX;
            int rectHeight = height - edgeY;

            int startX = getX();
            int startY = getY();

            if (selectingSecondary) {
                startX += edgeX;
                startY += edgeY;
            }
            float centerX = startX + rectWidth / 2f;
            float centerY = startY + rectHeight / 2f;

            graphics.enableScissor(startX, startY, startX + rectWidth, startY + rectHeight);

            poseStack.translate(centerX, centerY)
                    .rotate((float) Math.toRadians(45))
                    .translate(-centerX, -centerY);

            poseStack.translate(2 * rectHeight / 3f, 2 * rectHeight / 3f);
            graphics.fill(
                    startX,
                    startY,
                    startX + rectWidth,
                    startY + rectHeight,
                    ARGB.white(alpha)
            );

            graphics.disableScissor();
            poseStack.popMatrix();
        }
        //~ !gui_rendering


        //~ widget_events
        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            int edgeX = width / 3;
            int edgeY = height / 3;

            if (mouseX < getRight() - edgeX && mouseY < getBottom() - edgeY) {
                selectingSecondary = false;
            } else if (mouseX > getX() + edgeX && mouseY > getY() + edgeY) {
                selectingSecondary = true;
            } else {
                return false;
            }
            updateSelectedPaletteEntry();
            return true;
        }
        //~ !widget_events

        @Override
        protected void updateWidgetNarration(@NotNull NarrationElementOutput narrationElementOutput) {}
    }

    private class ColorPaletteEntry extends AbstractWidget {
        private final DrawingEngine<?> drawingEngine;
        private final MapColor mapColor;
        private final int color;


        public ColorPaletteEntry(int width, int height, DrawingEngine<?> drawingEngine, MapColorEntry colorEntry) {
            super(0, 0, width, height, Component.empty());
            this.drawingEngine = drawingEngine;
            this.mapColor = colorEntry.mapColor();
            this.color = colorEntry.getRenderColor();
        }

        public float getAlpha() {
            return alpha;
        }

        //~ gui_rendering
        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            graphics.fill(getX(), getY(), getRight(), getBottom(), ARGB.color(alpha, color));
            if (!selectingSecondary && drawingEngine.getMainColor() == color
                    || selectingSecondary && drawingEngine.getSecondaryColor() == color
            ) {
                selectedColorEntry = this;
            }
            if (isMouseOver(mouseX, mouseY)) {
                hoveredColorEntry = this;
                RenderUtils.renderOutline(graphics, getX(), getY(), width, height, ARGB.color(alpha, -1));
            }
        }
        //~ !gui_rendering

        //~ widget_events
        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (selectingSecondary ^ button == 1) {
                drawingEngine.setSecondaryColor(color);
            } else {
                drawingEngine.setMainColor(color);
            }
            return true;
        }
        //~ !widget_events

        @Override
        protected void updateWidgetNarration(@NotNull NarrationElementOutput narrationElementOutput) {}
    }

    private record ColorPickerPaletteState(
            int huesCount,
            Collection<MapColor> mapColors
    ) {}
}
