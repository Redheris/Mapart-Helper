package rh.maparthelper.gui.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.layouts.SpacerElement;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.CommonColors;
import net.minecraft.util.Util;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.MapColor;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3x2fStack;
import rh.maparthelper.MapartHelper;
import rh.maparthelper.conversion.MapartImageUpdater;
import rh.maparthelper.gui.widget.BlockItemWidget;
import rh.maparthelper.gui.widget.DecorativeButtonWidget;
import rh.maparthelper.gui.widget.MapColorWidget;
import rh.maparthelper.gui.widget.dropdown.PresetPatchesListDropdown;
import rh.maparthelper.gui.widget.input.AdjEditBox;
import rh.maparthelper.gui.widget.layout.AdjScrollableLayoutWidget;
import rh.maparthelper.gui.widget.layout.OverlayLayout;
import rh.maparthelper.mapart.MapartProcessing;
import rh.maparthelper.palette.*;
import rh.maparthelper.util.FileUtils;
import rh.maparthelper.util.RenderUtils;

import java.util.*;

//? >=1.21.10
//import net.minecraft.client.input.MouseButtonEvent;

public class PresetsEditorScreen extends ScreenAdapted {
    private static final Identifier NEW_PRESET_ICON = MapartHelper.identifier("textures/gui/icons/new_file.png");
    private static final Identifier NEW_DEFAULT_PRESET_ICON = MapartHelper.identifier("textures/gui/icons/new_default_file.png");
    private static final Identifier COPY_PRESET_ICON = MapartHelper.identifier("textures/gui/icons/copy_file.png");
    private static final Identifier OPEN_FOLDER_ICON = MapartHelper.identifier("textures/gui/icons/open_file.png");
    private static final Identifier UPDATE_ICON = MapartHelper.identifier("textures/gui/icons/reset.png");

    private final PaletteDataManager paletteDataManager = PaletteDataManager.getInstance();
    private PalettePresetsHandler presetsHandler = paletteDataManager.getPresetsHandler();

    private final MapartEditorScreen parent;
    private final MapartProcessing mapart;

    private final int boxX;
    private final int boxY;
    private final int marginRight;
    private final int marginBottom;
    private int boxWidth;
    private int boxHeight;

    private Map<UUID, RegisteredPresetPatch> patches = presetsHandler.createPresetPatches();
    private UUID editingPresetUUID = presetsHandler.getSelectedPreset().uuid();
    private RegisteredPresetPatch editingPreset = patches.get(editingPresetUUID);

    private LinearLayout header;
    private PresetPatchesListDropdown presetsListDropdownButton;
    private AdjEditBox filenameField;
    private EditBox presetNameField;
    private AdjScrollableLayoutWidget colorsEditorScrollable;

    private int textFieldsWidth;

    protected PresetsEditorScreen(MapartEditorScreen parent, MapartProcessing mapart, int x, int y, int marginRight, int marginBottom) {
        super(parent, Component.translatable("maparthelper.gui.presets_editor_screen"));
        this.parent = parent;
        this.mapart = mapart;
        this.boxX = x;
        this.boxY = y;
        this.marginRight = marginRight;
        this.marginBottom = marginBottom;
    }

    @Override
    protected void preInit() {
        parent.width = width;
        parent.height = height;

        this.boxWidth = width - boxX - marginRight;
        this.boxHeight = height - boxY - marginBottom;

        textFieldsWidth = Math.max((int) (boxWidth * 0.25), 142);
    }

    @Override
    protected Set<OverlayLayout> initOverlays() {
        Set<OverlayLayout> overlays = new HashSet<>();

        presetsListDropdownButton = new PresetPatchesListDropdown(
                this,
                20, 20,
                textFieldsWidth + 20,
                120,
                false,
                this::changeEditingPreset,
                patch -> {
                    if (patch.getState() == PatchTypes.CREATED) {
                        deletePatch(patch.getUUID());
                    }
                    updatePresetNameFieldState();
                },
                () -> editingPresetUUID,
                patches
        );
        presetsListDropdownButton.setTooltip(Tooltip.create(Component.translatable("maparthelper.gui.presets.choose_preset")));

        overlays.add(presetsListDropdownButton.getOverlay());

        return overlays;
    }

