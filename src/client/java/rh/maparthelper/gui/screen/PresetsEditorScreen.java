package rh.maparthelper.gui.screen;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.LayoutSettings;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.layouts.SpacerElement;
import net.minecraft.network.chat.Component;
import net.minecraft.util.CommonColors;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.MapColor;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3x2fStack;
import rh.maparthelper.config.palette.PaletteConfigManager;
import rh.maparthelper.config.palette.PalettePresetsConfig;
import rh.maparthelper.conversion.MapartImageUpdater;
import rh.maparthelper.gui.widget.BlockItemWidget;
import rh.maparthelper.gui.widget.MapColorWidget;
import rh.maparthelper.gui.widget.dropdown.PresetsListDropdownWidget;
import rh.maparthelper.gui.widget.input.AdjEditBox;
import rh.maparthelper.gui.widget.layout.AdjScrollableLayoutWidget;
import rh.maparthelper.gui.widget.layout.OverlayLayout;
import rh.maparthelper.util.RenderUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

//? >=1.21.10
//import net.minecraft.client.input.MouseButtonEvent;

public class PresetsEditorScreen extends ScreenAdapted {
    private final MapartEditorScreen parent;
    private final int boxX;
    private final int boxY;
    private final int marginRight;
    private final int marginBottom;
    private int boxWidth;
    private int boxHeight;

    private PalettePresetsConfig.Editable presetsConfig = PaletteConfigManager.presetsConfig.getEditable();
    private String editingPreset = presetsConfig.getCurrentPresetFilename();

    private final Set<String> deletedPresets = new HashSet<>();
    private final Set<String> updatedPresets = new HashSet<>();

    private PresetsListDropdownWidget presetsListDropdownButton;

    private AdjEditBox presetNameField;
    private AdjScrollableLayoutWidget colorsEditorScrollable;

    protected PresetsEditorScreen(MapartEditorScreen parent, int x, int y, int marginRight, int marginBottom) {
        super(Component.translatable("maparthelper.gui.presets_editor_screen"));
        this.parent = parent;
        this.boxX = x;
        this.boxY = y;
        this.marginRight = marginRight;
        this.marginBottom = marginBottom;
    }

    @Override
    protected void preInit() {
        parent.width = width;
        parent.height = height;
        parent.init();

        this.boxWidth = parent.width - boxX - marginRight;
        this.boxHeight = parent.height - boxY - marginBottom;
    }

    @Override
    protected Set<OverlayLayout> initOverlays() {
        Set<OverlayLayout> overlays = new HashSet<>();

        presetsListDropdownButton = new PresetsListDropdownWidget(
                this,
                20, 20,
                (int) (boxWidth * 0.35),
                120,
                false,
                Component.nullToEmpty("☰"),
                this::changeEditingPreset,
                presetsConfig.presetFiles
        );

        overlays.add(presetsListDropdownButton.getOverlay());

        return overlays;
    }

