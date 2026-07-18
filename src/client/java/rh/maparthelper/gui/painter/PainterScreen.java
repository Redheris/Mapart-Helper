package rh.maparthelper.gui.painter;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import org.jetbrains.annotations.NotNull;
import rh.maparthelper.MapartHelper;
import rh.maparthelper.gui.painter.cursor.PainterCursorManager;
import rh.maparthelper.gui.painter.hotkey.HotkeyActionType;
import rh.maparthelper.gui.painter.hotkey.ShortcutKeysHandler;
import rh.maparthelper.gui.painter.widget.ToolSettingsLayoutBuilder;
import rh.maparthelper.gui.painter.widget.overlay.*;
import rh.maparthelper.gui.screen.FullscreenImageViewScreen;
import rh.maparthelper.gui.widget.DecorativeButtonWidget;
import rh.maparthelper.gui.widget.dropdown.DropdownOverlayButton;
import rh.maparthelper.gui.widget.layout.OverlayLayout;
import rh.maparthelper.painter.PainterProject;
import rh.maparthelper.painter.drawing.DrawingEngine;
import rh.maparthelper.painter.drawing.tool.AbstractSelectionTool;
import rh.maparthelper.painter.history.HistoryManager;
import rh.maparthelper.painter.history.action.HistoryActionType;
import rh.maparthelper.painter.layer.DynamicTextureLayer;
import rh.maparthelper.painter.layer.LayerManager;
import rh.maparthelper.painter.surface.NativeImageSurface;
import rh.maparthelper.render.pipeline.PainterSelectionUniform;
import rh.maparthelper.state.ActiveModScreenManager;
import rh.maparthelper.state.painter.MapartPainterState;

import java.util.HashSet;
import java.util.Set;

//? if >=1.21.10 {
/*import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
*///?}

public class PainterScreen extends FullscreenImageViewScreen {
    private static final Identifier OPEN_FOLDER_ICON = MapartHelper.identifier("textures/gui/icons/open_file.png");
    private static final Identifier NEW_PROJECT_ICON = MapartHelper.identifier("textures/gui/icons/new_file.png");
    private static final Identifier UNDO_ICON = MapartHelper.identifier(
            "textures/gui/icons/painter/undo.png"
    );
    private static final Identifier UNDO_DISABLED_ICON = MapartHelper.identifier(
            "textures/gui/icons/painter/undo_disabled.png"
    );
    private static final Identifier REDO_ICON = MapartHelper.identifier(
            "textures/gui/icons/painter/redo.png"
    );
    private static final Identifier REDO_DISABLED_ICON = MapartHelper.identifier(
            "textures/gui/icons/painter/redo_disabled.png"
    );

    public static final Identifier SELECTION_MASK_ID = MapartHelper.identifier("image_selection_texture");
    public static DynamicTexture selectionMask;

    private final PainterProject<NativeImageSurface, DynamicTextureLayer> painterProject;
    private final DrawingEngine<NativeImageSurface> drawingEngine;
    private final LayerManager<NativeImageSurface, DynamicTextureLayer> layerManager;
    private final HistoryManager historyManager;
    private final ShortcutKeysHandler shortcutKeysHandler;

    private PainterViewWidget painterViewWidget;
    private ToolSettingsLayoutBuilder toolSettingsLayoutBuilder;

    private PainterToolPickerOverlay toolPickerOverlay;
    private ColorPickerOverlay colorsPickerOverlay;
    private LayerPropertiesOverlay layerPropertiesOverlay;
    private LayersOverlay layersOverlay;
    private OverlayLayout saveProjectOverlay;

    private SelectionModesDropdown selectionModesDropdown;

    private DecorativeButtonWidget undoButton;
    private DecorativeButtonWidget redoButton;

