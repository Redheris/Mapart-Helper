package rh.maparthelper.gui.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.LayoutSettings;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.CommonColors;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.MapColor;
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

//? >=1.21.10
//import net.minecraft.client.input.KeyEvent;

//? if <= 1.21.8 {
import me.shedaniel.autoconfig.AutoConfig;
//?} else
//import me.shedaniel.autoconfig.AutoConfigClient;

@Environment(EnvType.CLIENT)
public class MapartEditorScreen extends ScreenAdapted {
    private static final Identifier SETTINGS_TEXTURE = Identifier.fromNamespaceAndPath(MapartHelper.MOD_ID, "textures/gui/sprites/mapart_editor/settings.png");
    protected final MapartProcessing mapart = CurrentConversionSettings.mapart;

    private LinearLayout settingsLeft;
    private LinearLayout settingsRight;
    private LinearLayout mapartOptions;
    private MapartPreviewWidget mapartPreview;
    private final int baseElementWidth = 165;
    private boolean shortElements = false;

    private Button saveNBT;
    private Button saveSplitNBT;
    private Button saveZipNBT;
    private Button getMapItemsButton;
    private Button showInWorldButton;
    private Button resetExcludedColors;
    private Button toggleManualCroppingButtonsButton;

    private final MaterialListPanel materialList = new MaterialListPanel(this, mapart, 0, 0, 0, 0);

    public MapartEditorScreen() {
        super(Component.translatable("maparthelper.gui.mapart_editor_screen"));
    }

    public void setHighlightingColor(MapColor color) {
        mapartPreview.setHighlightingColor(color);
    }

    public void updateMaterialList() {
        materialList.updateMaterialList(this::addRenderableWidget, this::removeWidget);
    }

    @Override
    protected void init() {
        super.init();

        initMapartOptionsPanel();

        int middleWidth = width - 2 * baseElementWidth - 42;
        int widthDiff = mapartOptions.getWidth() - middleWidth;
        shortElements = widthDiff > 0;

        initRightPanel();
        initLeftPanel();

        // =========== Mapart preview area ===========

        mapartPreview = new MapartPreviewWidget(mapart,
                settingsLeft.getX() + settingsLeft.getWidth() + 9, 33,
                settingsRight.getX() - 15, this.height - 20
        );
        this.addRenderableWidget(mapartPreview);

        mapartOptions.setPosition(mapartPreview.getImageX(), 10);
        mapartOptions.arrangeElements();
        mapartOptions.visitWidgets(this::addRenderableWidget);

        updateMapartOutputButtons();
    }

    private int currentElementWidth() {
        return shortElements ? 135 : baseElementWidth;
    }

