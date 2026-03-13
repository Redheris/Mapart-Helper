package rh.maparthelper.gui.screen;

import me.shedaniel.autoconfig.AutoConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import rh.maparthelper.MapartHelper;
import rh.maparthelper.colors.MapColorEntry;
import rh.maparthelper.command.FakeMapsPreview;
import rh.maparthelper.config.MapartHelperConfig;
import rh.maparthelper.config.MaterialsCountModes;
import rh.maparthelper.config.UseAuxBlocks;
import rh.maparthelper.config.palette.PaletteColors;
import rh.maparthelper.config.palette.PaletteConfigManager;
import rh.maparthelper.conversion.CroppingMode;
import rh.maparthelper.conversion.CurrentConversionSettings;
import rh.maparthelper.conversion.MapartImageUpdater;
import rh.maparthelper.conversion.NativeImageUtils;
import rh.maparthelper.conversion.dithering.ColorConverters;
import rh.maparthelper.conversion.schematic.MapartToNBT;
import rh.maparthelper.conversion.staircases.StaircaseStyles;
import rh.maparthelper.gui.input.TextFieldPredicates;
import rh.maparthelper.gui.input.TextFieldValidators;
import rh.maparthelper.gui.screen.panel.ImagePreprocessingDropdown;
import rh.maparthelper.gui.screen.panel.MaterialListPanel;
import rh.maparthelper.gui.widget.*;
import rh.maparthelper.gui.widget.input.AdjTextFieldWidget;
import rh.maparthelper.mapart.MapartProcessing;
import rh.maparthelper.mapart.MapartSaver;
import rh.maparthelper.server.MapCreator;
import rh.maparthelper.util.FileDialogsUtils;

import java.nio.file.Path;
import java.time.Duration;
import java.util.List;

@Environment(EnvType.CLIENT)
public class MapartEditorScreen extends ScreenAdapted {
    private static final Identifier SETTINGS_TEXTURE = Identifier.of(MapartHelper.MOD_ID, "textures/gui/sprites/mapart_editor/settings.png");
    protected final MapartProcessing mapart = CurrentConversionSettings.mapart;

    private DirectionalLayoutWidget settingsLeft;
    private DirectionalLayoutWidget settingsRight;
    private MapartPreviewWidget mapartPreview;
    private final int baseElementWidth = 165;

    private ButtonWidget saveNBT;
    private ButtonWidget saveSplitNBT;
    private ButtonWidget saveZipNBT;
    private ButtonWidget getMapItemsButton;
    private ButtonWidget showInWorldButton;
    private ButtonWidget resetExcludedColors;

    private final MaterialListPanel materialList = new MaterialListPanel(this, mapart, 0, 0, 0, 0);

    public MapartEditorScreen() {
        super(Text.translatable("maparthelper.gui.mapart_editor_screen"));
    }

    public void setHighlightingColor(MapColor color) {
        mapartPreview.setHighlightingColor(color);
    }

    public void updateMaterialList() {
        materialList.updateMaterialList(this::addDrawableChild, this::remove);
    }

