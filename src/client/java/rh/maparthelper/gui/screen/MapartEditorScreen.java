package rh.maparthelper.gui.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.LayoutSettings;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.layouts.SpacerElement;
import net.minecraft.client.player.LocalPlayer;
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
import rh.maparthelper.config.ConfigScreenFactory;
import rh.maparthelper.config.ConversionConfiguration;
import rh.maparthelper.config.MaterialsCountModes;
import rh.maparthelper.config.UseAuxBlocks;
import rh.maparthelper.conversion.CroppingMode;
import rh.maparthelper.conversion.CurrentConversionSettings;
import rh.maparthelper.conversion.MapartImageUpdater;
import rh.maparthelper.conversion.NativeImageUtils;
import rh.maparthelper.conversion.dithering.ColorConverters;
import rh.maparthelper.conversion.dithering.DitheringTypes;
import rh.maparthelper.conversion.schematic.MapartToNBT;
import rh.maparthelper.conversion.staircases.StaircaseStyles;
import rh.maparthelper.gui.input.TextFieldPredicates;
import rh.maparthelper.gui.input.TextFieldValidators;
import rh.maparthelper.gui.screen.panel.MaterialListPanel;
import rh.maparthelper.gui.widget.BlockItemWidget;
import rh.maparthelper.gui.widget.DecorativeButtonWidget;
import rh.maparthelper.gui.widget.MapartPreviewWidget;
import rh.maparthelper.gui.widget.dropdown.*;
import rh.maparthelper.gui.widget.input.AdjEditBox;
import rh.maparthelper.gui.widget.input.IntegerFieldWidget;
import rh.maparthelper.gui.widget.layout.AdjScrollableLayoutWidget;
import rh.maparthelper.gui.widget.layout.OverlayLayout;
import rh.maparthelper.gui.widget.layout.OverlayLayoutFactory;
import rh.maparthelper.mapart.MapartProcessing;
import rh.maparthelper.mapart.MapartSaver;
import rh.maparthelper.palette.PaletteColors;
import rh.maparthelper.palette.PaletteDataManager;
import rh.maparthelper.palette.PalettePresetsHandler;
import rh.maparthelper.server.MapCreator;
import rh.maparthelper.util.FileDialogsUtils;

import java.nio.file.Path;
import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

//? >=1.21.10
//import net.minecraft.client.input.KeyEvent;

@Environment(EnvType.CLIENT)
public class MapartEditorScreen extends ScreenAdapted {
    private static final Identifier OPEN_FILE_ICON = MapartHelper.identifier("textures/gui/icons/open_file.png");
    private static final Identifier SAVE_ICON = MapartHelper.identifier("textures/gui/icons/save.png");
    private static final Identifier GRID_ICON = MapartHelper.identifier("textures/gui/icons/grid.png");
    private static final Identifier PREVIEW_ICON = MapartHelper.identifier("textures/gui/icons/preview.png");
    private static final Identifier PREVIEW_DISABLED_ICON = MapartHelper.identifier("textures/gui/icons/preview_disabled.png");
    private static final Identifier CROPPING_BUTTONS_ICON = MapartHelper.identifier("textures/gui/icons/cropping.png");
    private static final Identifier RESET_ICON = MapartHelper.identifier("textures/gui/icons/reset.png");
    private static final Identifier RESET_DISABLED_ICON = MapartHelper.identifier("textures/gui/icons/reset_disabled.png");
    private static final Identifier FULLSCREEN_ICON = MapartHelper.identifier("textures/gui/icons/fullscreen.png");
    private static final Identifier SETTINGS_ICON = MapartHelper.identifier("textures/gui/icons/settings.png");

    private final MapartProcessing mapart = CurrentConversionSettings.mapart;
    private final PaletteDataManager paletteDataManager = PaletteDataManager.getInstance();
    private final PalettePresetsHandler palettePresetsHandler = paletteDataManager.getPresetsHandler();

    private LinearLayout settingsLeft;
    private LinearLayout settingsRight;
    private LinearLayout mapartOptions;
    private MapartPreviewWidget mapartPreview;
    private final int baseElementWidth = 165;
    private boolean shortElements = false;

