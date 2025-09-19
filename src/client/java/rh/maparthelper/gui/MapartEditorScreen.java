package rh.maparthelper.gui;

import me.shedaniel.autoconfig.AutoConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.MapColor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import rh.maparthelper.MapartHelper;
import rh.maparthelper.command.FakeMapsPreview;
import rh.maparthelper.config.ConversionConfiguration;
import rh.maparthelper.config.MapartHelperConfig;
import rh.maparthelper.config.UseAuxBlocks;
import rh.maparthelper.config.palette.PaletteConfigManager;
import rh.maparthelper.config.palette.PalettePresetsConfig;
import rh.maparthelper.conversion.CroppingMode;
import rh.maparthelper.conversion.CurrentConversionSettings;
import rh.maparthelper.conversion.MapartImageConverter;
import rh.maparthelper.conversion.dithering.DitheringAlgorithms;
import rh.maparthelper.conversion.schematic.MapartToNBT;
import rh.maparthelper.conversion.schematic.NbtSchematicUtils;
import rh.maparthelper.conversion.staircases.StaircaseStyles;
import rh.maparthelper.gui.widget.*;

import java.nio.file.Path;
import java.time.Duration;
import java.util.List;

@Environment(EnvType.CLIENT)
public class MapartEditorScreen extends ScreenAdapted {
    DirectionalLayoutWidget settingsLeft;
    DirectionalLayoutWidget settingsRight;
    ScrollableGridWidget materialList;
    MapartPreviewWidget mapartPreview;
    private final int baseElementWidth = 165;

    private int auxBlockCount = 0;

    public MapartEditorScreen() {
        super(Text.translatable("maparthelper.gui.mapart_editor_screen"));
    }

    public void updateMaterialList() {
        this.remove(materialList);
        if (CurrentConversionSettings.imagePath == null) return;
        int listTop = settingsRight.getY() + settingsRight.getHeight();

        materialList = new ScrollableGridWidget(
                null,
                settingsRight.getX() - 6, listTop,
                width - settingsRight.getX() - 1, height - listTop, 6
        );
        materialList.setLeftScroll(true);
        materialList.grid.setColumnSpacing(0);
        materialList.grid.getMainPositioner().alignVerticalCenter();
        GridWidget.Adder materialListAdder = materialList.grid.createAdder(2);

        PalettePresetsConfig palette = PaletteConfigManager.presetsConfig;
        MapartImageConverter.MapColorCount[] colorsCounter = MapartImageConverter.getColorsCounter();

        this.auxBlockCount = CurrentConversionSettings.getWidth() * 128;
        BlockItemWidget auxBlockItemWidget = new BlockItemWidget(0, 0, 24, MapartHelper.conversionSettings.auxBlock);
        auxBlockItemWidget.insertToTooltip(1, Text.translatable("maparthelper.aux_block").formatted(Formatting.GRAY));
        TextWidget auxAmountText = new TextWidget(Text.empty(), textRenderer);
        materialListAdder.add(auxBlockItemWidget, materialList.grid.copyPositioner().marginLeft(6));
        materialListAdder.add(auxAmountText);

        for (MapartImageConverter.MapColorCount colorCount : colorsCounter) {
            addBlockToMaterialList(materialListAdder, palette, colorCount);
        }

        Text amountText = Text.of(getAmountString(auxBlockCount, auxBlockItemWidget.getStackSize()));
        auxAmountText.setWidth(textRenderer.getWidth(amountText));
        auxAmountText.setMessage(amountText);
        auxAmountText.setTooltip(Tooltip.of(amountText));

        materialList.refreshPositions();
        this.addDrawableChild(materialList);
    }

    private void addBlockToMaterialList(GridWidget.Adder adder, PalettePresetsConfig palette, MapartImageConverter.MapColorCount color) {
        if (color.amount() == 0) return;
        Block block = palette.getBlockOfMapColor(MapColor.get(color.id()));
        if (block == null) return;

        BlockItemWidget blockItemWidget = new BlockItemWidget(0, 0, 24, block);
        adder.add(blockItemWidget, materialList.grid.copyPositioner().marginLeft(6));
        TextWidget amountText = new TextWidget(Text.of(getAmountString(color.amount(), block.asItem().getMaxCount())), textRenderer);
        adder.add(amountText);
        amountText.setTooltip(Tooltip.of(amountText.getMessage()));
        amountText.setTooltipDelay(Duration.ofMillis(100));

        if (NbtSchematicUtils.needsAuxBlock(block)) {
            auxBlockCount += color.amount();
        }
    }