    private void initMapartOptionsPanel() {
        mapartOptions = LinearLayout.horizontal().spacing(2);

        if (MapartHelper.commonConfig.mapartEditor.showImageImportButton) {
            Button importButton = Button.builder(
                    Component.literal("📂"),
                    btn -> FileDialogsUtils.openImageImportDialog(path ->
                            Minecraft.getInstance().execute(() -> readImage(Path.of(path)))
                    )
            ).size(20, 20).build();

            importButton.setTooltip(Tooltip.create(Component.translatable("maparthelper.gui.import_tooltip", "Import Image")));
            mapartOptions.addChild(importButton);
        }

        mapartOptions.addChild(createSaveMapartDropdown());

        Button showGridButton = Button.builder(
                Component.literal("#").withStyle(CurrentConversionSettings.doShowGrid ? ChatFormatting.AQUA : ChatFormatting.RESET),
                (btn) -> {
                    boolean doShowGrid = !CurrentConversionSettings.doShowGrid;
                    CurrentConversionSettings.doShowGrid = doShowGrid;
                    btn.setMessage(btn.getMessage().copy().withStyle(doShowGrid ? ChatFormatting.AQUA : ChatFormatting.RESET));
                }
        ).size(20, 20).build();
        showGridButton.setTooltip(Tooltip.create(Component.translatable("maparthelper.gui.showGrid_tooltip")));
        mapartOptions.addChild(showGridButton);

        showInWorldButton = Button.builder(
                Component.nullToEmpty("\uD83C\uDF0D"),
                (btn) -> {
                    //? <=1.21.8
                    if (minecraft == null) return;
                    if (minecraft.player == null) return;
                    if (FakeMapsPreview.createFakeFramesFromMapart(mapart, minecraft.player)) {
                        FakeMapsPreview.showFakeFrames(minecraft.player, mapart.getWidth(), mapart.getHeight());
                        this.onClose();
                    }
                }
        ).size(20, 20).build();
        showInWorldButton.setTooltip(Tooltip.create(Component.translatable("maparthelper.gui.showInWorld_tooltip")));
        mapartOptions.addChild(showInWorldButton);

        toggleManualCroppingButtonsButton = Button.builder(
                Component.literal("\uD83D\uDDBC").withStyle(CurrentConversionSettings.doShowManualCroppingButtons ? ChatFormatting.RESET : ChatFormatting.DARK_GRAY),
                (btn) -> {
                    boolean doShowManualCroppingButtons = !CurrentConversionSettings.doShowManualCroppingButtons;
                    CurrentConversionSettings.doShowManualCroppingButtons = doShowManualCroppingButtons;
                    btn.setMessage(btn.getMessage().copy().withStyle(doShowManualCroppingButtons ? ChatFormatting.RESET : ChatFormatting.DARK_GRAY));
                }
        ).size(20, 20).build();
        toggleManualCroppingButtonsButton.active = CurrentConversionSettings.cropMode == CroppingMode.USER_CROP;
        toggleManualCroppingButtonsButton.setTooltip(Tooltip.create(Component.translatable("maparthelper.gui.toggle_manual_cropping_buttons")));
        mapartOptions.addChild(toggleManualCroppingButtonsButton);

        Button resetMapartButton = Button.builder(
                Component.literal("⟲").withStyle(ChatFormatting.BOLD),
                b -> {
                    CurrentConversionSettings.resetMapart();
                    updateMapartOutputButtons();
                    updateMaterialList();
                    updateResetExcludedColorsButton(false);
                }
        ).size(20, 20).build();
        resetMapartButton.setTooltip(Tooltip.create(Component.translatable("maparthelper.gui.reset_mapart")));
        mapartOptions.addChild(resetMapartButton);
        mapartOptions.arrangeElements();
    }