    @Override
    protected void init() {
        super.init();

        settingsLeft = DirectionalLayoutWidget.vertical();
        settingsLeft.setPosition(5, 20);
        Positioner settingsLeftPositioner = settingsLeft.getMainPositioner().marginTop(5);

        AdjTextFieldWidget mapartName = new AdjTextFieldWidget(
                textRenderer, baseElementWidth, 20, mapart.mapartName, "Mapart name"
        );
        mapartName.setPlaceholder(Text.translatable("maparthelper.gui.mapart_name_field").withColor(Colors.GRAY));
        mapartName.setValueValidator(s -> !s.isBlank());
        mapartName.setTextPredicate(TextFieldPredicates.validPathName());
        mapartName.setValueConsumer(newValue -> mapart.mapartName = newValue);
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
                MapartHelper.conversionSettings.isShowOriginalImage() ? previewOriginal : previewMapart,
                (btn) -> {
                    MapartHelper.conversionSettings.toggleShowOriginalImage();
                    btn.setMessage(MapartHelper.conversionSettings.isShowOriginalImage() ? previewOriginal : previewMapart);
                    MapartImageUpdater.updateMapart(mapart);
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
                Text.translatable("maparthelper.gui.option." + MapartHelper.conversionSettings.getStaircaseStyle().name())
        );
        staircaseStyle.toggleTooltips(MapartHelper.commonConfig.mapartEditor.showStaircaseTooltips);
        staircaseStyle.addEntries(
                e -> {
                    if (MapartHelper.conversionSettings.setStaircaseStyle((StaircaseStyles) e))
                        MapartImageUpdater.updateMapart(mapart);
                },
                StaircaseStyles.values()
        );
        adder.add(staircaseStyle);

        EnumDropdownMenuWidget colorConverter = new EnumDropdownMenuWidget(
                this, 0, 0, baseElementWidth, 20, baseElementWidth,
                Text.translatable("maparthelper.gui.ditheringAlg"),
                Text.translatable("maparthelper.gui.option." + MapartHelper.conversionSettings.getColorConverter().name())
        );
        colorConverter.setLeftScroll(true);
        colorConverter.addEntries(
                e -> {
                    MapartHelper.conversionSettings.setColorConverter((ColorConverters) e);
                    MapartImageUpdater.updateMapart(mapart);
                },
                ColorConverters.values()
        );
        adder.add(colorConverter);

        Text isOn = Text.translatable("maparthelper.gui.isOn");
        Text isOff = Text.translatable("maparthelper.gui.isOff");
        ButtonWidget useLAB = ButtonWidget.builder(
                Text.literal("LAB: ").append(MapartHelper.conversionSettings.useLAB() ? isOn : isOff),
                (btn) -> {
                    MapartHelper.conversionSettings.toggleLAB();
                    btn.setMessage(Text.literal("LAB: ").append(MapartHelper.conversionSettings.useLAB() ? isOn : isOff));
                    MapartImageUpdater.updateMapart(mapart);
                }
        ).size(80, 20).build();

        if (MapartHelper.commonConfig.mapartEditor.showUseLABTooltip) {
            useLAB.setTooltip(Tooltip.of(Text.translatable("maparthelper.gui.useLAB_tooltip")));
            useLAB.setTooltipDelay(Duration.ofMillis(500));
        }
        adder.add(useLAB);

        adder.add(new ImagePreprocessingDropdown(this, mapart, 100, baseElementWidth));

        DirectionalLayoutWidget bgColorChoose = DirectionalLayoutWidget.horizontal();
        bgColorChoose.getMainPositioner().alignVerticalCenter();
        bgColorChoose.add(new TextWidget(Text.translatable("maparthelper.gui.backgroundColor"), textRenderer));
        bgColorChoose.add(new MapColorPickerWidget(
                this, mapart, 20, 20,
                baseElementWidth, 180, 4
        ));
        adder.add(bgColorChoose);

        adder.add(
                new TextWidget(Text.translatable("maparthelper.aux_block"), textRenderer),
                settingsLeftPositioner.copy().marginTop(15)
        );
        String currentAuxBlock = Registries.BLOCK.getId(MapartHelper.conversionSettings.getAuxBlock()).toString();
        if (currentAuxBlock.contains("minecraft:"))
            currentAuxBlock = currentAuxBlock.substring(10);
        BlockItemWidget auxBlockPreview = new BlockItemWidget(0, 0, 24, MapartHelper.conversionSettings.getAuxBlock(), false);

        GridWidget auxBlock = new GridWidget().setSpacing(5);
        auxBlock.getMainPositioner().alignVerticalCenter();
        GridWidget.Adder auxAdder = auxBlock.createAdder(2);
        auxAdder.add(createAuxBlockFieldWidget(auxBlockPreview, currentAuxBlock));
        auxAdder.add(auxBlockPreview);
        adder.add(auxBlock);

        EnumDropdownMenuWidget useAuxBlocks = new EnumDropdownMenuWidget(
                this, 0, 0,
                baseElementWidth, 20, baseElementWidth,
                Text.translatable("maparthelper.gui.use_aux"),
                Text.translatable("maparthelper.gui.option." + MapartHelper.conversionSettings.getUseAuxBlocks())
        );
        useAuxBlocks.addEntries(
                e -> {
                    MapartHelper.conversionSettings.setUseAuxBlocks((UseAuxBlocks) e);
                    updateMaterialList();
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
                    updateResetExcludedColorsButton(false);
                    PaletteConfigManager.changeCurrentPreset(s);
                    MapColor oldBgColor = MapartHelper.conversionSettings.getBackgroundColor().mapColor();
                    if (PaletteConfigManager.presetsConfig.getBlockOfMapColor(oldBgColor) == null) {
                        MapartHelper.conversionSettings.setBackgroundColor(MapColorEntry.CLEAR);
                    }
                    MapartImageUpdater.updateMapart(mapart);
                },
                PaletteConfigManager.presetsConfig.presetFiles
        );
        TextWidget currentPresetLabel = new TextWidget(Text.translatable("maparthelper.gui.current_preset_label"), textRenderer);
        settingsRight.add(currentPresetLabel);
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
                Text.of(materialList.isMaterialsAscendingOrder() ? "▲" : "▼"),
                btn -> {
                    materialList.toggleMaterialsAscendingOrder();
                    btn.setMessage(Text.of(materialList.isMaterialsAscendingOrder() ? "▲" : "▼"));
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
                    MapartImageUpdater.revertExcludingColors(mapart);
                    updateResetExcludedColorsButton(false);
                }
        ).size(14, 14).build();
        updateResetExcludedColorsButton(PaletteColors.excludingColorsAmount() > 0);
        materialListSettings.add(resetExcludedColors);
        settingsRight.add(materialListSettings);