    private String getAmountString(int amount, int stackSize) {
        StringBuilder text = new StringBuilder();
        int shBoxSize = 27 * stackSize;
        int shBoxes = amount / shBoxSize;
        int stacks = amount % shBoxSize / stackSize;
        int items = amount % shBoxSize % stackSize;
        boolean counted = shBoxes > 0 || stacks > 0;

        if (shBoxes > 0)
            text.append(shBoxes).append("§3").append(Text.translatable("maparthelper.gui.shulker_box_abbr").getString()).append("§r");
        if (stacks > 0) {
            text.append(shBoxes > 0 ? " + " : "").append(stacks);
            if (stackSize > 1) text.append("§3x").append(stackSize).append("§r");
        }
        if (counted) {
            text.insert(0, " = ");
            if (items > 0) text.append(" + ").append(items);
        }

        return text.insert(0, "" + amount).toString();
    }

    @Override
    protected void init() {
        super.init();
        settingsLeft = DirectionalLayoutWidget.vertical();
        settingsLeft.setPosition(5, 20);
        Positioner settingsLeftPositioner = settingsLeft.getMainPositioner().marginTop(5);

        TextFieldWidget mapartName = createTextInputFieldWidget(baseElementWidth, CurrentConversionSettings.mapartName, -1);
        mapartName.setChangedListener(value -> {
            mapartName.setEditableColor(Colors.WHITE);
            if (value.isEmpty()) {
                mapartName.setSuggestion(Text.translatable("maparthelper.gui.mapart_name_field").getString());
                return;
            }
            mapartName.setSuggestion(null);
            if (value.matches(".*[<>:\"/|?*\\\\].*")) {
                mapartName.setEditableColor(Colors.LIGHT_RED);
                return;
            }
            CurrentConversionSettings.mapartName = value;
        });
        settingsLeft.add(new TextWidget(Text.translatable("maparthelper.gui.mapart_name_field"), textRenderer));
        settingsLeft.add(mapartName, settingsLeftPositioner.copy().marginTop(0));

        GridWidget size = createSizeSettingsGrid();
        settingsLeft.add(size);

        settingsLeft.refreshPositions();
        settingsLeft.forEachChild(this::addDrawableChild);

        int listTop = settingsLeft.getY() + settingsLeft.getHeight();
        ScrollableGridWidget settingsLeftScrollable = new ScrollableGridWidget(
                null,
                settingsLeft.getX(), listTop,
                baseElementWidth + 6, height - listTop, 6
        );
        settingsLeftScrollable.grid.getMainPositioner().marginTop(5);
        GridWidget.Adder adder = settingsLeftScrollable.grid.createAdder(1);

        Text previewMapart = Text.translatable("maparthelper.gui.previewMapart");
        Text previewOriginal = Text.translatable("maparthelper.gui.previewOriginal").formatted(Formatting.GOLD);
        ButtonWidget previewMode = ButtonWidget.builder(
                MapartHelper.conversionSettings.showOriginalImage ? previewOriginal : previewMapart,
                (btn) -> {
                    MapartHelper.conversionSettings.showOriginalImage = !MapartHelper.conversionSettings.showOriginalImage;
                    btn.setMessage(MapartHelper.conversionSettings.showOriginalImage ? previewOriginal : previewMapart);
                    MapartImageConverter.updateMapart();
                }
        ).size(baseElementWidth, 20).build();
        adder.add(new TextWidget(Text.translatable("maparthelper.gui.previewMode"), textRenderer));
        adder.add(previewMode, settingsLeftPositioner.copy().marginTop(0));

        EnumDropdownMenuWidget croppingMode = new EnumDropdownMenuWidget(
                this, 0, 0, baseElementWidth, 20, baseElementWidth,
                Text.translatable("maparthelper.gui.cropMode"),
                Text.translatable("maparthelper.gui.option." + CurrentConversionSettings.cropMode.name())
        );
        croppingMode.addEntries(
                e -> {
                    CurrentConversionSettings.cropMode = (CroppingMode) e;
                    MapartImageConverter.updateMapart();
                },
                CroppingMode.values()
        );
        croppingMode.forEachEntry(this::addSelectableChild);
        adder.add(croppingMode);

        EnumDropdownMenuWidget staircaseStyle = new EnumDropdownMenuWidget(
                this, 0, 0, baseElementWidth, 20, baseElementWidth,
                Text.translatable("maparthelper.gui.staircaseStyle"),
                Text.translatable("maparthelper.gui.option." + MapartHelper.conversionSettings.staircaseStyle.name())
        );
        staircaseStyle.addEntries(
                e -> {
                    ConversionConfiguration config = MapartHelper.conversionSettings;
                    boolean was3D = config.use3D();
                    config.staircaseStyle = (StaircaseStyles) e;
                    if (config.use3D() != was3D)
                        MapartImageConverter.updateMapart();
                    AutoConfig.getConfigHolder(MapartHelperConfig.class).save();
                },
                StaircaseStyles.values()
        );
        staircaseStyle.forEachEntry(this::addSelectableChild);
        adder.add(staircaseStyle);

        EnumDropdownMenuWidget ditheringAlg = new EnumDropdownMenuWidget(
                this, 0, 0, baseElementWidth, 20, baseElementWidth,
                Text.translatable("maparthelper.gui.ditheringAlg"),
                Text.translatable("maparthelper.gui.option." + MapartHelper.conversionSettings.ditheringAlgorithm.name())
        );
        ditheringAlg.setLeftScroll(true);
        ditheringAlg.addEntries(
                e -> {
                    MapartHelper.conversionSettings.ditheringAlgorithm = (DitheringAlgorithms) e;
                    MapartImageConverter.updateMapart();
                    AutoConfig.getConfigHolder(MapartHelperConfig.class).save();
                },
                DitheringAlgorithms.values()
        );
        ditheringAlg.forEachEntry(this::addSelectableChild);
        adder.add(ditheringAlg);

        Text isOn = Text.translatable("maparthelper.gui.isOn");
        Text isOff = Text.translatable("maparthelper.gui.isOff");
        ButtonWidget useLAB = ButtonWidget.builder(
                Text.literal("LAB: ").append(MapartHelper.conversionSettings.useLAB ? isOn : isOff),
                (btn) -> {
                    MapartHelper.conversionSettings.useLAB = !MapartHelper.conversionSettings.useLAB;
                    btn.setMessage(Text.literal("LAB: ").append(MapartHelper.conversionSettings.useLAB ? isOn : isOff));
                    MapartImageConverter.updateMapart();
                }
        ).size(80, 20).build();

        if (MapartHelper.commonConfig.showUseLABTooltip) {
            useLAB.setTooltip(Tooltip.of(Text.translatable("maparthelper.gui.useLAB_tooltip")));
            useLAB.setTooltipDelay(Duration.ofMillis(500));
        }
        adder.add(useLAB);

        DropdownMenuWidget imagePreprocessing = createImagePreprocessingDropdown();
        imagePreprocessing.forEachEntry(this::addSelectableChild);
        adder.add(imagePreprocessing);

        adder.add(
                new TextWidget(Text.translatable("maparthelper.aux_block"), textRenderer),
                settingsLeftPositioner.copy().marginTop(15)
        );
        String currentAuxBlock = Registries.BLOCK.getId(MapartHelper.conversionSettings.auxBlock).toString();
        if (currentAuxBlock.contains("minecraft:"))
            currentAuxBlock = currentAuxBlock.substring(10);
        BlockItemWidget auxBlockPreview = new BlockItemWidget(0, 0, 24, MapartHelper.conversionSettings.auxBlock, false);
        TextFieldWidget auxBlockIdField = createTextInputFieldWidget(
                baseElementWidth - auxBlockPreview.getWidth() - 5,
                currentAuxBlock,
                -1
        );
        auxBlockIdField.setChangedListener(s -> {
            auxBlockIdField.setEditableColor(Colors.WHITE);
            int delimiterInd = s.indexOf(':');
            if (delimiterInd != -1 && !Identifier.isNamespaceValid(s.substring(0, delimiterInd))
                    || !Identifier.isPathValid(s.substring(delimiterInd + 1))
            ) {
                auxBlockIdField.setEditableColor(Colors.LIGHT_RED);
                return;
            }
            if (s.equals(Registries.BLOCK.getId(MapartHelper.conversionSettings.auxBlock).toString()))
                return;
            Identifier id = Identifier.of(s);
            Block newBlock = Registries.BLOCK.get(id);
            if (newBlock != Blocks.AIR && !NbtSchematicUtils.needsAuxBlock(newBlock)) {
                MapartHelper.conversionSettings.auxBlock = newBlock;
                auxBlockPreview.setBlock(newBlock);
                updateMaterialList();
                AutoConfig.getConfigHolder(MapartHelperConfig.class).save();
            } else {
                auxBlockIdField.setEditableColor(Colors.LIGHT_RED);
            }
        });
        GridWidget auxBlock = new GridWidget().setSpacing(5);
        auxBlock.getMainPositioner().alignVerticalCenter();
        GridWidget.Adder auxAdder = auxBlock.createAdder(2);
        auxAdder.add(auxBlockIdField);
        auxAdder.add(auxBlockPreview);
        adder.add(auxBlock);

        EnumDropdownMenuWidget useAuxBlocks = new EnumDropdownMenuWidget(
                this, 0, 0,
                baseElementWidth, 20, baseElementWidth,
                Text.translatable("maparthelper.gui.use_aux"),
                Text.translatable("maparthelper.gui.option." + MapartHelper.conversionSettings.useAuxBlocks)
        );
        useAuxBlocks.addEntries(
                e -> {
                    UseAuxBlocks was = MapartHelper.conversionSettings.useAuxBlocks;
                    MapartHelper.conversionSettings.useAuxBlocks = (UseAuxBlocks) e;
                    if (was != MapartHelper.conversionSettings.useAuxBlocks) {
                        updateMaterialList();
                        AutoConfig.getConfigHolder(MapartHelperConfig.class).save();
                    }
                },
                UseAuxBlocks.values()
        );
        useAuxBlocks.forEachEntry(this::addSelectableChild);
        adder.add(useAuxBlocks);

        settingsLeftScrollable.refreshPositions();
        this.addDrawableChild(settingsLeftScrollable);

        // =========== Presets and Material List area ===========

        settingsRight = DirectionalLayoutWidget.vertical();
        Positioner settingsRightPositioner = settingsRight.getMainPositioner().marginTop(5);

        PresetsDropdownMenuWidget presetsList = new PresetsDropdownMenuWidget(
                this, 0, 0, baseElementWidth, 20, baseElementWidth,
                Text.of("\"" + PaletteConfigManager.presetsConfig.getCurrentPresetName() + "\""), true
        );
        presetsList.addEntries(
                s -> {
                    PaletteConfigManager.changeCurrentPreset(s);
                    MapartImageConverter.updateMapart();
                },
                PaletteConfigManager.presetsConfig.presetFiles
        );
        settingsRight.add(new TextWidget(Text.translatable("maparthelper.gui.current_preset_label"), textRenderer));
        presetsList.forEachEntry(this::addSelectableChild);
        settingsRight.add(presetsList, settingsRightPositioner.copy().marginTop(0));

        ButtonWidget presetsEditor = ButtonWidget.builder(
                Text.translatable("maparthelper.gui.presets_editor_screen"),
                (btn) ->
                        MinecraftClient.getInstance().setScreen(
                                new PresetsEditorScreen(this, 45, 30, 45, 30)
                        )
        ).size(baseElementWidth, 20).build();
        settingsRight.add(presetsEditor);

        settingsRight.add(
                new TextWidget(Text.translatable("maparthelper.gui.material_list_label"), textRenderer),
                settingsRightPositioner.copy().marginTop(15)
        );

        settingsRight.refreshPositions();
        settingsRight.setPosition(width - settingsRight.getWidth() - 5, 20);
        settingsRight.forEachChild(this::addDrawableChild);

        updateMaterialList();

        // =========== Mapart preview area ===========

        mapartPreview = new MapartPreviewWidget(
                settingsLeft.getX() + settingsLeft.getWidth() + 15, 33,
                settingsRight.getX() - 15, this.height - 20
        );
        this.addDrawableChild(mapartPreview);

        DirectionalLayoutWidget mapartOptions = DirectionalLayoutWidget.horizontal().spacing(2);
        mapartOptions.setPosition(mapartPreview.getX(), 10);

        mapartOptions.add(createSaveMapartDropdown());

        ButtonWidget showGridButton = ButtonWidget.builder(
                Text.of("#"),
                (btn) -> CurrentConversionSettings.doShowGrid = !CurrentConversionSettings.doShowGrid
        ).size(20, 20).build();
        showGridButton.setTooltip(Tooltip.of(Text.translatable("maparthelper.gui.showGrid_tooltip")));
        mapartOptions.add(showGridButton);

        ButtonWidget showInWorldButton = ButtonWidget.builder(
                Text.of("\uD83C\uDF0D"),
                (btn) -> {
                    if (client == null || client.player == null) return;
                    if (FakeMapsPreview.createFakeFramesFromMapart(client.player)) {
                        FakeMapsPreview.showFakeFrames(client.player, CurrentConversionSettings.getWidth(), CurrentConversionSettings.getHeight());
                        this.close();
                    }
                }
        ).size(20, 20).build();
        showInWorldButton.setTooltip(Tooltip.of(Text.translatable("maparthelper.gui.showInWorld_tooltip")));
        mapartOptions.add(showInWorldButton);

        ButtonWidget resetMapartButton = ButtonWidget.builder(
                Text.of("⟲"),
                b -> {
                    CurrentConversionSettings.resetMapart();
                    updateMaterialList();
                }
        ).size(20, 20).build();
        resetMapartButton.setTooltip(Tooltip.of(Text.translatable("maparthelper.gui.reset_mapart")));
        mapartOptions.add(resetMapartButton);

        mapartOptions.refreshPositions();
        mapartOptions.forEachChild(this::addDrawableChild);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, settingsLeft.getX() + settingsLeft.getWidth() + 7, height, 0x77000000);
        context.fill(settingsRight.getX() - 7, 0, width, height, 0x77000000);
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (CurrentConversionSettings.cropMode == CroppingMode.USER_CROP) {
            if (mapartPreview.keyPressed(keyCode, scanCode, modifiers))
                return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (CurrentConversionSettings.cropMode == CroppingMode.USER_CROP) {
            if (mapartPreview.keyReleased(keyCode, scanCode, modifiers))
                return true;
        }
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        if (this.hoveredElement(mouseX, mouseY).orElse(null) == mapartPreview)
            mapartPreview.mouseMoved(mouseX, mouseY);
        super.mouseMoved(mouseX, mouseY);
    }