    @Override
    protected void initContent() {
        LinearLayout presetBarRight = LinearLayout.horizontal();
        presetBarRight.defaultCellSetting().alignVerticallyMiddle().paddingRight(1);

        DecorativeButtonWidget updateFiles = DecorativeButtonWidget.builderSimpleTexture(
                UPDATE_ICON,
                b -> this.updateFiles()
        ).size(20, 20).textureSize(16, 16).vanillaButtonBackground(true).build();
        updateFiles.setTooltip(Tooltip.create(Component.translatable("maparthelper.gui.presets.updateFiles_tooltip")));

        DecorativeButtonWidget openFolder = DecorativeButtonWidget.builderSimpleTexture(
                OPEN_FOLDER_ICON,
                btn -> Util.getPlatform().openPath(PaletteDataManager.PRESETS_PATH)
        ).size(20, 20).textureSize(16, 16).vanillaButtonBackground(true).build();
        openFolder.setTooltip(Tooltip.create(Component.translatable("maparthelper.gui.presets.open_presets_folder")));

        presetBarRight.addChild(updateFiles);
        presetBarRight.addChild(openFolder);
        presetBarRight.addChild(Button.builder(Component.translatable("maparthelper.gui.save"), b -> saveChanges())
                .size(60, 20).build());
        presetBarRight.addChild(Button.builder(Component.nullToEmpty("❌"), b -> this.onClose())
                .size(20, 20).build());

        presetBarRight.arrangeElements();
        presetBarRight.setPosition(
                boxX + boxWidth - presetBarRight.getWidth(),
                boxY + 5
        );
        presetBarRight.visitWidgets(this::addRenderableWidget);

        header = LinearLayout.vertical();
        header.setPosition(boxX + 5, boxY + 5);

        LinearLayout presetBarTopLeft = LinearLayout.horizontal();
        presetBarTopLeft.defaultCellSetting().alignVerticallyMiddle();

        addStringWidgetAlignRight(presetBarTopLeft, Component.translatable("maparthelper.gui.filename"));

        filenameField = new AdjEditBox(font,
                textFieldsWidth, 20,
                editingPreset.getShortFilename(),
                false
        );
        filenameField.setTextColor(editingPreset.isAutoFilename() ? CommonColors.LIGHT_GRAY : -1);
        filenameField.setFilter(value -> !value.matches(".*[\\\\/:*?\"<>|].*"));
        filenameField.setValueConsumer(value -> {
            if (filenameField.isFocused()) {
                editingPreset.setShortFilename(value);
                presetsListDropdownButton.updateNameFor(editingPreset);
                filenameField.setTextColor(editingPreset.isAutoFilename() ? CommonColors.LIGHT_GRAY : -1);
            }
        });
        presetBarTopLeft.addChild(filenameField);

        LinearLayout presetBarBottomLeft = LinearLayout.horizontal();
        presetBarBottomLeft.defaultCellSetting().alignVerticallyMiddle().paddingRight(1);

        addStringWidgetAlignRight(presetBarBottomLeft, Component.translatable("maparthelper.gui.preset"));

        presetNameField = new EditBox(font,
                textFieldsWidth, 20,
                Component.literal("Preset name")
        );
        presetNameField.setMaxLength(100);
        presetNameField.setValue(editingPreset.getPresetName());
        updatePresetNameFieldState();
        presetBarBottomLeft.addChild(presetNameField);
        presetBarBottomLeft.addChild(presetsListDropdownButton);
        presetsListDropdownButton.setOverlayXOffset(-presetNameField.getWidth() - 1);

        presetNameField.setHint(Component.translatable("maparthelper.gui.presets.preset_name").withColor(CommonColors.GRAY));
        presetNameField.setResponder(presetName -> {
            if (!editingPreset.getPresetName().equals(presetName)) {
                editingPreset.setPresetName(presetName);
                presetsListDropdownButton.updateNameFor(editingPreset);
                if (editingPreset.isAutoFilename()) {
                    filenameField.setValue(editingPreset.getShortFilename());
                }
            }
        });

        DecorativeButtonWidget createEmptyPreset = DecorativeButtonWidget.builderSimpleTexture(
                NEW_PRESET_ICON,
                b -> this.createNewPreset(false)
        ).vanillaButtonBackground(true).size(20, 20).textureSize(16, 16).build();
        createEmptyPreset.setTooltip(Tooltip.create(Component.translatable("maparthelper.gui.presets.createEmptyPreset_tooltip")));
        presetBarBottomLeft.addChild(createEmptyPreset);

        DecorativeButtonWidget createDefaultPreset = DecorativeButtonWidget.builderSimpleTexture(
                NEW_DEFAULT_PRESET_ICON,
                b -> this.createNewPreset(true)
        ).vanillaButtonBackground(true).size(20, 20).textureSize(16, 16).build();
        createDefaultPreset.setTooltip(Tooltip.create(Component.translatable("maparthelper.gui.presets.createDefaultPreset_tooltip")));
        presetBarBottomLeft.addChild(createDefaultPreset);

        DecorativeButtonWidget duplicatePreset = DecorativeButtonWidget.builderSimpleTexture(
                COPY_PRESET_ICON,
                b -> this.duplicatePreset()
        ).vanillaButtonBackground(true).size(20, 20).textureSize(16, 16).build();
        duplicatePreset.setTooltip(Tooltip.create(Component.translatable("maparthelper.gui.presets.duplicatePreset_tooltip")));
        presetBarBottomLeft.addChild(duplicatePreset);

        header.addChild(presetBarTopLeft);
        header.addChild(presetBarBottomLeft);
        header.arrangeElements();
        header.visitWidgets(this::addRenderableWidget);

        // =========== Colors editing area ===========

        int squareSize = 24;
        int columns = (boxWidth - 5) / (squareSize + 5);

        GridLayout colorsEditorContent = new GridLayout();
        colorsEditorContent.addChild(SpacerElement.width(boxWidth - 11), 0, 0, 1, columns);
        colorsEditorContent.defaultCellSetting().alignHorizontallyCenter().padding(0, 4, 0, 0);

        for (int i = 0; i < 63; i++) {
            MapColor mapColor = MapColor.byId(i + 1);
            if (mapColor == MapColor.NONE) break;

            MapColorWidget color = new MapColorWidget(0, 0, squareSize, squareSize, mapColor, false);
            color.showColorName(true);
            int row = 2 * columns * (i / columns);
            if (row == 0)
                colorsEditorContent.addChild(color, row, i % columns);
            else
                colorsEditorContent.addChild(color, row, i % columns, colorsEditorContent.newCellSettings().paddingTop(10));

            LinearLayout blocksListContent = LinearLayout.vertical();
            blocksListContent.defaultCellSetting().alignHorizontallyCenter().alignVerticallyMiddle().paddingLeft(1);

            MapColorBlockWidget noneBlock = new MapColorBlockWidget(
                    0, 0, squareSize,
                    Blocks.BARRIER, mapColor,
                    (mx, my) -> editingPreset.removeColor(mapColor)
            );
            noneBlock.setTooltip(Component.translatable("maparthelper.gui.presets.remove_color"));

            blocksListContent.addChild(noneBlock);

            List<Block> blocks = paletteDataManager.getCompletePalette().palette.get(mapColor.id);
            if (blocks != null) {
                for (Block block : blocks) {
                    MapColorBlockWidget blockWidget = new MapColorBlockWidget(
                            0, 0, squareSize,
                            block, mapColor,
                            (mx, my) -> editingPreset.updateEntry(mapColor, block)
                    );
                    blocksListContent.addChild(blockWidget);
                }
            }

            AdjScrollableLayoutWidget blocksListScrollable = new AdjScrollableLayoutWidget(
                    blocksListContent, 150
            );
            blocksListScrollable.setWidth(squareSize + 5);
            blocksListScrollable.setScrollBarWidth(3);

            colorsEditorContent.addChild(
                    blocksListScrollable,
                    row + columns,
                    i % columns,
                    colorsEditorContent.newCellSettings().alignHorizontallyCenter()
            );
        }

        colorsEditorScrollable = new AdjScrollableLayoutWidget(
                colorsEditorContent, boxHeight - header.getHeight() - 9
        );
        colorsEditorScrollable.setWidth(boxWidth);
        colorsEditorScrollable.setPosition(boxX, boxY + header.getHeight() + 9);
        colorsEditorScrollable.setDeltaYPerScroll(25);

        colorsEditorScrollable.arrangeElements();
        colorsEditorScrollable.visitWidgets(this::addRenderableWidget);
    }

