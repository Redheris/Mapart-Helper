package rh.maparthelper.gui;

import me.shedaniel.autoconfig.AutoConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.MapColor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.render.state.ItemGuiElementRenderState;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.*;
import net.minecraft.client.render.item.KeyedItemRenderState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.registry.Registries;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.joml.Matrix3x2f;
import rh.maparthelper.MapartHelper;
import rh.maparthelper.colors.MapColorEntry;
import rh.maparthelper.command.FakeMapsPreview;
import rh.maparthelper.config.ConversionConfiguration;
import rh.maparthelper.config.MapartHelperConfig;
import rh.maparthelper.config.UseAuxBlocks;
import rh.maparthelper.config.palette.PaletteColors;
import rh.maparthelper.config.palette.PaletteConfigManager;
import rh.maparthelper.config.palette.PalettePresetsConfig;
import rh.maparthelper.conversion.CroppingMode;
import rh.maparthelper.conversion.CurrentConversionSettings;
import rh.maparthelper.conversion.MapartImageUpdater;
import rh.maparthelper.conversion.dithering.DitheringAlgorithms;
import rh.maparthelper.conversion.mapart.ConvertedMapartImage;
import rh.maparthelper.conversion.schematic.MapartToNBT;
import rh.maparthelper.conversion.schematic.NbtSchematicUtils;
import rh.maparthelper.conversion.staircases.StaircaseStyles;
import rh.maparthelper.gui.widget.*;
import rh.maparthelper.render.ScaledItemGuiElementRenderer;
import rh.maparthelper.util.InventoryItemsCounter;

import java.nio.file.Path;
import java.time.Duration;
import java.util.*;

@Environment(EnvType.CLIENT)
public class MapartEditorScreen extends ScreenAdapted {
    private static final Identifier SETTINGS_TEXTURE = Identifier.of(MapartHelper.MOD_ID, "textures/gui/sprites/mapart_editor/settings.png");
    protected final ConvertedMapartImage mapart = CurrentConversionSettings.mapart;

    private DirectionalLayoutWidget settingsLeft;
    private DirectionalLayoutWidget settingsRight;
    private ScrollableGridWidget materialList;
    private MapartPreviewWidget mapartPreview;
    private final int baseElementWidth = 165;

    private ButtonWidget saveNBT;
    private ButtonWidget saveSplitNBT;
    private ButtonWidget saveZipNBT;
    private ButtonWidget showInWorldButton;
    private ButtonWidget resetExcludedColors;

    private int auxBlockCount = 0;
    private static boolean materialsAscendingOrder = false;
    private static boolean displayRemainingAmount = false;
    private final InventoryItemsCounter inventoryItemsCounter = new InventoryItemsCounter();

    public MapartEditorScreen() {
        super(Text.translatable("maparthelper.gui.mapart_editor_screen"));
        if (MinecraftClient.getInstance().player != null) {
            inventoryItemsCounter.count(MinecraftClient.getInstance().player.getInventory());
        }
    }

    public void updateMaterialList() {
        MaterialListBlockWidget.fixedHighlight = null;
        MaterialListBlockWidget.selectedForExcluding.clear();
        this.remove(materialList);

        if (!CurrentConversionSettings.isMapartConverted()) return;

        int listTop = settingsRight.getY() + settingsRight.getHeight();
        materialList = new ScrollableGridWidget(
                null,
                settingsRight.getX() - 6, listTop,
                width - settingsRight.getX() - 5, height - listTop, 6
        );
        materialList.setLeftScroll(true);
        materialList.grid.setColumnSpacing(0);
        materialList.grid.getMainPositioner().alignVerticalCenter();

        GridWidget.Adder materialListAdder = materialList.grid.createAdder(2);
        PalettePresetsConfig palette = PaletteConfigManager.presetsConfig;

        ConvertedMapartImage.MapColorCount[] colorsCounter = mapart.getColorCounts(materialsAscendingOrder);

        this.auxBlockCount = mapart.getWidth() * 128;
        BlockItemWidget auxBlockItemWidget = new BlockItemWidget(0, 0, 24, MapartHelper.conversionSettings.auxBlock);
        auxBlockItemWidget.insertToTooltip(1, Text.translatable("maparthelper.aux_block").formatted(Formatting.GRAY));

        TextWidget auxAmountText = new TextWidget(Text.empty(), textRenderer);
        materialListAdder.add(auxBlockItemWidget, materialList.grid.copyPositioner().marginLeft(6));
        materialListAdder.add(auxAmountText);

        if (displayRemainingAmount) calculateRemainingCounts(colorsCounter);

        for (ConvertedMapartImage.MapColorCount colorCount : colorsCounter) {
            addBlockToMaterialList(materialListAdder, palette, colorCount);
        }

        MutableText amountText = Text.literal(getAmountString(auxBlockCount, auxBlockItemWidget.getStackSize()));
        if (auxBlockCount == 0)
            amountText = amountText.formatted(Formatting.GREEN);
        auxAmountText.setWidth(textRenderer.getWidth(amountText));
        auxAmountText.setMessage(amountText);
        auxAmountText.setTooltip(Tooltip.of(amountText));

        materialList.refreshPositions();
        this.addDrawableChild(materialList);
    }