    @Override
    public void onFilesDropped(List<Path> paths) {
        CurrentConversionSettings.resetMapart();
        CurrentConversionSettings.imagePath = paths.getFirst();
        MapartImageConverter.readAndUpdateMapartImage(CurrentConversionSettings.imagePath);
    }


    private TextFieldWidget createTextInputFieldWidget(int width, String initialValue, int maxLength) {
        TextFieldWidget textInputField = new TextFieldWidget(textRenderer,
                width, 20,
                Text.empty()
        );
        if (maxLength != -1)
            textInputField.setMaxLength(maxLength);
        textInputField.setText(initialValue);
        return textInputField;
    }

    private GridWidget createSizeSettingsGrid() {
        GridWidget size = new GridWidget().setSpacing(10).setRowSpacing(1);
        GridWidget.Adder adder = size.createAdder(2);

        TextFieldWidget widthInput = createTextInputFieldWidget(
                30, "" + CurrentConversionSettings.getWidth(), 3
        );
        widthInput.setChangedListener(value -> {
            widthInput.setEditableColor(Colors.WHITE);
            if (value.isEmpty()) {
                widthInput.setSuggestion("x");
                return;
            }
            widthInput.setSuggestion(null);
            try {
                if (CurrentConversionSettings.setWidth(Integer.parseInt(value))) {
                    MapartImageConverter.updateMapart();
                }
            } catch (NumberFormatException e) {
                widthInput.setEditableColor(Colors.LIGHT_RED);
            }
        });

        TextFieldWidget heightInput = createTextInputFieldWidget(
                30, "" + CurrentConversionSettings.getHeight(), 3
        );
        heightInput.setChangedListener(value -> {
            heightInput.setEditableColor(Colors.WHITE);
            if (value.isEmpty()) {
                heightInput.setSuggestion("y");
                return;
            }
            heightInput.setSuggestion(null);
            try {
                if (CurrentConversionSettings.setHeight(Integer.parseInt(value))) {
                    MapartImageConverter.updateMapart();
                }
            } catch (NumberFormatException e) {
                heightInput.setEditableColor(Colors.LIGHT_RED);
            }
        });
        adder.add(new TextWidget(Text.translatable("maparthelper.gui.mapart_size_label"), textRenderer), 2);
        adder.add(widthInput);
        adder.add(heightInput);

        return size;
    }