    public PainterScreen() {
        super(Component.translatable("maparthelper.gui.screen.mapart_painter"));
        MapartPainterState painterState = MapartPainterState.getInstance();
        this.painterProject = painterState.getPainterProject();
        if (painterProject == null) {
            throw new IllegalStateException("Cannot open Mapart Painter screen for null project");
        }
        this.drawingEngine = painterProject.getDrawingEngine();
        this.layerManager = painterProject.getLayerManager();
        this.historyManager = painterProject.getHistoryManager();
        this.shortcutKeysHandler = new ShortcutKeysHandler(painterProject, this::undo, this::redo);
        ActiveModScreenManager.getInstance().setActiveModScreen(ActiveModScreenManager.ModScreen.MAPART_PAINTER);
    }

    private void undo() {
        if (drawingEngine.isProcessing()) return;
        HistoryActionType historyActionType = historyManager.undo();
        updateHistoryButtonsState();
        if (historyActionType == HistoryActionType.LAYERS) {
            layersOverlay.rebuildContent();
            layerPropertiesOverlay.setLayer(layerManager.getSelectedLayer());
        }
    }

    private void redo() {
        if (drawingEngine.isProcessing()) return;
        HistoryActionType historyActionType = historyManager.redo();
        updateHistoryButtonsState();
        if (historyActionType == HistoryActionType.LAYERS) {
            layersOverlay.rebuildContent();
            layerPropertiesOverlay.setLayer(layerManager.getSelectedLayer());
        }
    }

    @Override
    protected void preInit() {
        toolSettingsLayoutBuilder = new ToolSettingsLayoutBuilder(this);
    }

    @Override
    protected Set<OverlayLayout> initOverlays() {
        Set<OverlayLayout> overlays = new HashSet<>();

        toolPickerOverlay = new PainterToolPickerOverlay(this, painterProject, 5, 35);
        overlays.add(toolPickerOverlay);

        colorsPickerOverlay = new ColorPickerOverlay(this, painterProject, 5);
        overlays.add(colorsPickerOverlay);

        layerPropertiesOverlay = new LayerPropertiesOverlay(this, painterProject);
        overlays.add(layerPropertiesOverlay);

        layersOverlay = new LayersOverlay(this, painterProject, layerPropertiesOverlay, 35, 5);
        overlays.add(layersOverlay);

        selectionModesDropdown = toolSettingsLayoutBuilder.getSelectionModesDropdown();
        overlays.add(selectionModesDropdown.getOverlay());

        saveProjectOverlay = SaveProjectOverlayBuilder.create(layerManager);

        return overlays;
    }

    @Override
    protected void initContent() {
        toolSettingsLayoutBuilder.visitWidgets(this::addWidget);
        rebuildToolSettingsLayout();
        super.initContent();

        this.addRenderableOnly(toolSettingsLayoutBuilder.getBackground());
        toolSettingsLayoutBuilder.visitWidgets(this::addRenderableOnly);
    }

    @Override
    protected LinearLayout headerLeft() {
        LinearLayout headerLeft = super.headerLeft();
        headerLeft.defaultCellSetting().alignHorizontallyLeft().alignVerticallyMiddle();

        undoButton = DecorativeButtonWidget.builder(
                UNDO_ICON,
                UNDO_DISABLED_ICON,
                btn -> this.undo()
        ).size(16, 16).build();
        redoButton = DecorativeButtonWidget.builder(
                REDO_ICON,
                REDO_DISABLED_ICON,
                btn -> this.redo()
        ).size(16, 16).build();

        undoButton.setTooltip(Tooltip.create(Component.translatable("maparthelper.gui.mapart_painter.undo")));
        redoButton.setTooltip(Tooltip.create(Component.translatable("maparthelper.gui.mapart_painter.redo")));

        headerLeft.addChild(undoButton);
        headerLeft.addChild(redoButton);

        updateHistoryButtonsState();

        return headerLeft;
    }

