package rh.maparthelper.gui.painter.hotkey;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;
import rh.maparthelper.painter.PainterProject;
import rh.maparthelper.painter.drawing.DrawingEngine;
import rh.maparthelper.painter.drawing.tool.*;
import rh.maparthelper.painter.drawing.tool.settings.SelectionToolSettings;
import rh.maparthelper.painter.layer.DynamicTextureLayer;
import rh.maparthelper.painter.surface.NativeImageSurface;
import rh.maparthelper.util.CompatUtils;

//? if >=1.21.10
//import net.minecraft.client.input.KeyEvent;

public class ShortcutKeysHandler {
    private final DrawingEngine<NativeImageSurface> drawingEngine;
    private final QuickToolSelector quickToolSelector;
    private final QuickSelectionModeSelector quickSelectionModeSelector;
    private final Runnable undo;
    private final Runnable redo;

    private final HandTool handTool;
    private final EyedropperTool eyedropperTool;
    private final RectangleSelectionTool rectangleSelectionTool;
    private final MagicWandTool<NativeImageSurface> magicWandTool;
    private final SelectionBrushTool selectionBrushTool;
    private final FloodFillTool<NativeImageSurface> floodFillTool;
    private final BrushTool<NativeImageSurface> brushTool;
    private final EraserTool<NativeImageSurface> eraserTool;

    public ShortcutKeysHandler(@NotNull PainterProject<NativeImageSurface, DynamicTextureLayer> painterProject, Runnable undo, Runnable redo) {
        this.drawingEngine = painterProject.getDrawingEngine();
        this.quickToolSelector = new QuickToolSelector(drawingEngine);

        ToolSettingsProvider toolSettings = ToolSettingsProvider.getInstance();
        var layerManager = painterProject.getLayerManager();

        this.quickSelectionModeSelector = new QuickSelectionModeSelector(toolSettings.SELECTION);
        this.undo = undo;
        this.redo = redo;
        this.handTool = new HandTool();
        this.eyedropperTool = new EyedropperTool(layerManager, drawingEngine);
        this.rectangleSelectionTool = new RectangleSelectionTool(toolSettings.SELECTION, drawingEngine.selection);
        this.selectionBrushTool = new SelectionBrushTool(toolSettings.BRUSH, toolSettings.SELECTION, drawingEngine.selection);
        this.magicWandTool = new MagicWandTool<>(toolSettings.FLOOD_FILL, toolSettings.SELECTION, drawingEngine.selection, layerManager);
        this.floodFillTool = new FloodFillTool<>(toolSettings.FLOOD_FILL, layerManager, drawingEngine.selection);
        this.brushTool = new BrushTool<>(toolSettings.BRUSH, layerManager, drawingEngine.selection);
        this.eraserTool = new EraserTool<>(toolSettings.BRUSH, layerManager, drawingEngine.selection);
    }

    //~ widget_events
    public HotkeyActionType keyPressed(int keyCode, int scanCode, int modifiers) {
        if (drawingEngine.isProcessing() && (quickToolSelector.isInUse() || quickSelectionModeSelector.isApplied()))
            return HotkeyActionType.NONE;

        if (CompatUtils.hasControlDown() && keyCode == GLFW.GLFW_KEY_Z) {
            if (CompatUtils.hasShiftDown()) {
                this.redo.run();
            } else {
                this.undo.run();
            }
            return HotkeyActionType.HISTORY;
        }

        if (keyCode == GLFW.GLFW_KEY_ESCAPE && drawingEngine.selection.isActive()) {
            drawingEngine.clearSelection();
            return HotkeyActionType.SELECTION_CHANGE;
        }

        if (keyCode == GLFW.GLFW_KEY_SPACE) {
            quickToolSelector.use(handTool);
            return HotkeyActionType.QUICK_TOOL;
        }
        PainterTool selectedPainterTool = drawingEngine.getSelectedTool();
        if (selectedPainterTool instanceof AbstractSelectionTool) {
            if (CompatUtils.hasControlDown()) {
                quickSelectionModeSelector.apply(SelectionToolSettings.SelectionMode.CONCAT);
                return HotkeyActionType.QUICK_SELECTION_MODE;
            } else if (CompatUtils.hasAltDown()) {
                quickSelectionModeSelector.apply(SelectionToolSettings.SelectionMode.SUBTRACT);
                return HotkeyActionType.QUICK_SELECTION_MODE;
            }
        }
        if (selectedPainterTool instanceof AbstractDrawingTool<?>) {
            if (CompatUtils.hasAltDown()) {
                quickToolSelector.use(eyedropperTool);
                return HotkeyActionType.QUICK_TOOL;
            }
        }
        PainterTool nextTool = switch (keyCode) {
            case GLFW.GLFW_KEY_S -> rectangleSelectionTool;
            case GLFW.GLFW_KEY_H -> handTool;
            case GLFW.GLFW_KEY_F -> floodFillTool;
            case GLFW.GLFW_KEY_B -> brushTool;
            case GLFW.GLFW_KEY_Q -> eyedropperTool;
            case GLFW.GLFW_KEY_E -> eraserTool;
            case GLFW.GLFW_KEY_W -> magicWandTool;
            case GLFW.GLFW_KEY_A -> selectionBrushTool;
            default -> null;
        };
        if (nextTool != null) {
            drawingEngine.setSelectedTool(nextTool);
            return HotkeyActionType.TOOL_CHANGE;
        }
        return HotkeyActionType.NONE;
    }

    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        quickToolSelector.release();
        return quickSelectionModeSelector.release();
    }
    //~ !widget_events
}