    private ImageAdjustmentSliderWidget createBrightnessSlider() {
        Text brightness = Text.translatable("maparthelper.gui.brightness");
        return new ImageAdjustmentSliderWidget(
                baseElementWidth, 15, 0.f, 2.f, true,
                CurrentConversionSettings.brightness,
                value -> {
                    CurrentConversionSettings.brightness = value.floatValue();
                    MapartImageConverter.updateMapart();
                },
                value -> String.format(brightness.getString() + ": %.2f", value)
        );
    }

    private ImageAdjustmentSliderWidget createContrastSlider() {
        Text contrast = Text.translatable("maparthelper.gui.contrast");
        return new ImageAdjustmentSliderWidget(
                baseElementWidth, 15, -255, 255, false,
                CurrentConversionSettings.contrast,
                value -> {
                    CurrentConversionSettings.contrast = value.floatValue();
                    MapartImageConverter.updateMapart();
                },
                value -> String.format(contrast.getString() + ": %.0f", value)
        );
    }

    private ImageAdjustmentSliderWidget createSaturationSlider() {
        Text saturation = Text.translatable("maparthelper.gui.saturation");
        return new ImageAdjustmentSliderWidget(
                baseElementWidth, 15, 0.f, 2.f, true,
                CurrentConversionSettings.saturation,
                value -> {
                    CurrentConversionSettings.saturation = value.floatValue();
                    MapartImageConverter.updateMapart();
                },
                value -> String.format(saturation.getString() + ": %.2f", value)
        );
    }