    private void initLeftPanel() {
        final int elementWidth = currentElementWidth();
        settingsLeft = LinearLayout.vertical();
        settingsLeft.setPosition(5, 20);
        LayoutSettings settingsLeftPositioner = settingsLeft.defaultCellSetting().paddingTop(5);
        LayoutSettings topLabeledPositioner = settingsLeftPositioner.copy().paddingTop(0);

        AdjTextFieldWidget mapartName = new AdjTextFieldWidget(
                font, elementWidth, 20, mapart.mapartName, "Mapart name"
        );
        mapartName.setHint(Component.translatable("maparthelper.gui.mapart_name_field").withColor(CommonColors.GRAY));
        mapartName.setValueValidator(s -> !s.isBlank());
        mapartName.setFilter(TextFieldPredicates.validPathName());
        mapartName.setValueConsumer(newValue -> mapart.mapartName = newValue);
        settingsLeft.addChild(new StringWidget(Component.translatable("maparthelper.gui.mapart_name_field"), font));
        settingsLeft.addChild(mapartName, topLabeledPositioner);

        GridLayout size = createSizeSettingsGrid();
        settingsLeft.addChild(size);

        settingsLeft.arrangeElements();
        settingsLeft.visitWidgets(this::addRenderableWidget);

        this.addRenderableWidget(
                DecorativeButtonWidget.builder(
                        SETTINGS_TEXTURE, btn -> Minecraft.getInstance().setScreen(
                                //? if <=1.21.8 {
                                AutoConfig.getConfigScreen(MapartHelperConfig.class, this).get()
                                //?} else
                                //AutoConfigClient.getConfigScreen(MapartHelperConfig.class, this).get()
                        )
                ).dimensions(2, 4, 14, 14).build()
        );

        int listTop = settingsLeft.getY() + settingsLeft.getHeight();
        ScrollableGridWidget settingsLeftScrollable = new ScrollableGridWidget(
                null,
                settingsLeft.getX(), listTop,
                elementWidth + 6, height - listTop, 6
        );
        settingsLeftScrollable.grid.defaultCellSetting().paddingTop(5);
        GridLayout.RowHelper adder = settingsLeftScrollable.grid.createRowHelper(1);

        Component previewMapart = Component.translatable("maparthelper.gui.previewMapart");
        Component previewOriginal = Component.translatable("maparthelper.gui.previewOriginal").withStyle(ChatFormatting.GOLD);
        Button previewMode = Button.builder(
                MapartHelper.conversionSettings.isShowOriginalImage() ? previewOriginal : previewMapart,
                (btn) -> {
                    MapartHelper.conversionSettings.toggleShowOriginalImage();
                    btn.setMessage(MapartHelper.conversionSettings.isShowOriginalImage() ? previewOriginal : previewMapart);
                    MapartImageUpdater.updateMapart(mapart);
                }
        ).size(elementWidth, 20).build();
        adder.addChild(new StringWidget(Component.translatable("maparthelper.gui.previewMode"), font));
        adder.addChild(previewMode, topLabeledPositioner);

        // ===============================================

        if (shortElements)
            adder.addChild(new StringWidget(Component.translatable("maparthelper.gui.cropMode"), font));
        EnumDropdownMenuWidget croppingMode = new EnumDropdownMenuWidget(
                this, 0, 0, elementWidth, 20, elementWidth,
                Component.translatable("maparthelper.gui.cropMode"),
                Component.translatable("maparthelper.gui.option." + CurrentConversionSettings.cropMode.name()),
                !shortElements
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
        if (shortElements) adder.addChild(croppingMode, topLabeledPositioner);
        else adder.addChild(croppingMode);

        if (shortElements)
            adder.addChild(new StringWidget(Component.translatable("maparthelper.gui.staircaseStyle"), font));
        EnumDropdownMenuWidget staircaseStyle = new EnumDropdownMenuWidget(
                this, 0, 0, elementWidth, 20, elementWidth,
                Component.translatable("maparthelper.gui.staircaseStyle"),
                Component.translatable("maparthelper.gui.option." + MapartHelper.conversionSettings.getStaircaseStyle().name()),
                !shortElements
        );
        staircaseStyle.toggleTooltips(MapartHelper.commonConfig.mapartEditor.showStaircaseTooltips);
        staircaseStyle.addEntries(
                e -> {
                    if (MapartHelper.conversionSettings.setStaircaseStyle((StaircaseStyles) e))
                        MapartImageUpdater.updateMapart(mapart);
                    updateMapartOutputButtons();
                },
                StaircaseStyles.FLAT_2D,
                StaircaseStyles.VALLEY_3D,
                StaircaseStyles.WAVES_3D,
                MapartHelper.commonConfig.mapartEditor.displayUnobtainableMode ? StaircaseStyles.UNOBTAINABLE : null
        );
        if (shortElements) adder.addChild(staircaseStyle, topLabeledPositioner);
        else adder.addChild(staircaseStyle);

        if (shortElements)
            adder.addChild(new StringWidget(Component.translatable("maparthelper.gui.ditheringAlg"), font));
        EnumDropdownMenuWidget colorConverter = new EnumDropdownMenuWidget(
                this, 0, 0, elementWidth, 20, elementWidth, 280,
                Component.translatable("maparthelper.gui.ditheringAlg"),
                Component.translatable("maparthelper.gui.option." + MapartHelper.conversionSettings.getColorConverter().name()),
                !shortElements
        );
        colorConverter.setLeftScroll(true);
        colorConverter.addEntries(
                e -> {
                    MapartHelper.conversionSettings.setColorConverter((ColorConverters) e);
                    MapartImageUpdater.updateMapart(mapart);
                },
                ColorConverters.values()
        );
        if (shortElements) adder.addChild(colorConverter, topLabeledPositioner);
        else adder.addChild(colorConverter);

        Component isOn = Component.translatable("maparthelper.gui.isOn");
        Component isOff = Component.translatable("maparthelper.gui.isOff");
        Button useLAB = Button.builder(
                Component.literal("LAB: ").append(MapartHelper.conversionSettings.useLAB() ? isOn : isOff),
                (btn) -> {
                    MapartHelper.conversionSettings.toggleLAB();
                    btn.setMessage(Component.literal("LAB: ").append(MapartHelper.conversionSettings.useLAB() ? isOn : isOff));
                    MapartImageUpdater.updateMapart(mapart);
                }
        ).size(80, 20).build();

        if (MapartHelper.commonConfig.mapartEditor.showUseLABTooltip) {
            useLAB.setTooltip(Tooltip.create(Component.translatable("maparthelper.gui.useLAB_tooltip")));
            useLAB.setTooltipDelay(Duration.ofMillis(500));
        }
        adder.addChild(useLAB);

        adder.addChild(new ImagePreprocessingDropdown(this, mapart, 100, elementWidth));

        LinearLayout bgColorChoose = LinearLayout.horizontal();
        bgColorChoose.defaultCellSetting().alignVerticallyMiddle();
        bgColorChoose.addChild(new StringWidget(Component.translatable("maparthelper.gui.backgroundColor"), font));
        bgColorChoose.addChild(new MapColorPickerWidget(
                this, mapart, 20, 20,
                elementWidth, 180, 4
        ));
        adder.addChild(bgColorChoose);

        adder.addChild(
                new StringWidget(Component.translatable("maparthelper.aux_block"), font),
                settingsLeftPositioner.copy().paddingTop(15)
        );
        String currentAuxBlock = BuiltInRegistries.BLOCK.getKey(MapartHelper.conversionSettings.getAuxBlock()).toString();
        if (currentAuxBlock.contains("minecraft:"))
            currentAuxBlock = currentAuxBlock.substring(10);
        BlockItemWidget auxBlockPreview = new BlockItemWidget(0, 0, 24, MapartHelper.conversionSettings.getAuxBlock(), false);

        GridLayout auxBlock = new GridLayout().spacing(5);
        auxBlock.defaultCellSetting().alignVerticallyMiddle();
        GridLayout.RowHelper auxAdder = auxBlock.createRowHelper(2);
        auxAdder.addChild(createAuxBlockFieldWidget(auxBlockPreview, currentAuxBlock));
        auxAdder.addChild(auxBlockPreview);
        adder.addChild(auxBlock);

        EnumDropdownMenuWidget useAuxBlocks = new EnumDropdownMenuWidget(
                this, 0, 0,
                elementWidth, 20, elementWidth,
                Component.translatable("maparthelper.gui.use_aux"),
                Component.translatable("maparthelper.gui.option." + MapartHelper.conversionSettings.getUseAuxBlocks())
        );
        useAuxBlocks.addEntries(
                e -> {
                    MapartHelper.conversionSettings.setUseAuxBlocks((UseAuxBlocks) e);
                    updateMaterialList();
                },
                UseAuxBlocks.values()
        );
        adder.addChild(useAuxBlocks);

        settingsLeftScrollable.arrangeElements();
        this.addRenderableWidget(settingsLeftScrollable);
    }

    private void initRightPanel() {
        final int elementWidth = currentElementWidth();
        settingsRight = LinearLayout.vertical();
        LayoutSettings settingsRightPositioner = settingsRight.defaultCellSetting().paddingTop(5);

        PresetsDropdownMenuWidget presetsList = new PresetsDropdownMenuWidget(
                this, 0, 0, elementWidth, 20, elementWidth,
                Component.nullToEmpty("\"" + PaletteConfigManager.presetsConfig.getCurrentPresetName() + "\""), true
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
        StringWidget currentPresetLabel = new StringWidget(Component.translatable("maparthelper.gui.current_preset_label"), font);
        settingsRight.addChild(currentPresetLabel);
        settingsRight.addChild(presetsList, settingsRightPositioner.copy().paddingTop(0));

        Button presetsEditor = Button.builder(
                Component.translatable("maparthelper.gui.presets_editor_screen"),
                (btn) ->
                        Minecraft.getInstance().setScreen(
                                new PresetsEditorScreen(this, 45, 30, 45, 30)
                        )
        ).size(elementWidth, 20).build();
        settingsRight.addChild(presetsEditor);

        LinearLayout materialListSettings = LinearLayout.horizontal().spacing(2);
        materialListSettings.defaultCellSetting().alignVerticallyBottom();
        materialListSettings.addChild(DecorativeButtonWidget.builder(
                Component.nullToEmpty(materialList.isMaterialsAscendingOrder() ? "▲" : "▼"),
                btn -> {
                    materialList.toggleMaterialsAscendingOrder();
                    btn.setMessage(Component.nullToEmpty(materialList.isMaterialsAscendingOrder() ? "▲" : "▼"));
                    updateMaterialList();
                }
        ).size(10, 10).build());
        materialListSettings.addChild(new StringWidget(Component.translatable("maparthelper.gui.material_list_label"), font));
        resetExcludedColors = Button.builder(
                Component.literal("⟲")
                        .withStyle(ChatFormatting.BOLD)
                        .withStyle(PaletteColors.excludingColorsAmount() > 0 ? ChatFormatting.GOLD : ChatFormatting.WHITE),
                btn -> {
                    if (PaletteColors.excludingColorsAmount() == 0) return;
                    MapartImageUpdater.revertExcludingColors(mapart);
                    updateResetExcludedColorsButton(false);
                }
        ).size(14, 14).build();
        updateResetExcludedColorsButton(PaletteColors.excludingColorsAmount() > 0);
        materialListSettings.addChild(resetExcludedColors);
        settingsRight.addChild(materialListSettings);

        Component perMapCountMode = Component.translatable("maparthelper.gui.countMode.perBlock");
        Component fullCountMode = Component.translatable("maparthelper.gui.countMode.full");
        Button materialsCountMode = Button
                .builder(MapartHelper.conversionSettings.getMaterialsCountMode() == MaterialsCountModes.FULL ? fullCountMode : perMapCountMode,
                        btn -> {
                            MapartHelper.conversionSettings.nextMaterialsCountMode();
                            MaterialsCountModes mode = MapartHelper.conversionSettings.getMaterialsCountMode();
                            btn.setMessage(mode == MaterialsCountModes.FULL ? fullCountMode : perMapCountMode);
                            btn.setTooltip(Tooltip.create(mode.getDescription()));
                            updateMaterialList();
                        })
                .size(elementWidth, 14)
                .build();
        materialsCountMode.setTooltip(Tooltip.create(MapartHelper.conversionSettings.getMaterialsCountMode().getDescription()));
        settingsRight.addChild(materialsCountMode);

        Component remaining = Component.translatable("maparthelper.gui.amount_remaining").withStyle(ChatFormatting.GOLD);
        Component description = Component.translatable("maparthelper.gui.amount_remaining_description");
        Component total = Component.translatable("maparthelper.gui.amount_total");
        Button amountDisplayMode = Button
                .builder(materialList.isDisplayRemainingAmount() ? remaining : total,
                        btn -> {
                            materialList.toggleDisplayRemainingAmount();
                            btn.setMessage(materialList.isDisplayRemainingAmount() ? remaining : total);
                            btn.setTooltip(materialList.isDisplayRemainingAmount() ? Tooltip.create(description) : null);
                            updateMaterialList();
                        })
                .size(elementWidth, 14)
                .build();
        amountDisplayMode.setTooltip(materialList.isDisplayRemainingAmount() ? Tooltip.create(description) : null);
        settingsRight.addChild(amountDisplayMode, settingsRightPositioner.copy().paddingTop(2));

        settingsRight.arrangeElements();
        settingsRight.setPosition(width - settingsRight.getWidth() - 5, 20);
        settingsRight.visitWidgets(this::addRenderableWidget);

        // Widget positions adjustments
        resetExcludedColors.setX(width - 5 - resetExcludedColors.getWidth());

        if (PaletteConfigManager.isPaletteOutdated()) {
            Identifier warningTex = Identifier.parse("textures/gui/sprites/dialog/warning_button.png");
            Identifier warningTexHovered = Identifier.parse("textures/gui/sprites/dialog/warning_button_highlighted.png");
            var regenBtn = new DecorativeButtonWidget.Builder(warningTex, btn ->
                    Minecraft.getInstance().setScreen(new PaletteUpdateSuggestionScreen(this))
            )
                    .dimensions(currentPresetLabel.getX(), 2, 20, 20)
                    .highlightedTexture(warningTexHovered)
                    .build();
            this.addRenderableWidget(regenBtn);
        }

        materialList.setPosition(settingsRight.getX(), settingsRight.getY() + settingsRight.getHeight());
        materialList.setSize(settingsRight.getWidth(), height - settingsRight.getHeight());
        materialList.setDisplayTotalCount(!shortElements);
        updateMaterialList();
    }

    private @NotNull AdjTextFieldWidget createAuxBlockFieldWidget(BlockItemWidget auxBlockPreview, String currentAuxBlock) {
        AdjTextFieldWidget auxBlockId = new AdjTextFieldWidget(
                font, currentElementWidth() - auxBlockPreview.getWidth() - 5, 20,
                currentAuxBlock, "Auxiliary block identifier"
        );
        auxBlockId.setValueValidator(TextFieldValidators.auxBlockIdentifier());
        auxBlockId.setValueConsumer(idStr -> {
            Identifier id = Identifier.parse(idStr);
            Block block = BuiltInRegistries.BLOCK.getValue(id);
            MapartHelper.conversionSettings.setAuxBlock(block);
            auxBlockPreview.setBlock(block);
            updateMaterialList();
        });
        return auxBlockId;
    }

    @Override
    public void render(@NotNull GuiGraphics context, int mouseX, int mouseY, float partialTick) {
        context.fill(0, 0, settingsLeft.getX() + settingsLeft.getWidth() + 7, height, 0x77000000);
        context.fill(settingsRight.getX() - 7, 0, width, height, 0x77000000);
        super.render(context, mouseX, mouseY, partialTick);

        if (!MaterialListPanel.MaterialListBlockWidget.isHoveringAny()) {
            MaterialListPanel.MaterialListBlockWidget.setDefaultHighlight(mapartPreview);
        }
        MaterialListPanel.MaterialListBlockWidget.resetHovering();
    }

    //~ widget_events
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
    //~ !widget_events

    @Override
    public void onFilesDrop(List<Path> paths) {
        readImage(paths.getFirst());
    }

    private GridLayout createSizeSettingsGrid() {
        GridLayout size = new GridLayout().spacing(10).rowSpacing(1);
        GridLayout.RowHelper adder = size.createRowHelper(2);

        AdjTextFieldWidget widthInput = new AdjTextFieldWidget(
                font, 30, 20, "" + mapart.getWidth(), "Width"
        );
        widthInput.setHint(Component.literal("x").withColor(CommonColors.GRAY));
        widthInput.setFilter(TextFieldPredicates.positiveInt());
        widthInput.setValueConsumer(s -> {
            int value = Integer.parseInt(s);
            if (value != mapart.getWidth()) {
                CurrentConversionSettings.guiMapartImage = null;
                MapartImageUpdater.resizeMapartImage(mapart, value, mapart.getHeight());
            }
        });

        AdjTextFieldWidget heightInput = new AdjTextFieldWidget(
                font, 30, 20, "" + mapart.getHeight(), "Height"
        );
        heightInput.setHint(Component.literal("y").withColor(CommonColors.GRAY));
        heightInput.setFilter(TextFieldPredicates.positiveInt());
        heightInput.setValueConsumer(s -> {
            int value = Integer.parseInt(s);
            if (value != mapart.getHeight()) {
                CurrentConversionSettings.guiMapartImage = null;
                MapartImageUpdater.resizeMapartImage(mapart, mapart.getWidth(), value);
            }
        });

        adder.addChild(new StringWidget(Component.translatable("maparthelper.gui.mapart_size_label"), font), 2);
        adder.addChild(widthInput);
        adder.addChild(heightInput);

        return size;
    }

    private DropdownMenuWidget createSaveMapartDropdown() {
        boolean isIntegratedServer = Minecraft.getInstance().hasSingleplayerServer();

        Button saveImage = Button.builder(
                Component.translatable("maparthelper.gui.savePNG"),
                (btn) -> {
                    Player player = /*? if <=1.21.8 {*/ minecraft == null ? null : /*?}*/ minecraft.player;
                    MapartSaver.saveMapartImage(mapart.mapartName, CurrentConversionSettings.guiMapartImage, player);
                }
        ).size(156, 20).build();

        saveNBT = Button.builder(
                Component.translatable("maparthelper.gui.saveNBT"),
                (btn) -> MapartToNBT.saveNBT(true)
        ).size(156, 20).build();

        saveSplitNBT = Button.builder(
                Component.translatable("maparthelper.gui.saveEveryNBT"),
                (btn) -> MapartToNBT.saveNBT(false)
        ).size(156, 20).build();

        saveZipNBT = Button.builder(
                Component.translatable("maparthelper.gui.saveZip"),
                (btn) -> MapartToNBT.saveNBTAsZip()
        ).size(156, 20).build();

        Minecraft mc = Minecraft.getInstance();
        if (isIntegratedServer && mc.getSingleplayerServer() != null && mc.player != null) {
            ServerPlayer serverPlayer = mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());
            getMapItemsButton = Button.builder(
                    Component.translatable("maparthelper.gui.save_map_items").withStyle(ChatFormatting.GOLD),
                    btn -> {
                        int[][] maps = NativeImageUtils.divideImageByMaps(
                                mapart.getWidth(), mapart.getHeight(), mapart.getNativeImage()
                        );
                        MapCreator.getMapsForMapart(maps, mapart.getWidth(), mapart.mapartName, mc.getSingleplayerServer().overworld(), serverPlayer);
                    }
            ).size(156, 20).build();
        }

        DropdownMenuWidget saveMapart = new DropdownMenuWidget(this, 0, 0, 20, 20, 160, -1, Component.literal("\uD83D\uDDAB"));
        saveMapart.setTooltip(Tooltip.create(Component.translatable("maparthelper.gui.save_mapart_as")));
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
        boolean obtainableNbt = !(MapartHelper.conversionSettings.getStaircaseStyle() == StaircaseStyles.UNOBTAINABLE);
        saveNBT.active = active && obtainableNbt;
        saveSplitNBT.active = active && obtainableNbt;
        saveZipNBT.active = active && obtainableNbt;
        if (getMapItemsButton != null)
            getMapItemsButton.active = active;
        showInWorldButton.active = active;
        if (!obtainableNbt) {
            Component unobtainableNbt = Component.translatable("maparthelper.gui.nbt_is_unobtainable");
            saveNBT.setTooltip(Tooltip.create(unobtainableNbt));
            saveSplitNBT.setTooltip(Tooltip.create(unobtainableNbt));
            saveZipNBT.setTooltip(Tooltip.create(unobtainableNbt));
        } else if (active) {
            saveNBT.setTooltip(null);
            saveSplitNBT.setTooltip(null);
            saveZipNBT.setTooltip(null);
        }
        if (active) {
            if (getMapItemsButton != null) {
                getMapItemsButton.setTooltip(Tooltip.create(
                        Component.translatable("maparthelper.gui.singleplayer_only").withStyle(ChatFormatting.GOLD)
                ));
            }
            showInWorldButton.setTooltip(Tooltip.create(Component.translatable("maparthelper.gui.showInWorld_tooltip")));
        } else {
            Tooltip disabled = Tooltip.create(Component.translatable("maparthelper.gui.enableColorAdaptation"));
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
            MutableComponent excludedAmount = Component.translatable("maparthelper.gui.excluded_colors_amount", PaletteColors.excludingColorsAmount());
            MutableComponent revertExcluding = Component.translatable("maparthelper.gui.revert_excluding_colors");
            resetExcludedColors.setMessage(Component.literal("⟲").withStyle(ChatFormatting.BOLD, ChatFormatting.GOLD));
            resetExcludedColors.setTooltip(Tooltip.create(
                    excludedAmount.withStyle(ChatFormatting.GOLD).append("\n")
                            .append(revertExcluding.withStyle(ChatFormatting.GRAY))
            ));
            resetExcludedColors.active = true;
        } else {
            resetExcludedColors.setMessage(Component.literal("⟲").withStyle(ChatFormatting.BOLD, ChatFormatting.WHITE));
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