    private void createNewPreset(boolean createDefault) {
        RegisteredPresetPatch newPreset;
        List<String> existingPresetNames = patches.values().stream()
                .map(RegisteredPresetPatch::getPresetName)
                .toList();
        if (createDefault) {
            newPreset = new RegisteredPresetPatch(
                    PaletteGenerator.generateDefaultPreset(paletteDataManager.getCompletePalette().palette),
                    "New default preset",
                    FileUtils.makeUniqueName(
                            existingPresetNames::contains,
                            "New default preset",
                            null,
                            "%s (%d)"
                    )
            );
        } else {
            newPreset = new RegisteredPresetPatch();
            newPreset.setPresetName(
                    FileUtils.makeUniqueName(
                            existingPresetNames::contains,
                            "New empty preset",
                            null,
                            "%s (%d)"
                    )
            );
        }

        patches.put(newPreset.getUUID(), newPreset);
        rebuildWidgets();
        changeEditingPreset(newPreset.getUUID());
    }

    private void deletePatch(UUID presetUUID) {
        patches.remove(presetUUID);
        if (presetUUID.equals(editingPresetUUID)) {
            changeEditingPreset(presetsHandler.getSelectedPreset().uuid());
        }
        rebuildWidgets();
        presetsListDropdownButton.getOverlay().setVisible(true);
    }