    private DropdownMenuWidget createImagePreprocessingDropdown() {
        ImageAdjustmentSliderWidget sliderBrightness = createBrightnessSlider();
        ImageAdjustmentSliderWidget sliderContrast = createContrastSlider();
        ImageAdjustmentSliderWidget sliderSaturation = createSaturationSlider();

        ButtonWidget reset = ButtonWidget.builder(
                Text.translatable("maparthelper.gui.reset"),
                (btn) -> {
                    CurrentConversionSettings.brightness = 1.0f;
                    CurrentConversionSettings.contrast = 0.0f;
                    CurrentConversionSettings.saturation = 1.0f;
                    sliderBrightness.setValue(0.5f);
                    sliderContrast.setValue(0.5f);
                    sliderSaturation.setValue(0.5f);
                }
        ).size(80, 20).build();

        DropdownMenuWidget imagePreprocessing = new DropdownMenuWidget(
                this, 0, 0, 100, 20, baseElementWidth + 4, -1,
                Text.translatable("maparthelper.gui.image_preprocessing")
        );
        imagePreprocessing.addEntry(reset);
        imagePreprocessing.addEntry(sliderBrightness);
        imagePreprocessing.addEntry(sliderContrast);
        imagePreprocessing.addEntry(sliderSaturation);

        return imagePreprocessing;
    }