    @Override
    protected void initContent() {
        LinearLayout presetBarLeft = LinearLayout.horizontal();
        presetBarLeft.setPosition(boxX + 5, boxY + 5);
        LayoutSettings presetBarLeftPositioner = presetBarLeft.defaultCellSetting().alignVerticallyMiddle();

        StringWidget presetNameLabel = new StringWidget(Component.translatable("maparthelper.gui.preset"), font);
        presetBarLeft.addChild(presetNameLabel, presetBarLeftPositioner.copy().paddingRight(5));

        presetNameField = new AdjEditBox(
                font, (int) (boxWidth * 0.3), 20, presetsConfig.presetFiles.get(editingPreset), "Preset name"
        );
        presetBarLeft.addChild(presetNameField);

        presetsListDropdownButton.setOverlayXOffset(-presetNameField.getWidth());
        presetsListDropdownButton.setTooltip(Tooltip.create(Component.translatable("maparthelper.gui.presets.choose_preset")));
        presetBarLeft.addChild(presetsListDropdownButton);

        presetNameField.setHint(Component.translatable("maparthelper.gui.presets.preset_name").withColor(CommonColors.GRAY));
        presetNameField.setValueConsumer(value -> {
            presetsConfig.presetFiles.put(editingPreset, value);
            presetsListDropdownButton.updateNames(presetsConfig.presetFiles.values());
        });

        Button createEmptyPreset = Button.builder(Component.nullToEmpty("\uD83D\uDDCB"), b -> this.createNewPreset(false))
                .size(17, 20)
                .build();
        createEmptyPreset.setTooltip(Tooltip.create(Component.translatable("maparthelper.gui.presets.createEmptyPreset_tooltip")));
        presetBarLeft.addChild(createEmptyPreset);

        Button createDefaultPreset = Button.builder(Component.nullToEmpty("➕"), b -> this.createNewPreset(true))
                .size(17, 20)
                .build();
        createDefaultPreset.setTooltip(Tooltip.create(Component.translatable("maparthelper.gui.presets.createDefaultPreset_tooltip")));
        presetBarLeft.addChild(createDefaultPreset);

        Button duplicatePreset = Button.builder(Component.nullToEmpty("\uD83D\uDDD0"), b -> this.duplicatePreset())
                .size(17, 20)
                .build();
        duplicatePreset.setTooltip(Tooltip.create(Component.translatable("maparthelper.gui.presets.duplicatePreset_tooltip")));
        presetBarLeft.addChild(duplicatePreset);

        Button deletePreset = Button.builder(Component.nullToEmpty("\uD83D\uDDD1"), b -> this.deletePreset())
                .size(17, 20)
                .build();
        deletePreset.setTooltip(Tooltip.create(Component.translatable("maparthelper.gui.presets.deletePreset_tooltip")));
        presetBarLeft.addChild(deletePreset);

        presetBarLeft.arrangeElements();
        presetBarLeft.visitWidgets(this::addRenderableWidget);


        LinearLayout presetBarRight = LinearLayout.horizontal();
        presetBarRight.setPosition(0, boxY + 5);
        presetBarRight.defaultCellSetting().alignVerticallyMiddle().paddingRight(1);

        Button updateFiles = Button.builder(Component.literal("⟲").withStyle(ChatFormatting.BOLD), b -> this.updateFiles())
                .size(18, 20)
                .build();
        updateFiles.setTooltip(Tooltip.create(Component.translatable("maparthelper.gui.presets.updateFiles_tooltip")));
        presetBarRight.addChild(updateFiles);

        Button save = Button.builder(Component.translatable("maparthelper.gui.save"), b -> saveChanges())
                .size(60, 20)
                .build();
        presetBarRight.addChild(save);

        Button close = Button.builder(Component.nullToEmpty("❌"), b -> this.onClose())
                .size(20, 20)
                .build();
        presetBarRight.addChild(close);

        presetBarRight.arrangeElements();
        presetBarRight.setX(boxX + boxWidth - presetBarRight.getWidth());
        presetBarRight.visitWidgets(this::addRenderableWidget);

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
                    (mx, my) -> {
                        presetsConfig.getPreset(editingPreset).removeColor(mapColor);
                        updatedPresets.add(editingPreset);
                    }
            );
            noneBlock.setTooltip(Component.translatable("maparthelper.gui.presets.remove_color"));

            blocksListContent.addChild(noneBlock);