    private void addBlockToMaterialList(GridWidget.Adder adder, PalettePresetsConfig palette, ConvertedMapartImage.MapColorCount color) {
        MapColor mapColor = MapColor.get(color.id());
        Block block = palette.getBlockOfMapColor(mapColor);
        if (block == null) return;

        MaterialListBlockWidget blockItemWidget = new MaterialListBlockWidget(0, 0, 24, block, mapColor);
        adder.add(blockItemWidget, materialList.grid.copyPositioner().marginLeft(6));
        MutableText text = Text.literal(getAmountString(color.amount(), block.asItem().getMaxCount()));
        if (color.amount() == 0)
            text = text.formatted(Formatting.GREEN);
        TextWidget amountText = new TextWidget(text, textRenderer);
        adder.add(amountText);
        amountText.setTooltip(Tooltip.of(amountText.getMessage()));
        amountText.setTooltipDelay(Duration.ofMillis(100));

        if (NbtSchematicUtils.needsAuxBlock(block)) {
            auxBlockCount += color.amount();
        }
    }

    private void calculateRemainingCounts(ConvertedMapartImage.MapColorCount[] colors) {
        PlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) return;

        PalettePresetsConfig paletteConfig = PaletteConfigManager.presetsConfig;

        List<Item> countingBlocks = Arrays.stream(colors)
                .map(c -> paletteConfig.getBlockOfMapColor(MapColor.get(c.id())).asItem())
                .toList();

        Item auxBlockItem = MapartHelper.conversionSettings.auxBlock.asItem();
        Map<Item, Integer> inventory = inventoryItemsCounter.getCounts();

        for (int i = 0; i < colors.length; i++) {
            Item item = countingBlocks.get(i);
            int have = inventory.getOrDefault(item, 0);
            int remaining = colors[i].amount() - have;
            if (item == auxBlockItem) {
                auxBlockCount = Math.max(0, auxBlockCount + Math.min(0, remaining));
            }
            colors[i] = new ConvertedMapartImage.MapColorCount(colors[i].id(), Math.max(0, remaining));
        }
        if (!countingBlocks.contains(auxBlockItem)) {
            auxBlockCount = Math.max(0, auxBlockCount - inventory.getOrDefault(auxBlockItem, 0));
        }

        Comparator<ConvertedMapartImage.MapColorCount> cmp = Comparator.comparingInt(ConvertedMapartImage.MapColorCount::amount);
        Arrays.sort(colors, materialsAscendingOrder ? cmp : cmp.reversed());
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
        if (MinecraftClient.getInstance().player == null) displayRemainingAmount = false;

        settingsLeft = DirectionalLayoutWidget.vertical();
        settingsLeft.setPosition(5, 20);
        Positioner settingsLeftPositioner = settingsLeft.getMainPositioner().marginTop(5);