    @Override
    protected LinearLayout headerRight() {
        LinearLayout headerRight = LinearLayout.horizontal().spacing(1);
        headerRight.defaultCellSetting().alignVerticallyMiddle().alignHorizontallyRight();

        DecorativeButtonWidget newProject = DecorativeButtonWidget.builderSimpleTexture(
                NEW_PROJECT_ICON,
                btn -> Minecraft.getInstance().setScreen(new CreateNewProjectScreen(this))
        ).size(20, 20).textureSize(16, 16).vanillaButtonBackground(true).build();
        newProject.setTooltip(Tooltip.create(Component.translatable("maparthelper.gui.mapart_painter.new_project")));
        headerRight.addChild(newProject);

        DecorativeButtonWidget openFolder = DecorativeButtonWidget.builderSimpleTexture(
                OPEN_FOLDER_ICON,
                btn -> Util.getPlatform().openPath(MapartPainterState.PAINTER_PNG_OUTPUT_DIR)
        ).size(20, 20).textureSize(16, 16).vanillaButtonBackground(true).build();
        openFolder.setTooltip(Tooltip.create(Component.translatable("maparthelper.gui.mapart_painter.open_painter_folder")));
        headerRight.addChild(openFolder);

        headerRight.addChild(new DropdownOverlayButton(
                this, saveProjectOverlay, 82, 20,
                Component.translatable("maparthelper.gui.mapart_painter.save_project")
        ));

        return headerRight;
    }

    @Override
    protected PainterViewWidget initImageViewWidget() {
        this.painterViewWidget = new PainterViewWidget(painterProject, 0, 30, width, height - 30);
        return this.painterViewWidget;
    }

    //~ widget_events
    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (this.getFocused() != null) {
            boolean result = this.getFocused().mouseReleased(mouseX, mouseY, button);
            updateHistoryButtonsState();
            return result;
        }
        updateHistoryButtonsState();
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        HotkeyActionType hotkeyActionType = shortcutKeysHandler.keyPressed(keyCode, scanCode, modifiers);
        if (hotkeyActionType == HotkeyActionType.HISTORY) {
            painterViewWidget.updateSelectionMaskTexture();
            painterViewWidget.updateSelectionUniform();
        }
        if (hotkeyActionType == HotkeyActionType.QUICK_SELECTION_MODE) {
            selectionModesDropdown.updateDropdownButtonState();
        }
        if (hotkeyActionType == HotkeyActionType.TOOL_CHANGE) {
            PainterSelectionUniform.set(drawingEngine.getSelectedTool() instanceof AbstractSelectionTool);
            PainterCursorManager.getInstance().updateCursorAreaUniform();
            rebuildToolSettingsLayout();
        }
        if (hotkeyActionType == HotkeyActionType.SELECTION_CHANGE) {
            painterViewWidget.updateSelectionMaskTexture();
        }

        return hotkeyActionType != HotkeyActionType.NONE || super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (shortcutKeysHandler.keyReleased(keyCode, scanCode, modifiers)) {
            selectionModesDropdown.updateDropdownButtonState();
        }
        return super.keyReleased(keyCode, scanCode, modifiers);
    }
    //~ !widget_events

    //~ gui_rendering
    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        updateOverlayAlpha(toolPickerOverlay, mouseX, mouseY);
        updateOverlayAlpha(colorsPickerOverlay, mouseX, mouseY);
        updateOverlayAlpha(layersOverlay, mouseX, mouseY);
        super.render(graphics, mouseX, mouseY, partialTick);
    }
    //~ !gui_rendering

    private void updateOverlayAlpha(OverlayLayout overlayLayout, int mouseX, int mouseY) {
        if (overlayLayout.isMouseOverLayout(mouseX, mouseY)) {
            overlayLayout.setAlpha(1.0f);
        } else if (overlayLayout.getRectangle().intersects(painterViewWidget.getImageRectangle())) {
            overlayLayout.setAlpha(0.8f);
        } else {
            overlayLayout.setAlpha(1.0f);
        }
    }

    protected void updateHistoryButtonsState() {
        undoButton.active = historyManager.hasUndo();
        redoButton.active = historyManager.hasRedo();
    }

    @Override
    public void onClose() {
        super.onClose();
    }

    public void rebuildToolSettingsLayout() {
        toolSettingsLayoutBuilder.arrangeWidget(this, drawingEngine);
    }
}