    private DropdownMenuWidget createSaveMapartDropdown() {
        ButtonWidget saveImage = ButtonWidget.builder(
                Text.translatable("maparthelper.gui.savePNG"),
                (btn) -> {
                    PlayerEntity player = client != null ? client.player : null;
                    MapartImageConverter.saveMapartImage(CurrentConversionSettings.mapartName, player);
                }
        ).size(156, 20).build();

        ButtonWidget saveNBT = ButtonWidget.builder(
                Text.translatable("maparthelper.gui.saveNBT"),
                (btn) -> MapartToNBT.saveNBT(true)
        ).size(156, 20).build();

        ButtonWidget saveSplitNBT = ButtonWidget.builder(
                Text.translatable("maparthelper.gui.saveEveryNBT"),
                (btn) -> MapartToNBT.saveNBT(false)
        ).size(156, 20).build();

        ButtonWidget saveZipNBT = ButtonWidget.builder(
                Text.translatable("maparthelper.gui.saveZip"),
                (btn) -> MapartToNBT.saveNBTAsZip()
        ).size(156, 20).build();

        DropdownMenuWidget saveMapart = new DropdownMenuWidget(this, 0, 0, 20, 20, 160, -1, Text.of("\uD83D\uDDAB"));
        saveMapart.setTooltip(Tooltip.of(Text.translatable("maparthelper.gui.save_mapart_as")));
        saveMapart.addEntry(saveImage);
        saveMapart.addEntry(saveNBT);
        saveMapart.addEntry(saveSplitNBT);
        saveMapart.addEntry(saveZipNBT);

        return saveMapart;
    }
}