    private void updatePresetNameFieldState() {
        boolean removed = editingPreset.getState() == PatchTypes.REMOVED;
        this.presetNameField.active = !removed;
        this.presetNameField.setTextColor(removed ? CommonColors.GRAY : -1);
        this.filenameField.active = !removed;
        this.filenameField.setTextColor(removed ? CommonColors.GRAY : editingPreset.isAutoFilename() ? CommonColors.LIGHT_GRAY : -1);
    }

    private void duplicatePreset() {
        RegisteredPresetPatch newPreset = RegisteredPresetPatch.duplicate(editingPreset);
        patches.put(newPreset.getUUID(), newPreset);
        rebuildWidgets();
        changeEditingPreset(newPreset.getUUID());
    }

    private void changeEditingPreset(UUID presetUUID) {
        this.editingPresetUUID = presetUUID;
        this.editingPreset = patches.get(editingPresetUUID);
        this.presetNameField.setValue(editingPreset.getPresetName());
        this.filenameField.setValue(editingPreset.getShortFilename());
        updatePresetNameFieldState();
        presetsListDropdownButton.updateNameFor(editingPreset);
    }

    private void updateFiles() {
        paletteDataManager.updatePaletteAndPresets();

        this.presetsHandler = paletteDataManager.getPresetsHandler();
        this.patches = presetsHandler.createPresetPatches();
        this.editingPresetUUID = presetsHandler.getSelectedPreset().uuid();
        this.editingPreset = patches.get(editingPresetUUID);

        rebuildWidgets();
    }