            List<Block> blocks = PaletteConfigManager.completePalette.palette.get(mapColor.id);
            if (blocks != null) {
                for (Block block : blocks) {
                    MapColorBlockWidget blockWidget = new MapColorBlockWidget(
                            0, 0, squareSize,
                            block, mapColor,
                            (mx, my) -> {
                                presetsConfig.getPreset(editingPreset).updateColor(mapColor, block);
                                updatedPresets.add(editingPreset);
                            }
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
                colorsEditorContent, boxHeight - 31
        );
        colorsEditorScrollable.setWidth(boxWidth);
        colorsEditorScrollable.setPosition(boxX, boxY + 31);

        colorsEditorScrollable.arrangeElements();
        colorsEditorScrollable.visitWidgets(this::addRenderableWidget);
    }

    private void createNewPreset(boolean createDefault) {
        String newPreset = presetsConfig.createNewPreset(createDefault, updatedPresets, deletedPresets);
        changeEditingPreset(newPreset);
        rebuildWidgets();
    }

    private void deletePreset() {
        PalettePresetsConfig.Editable updatedConfig = presetsConfig.deletePreset(editingPreset, updatedPresets, deletedPresets);
        boolean configEmptied = updatedConfig != presetsConfig;
        if (configEmptied) {
            presetsConfig = updatedConfig;
        } else {
            updatedPresets.remove(editingPreset);
        }
        changeEditingPreset(presetsConfig.getCurrentPresetFilename());
        rebuildWidgets();
    }

    private void duplicatePreset() {
        String newPreset = presetsConfig.duplicatePreset(editingPreset, updatedPresets, deletedPresets);
        changeEditingPreset(newPreset);
        rebuildWidgets();
    }

    private void changeEditingPreset(String presetFile) {
        this.editingPreset = presetFile;
        this.presetNameField.setValue(presetsConfig.presetFiles.get(presetFile));
    }

    private void updateFiles() {
        PaletteConfigManager.updateCompletePalette();
        PaletteConfigManager.readPresetsConfigFile();

        this.presetsConfig = PaletteConfigManager.presetsConfig.getEditable();
        this.editingPreset = presetsConfig.getCurrentPresetFilename();

        this.deletedPresets.clear();
        this.updatedPresets.clear();
        rebuildWidgets();
    }

    private void saveChanges() {
        boolean updateMapart = !presetsConfig.getCurrentPresetFilename().equals(PaletteConfigManager.presetsConfig.getCurrentPresetFilename());
        updateMapart |= updatedPresets.contains(presetsConfig.getCurrentPresetFilename());
        PaletteConfigManager.presetsConfig = presetsConfig;
        if (!updatedPresets.isEmpty()) {
            for (String filename : updatedPresets) {
                PaletteConfigManager.savePresetFile(filename);
            }
            updatedPresets.clear();
        }
        if (!deletedPresets.isEmpty()) {
            for (String filename : deletedPresets) {
                PaletteConfigManager.deletePresetFile(filename);
            }
            deletedPresets.clear();
        }
        if (updateMapart) MapartImageUpdater.updateMapart(parent.mapart);
        PaletteConfigManager.savePresetsConfigFile();
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
        graphics.fill(boxX, boxY, boxX + w, boxY + 30, 0x44000000);
        RenderUtils.renderOutline(graphics, boxX - 1, boxY - 1, w + 2, h + 2, 0x44FFFFFF);
        graphics.hLine(boxX, boxX + w - 1, boxY + 30, 0x77FFFFFF);

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void renderBackground(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    }
    //~ !gui_rendering

    @Override
    public void onClose() {
        //? <=1.21.8
        assert this.minecraft != null;
        this.minecraft.setScreen(this.parent);
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

            PalettePresetsConfig.PalettePreset preset = presetsConfig.getPreset(editingPreset);
            Block presetBlock = preset.colors.get(mapColor);
            boolean flag = presetBlock == null && this.getBlock() == Blocks.BARRIER;
            flag = flag || (presetBlock != null && presetBlock == this.getBlock());
            if (flag) {
                graphics.guiRenderState.nextStratum();
                RenderUtils.renderOutline(graphics, this.getX(), this.getY(), this.getWidth(), this.getHeight(), CommonColors.HIGH_CONTRAST_DIAMOND);
            }
        }
        //~ !gui_rendering

        interface ClickAction {
            void click(double mouseX, double mouseY);
        }
    }
}