        Text perMapCountMode = Text.translatable("maparthelper.gui.countMode.perBlock");
        Text fullCountMode = Text.translatable("maparthelper.gui.countMode.full");
        ButtonWidget materialsCountMode = ButtonWidget
                .builder(MapartHelper.conversionSettings.getMaterialsCountMode() == MaterialsCountModes.FULL ? fullCountMode : perMapCountMode,
                        btn -> {
                            MapartHelper.conversionSettings.nextMaterialsCountMode();
                            MaterialsCountModes mode = MapartHelper.conversionSettings.getMaterialsCountMode();
                            btn.setMessage(mode == MaterialsCountModes.FULL ? fullCountMode : perMapCountMode);
                            btn.setTooltip(Tooltip.of(mode.getDescription()));
                            updateMaterialList();
                        })
                .size(baseElementWidth, 14)
                .build();
        materialsCountMode.setTooltip(Tooltip.of(MapartHelper.conversionSettings.getMaterialsCountMode().getDescription()));
        settingsRight.add(materialsCountMode);

        Text remaining = Text.translatable("maparthelper.gui.amount_remaining").formatted(Formatting.GOLD);
        Text description = Text.translatable("maparthelper.gui.amount_remaining_description");
        Text total = Text.translatable("maparthelper.gui.amount_total");
        ButtonWidget amountDisplayMode = ButtonWidget
                .builder(materialList.isDisplayRemainingAmount() ? remaining : total,
                        btn -> {
                            materialList.toggleDisplayRemainingAmount();
                            btn.setMessage(materialList.isDisplayRemainingAmount() ? remaining : total);
                            btn.setTooltip(materialList.isDisplayRemainingAmount() ? Tooltip.of(description) : null);
                            updateMaterialList();
                        })
                .size(baseElementWidth, 14)
                .build();
        amountDisplayMode.setTooltip(materialList.isDisplayRemainingAmount() ? Tooltip.of(description) : null);
        settingsRight.add(amountDisplayMode, settingsRightPositioner.copy().marginTop(2));

        settingsRight.refreshPositions();
        settingsRight.setPosition(width - settingsRight.getWidth() - 5, 20);
        settingsRight.forEachChild(this::addDrawableChild);

        // Widget positions adjustments
        resetExcludedColors.setX(width - 5 - resetExcludedColors.getWidth());

        if (PaletteConfigManager.isPaletteOutdated()) {
            Identifier warningTex = Identifier.of("textures/gui/sprites/dialog/warning_button.png");
            Identifier warningTexHovered = Identifier.of("textures/gui/sprites/dialog/warning_button_highlighted.png");
            var regenBtn = new DecorativeButtonWidget.Builder(warningTex, btn ->
                MinecraftClient.getInstance().setScreen(new PaletteUpdateSuggestionScreen(this))
            )
                    .dimensions(currentPresetLabel.getX(), 2, 20, 20)
                    .highlightedTexture(warningTexHovered)
                    .build();
            this.addDrawableChild(regenBtn);
        }

        updateMaterialList();
        materialList.setPosition(settingsRight.getX(), settingsRight.getY() + settingsRight.getHeight());
        materialList.setSize(settingsRight.getWidth(), height - settingsRight.getHeight());

        // =========== Mapart preview area ===========