    private EnumListDropdown croppingModeDropdownButton;
    private EnumListDropdown staircaseStyleDropdownButton;
    private EnumListDropdown colorConverterDropdownButton;
    private EnumListDropdown useAuxBlocksDropdownButton;
    private PresetsListDropdown presetsListDropdownButton;
    private MapColorPickerDropdown mapColorPickerDropdownWidget;
    private DropdownOverlayWidget saveMapartDropdownWidget;
    private DropdownOverlayWidget rgbPropagationDropdownWidget;
    private ImagePreprocessingDropdown preprocessingDropdownWidget;

    private Button saveNBT;
    private Button saveSplitNBT;
    private Button saveZipNBT;
    private Button getMapItemsButton;
    private DecorativeButtonWidget showInWorldButton;
    private DecorativeButtonWidget resetExcludedColors;
    private DecorativeButtonWidget toggleCroppingControlsButton;

    private final MaterialListPanel materialList = new MaterialListPanel(this, mapart, 0, 0, 0, 0);

    public MapartEditorScreen() {
        super(null, Component.translatable("maparthelper.gui.mapart_editor_screen"));
    }

    public void setHighlightingColor(MapColor color) {
        mapartPreview.setHighlightingColor(color);
    }

    public void updateMaterialList() {
        materialList.updateMaterialList(this::addRenderableWidget, this::removeWidget);
    }

    @Override
    protected void preInit() {
        initMapartOptionsPanel();

        int middleWidth = width - 2 * baseElementWidth - 42;
        int widthDiff = mapartOptions.getWidth() - middleWidth;
        shortElements = widthDiff > 0;
    }

    @Override
    protected void initContent() {
        initRightPanel();
        initLeftPanel();

        // =========== Mapart preview area ===========

        mapartPreview = new MapartPreviewWidget(mapart,
                settingsLeft.getX() + settingsLeft.getWidth() + 2, 33,
                settingsRight.getX() - 15, this.height - 20
        );
        this.addRenderableWidget(mapartPreview);

        mapartOptions.setPosition(mapartPreview.getImageX(), mapartPreview.getY() - mapartOptions.getHeight() - 2);
        mapartOptions.arrangeElements();
        mapartOptions.visitWidgets(this::addRenderableWidget);

        updateMapartOutputButtons();
    }