        TextFieldWidget mapartName = createTextInputFieldWidget(baseElementWidth, mapart.mapartName, -1);
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
            mapart.mapartName = value;
        });
        settingsLeft.add(new TextWidget(Text.translatable("maparthelper.gui.mapart_name_field"), textRenderer));
        settingsLeft.add(mapartName, settingsLeftPositioner.copy().marginTop(0));

        GridWidget size = createSizeSettingsGrid();
        settingsLeft.add(size);

        settingsLeft.refreshPositions();
        settingsLeft.forEachChild(this::addDrawableChild);

        this.addDrawableChild(
                DecorativeButtonWidget.builder(
                        SETTINGS_TEXTURE, btn -> MinecraftClient.getInstance().setScreen(
                                AutoConfig.getConfigScreen(MapartHelperConfig.class, this).get()
                        )
                ).dimensions(2, 4, 14, 14).build()
        );

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
                    MapartImageUpdater.updateMapart(mapart);
                    updateResetExcludedColorsButton();
                }
        ).size(baseElementWidth, 20).build();
        adder.add(new TextWidget(Text.translatable("maparthelper.gui.previewMode"), textRenderer));
        adder.add(previewMode, settingsLeftPositioner.copy().marginTop(0));

        // The button will be added in the Mapart preview area below
        ButtonWidget toggleManualCroppingButtonsButton = ButtonWidget.builder(
                Text.literal("\uD83D\uDDBC").formatted(CurrentConversionSettings.doShowManualCroppingButtons ? Formatting.RESET : Formatting.DARK_GRAY),
                (btn) -> {
                    boolean doShowManualCroppingButtons = !CurrentConversionSettings.doShowManualCroppingButtons;
                    CurrentConversionSettings.doShowManualCroppingButtons = doShowManualCroppingButtons;
                    btn.setMessage(btn.getMessage().copy().formatted(doShowManualCroppingButtons ? Formatting.RESET : Formatting.DARK_GRAY));
                }
        ).size(20, 20).build();
        // ===============================================

        EnumDropdownMenuWidget croppingMode = new EnumDropdownMenuWidget(
                this, 0, 0, baseElementWidth, 20, baseElementWidth,
                Text.translatable("maparthelper.gui.cropMode"),
                Text.translatable("maparthelper.gui.option." + CurrentConversionSettings.cropMode.name())
        );
        croppingMode.addEntries(
                e -> {
                    CroppingMode cropMode = (CroppingMode) e;
                    CurrentConversionSettings.cropMode = cropMode;
                    toggleManualCroppingButtonsButton.active = cropMode == CroppingMode.USER_CROP;
                    MapartImageUpdater.changeCroppingMode(mapart, cropMode);
                },
                CroppingMode.values()
        );
        adder.add(croppingMode);

        EnumDropdownMenuWidget staircaseStyle = new EnumDropdownMenuWidget(
                this, 0, 0, baseElementWidth, 20, baseElementWidth,
                Text.translatable("maparthelper.gui.staircaseStyle"),
                Text.translatable("maparthelper.gui.option." + MapartHelper.conversionSettings.staircaseStyle.name())
        );
        staircaseStyle.toggleTooltips(MapartHelper.commonConfig.mapartEditor.showStaircaseTooltips);
        staircaseStyle.addEntries(
                e -> {
                    ConversionConfiguration config = MapartHelper.conversionSettings;
                    boolean was3D = config.use3D();
                    config.staircaseStyle = (StaircaseStyles) e;
                    if (config.use3D() != was3D)
                        MapartImageUpdater.updateMapart(mapart);
                    AutoConfig.getConfigHolder(MapartHelperConfig.class).save();
                },
                StaircaseStyles.values()
        );
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
                    MapartImageUpdater.updateMapart(mapart);
                    AutoConfig.getConfigHolder(MapartHelperConfig.class).save();
                },
                DitheringAlgorithms.values()
        );
        adder.add(ditheringAlg);

        Text isOn = Text.translatable("maparthelper.gui.isOn");
        Text isOff = Text.translatable("maparthelper.gui.isOff");
        ButtonWidget useLAB = ButtonWidget.builder(
                Text.literal("LAB: ").append(MapartHelper.conversionSettings.useLAB ? isOn : isOff),
                (btn) -> {
                    MapartHelper.conversionSettings.useLAB = !MapartHelper.conversionSettings.useLAB;
                    btn.setMessage(Text.literal("LAB: ").append(MapartHelper.conversionSettings.useLAB ? isOn : isOff));
                    MapartImageUpdater.updateMapart(mapart);
                }
        ).size(80, 20).build();

        if (MapartHelper.commonConfig.mapartEditor.showUseLABTooltip) {
            useLAB.setTooltip(Tooltip.of(Text.translatable("maparthelper.gui.useLAB_tooltip")));
            useLAB.setTooltipDelay(Duration.ofMillis(500));
        }
        adder.add(useLAB);

        DropdownMenuWidget imagePreprocessing = createImagePreprocessingDropdown();
        adder.add(imagePreprocessing);

        GridWidget bgColor = new GridWidget().setColumnSpacing(2);
        bgColor.getMainPositioner().alignVerticalCenter();
        GridWidget.Adder bgAdder = bgColor.createAdder(2);
        DropdownMenuWidget colorPicker = new MapColorPickerWidget(this, 0, 0, 20, 20, baseElementWidth, 180, 4,
                () -> MapartHelper.conversionSettings.backgroundColor
        );
        new MapColorsPaletteWidget(
                0, 2, baseElementWidth - 4, 20, 4,
                c -> {
                    MapColorEntry current = MapartHelper.conversionSettings.backgroundColor;
                    if (current.mapColor() != c.mapColor() || current.brightness() != c.brightness()) {
                        MapartHelper.conversionSettings.backgroundColor = c;
                        AutoConfig.getConfigHolder(MapartHelperConfig.class).save();
                        MapartImageUpdater.updateMapart(mapart);
                    }
                }
        ).forEachChild(colorPicker::addEntry);

        bgAdder.add(new TextWidget(Text.translatable("maparthelper.gui.backgroundColor"), textRenderer));
        bgAdder.add(colorPicker);
        adder.add(bgColor);

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
                    PaletteColors.clearExcludingColors();
                    updateResetExcludedColorsButton();
                    PaletteConfigManager.changeCurrentPreset(s);
                    MapColor oldBgColor = MapartHelper.conversionSettings.backgroundColor.mapColor();
                    if (PaletteConfigManager.presetsConfig.getBlockOfMapColor(oldBgColor) == null) {
                        MapartHelper.conversionSettings.backgroundColor = MapColorEntry.CLEAR;
                    }
                    MapartImageUpdater.updateMapart(mapart);
                },
                PaletteConfigManager.presetsConfig.presetFiles
        );
        settingsRight.add(new TextWidget(Text.translatable("maparthelper.gui.current_preset_label"), textRenderer));
        settingsRight.add(presetsList, settingsRightPositioner.copy().marginTop(0));

        ButtonWidget presetsEditor = ButtonWidget.builder(
                Text.translatable("maparthelper.gui.presets_editor_screen"),
                (btn) ->
                        MinecraftClient.getInstance().setScreen(
                                new PresetsEditorScreen(this, 45, 30, 45, 30)
                        )
        ).size(baseElementWidth, 20).build();
        settingsRight.add(presetsEditor);

        DirectionalLayoutWidget materialListSettings = DirectionalLayoutWidget.horizontal().spacing(2);
        materialListSettings.getMainPositioner().alignBottom();
        materialListSettings.add(DecorativeButtonWidget.builder(
                Text.of(materialsAscendingOrder ? "▲" : "▼"),
                btn -> {
                    materialsAscendingOrder = !materialsAscendingOrder;
                    btn.setMessage(Text.of(materialsAscendingOrder ? "▲" : "▼"));
                    updateMaterialList();
                }
        ).size(10, 10).build());
        materialListSettings.add(new TextWidget(Text.translatable("maparthelper.gui.material_list_label"), textRenderer));
        resetExcludedColors = ButtonWidget.builder(
                Text.literal("⟲")
                        .formatted(Formatting.BOLD)
                        .formatted(PaletteColors.excludingColorsAmount() > 0 ? Formatting.GOLD : Formatting.WHITE),
                btn -> {
                    if (PaletteColors.excludingColorsAmount() == 0) return;
                    MapartImageUpdater.revertRemovingColors(mapart);
                    btn.setMessage(btn.getMessage().copy().formatted(Formatting.BOLD, Formatting.WHITE));
                    btn.setTooltip(null);
                }
        ).size(14, 14).build();
        updateResetExcludedColorsButton();
        materialListSettings.add(resetExcludedColors);
        settingsRight.add(materialListSettings);

        if (MinecraftClient.getInstance().player != null) {
            Text remaining = Text.translatable("maparthelper.gui.amount_remaining").formatted(Formatting.GOLD);
            Text description = Text.translatable("maparthelper.gui.amount_remaining_description");
            Text total = Text.translatable("maparthelper.gui.amount_total");
            ButtonWidget amountDisplayMode = ButtonWidget
                    .builder(displayRemainingAmount ? remaining : total,
                            btn -> {
                                displayRemainingAmount = !displayRemainingAmount;
                                btn.setMessage(displayRemainingAmount ? remaining : total);
                                btn.setTooltip(displayRemainingAmount ? Tooltip.of(description) : null);
                                updateMaterialList();
                            })
                    .size(baseElementWidth, 14)
                    .build();
            amountDisplayMode.setTooltip(displayRemainingAmount ? Tooltip.of(description) : null);
            settingsRight.add(amountDisplayMode);
        }

        settingsRight.refreshPositions();
        settingsRight.setPosition(width - settingsRight.getWidth() - 5, 20);
        settingsRight.forEachChild(this::addDrawableChild);

        // Widget positions adjustments
        resetExcludedColors.setX(width - 5 - resetExcludedColors.getWidth());

        updateMaterialList();

        // =========== Mapart preview area ===========

        mapartPreview = new MapartPreviewWidget(mapart,
                settingsLeft.getX() + settingsLeft.getWidth() + 9, 33,
                settingsRight.getX() - 15, this.height - 20
        );
        this.addDrawableChild(mapartPreview);

        DirectionalLayoutWidget mapartOptions = DirectionalLayoutWidget.horizontal().spacing(2);
        mapartOptions.setPosition(mapartPreview.getImageX(), 10);

        mapartOptions.add(createSaveMapartDropdown());

        ButtonWidget showGridButton = ButtonWidget.builder(
                Text.literal("#").formatted(CurrentConversionSettings.doShowGrid ? Formatting.AQUA : Formatting.RESET),
                (btn) -> {
                    boolean doShowGrid = !CurrentConversionSettings.doShowGrid;
                    CurrentConversionSettings.doShowGrid = doShowGrid;
                    btn.setMessage(btn.getMessage().copy().formatted(doShowGrid ? Formatting.AQUA : Formatting.RESET));
                }
        ).size(20, 20).build();
        showGridButton.setTooltip(Tooltip.of(Text.translatable("maparthelper.gui.showGrid_tooltip")));
        mapartOptions.add(showGridButton);

        showInWorldButton = ButtonWidget.builder(
                Text.of("\uD83C\uDF0D"),
                (btn) -> {
                    if (client == null || client.player == null) return;
                    if (FakeMapsPreview.createFakeFramesFromMapart(mapart, client.player)) {
                        FakeMapsPreview.showFakeFrames(client.player, mapart.getWidth(), mapart.getHeight());
                        this.close();
                    }
                }
        ).size(20, 20).build();
        showInWorldButton.setTooltip(Tooltip.of(Text.translatable("maparthelper.gui.showInWorld_tooltip")));
        mapartOptions.add(showInWorldButton);

        toggleManualCroppingButtonsButton.active = CurrentConversionSettings.cropMode == CroppingMode.USER_CROP;
        toggleManualCroppingButtonsButton.setTooltip(Tooltip.of(Text.translatable("maparthelper.gui.toggle_manual_cropping_buttons")));
        mapartOptions.add(toggleManualCroppingButtonsButton);

        ButtonWidget resetMapartButton = ButtonWidget.builder(
                Text.literal("⟲").formatted(Formatting.BOLD),
                b -> {
                    CurrentConversionSettings.resetMapart();
                    updateMapartOutputButtons();
                    updateMaterialList();
                    updateResetExcludedColorsButton();
                }
        ).size(20, 20).build();
        resetMapartButton.setTooltip(Tooltip.of(Text.translatable("maparthelper.gui.reset_mapart")));
        mapartOptions.add(resetMapartButton);

        mapartOptions.refreshPositions();
        mapartOptions.forEachChild(this::addDrawableChild);

        updateMapartOutputButtons();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, settingsLeft.getX() + settingsLeft.getWidth() + 7, height, 0x77000000);
        context.fill(settingsRight.getX() - 7, 0, width, height, 0x77000000);
        super.render(context, mouseX, mouseY, delta);

        if (!MaterialListBlockWidget.hoveringAny) {
            MaterialListBlockWidget.setDefaultHighlight(mapartPreview);
        }
        MaterialListBlockWidget.hoveringAny = false;
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
    public void onFilesDropped(List<Path> paths) {
        CurrentConversionSettings.resetMapart();
        MapartImageUpdater.readAndUpdateMapartImage(mapart, paths.getFirst());
        updateResetExcludedColorsButton();
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
                30, "" + mapart.getWidth(), 3
        );
        widthInput.setChangedListener(value -> {
            widthInput.setEditableColor(Colors.WHITE);
            if (value.isEmpty()) {
                widthInput.setSuggestion("x");
                return;
            }
            widthInput.setSuggestion(null);
            try {
                int newWidth = Integer.parseInt(value);
                if (newWidth <= 0) {
                    widthInput.setEditableColor(Colors.LIGHT_RED);
                } else if (newWidth != mapart.getWidth()) {
                    CurrentConversionSettings.guiMapartImage = null;
                    MapartImageUpdater.resizeMapartImage(mapart, newWidth, mapart.getHeight());
                }
            } catch (NumberFormatException e) {
                widthInput.setEditableColor(Colors.LIGHT_RED);
            }
        });

        TextFieldWidget heightInput = createTextInputFieldWidget(
                30, "" + mapart.getHeight(), 3
        );
        heightInput.setChangedListener(value -> {
            heightInput.setEditableColor(Colors.WHITE);
            if (value.isEmpty()) {
                heightInput.setSuggestion("y");
                return;
            }
            heightInput.setSuggestion(null);
            try {
                int newHeight = Integer.parseInt(value);
                if (newHeight <= 0) {
                    heightInput.setEditableColor(Colors.LIGHT_RED);
                } else if (newHeight != mapart.getHeight()) {
                    CurrentConversionSettings.guiMapartImage = null;
                    MapartImageUpdater.resizeMapartImage(mapart, mapart.getWidth(), newHeight);
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
                    MapartImageUpdater.updateMapart(mapart);
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
                    MapartImageUpdater.updateMapart(mapart);
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
                    MapartImageUpdater.updateMapart(mapart);
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
                    mapart.saveMapartImage(player);
                }
        ).size(156, 20).build();

        saveNBT = ButtonWidget.builder(
                Text.translatable("maparthelper.gui.saveNBT"),
                (btn) -> MapartToNBT.saveNBT(true)
        ).size(156, 20).build();

        saveSplitNBT = ButtonWidget.builder(
                Text.translatable("maparthelper.gui.saveEveryNBT"),
                (btn) -> MapartToNBT.saveNBT(false)
        ).size(156, 20).build();

        saveZipNBT = ButtonWidget.builder(
                Text.translatable("maparthelper.gui.saveZip"),
                (btn) -> MapartToNBT.saveNBTAsZip()
        ).size(156, 20).build();

        DropdownMenuWidget saveMapart = new DropdownMenuWidget(this, 0, 0, 20, 20, 160, -1, Text.literal("\uD83D\uDDAB"));
        saveMapart.setTooltip(Tooltip.of(Text.translatable("maparthelper.gui.save_mapart_as")));
        saveMapart.addEntry(saveImage);
        saveMapart.addEntry(saveNBT);
        saveMapart.addEntry(saveSplitNBT);
        saveMapart.addEntry(saveZipNBT);

        return saveMapart;
    }

    public void updateMapartOutputButtons() {
        boolean active = CurrentConversionSettings.isMapartConverted();
        saveNBT.active = active;
        saveSplitNBT.active = active;
        saveZipNBT.active = active;
        showInWorldButton.active = active;
        if (active) {
            saveNBT.setTooltip(null);
            saveSplitNBT.setTooltip(null);
            saveZipNBT.setTooltip(null);
            showInWorldButton.setTooltip(Tooltip.of(Text.translatable("maparthelper.gui.showInWorld_tooltip")));
        } else {
            Tooltip disabled = Tooltip.of(Text.translatable("maparthelper.gui.enableColorAdaptation"));
            saveNBT.setTooltip(disabled);
            saveSplitNBT.setTooltip(disabled);
            saveZipNBT.setTooltip(disabled);
            showInWorldButton.setTooltip(disabled);
        }
    }

    private void updateResetExcludedColorsButton() {
        int excluded = PaletteColors.excludingColorsAmount();
        if (excluded > 0) {
            MutableText excludedAmount = Text.translatable("maparthelper.gui.excluded_colors_amount", excluded);
            MutableText revertExcluding = Text.translatable("maparthelper.gui.revert_excluding_colors");
            resetExcludedColors.setMessage(resetExcludedColors.getMessage().copy().formatted(Formatting.BOLD, Formatting.GOLD));
            resetExcludedColors.setTooltip(Tooltip.of(
                    excludedAmount.formatted(Formatting.GOLD).append("\n")
                            .append(revertExcluding.formatted(Formatting.GRAY))
            ));
        } else {
            resetExcludedColors.setMessage(resetExcludedColors.getMessage().copy().formatted(Formatting.BOLD, Formatting.WHITE));
            resetExcludedColors.setTooltip(null);
        }
    }

    private class MaterialListBlockWidget extends BlockItemWidget {
        static final Set<MapColor> selectedForExcluding = new HashSet<>();
        static MaterialListBlockWidget fixedHighlight;
        static boolean hoveringAny = false;
        private final MapColor mapColor;
        private boolean confirmRemoving = false;

        public MaterialListBlockWidget(int x, int y, int squareSize, Block block, MapColor mapColor) {
            super(x, y, squareSize, block);
            this.mapColor = mapColor;
            this.tooltip = new ArrayList<>(List.of(
                    block.getName().asOrderedText(),
                    Text.translatable("maparthelper.gui.LMB_to_highlight").formatted(Formatting.GRAY).asOrderedText(),
                    Text.translatable("maparthelper.gui.RMB_to_remove").formatted(Formatting.GRAY).asOrderedText()
            ));
            if (client != null && client.options.advancedItemTooltips)
                this.tooltip.add(Text.literal(Registries.BLOCK.getId(block).toString()).formatted(Formatting.DARK_GRAY).asOrderedText());
        }

        @Override
        protected void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
            super.renderWidget(context, mouseX, mouseY, deltaTicks);
            if (fixedHighlight == this) {
                context.createNewRootLayer();
                context.drawBorder(getX(), getY(), this.width, this.height, MapartHelper.commonConfig.mapartEditor.previewHighlightingColor);
            } else if (MapartHelper.commonConfig.mapartEditor.previewHighlightOnHover
                    && context.scissorContains(mouseX, mouseY) && isMouseOver(mouseX, mouseY)) {
                mapartPreview.setHighlightingColor(mapColor);
                hoveringAny = true;
            }
            if (confirmRemoving && client != null) {
                KeyedItemRenderState keyedItemRenderState = new KeyedItemRenderState();
                client.getItemModelManager().clearAndUpdate(keyedItemRenderState, Blocks.BARRIER.asItem().getDefaultStack(), ItemDisplayContext.GUI, client.world, client.player, 0);
                ItemGuiElementRenderState itemRenderState = new ItemGuiElementRenderState(
                        "RemoveColor",
                        new Matrix3x2f(context.getMatrices()),
                        keyedItemRenderState,
                        getX(), getY(),
                        context.scissorStack.peekLast()
                );
                context.state.addSpecialElement(new ScaledItemGuiElementRenderer.ScaledItemGuiElementRenderState(
                        itemRenderState,
                        getX(), getY(),
                        getX() + width, getY() + height,
                        width
                ));
            }
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (button == 0) {
                if (confirmRemoving) {
                    MapartImageUpdater.removeColorsFromMapart(mapart, selectedForExcluding);
                    updateResetExcludedColorsButton();
                    selectedForExcluding.clear();
                    return true;
                }
                if (fixedHighlight == this) {
                    fixedHighlight = null;
                } else {
                    fixedHighlight = this;
                    mapartPreview.setHighlightingColor(mapColor);
                }
                return true;
            }
            if (button == 1) {
                if (fixedHighlight == this) {
                    fixedHighlight = null;
                }
                if (!confirmRemoving) {
                    confirmRemoving = true;
                    selectedForExcluding.add(mapColor);
                    tooltip.set(1, Text.translatable("maparthelper.gui.LMB_to_confirm").formatted(Formatting.RED).asOrderedText());
                    tooltip.set(2, Text.translatable("maparthelper.gui.RMB_to_cancel").formatted(Formatting.RED).asOrderedText());
                } else {
                    confirmRemoving = false;
                    selectedForExcluding.remove(mapColor);
                    tooltip.set(1, Text.translatable("maparthelper.gui.LMB_to_highlight").formatted(Formatting.GRAY).asOrderedText());
                    tooltip.set(2, Text.translatable("maparthelper.gui.RMB_to_remove").formatted(Formatting.GRAY).asOrderedText());
                }
            }
            return true;
        }

        public static void setDefaultHighlight(MapartPreviewWidget mapartPreview) {
            if (fixedHighlight == null)
                mapartPreview.setHighlightingColor(MapColor.CLEAR);
            else
                mapartPreview.setHighlightingColor(fixedHighlight.mapColor);
        }
    }
}