        mapartPreview = new MapartPreviewWidget(mapart,
                settingsLeft.getX() + settingsLeft.getWidth() + 9, 33,
                settingsRight.getX() - 15, this.height - 20
        );
        this.addDrawableChild(mapartPreview);

        DirectionalLayoutWidget mapartOptions = DirectionalLayoutWidget.horizontal().spacing(2);
        mapartOptions.setPosition(mapartPreview.getImageX(), 10);

        if (MapartHelper.commonConfig.mapartEditor.showImageImportButton) {
            ButtonWidget importButton = ButtonWidget.builder(
                    Text.literal("📂"),
                    btn -> FileDialogsUtils.openImageImportDialog(path ->
                            MinecraftClient.getInstance().execute(() -> readImage(Path.of(path)))
                    )
            ).size(20, 20).build();

            importButton.setTooltip(Tooltip.of(Text.translatable("maparthelper.gui.import_tooltip", "Import Image")));
            mapartOptions.add(importButton);
        }

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
                    updateResetExcludedColorsButton(false);
                }
        ).size(20, 20).build();
        resetMapartButton.setTooltip(Tooltip.of(Text.translatable("maparthelper.gui.reset_mapart")));
        mapartOptions.add(resetMapartButton);

        mapartOptions.refreshPositions();
        mapartOptions.forEachChild(this::addDrawableChild);

        updateMapartOutputButtons();
    }

    private @NotNull AdjTextFieldWidget createAuxBlockFieldWidget(BlockItemWidget auxBlockPreview, String currentAuxBlock) {
        AdjTextFieldWidget auxBlockId = new AdjTextFieldWidget(
                textRenderer, baseElementWidth - auxBlockPreview.getWidth() - 5, 20,
                currentAuxBlock, "Auxiliary block identifier"
        );
        auxBlockId.setValueValidator(TextFieldValidators.auxBlockIdentifier());
        auxBlockId.setValueConsumer(idStr -> {
            Identifier id = Identifier.of(idStr);
            Block block = Registries.BLOCK.get(id);
            MapartHelper.conversionSettings.setAuxBlock(block);
            auxBlockPreview.setBlock(block);
            updateMaterialList();
        });
        return auxBlockId;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, settingsLeft.getX() + settingsLeft.getWidth() + 7, height, 0x77000000);
        context.fill(settingsRight.getX() - 7, 0, width, height, 0x77000000);
        super.render(context, mouseX, mouseY, delta);

        if (!MaterialListPanel.MaterialListBlockWidget.isHoveringAny()) {
            MaterialListPanel.MaterialListBlockWidget.setDefaultHighlight(mapartPreview);
        }
        MaterialListPanel.MaterialListBlockWidget.resetHovering();
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
        readImage(paths.getFirst());
    }

    private GridWidget createSizeSettingsGrid() {
        GridWidget size = new GridWidget().setSpacing(10).setRowSpacing(1);
        GridWidget.Adder adder = size.createAdder(2);

        AdjTextFieldWidget widthInput = new AdjTextFieldWidget(
                textRenderer, 30, 20, "" + mapart.getWidth(), "Width"
        );
        widthInput.setPlaceholder(Text.literal("x").withColor(Colors.GRAY));
        widthInput.setTextPredicate(TextFieldPredicates.positiveInt());
        widthInput.setValueConsumer(s -> {
            int value = Integer.parseInt(s);
            if (value != mapart.getWidth()) {
                CurrentConversionSettings.guiMapartImage = null;
                MapartImageUpdater.resizeMapartImage(mapart, value, mapart.getHeight());
            }
        });

        AdjTextFieldWidget heightInput = new AdjTextFieldWidget(
                textRenderer, 30, 20, "" + mapart.getHeight(), "Height"
        );
        heightInput.setPlaceholder(Text.literal("y").withColor(Colors.GRAY));
        heightInput.setTextPredicate(TextFieldPredicates.positiveInt());
        heightInput.setValueConsumer(s -> {
            int value = Integer.parseInt(s);
            if (value != mapart.getHeight()) {
                CurrentConversionSettings.guiMapartImage = null;
                MapartImageUpdater.resizeMapartImage(mapart, mapart.getWidth(), value);
            }
        });

        adder.add(new TextWidget(Text.translatable("maparthelper.gui.mapart_size_label"), textRenderer), 2);
        adder.add(widthInput);
        adder.add(heightInput);

        return size;
    }

    private DropdownMenuWidget createSaveMapartDropdown() {
        boolean isIntegratedServer = MinecraftClient.getInstance().isIntegratedServerRunning();

        ButtonWidget saveImage = ButtonWidget.builder(
                Text.translatable("maparthelper.gui.savePNG"),
                (btn) -> {
                    PlayerEntity player = client != null ? client.player : null;
                    MapartSaver.saveMapartImage(mapart.mapartName, CurrentConversionSettings.guiMapartImage, player);
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

        MinecraftClient mc = MinecraftClient.getInstance();
        if (isIntegratedServer && mc.getServer() != null && mc.player != null) {
            ServerPlayerEntity serverPlayer = mc.getServer().getPlayerManager().getPlayer(mc.player.getUuid());
            getMapItemsButton = ButtonWidget.builder(
                    Text.translatable("maparthelper.gui.save_map_items").formatted(Formatting.GOLD),
                    btn -> {
                        int[][] maps = NativeImageUtils.divideImageByMaps(
                                mapart.getWidth(), mapart.getHeight(), mapart.getNativeImage()
                        );
                        MapCreator.getMapsForMapart(maps, mapart.getWidth(), mapart.mapartName, mc.getServer().getOverworld(), serverPlayer);
                    }
            ).size(156, 20).build();
        }

        DropdownMenuWidget saveMapart = new DropdownMenuWidget(this, 0, 0, 20, 20, 160, -1, Text.literal("\uD83D\uDDAB"));
        saveMapart.setTooltip(Tooltip.of(Text.translatable("maparthelper.gui.save_mapart_as")));
        saveMapart.addEntry(saveImage);
        saveMapart.addEntry(saveNBT);
        saveMapart.addEntry(saveSplitNBT);
        saveMapart.addEntry(saveZipNBT);
        if (isIntegratedServer)
            saveMapart.addEntry(getMapItemsButton);

        return saveMapart;
    }

    public void updateMapartOutputButtons() {
        boolean active = CurrentConversionSettings.isMapartConverted();
        saveNBT.active = active;
        saveSplitNBT.active = active;
        saveZipNBT.active = active;
        if (getMapItemsButton != null)
            getMapItemsButton.active = active;
        showInWorldButton.active = active;
        if (active) {
            saveNBT.setTooltip(null);
            saveSplitNBT.setTooltip(null);
            saveZipNBT.setTooltip(null);
            if (getMapItemsButton != null) {
                getMapItemsButton.setTooltip(Tooltip.of(
                        Text.translatable("maparthelper.gui.singleplayer_only").formatted(Formatting.GOLD)
                ));
            }
            showInWorldButton.setTooltip(Tooltip.of(Text.translatable("maparthelper.gui.showInWorld_tooltip")));
        } else {
            Tooltip disabled = Tooltip.of(Text.translatable("maparthelper.gui.enableColorAdaptation"));
            saveNBT.setTooltip(disabled);
            saveSplitNBT.setTooltip(disabled);
            saveZipNBT.setTooltip(disabled);
            if (getMapItemsButton != null)
                getMapItemsButton.setTooltip(disabled);
            showInWorldButton.setTooltip(disabled);
        }
    }

    public void updateResetExcludedColorsButton(boolean active) {
        if (active) {
            MutableText excludedAmount = Text.translatable("maparthelper.gui.excluded_colors_amount", PaletteColors.excludingColorsAmount());
            MutableText revertExcluding = Text.translatable("maparthelper.gui.revert_excluding_colors");
            resetExcludedColors.setMessage(Text.literal("⟲").formatted(Formatting.BOLD, Formatting.GOLD));
            resetExcludedColors.setTooltip(Tooltip.of(
                    excludedAmount.formatted(Formatting.GOLD).append("\n")
                            .append(revertExcluding.formatted(Formatting.GRAY))
            ));
            resetExcludedColors.active = true;
        } else {
            resetExcludedColors.setMessage(Text.literal("⟲").formatted(Formatting.BOLD, Formatting.WHITE));
            resetExcludedColors.setTooltip(null);
            resetExcludedColors.active = false;
        }
    }

    private void readImage(Path filepath) {
        CurrentConversionSettings.resetMapart();
        MapartImageUpdater.readAndUpdateMapartImage(mapart, filepath);
        updateResetExcludedColorsButton(false);
    }
}