    @Override
    protected Set<OverlayLayout> initOverlays() {
        final int elementWidth = currentElementWidth();
        Set<OverlayLayout> overlays = new HashSet<>();

        croppingModeDropdownButton = new EnumListDropdown(
                this,
                elementWidth, 20,
                elementWidth, 150,
                Component.translatable("maparthelper.gui.cropMode"),
                CurrentConversionSettings.cropMode,
                !shortElements,
                true,
                e -> {
                    CroppingMode cropMode = (CroppingMode) e;
                    CurrentConversionSettings.cropMode = cropMode;
                    if (cropMode == CroppingMode.USER_CROP && CurrentConversionSettings.doShowCroppingControls) {
                        toggleCroppingControlsButton.setTextureColor(0xFF_55ffff);
                    } else {
                        toggleCroppingControlsButton.setTextureColor(-1);
                    }
                    MapartImageUpdater.changeCroppingMode(mapart, cropMode);
                },
                CroppingMode.values()
        );
        staircaseStyleDropdownButton = new EnumListDropdown(
                this,
                elementWidth, 20,
                elementWidth, 150,
                Component.translatable("maparthelper.gui.staircaseStyle"),
                MapartHelper.conversionConfig().getStaircaseStyle(),
                !shortElements,
                MapartHelper.commonConfig().showStaircaseTooltips,
                e -> {
                    if (MapartHelper.conversionConfig().setStaircaseStyle((StaircaseStyles) e))
                        MapartImageUpdater.updateMapart(mapart);
                    updateMapartOutputButtons();
                },
                StaircaseStyles.FLAT_2D,
                StaircaseStyles.VALLEY_3D,
                StaircaseStyles.WAVES_3D,
                MapartHelper.commonConfig().displayUnobtainableMode ? StaircaseStyles.UNOBTAINABLE : null
        );
        colorConverterDropdownButton = new EnumListDropdown(
                this,
                elementWidth, 20,
                elementWidth, 280,
                Component.translatable("maparthelper.gui.ditheringAlg"),
                MapartHelper.conversionConfig().getColorConverter(),
                !shortElements,
                true,
                btn -> {
                    if (MapartHelper.conversionConfig().getColorConverter().ditheringType() == DitheringTypes.ERROR_DIFFUSION) {
                        btn.setWidth(elementWidth - rgbPropagationDropdownWidget.getWidth() - 2);
                        rgbPropagationDropdownWidget.visible = true;
                        rgbPropagationDropdownWidget.setX(btn.getRight() + 2);
                    } else {
                        btn.setWidth(elementWidth);
                        rgbPropagationDropdownWidget.visible = false;
                    }
                },
                e -> {
                    MapartHelper.conversionConfig().setColorConverter((ColorConverters) e);
                    MapartImageUpdater.updateMapart(mapart);
                },

                ColorConverters.values()
        );
        useAuxBlocksDropdownButton = new EnumListDropdown(
                this,
                elementWidth, 20,
                elementWidth, 150,
                Component.translatable("maparthelper.gui.use_aux"),
                MapartHelper.conversionConfig().getUseAuxBlocks(),
                true, true,
                e -> {
                    MapartHelper.conversionConfig().setUseAuxBlocks((UseAuxBlocks) e);
                    updateMaterialList();
                },
                UseAuxBlocks.values()
        );
        presetsListDropdownButton = new PresetsListDropdown(
                this,
                elementWidth, 20,
                elementWidth, 150,
                true,
                Component.nullToEmpty("\"" + palettePresetsHandler.getSelectedPreset().presetName() + "\""),
                uuid -> {
                    PaletteColors.clearExcludingColors();
                    updateResetExcludedColorsButton(false);
                    paletteDataManager.changeSelectedPreset(uuid);
                    MapColor oldBgColor = MapartHelper.conversionConfig().getBackgroundColor().mapColor();
                    if (palettePresetsHandler.getSelectedPreset().getBlockOfMapColor(oldBgColor) == null) {
                        MapartHelper.conversionConfig().setBackgroundColor(MapColorEntry.CLEAR);
                    }
                    MapartImageUpdater.updateMapart(mapart);
                },
                palettePresetsHandler.getPresets()
        );
        mapColorPickerDropdownWidget = new MapColorPickerDropdown(
                this,
                mapart,
                20, 20,
                elementWidth, 160
        );
        OverlayLayout saveMapartDropdownOverlay = saveMapartDropdownWidget.getOverlay();

        rgbPropagationDropdownWidget = new ErrorPropagationDropdown(
                this, mapart,
                20, 20,
                16, 16,
                elementWidth + 4, 124,
                new WidgetSprites(
                        MapartHelper.identifier("textures/gui/icons/sliders.png"),
                        MapartHelper.identifier("textures/gui/icons/sliders.png")
                )
        );

        rgbPropagationDropdownWidget.setTooltip(Tooltip.create(Component.translatable("maparthelper.gui.errorPropagation")));
        rgbPropagationDropdownWidget.visible = false;
        rgbPropagationDropdownWidget.setOverlayXOffset(-rgbPropagationDropdownWidget.getOverlay().getWidth());

        preprocessingDropdownWidget = new ImagePreprocessingDropdown(
                this, mapart,
                100, 20,
                elementWidth + 4, 124,
                Component.translatable("maparthelper.gui.image_preprocessing")
        );

        overlays.add(croppingModeDropdownButton.getOverlay());
        overlays.add(staircaseStyleDropdownButton.getOverlay());
        overlays.add(colorConverterDropdownButton.getOverlay());
        overlays.add(useAuxBlocksDropdownButton.getOverlay());
        overlays.add(presetsListDropdownButton.getOverlay());
        overlays.add(mapColorPickerDropdownWidget.getOverlay());
        overlays.add(rgbPropagationDropdownWidget.getOverlay());
        overlays.add(preprocessingDropdownWidget.getOverlay());
        overlays.add(saveMapartDropdownOverlay);

        return overlays;
    }

    private int currentElementWidth() {
        return !shortElements ? baseElementWidth : MapartHelper.commonConfig().showImageImportButton ? 125 : 135;
    }