    private void saveChanges() {
        boolean updateMapart = !editingPresetUUID.equals(presetsHandler.getSelectedPreset().uuid());
        updateMapart |= patches.get(editingPresetUUID).getState() != PatchTypes.UNCHANGED;

        paletteDataManager.applyPresetPatches(editingPresetUUID, patches.values());

        if (updateMapart) {
            MapartImageUpdater.updateMapart(mapart);
        }

        this.patches = presetsHandler.createPresetPatches();
        this.editingPresetUUID = presetsHandler.getSelectedPreset().uuid();
        this.editingPreset = patches.get(editingPresetUUID);
        rebuildWidgets();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount))
            return true;
        return colorsEditorScrollable.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    //~ gui_rendering
    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        Matrix3x2fStack matrixStack = graphics.pose();

        matrixStack.pushMatrix();
        parent.render(graphics, 0, 0, partialTick);
        matrixStack.popMatrix();

        graphics.guiRenderState.nextStratum();
        this.renderBlurredBackground(graphics);
        this.renderMenuBackground(graphics);

        int w = boxWidth;
        int h = boxHeight;
        graphics.fill(boxX, boxY, boxX + w, boxY + h, 0x77000000);
        graphics.fill(boxX, boxY, boxX + w, boxY + header.getHeight() + 8, 0x44000000);
        RenderUtils.renderOutline(graphics, boxX - 1, boxY - 1, w + 2, h + 2, 0x44FFFFFF);
        graphics.hLine(boxX, boxX + w - 1, boxY + header.getHeight() + 8, 0x77FFFFFF);

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void renderBackground(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {}
    //~ !gui_rendering

    private void addStringWidgetAlignRight(LinearLayout layout, Component value) {
        //? if <=1.21.8 {
        StringWidget presetFilenameLabel = new StringWidget(60, 9, value, font);
        presetFilenameLabel.alignRight();
        layout.addChild(presetFilenameLabel, layout.newCellSettings().paddingRight(5));
        //?} else {
        /*StringWidget presetFilenameLabel = new StringWidget(value, font);
        int labelWidth = this.font.width(value);
        layout.addChild(presetFilenameLabel, layout.newCellSettings()
                .alignHorizontallyRight()
                .paddingLeft(60 - labelWidth)
                .paddingRight(5));
        *///?}
    }

    private class MapColorBlockWidget extends BlockItemWidget {
        private final MapColor mapColor;
        private final ClickAction clickAction;

        private MapColorBlockWidget(int x, int y, int squareSize, Block block, MapColor mapColor, ClickAction clickAction) {
            super(x, y, squareSize, block, true);
            this.mapColor = mapColor;
            this.clickAction = clickAction;
        }

        //~ widget_events
        @Override
        public void onClick(double mouseX, double mouseY) {
            this.clickAction.click(mouseX, mouseY);
            presetsListDropdownButton.updateNameFor(editingPreset);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            return super.mouseClicked(mouseX, mouseY, button);
        }
        //~ !widget_events

        //~ gui_rendering
        @Override
        protected void renderWidget(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            super.renderWidget(graphics, mouseX, mouseY, partialTick);

            Block selectedBlock = editingPreset.getBlockOfMapColor(mapColor);

            boolean renderingSelected = selectedBlock == null && this.getBlock() == Blocks.BARRIER;
            renderingSelected |= (selectedBlock != null && selectedBlock == this.getBlock());

            if (renderingSelected) {
                graphics.guiRenderState.nextStratum();
                RenderUtils.renderOutline(
                        graphics,
                        this.getX(), this.getY(),
                        this.getWidth(), this.getHeight(),
                        CommonColors.HIGH_CONTRAST_DIAMOND
                );
            }
        }
        //~ !gui_rendering

        interface ClickAction {
            void click(double mouseX, double mouseY);
        }
    }
}