    private void initMapartOptionsPanel() {
        mapartOptions = LinearLayout.horizontal().spacing(1);
        mapartOptions.defaultCellSetting().alignVerticallyMiddle();

        if (MapartHelper.commonConfig().showImageImportButton) {
            DecorativeButtonWidget importButton = DecorativeButtonWidget.builderSimpleTexture(
                    OPEN_FILE_ICON,
                    btn -> FileDialogsUtils.openImageImportDialog(path ->
                            Minecraft.getInstance().execute(() -> readImage(Path.of(path)))
                    )
            ).size(16, 16).build();
            importButton.setTooltip(Tooltip.create(Component.translatable("maparthelper.gui.import_tooltip", "Import Image")));
            mapartOptions.addChild(importButton);
        }

        saveMapartDropdownWidget = new DropdownOverlayWidget(
                this, createSaveMapartDropdown(),
                16, 16,
                false,
                false,
                new WidgetSprites(SAVE_ICON, SAVE_ICON)
        );
        saveMapartDropdownWidget.setTooltip(Tooltip.create(Component.translatable("maparthelper.gui.save_mapart_as")));
        mapartOptions.addChild(saveMapartDropdownWidget);

        DecorativeButtonWidget showGridButton = DecorativeButtonWidget.builderSimpleTexture(
                GRID_ICON,
                btn -> {
                    boolean doShowGrid = !CurrentConversionSettings.doShowGrid;
                    CurrentConversionSettings.doShowGrid = doShowGrid;
                    btn.setTextureColor(doShowGrid ? 0xFF_55ffff : -1);
                }
        ).size(16, 16).build();
        showGridButton.setTextureColor(CurrentConversionSettings.doShowGrid ? 0xFF_55ffff : -1);
        showGridButton.setTooltip(Tooltip.create(Component.translatable("maparthelper.gui.showGrid_tooltip")));
        mapartOptions.addChild(showGridButton);

        showInWorldButton = DecorativeButtonWidget.builder(
                PREVIEW_ICON,
                PREVIEW_DISABLED_ICON,
                btn -> {
                    LocalPlayer player = Minecraft.getInstance().player;
                    if (player == null) return;
                    if (FakeMapsPreview.createFakeFramesFromMapart(mapart, player)) {
                        FakeMapsPreview.showFakeFrames(player, mapart.getWidth(), mapart.getHeight());
                        this.onClose();
                    }
                }
        ).size(16, 16).build();
        showInWorldButton.setTooltip(Tooltip.create(Component.translatable("maparthelper.gui.showInWorld_tooltip")));
        mapartOptions.addChild(showInWorldButton);

        toggleCroppingControlsButton = DecorativeButtonWidget.builderSimpleTexture(
                CROPPING_BUTTONS_ICON,
                btn -> {
                    if (CurrentConversionSettings.cropMode != CroppingMode.USER_CROP) return;
                    boolean doShowButton = !CurrentConversionSettings.doShowCroppingControls;
                    CurrentConversionSettings.doShowCroppingControls = doShowButton;
                    btn.setMessage(btn.getMessage().copy().withStyle(doShowButton ? ChatFormatting.RESET : ChatFormatting.DARK_GRAY));
                    btn.setTextureColor(doShowButton ? 0xFF_55ffff : -1);
                }
        ).size(16, 16).build();
        if (CurrentConversionSettings.cropMode == CroppingMode.USER_CROP && CurrentConversionSettings.doShowCroppingControls) {
            toggleCroppingControlsButton.setTextureColor(0xFF_55ffff);
        } else {
            toggleCroppingControlsButton.setTextureColor(-1);
        }
        toggleCroppingControlsButton.setTooltip(Tooltip.create(Component.translatable("maparthelper.gui.toggle_manual_cropping_buttons")));
        mapartOptions.addChild(toggleCroppingControlsButton);

        DecorativeButtonWidget fullscreenButton = DecorativeButtonWidget.builderSimpleTexture(
                FULLSCREEN_ICON,
                btn -> Minecraft.getInstance().setScreen(new FullscreenImageViewScreen())
        ).size(16, 16).build();
        fullscreenButton.setTooltip(Tooltip.create(Component.translatable("maparthelper.gui.fullscreen_view.open_view_screen")));
        mapartOptions.addChild(fullscreenButton);

        DecorativeButtonWidget resetMapartButton = DecorativeButtonWidget.builderSimpleTexture(
                RESET_ICON,
                b -> {
                    CurrentConversionSettings.resetMapart();
                    updateMapartOutputButtons();
                    updateMaterialList();
                    updateResetExcludedColorsButton(false);
                }
        ).size(16, 16).build();
        resetMapartButton.setTooltip(Tooltip.create(Component.translatable("maparthelper.gui.reset_mapart")));
        mapartOptions.addChild(resetMapartButton);

        mapartOptions.arrangeElements();
    }

    private void initLeftPanel() {

        final int elementWidth = currentElementWidth();
        settingsLeft = LinearLayout.vertical();
        settingsLeft.setPosition(5, 20);
        settingsLeft.addChild(SpacerElement.width(elementWidth + 7));
        LayoutSettings settingsLeftPositioner = settingsLeft.defaultCellSetting().paddingTop(5);
        LayoutSettings topLabeledPositioner = settingsLeftPositioner.copy().paddingTop(0);

        AdjEditBox mapartName = new AdjEditBox(
                font, elementWidth, 20, mapart.mapartName
        );
        mapartName.setHint(Component.translatable("maparthelper.gui.mapart_name_field").withColor(CommonColors.GRAY));
        mapartName.setValueValidator(s -> !s.isBlank());
        mapartName.setFilter(TextFieldPredicates.validPathName());
        mapartName.setValueConsumer(newValue -> mapart.mapartName = newValue);
        settingsLeft.addChild(new StringWidget(Component.translatable("maparthelper.gui.mapart_name_field"), font));
        settingsLeft.addChild(mapartName, topLabeledPositioner);

        LinearLayout size = createSizeSettingsGrid();
        settingsLeft.addChild(size);

        settingsLeft.arrangeElements();
        settingsLeft.visitWidgets(this::addRenderableWidget);

        this.addRenderableWidget(DecorativeButtonWidget.builderSimpleTexture(
                SETTINGS_ICON, btn -> {
                    ConversionConfiguration.save();
                    Minecraft.getInstance().setScreen(ConfigScreenFactory.getConfigScreen(this));
                }
        ).dimensions(2, 4, 16, 16).build());

        int listTop = settingsLeft.getY() + settingsLeft.getHeight();
        LinearLayout settingsLeftContent = LinearLayout.vertical();
        settingsLeftContent.defaultCellSetting().paddingTop(5);

        Component previewMapart = Component.translatable("maparthelper.gui.previewMapart");
        Component previewOriginal = Component.translatable("maparthelper.gui.previewOriginal").withStyle(ChatFormatting.GOLD);
        Button previewMode = Button.builder(
                MapartHelper.conversionConfig().isShowOriginalImage() ? previewOriginal : previewMapart,
                (btn) -> {
                    MapartHelper.conversionConfig().toggleShowOriginalImage();
                    btn.setMessage(MapartHelper.conversionConfig().isShowOriginalImage() ? previewOriginal : previewMapart);
                    MapartImageUpdater.updateMapart(mapart);
                }
        ).size(elementWidth, 20).build();
        settingsLeftContent.addChild(new StringWidget(Component.translatable("maparthelper.gui.previewMode"), font));
        settingsLeftContent.addChild(previewMode, topLabeledPositioner);

        // ===============================================

        if (shortElements) {
            settingsLeftContent.addChild(new StringWidget(Component.translatable("maparthelper.gui.cropMode"), font));
            settingsLeftContent.addChild(croppingModeDropdownButton, topLabeledPositioner);
            settingsLeftContent.addChild(new StringWidget(Component.translatable("maparthelper.gui.staircaseStyle"), font));
            settingsLeftContent.addChild(staircaseStyleDropdownButton, topLabeledPositioner);
            settingsLeftContent.addChild(new StringWidget(Component.translatable("maparthelper.gui.ditheringAlg"), font));
        } else {
            settingsLeftContent.addChild(croppingModeDropdownButton);
            settingsLeftContent.addChild(staircaseStyleDropdownButton);
        }

        LinearLayout colorConverterLine = LinearLayout.horizontal().spacing(2);
        colorConverterDropdownButton.setWidth(elementWidth - rgbPropagationDropdownWidget.getWidth() - 2);
        if (MapartHelper.conversionConfig().getColorConverter().ditheringType() == DitheringTypes.ERROR_DIFFUSION) {
            rgbPropagationDropdownWidget.visible = true;
        }

        colorConverterLine.addChild(colorConverterDropdownButton);
        colorConverterLine.addChild(rgbPropagationDropdownWidget);
        if (shortElements) settingsLeftContent.addChild(colorConverterLine, topLabeledPositioner);
        else settingsLeftContent.addChild(colorConverterLine);

        Component isOn = Component.translatable("maparthelper.gui.isOn");
        Component isOff = Component.translatable("maparthelper.gui.isOff");
        Button useLAB = Button.builder(
                Component.literal("LAB: ").append(MapartHelper.conversionConfig().useLAB() ? isOn : isOff),
                (btn) -> {
                    MapartHelper.conversionConfig().toggleLAB();
                    btn.setMessage(Component.literal("LAB: ").append(MapartHelper.conversionConfig().useLAB() ? isOn : isOff));
                    MapartImageUpdater.updateMapart(mapart);
                }
        ).size(80, 20).build();

        if (MapartHelper.commonConfig().showUseLABTooltip) {
            useLAB.setTooltip(Tooltip.create(Component.translatable("maparthelper.gui.useLAB_tooltip")));
            useLAB.setTooltipDelay(Duration.ofMillis(500));
        }
        settingsLeftContent.addChild(useLAB);
        settingsLeftContent.addChild(preprocessingDropdownWidget);

        LinearLayout bgColorChoose = LinearLayout.horizontal();
        bgColorChoose.defaultCellSetting().alignVerticallyMiddle();
        bgColorChoose.addChild(new StringWidget(Component.translatable("maparthelper.gui.backgroundColor"), font));
        bgColorChoose.addChild(mapColorPickerDropdownWidget);
        settingsLeftContent.addChild(bgColorChoose);

        settingsLeftContent.addChild(
                new StringWidget(Component.translatable("maparthelper.aux_block"), font),
                settingsLeftPositioner.copy().paddingTop(15)
        );
        String currentAuxBlock = BuiltInRegistries.BLOCK.getKey(MapartHelper.conversionConfig().getAuxBlock()).toString();
        if (currentAuxBlock.contains("minecraft:"))
            currentAuxBlock = currentAuxBlock.substring(10);
        BlockItemWidget auxBlockPreview = new BlockItemWidget(0, 0, 24, MapartHelper.conversionConfig().getAuxBlock(), false);

        GridLayout auxBlock = new GridLayout().spacing(5);
        auxBlock.defaultCellSetting().alignVerticallyMiddle();
        GridLayout.RowHelper auxAdder = auxBlock.createRowHelper(2);
        auxAdder.addChild(createAuxBlockFieldWidget(auxBlockPreview, currentAuxBlock));
        auxAdder.addChild(auxBlockPreview);
        settingsLeftContent.addChild(auxBlock);

        settingsLeftContent.addChild(useAuxBlocksDropdownButton);

        AdjScrollableLayoutWidget settingsLeftScrollable = new AdjScrollableLayoutWidget(
                settingsLeftContent, height - listTop
        );
        settingsLeftScrollable.setMarginX(1);
        settingsLeftScrollable.setWidth(settingsLeft.getRectangle().right());
        settingsLeftScrollable.setPosition(0, listTop);

        settingsLeftScrollable.arrangeElements();
        settingsLeftScrollable.visitWidgets(this::addRenderableWidget);

        if (MapartHelper.conversionConfig().getColorConverter().ditheringType() != DitheringTypes.ERROR_DIFFUSION) {
            colorConverterDropdownButton.setWidth(elementWidth);
        }
    }

    private void initRightPanel() {
        final int elementWidth = currentElementWidth();
        settingsRight = LinearLayout.vertical();
        LayoutSettings settingsRightPositioner = settingsRight.defaultCellSetting().paddingTop(5);

        StringWidget currentPresetLabel = new StringWidget(Component.translatable("maparthelper.gui.current_preset_label"), font);
        settingsRight.addChild(currentPresetLabel);
        settingsRight.addChild(presetsListDropdownButton, settingsRightPositioner.copy().paddingTop(0));

        Button presetsEditor = Button.builder(
                Component.translatable("maparthelper.gui.presets_editor_screen"),
                (btn) -> {
                    ConversionConfiguration.save();
                    Minecraft.getInstance().setScreen(
                            new PresetsEditorScreen(this, mapart, 45, 20, 45, 20)
                    );
                }
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
                        })
                .size(10, 10)
                .vanillaButtonBackground(false)
                .textColorActive(CommonColors.LIGHT_GRAY)
                .build()
        );
        materialListSettings.addChild(new StringWidget(Component.translatable("maparthelper.gui.material_list_label"), font));

        resetExcludedColors = DecorativeButtonWidget.builder(
                RESET_ICON,
                RESET_DISABLED_ICON,
                btn -> {
                    if (PaletteColors.excludingColorsAmount() == 0) return;
                    MapartImageUpdater.revertExcludingColors(mapart);
                    updateResetExcludedColorsButton(false);
                }
        ).size(18, 18).textureSize(14, 14).vanillaButtonBackground(true).build();
        materialListSettings.addChild(resetExcludedColors);

        updateResetExcludedColorsButton(PaletteColors.excludingColorsAmount() > 0);
        settingsRight.addChild(materialListSettings);

        Component perMapCountMode = Component.translatable("maparthelper.gui.countMode.perBlock");
        Component fullCountMode = Component.translatable("maparthelper.gui.countMode.full");
        Button materialsCountMode = Button
                .builder(MapartHelper.conversionConfig().getMaterialsCountMode() == MaterialsCountModes.FULL ? fullCountMode : perMapCountMode,
                        btn -> {
                            MapartHelper.conversionConfig().nextMaterialsCountMode();
                            MaterialsCountModes mode = MapartHelper.conversionConfig().getMaterialsCountMode();
                            btn.setMessage(mode == MaterialsCountModes.FULL ? fullCountMode : perMapCountMode);
                            btn.setTooltip(Tooltip.create(mode.getDescription()));
                            updateMaterialList();
                        })
                .size(elementWidth, 14)
                .build();
        materialsCountMode.setTooltip(Tooltip.create(MapartHelper.conversionConfig().getMaterialsCountMode().getDescription()));
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

        if (paletteDataManager.isPaletteOutdated()) {
            var regenBtn = DecorativeButtonWidget.builderHighlightable(
                            Identifier.parse("textures/gui/sprites/dialog/warning_button.png"),
                            Identifier.parse("textures/gui/sprites/dialog/warning_button_highlighted.png"),
                            btn -> {
                                ConversionConfiguration.save();
                                Minecraft.getInstance().setScreen(new PaletteUpdateSuggestionScreen(this));
                            }
                    )
                    .dimensions(currentPresetLabel.getX(), 2, 20, 20)
                    .build();
            this.addRenderableWidget(regenBtn);
        }

        materialList.setPosition(settingsRight.getX(), settingsRight.getY() + settingsRight.getHeight());
        materialList.setSize(settingsRight.getWidth(), height - settingsRight.getHeight());
        materialList.setDisplayTotalCount(!shortElements);
        updateMaterialList();
    }

    private @NotNull AdjEditBox createAuxBlockFieldWidget(BlockItemWidget auxBlockPreview, String currentAuxBlock) {
        AdjEditBox auxBlockId = new AdjEditBox(
                font, currentElementWidth() - auxBlockPreview.getWidth() - 5, 20,
                currentAuxBlock
        );
        auxBlockId.setValueValidator(TextFieldValidators.auxBlockIdentifier());
        auxBlockId.setValueConsumer(idStr -> {
            Identifier id = Identifier.parse(idStr);
            Block block = BuiltInRegistries.BLOCK.getValue(id);
            MapartHelper.conversionConfig().setAuxBlock(block);
            auxBlockPreview.setBlock(block);
            updateMaterialList();
        });
        return auxBlockId;
    }

    //~ gui_rendering
    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(0, 0, settingsLeft.getX() + settingsLeft.getWidth(), height, 0x77000000);
        graphics.fill(settingsRight.getX() - 7, 0, width, height, 0x77000000);
        super.render(graphics, mouseX, mouseY, partialTick);

        if (!MaterialListPanel.MaterialListBlockWidget.isHoveringAny()) {
            MaterialListPanel.MaterialListBlockWidget.setDefaultHighlight(mapartPreview);
        }
        MaterialListPanel.MaterialListBlockWidget.resetHovering();
    }
    //~ !gui_rendering

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

    private LinearLayout createSizeSettingsGrid() {
        LinearLayout sizeLayout = LinearLayout.vertical().spacing(1);
        LinearLayout inputFieldsLayout = LinearLayout.horizontal().spacing(1);
        inputFieldsLayout.defaultCellSetting().alignVerticallyMiddle();

        // TODO: Configurable max value

        IntegerFieldWidget xSizeField = new IntegerFieldWidget(
                font, 30, 20, mapart.getWidth(), 1, 50
        );
        xSizeField.setIntegerValueConsumer(value -> {
            if (value != mapart.getWidth()) {
                CurrentConversionSettings.guiMapartImage = null;
                MapartImageUpdater.resizeMapartImage(mapart, value, mapart.getHeight());
            }
        });

        IntegerFieldWidget ySizeField = new IntegerFieldWidget(
                font, 30, 20, mapart.getHeight(), 1, 50
        );
        ySizeField.setIntegerValueConsumer(value -> {
            if (value != mapart.getHeight()) {
                CurrentConversionSettings.guiMapartImage = null;
                MapartImageUpdater.resizeMapartImage(mapart, mapart.getWidth(), value);
            }
        });

        inputFieldsLayout.addChild(new StringWidget(Component.literal("x:"), font));
        inputFieldsLayout.addChild(xSizeField, inputFieldsLayout.newCellSettings().paddingRight(10));
        inputFieldsLayout.addChild(new StringWidget(Component.literal("y:"), font));
        inputFieldsLayout.addChild(ySizeField);


        sizeLayout.addChild(new StringWidget(Component.translatable("maparthelper.gui.mapart_size_label"), font));
        sizeLayout.addChild(inputFieldsLayout);

        return sizeLayout;
    }

    private OverlayLayout createSaveMapartDropdown() {
        boolean isIntegratedServer = Minecraft.getInstance().hasSingleplayerServer();

        Button saveImage = Button.builder(
                Component.translatable("maparthelper.gui.savePNG"),
                (btn) -> {
                    Player player = /*? if <=1.21.8 {*/ minecraft == null ? null : /*?}*/ minecraft.player;
                    MapartSaver.saveMapartImage(mapart.mapartName, CurrentConversionSettings.guiMapartImage, player);
                }
        ).size(150, 20).build();

        saveNBT = Button.builder(
                Component.translatable("maparthelper.gui.saveNBT"),
                (btn) -> MapartToNBT.saveNBT(true)
        ).size(150, 20).build();

        saveSplitNBT = Button.builder(
                Component.translatable("maparthelper.gui.saveEveryNBT"),
                (btn) -> MapartToNBT.saveNBT(false)
        ).size(150, 20).build();

        saveZipNBT = Button.builder(
                Component.translatable("maparthelper.gui.saveZip"),
                (btn) -> MapartToNBT.saveNBTAsZip()
        ).size(150, 20).build();

        Minecraft mc = Minecraft.getInstance();
        if (isIntegratedServer && mc.getSingleplayerServer() != null && mc.player != null) {
            ServerPlayer serverPlayer = mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());
            getMapItemsButton = Button.builder(
                    Component.translatable("maparthelper.gui.save_map_items").withStyle(ChatFormatting.GOLD),
                    btn -> {
                        if (mapart.getNativeImage() == null) return;
                        int[][] maps = NativeImageUtils.divideImageByMaps(mapart.getNativeImage());
                        MapCreator.getMapsForMapart(maps, mapart.getWidth(), mapart.mapartName, mc.getSingleplayerServer().overworld(), serverPlayer);
                    }
            ).size(150, 20).build();
        }

        return OverlayLayoutFactory.listMenu(
                150, 160,
                saveImage, saveNBT, saveSplitNBT, saveZipNBT, getMapItemsButton
        );
    }

    public void updateMapartOutputButtons() {
        boolean active = CurrentConversionSettings.isMapartConverted();
        boolean obtainableNbt = !(MapartHelper.conversionConfig().getStaircaseStyle() == StaircaseStyles.UNOBTAINABLE);
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
            resetExcludedColors.setTooltip(Tooltip.create(
                    excludedAmount.withStyle(ChatFormatting.GOLD).append("\n")
                            .append(revertExcluding.withStyle(ChatFormatting.GRAY))
            ));
            resetExcludedColors.active = true;
            resetExcludedColors.setTextureColor(0xFF_ffaa00);
        } else {
            resetExcludedColors.setTooltip(null);
            resetExcludedColors.active = false;
            resetExcludedColors.setTextureColor(-1);
        }
    }

    private void readImage(Path filepath) {
        CurrentConversionSettings.resetMapart();
        MapartImageUpdater.readAndUpdateMapartImage(mapart, filepath);
        updateResetExcludedColorsButton(false);
    }

    @Override
    public void onClose() {
        ConversionConfiguration.save();
        super.onClose();
    }
}
