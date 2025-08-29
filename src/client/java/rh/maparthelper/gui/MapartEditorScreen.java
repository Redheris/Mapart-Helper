package rh.maparthelper.gui;

import me.shedaniel.autoconfig.AutoConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.*;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import rh.maparthelper.MapartHelper;
import rh.maparthelper.config.ConversionConfiguration;
import rh.maparthelper.config.MapartHelperConfig;
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

    private int auxBlockCount = 0;

    public MapartEditorScreen() {
        super(Text.translatable("maparthelper.mapart_editor_screen"));
    }

    public void updateMaterialList() {
        this.remove(materialList);
        int listTop = settingsRight.getY() + settingsRight.getHeight();

        materialList = new ScrollableGridWidget(
                null,
                settingsRight.getX() - 6, listTop,
                width - settingsRight.getX() - 1, height - listTop,
                listTop, height, 6
        );
        materialList.setLeftScroll(true);
        materialList.grid.setColumnSpacing(0);
        materialList.grid.getMainPositioner().alignVerticalCenter();
        GridWidget.Adder materialListAdder = materialList.grid.createAdder(2);

        PalettePresetsConfig palette = PaletteConfigManager.presetsConfig;
        MapartImageConverter.MapColorCount[] colorsCounter = MapartImageConverter.getColorsCounter();

        this.auxBlockCount = CurrentConversionSettings.getWidth() * 128;
        TextWidget auxAmount = addBlockToMaterialList(
                materialListAdder, palette, new MapartImageConverter.MapColorCount(0, auxBlockCount)
        );
        for (MapartImageConverter.MapColorCount colorCount : colorsCounter) {
            addBlockToMaterialList(materialListAdder, palette, colorCount);
        }
        if (auxAmount != null) {
            Block auxBlock = MapartHelper.config.conversionSettings.auxBlock;
            Text amountText = Text.of(getCountString(auxBlockCount, auxBlock.asItem().getMaxCount()));
            auxAmount.setWidth(textRenderer.getWidth(amountText));
            auxAmount.setMessage(amountText);
        }

        materialList.refreshPositions();
        this.addDrawableChild(materialList);
    }

    private TextWidget addBlockToMaterialList(GridWidget.Adder adder, PalettePresetsConfig palette, MapartImageConverter.MapColorCount color) {
        if (color.amount() == 0) return null;
        Block block = color.id() == 0 ? MapartHelper.config.conversionSettings.auxBlock : palette.getBlockOfMapColor(MapColor.get(color.id()));
        if (block == null) return null;

        adder.add(
                new BlockItemWidget(this, 0, 0, 24, 24, block),
                materialList.grid.copyPositioner().marginLeft(6)
        );
        TextWidget amountText = new TextWidget(Text.of(getCountString(color.amount(), block.asItem().getMaxCount())), textRenderer);
        adder.add(amountText);
        amountText.setTooltip(Tooltip.of(amountText.getMessage()));
        amountText.setTooltipDelay(Duration.ofMillis(100));

        if (color.id() > 0 && NbtSchematicUtils.needsAuxBlock(block)) {
            auxBlockCount += color.amount();
        }
        return amountText;
    }

    private String getCountString(int amount, int stackSize) {
        StringBuilder text = new StringBuilder();
        int shBoxSize = 27 * stackSize;
        int shBoxes = amount / shBoxSize;
        int stacks = amount % shBoxSize / stackSize;
        int items = amount % shBoxSize % stackSize;
        boolean counted = shBoxes > 0 || stacks > 0;

        if (shBoxes > 0) text.append(shBoxes).append("§3SB§r");
        if (stacks > 0) text.append(shBoxes > 0 ? " + " : "").append(stacks).append("§3x64§r");
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
        Positioner settingLeftPositioner = settingsLeft.getMainPositioner().marginTop(5);

        int baseElementWidth = 165;
        TextFieldWidget mapartName = createTextInputFieldWidget(baseElementWidth, CurrentConversionSettings.mapartName, -1);
        mapartName.setChangedListener(value -> {
            mapartName.setEditableColor(Colors.WHITE);
            if (value.isEmpty()) {
                mapartName.setSuggestion("Название мапарта");
                return;
            }
            mapartName.setSuggestion(null);
            if (value.matches(".*[<>:\"/|?*\\\\].*")) {
                mapartName.setEditableColor(Colors.LIGHT_RED);
                return;
            }
            CurrentConversionSettings.mapartName = value;
        });
        settingsLeft.add(new TextWidget(Text.literal("Название мапарта"), textRenderer));
        settingsLeft.add(mapartName, settingLeftPositioner.copy().marginTop(0));

        GridWidget size = createSizeSettingsGrid();
        settingsLeft.add(size);

        EnumDropdownMenuWidget croppingMode = new EnumDropdownMenuWidget(
                this, 0, 0, baseElementWidth, 20, baseElementWidth,
                Text.of("Кадрирование: "), Text.translatable("maparthelper.option." + CurrentConversionSettings.cropMode.name())
        );
        croppingMode.addEntries(
                e -> {
                    CurrentConversionSettings.cropMode = (CroppingMode) e;
                    MapartImageConverter.updateMapart();
                },
                CroppingMode.values()
        );
        croppingMode.forEachEntry(this::addSelectableChild);
        settingsLeft.add(croppingMode);

        EnumDropdownMenuWidget staircaseStyle = new EnumDropdownMenuWidget(
                this, 0, 0, baseElementWidth, 20, baseElementWidth,
                Text.of("Ступенчатость: "), Text.translatable("maparthelper.option." + MapartHelper.config.conversionSettings.staircaseStyle.name())
        );
        staircaseStyle.addEntries(
                e -> {
                    ConversionConfiguration config = MapartHelper.config.conversionSettings;
                    boolean was3D = config.use3D();
                    config.staircaseStyle = (StaircaseStyles) e;
                    if (config.use3D() != was3D)
                        MapartImageConverter.updateMapart();
                    AutoConfig.getConfigHolder(MapartHelperConfig.class).save();
                },
                StaircaseStyles.values()
        );
        staircaseStyle.forEachEntry(this::addSelectableChild);
        settingsLeft.add(staircaseStyle);

        EnumDropdownMenuWidget ditheringAlg = new EnumDropdownMenuWidget(
                this, 0, 0, baseElementWidth, 20, baseElementWidth,
                Text.of("Дизеринг: "), Text.translatable("maparthelper.option." + MapartHelper.config.conversionSettings.ditheringAlgorithm.name())
        );
        ditheringAlg.addEntries(
                e -> {
                    MapartHelper.config.conversionSettings.ditheringAlgorithm = (DitheringAlgorithms) e;
                    MapartImageConverter.updateMapart();
                    AutoConfig.getConfigHolder(MapartHelperConfig.class).save();
                },
                DitheringAlgorithms.values()
        );
        ditheringAlg.forEachEntry(this::addSelectableChild);
        settingsLeft.add(ditheringAlg);

        ButtonWidget useLAB = ButtonWidget.builder(
                MapartHelper.config.conversionSettings.useLAB ? Text.of("LAB: вкл") : Text.of("LAB: выкл"),
                (btn) -> {
                    MapartHelper.config.conversionSettings.useLAB = !MapartHelper.config.conversionSettings.useLAB;
                    btn.setMessage(MapartHelper.config.conversionSettings.useLAB ? Text.of("LAB: вкл") :
                            Text.of("LAB: выкл"));
                    MapartImageConverter.updateMapart();
                }
        ).size(80, 20).build();
        useLAB.setTooltip(Tooltip.of(Text.of("Улучшает подбор цветов. Заметно влияет на скорость обработки, поэтому для средних и больших артов рекомендуется применять §cпосле настройки остальных параметров")));
        useLAB.setTooltipDelay(Duration.ofSeconds(1));
        settingsLeft.add(useLAB);

        DropdownMenuWidget imagePreprocessing = createImagePreprocessingDropdown();
        imagePreprocessing.forEachEntry(this::addSelectableChild);
        settingsLeft.add(imagePreprocessing);

//        Useless when updates automatically
//        ButtonWidget submit = ButtonWidget.builder(
//                Text.of("Применить изменения"),
//                (btn) -> MapartImageConverter.updateMapart()
//        ).size(130, 20).build();
//        settings.add(submit, positioner.copy().alignHorizontalCenter());

        settingsLeft.refreshPositions();
        settingsLeft.forEachChild(this::addDrawableChild);

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
        settingsRight.add(new TextWidget(Text.of("Текущий пресет:"), textRenderer));
        presetsList.forEachEntry(this::addSelectableChild);
        settingsRight.add(presetsList, settingsRightPositioner.copy().marginTop(0));

        ButtonWidget presetsEditor = ButtonWidget.builder(
                Text.of("Редактор пресетов"),
                (btn) -> MinecraftClient.getInstance().setScreen(
                        new PresetsEditorScreen(this, Text.translatable("maparthelper.presets_editor_screen"),
                                45, 30, 45, 30
                        ))
        ).size(baseElementWidth, 20).build();
        settingsRight.add(presetsEditor);

        settingsRight.add(
                new TextWidget(Text.of("Список материалов"), textRenderer),
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
        showGridButton.setTooltip(Tooltip.of(Text.of("Переключить отображение сетки")));
        mapartOptions.add(showGridButton);

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
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (this.hoveredElement(mouseX, mouseY).orElse(null) != mapartPreview)
            mapartPreview.scaleImageCrop(mouseX, mouseY, verticalAmount, false);
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
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
                CurrentConversionSettings.setWidth(Integer.parseInt(value));
                MapartImageConverter.updateMapart();
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
                CurrentConversionSettings.setHeight(Integer.parseInt(value));
                MapartImageConverter.updateMapart();
            } catch (NumberFormatException e) {
                heightInput.setEditableColor(Colors.LIGHT_RED);
            }
        });
        adder.add(new TextWidget(Text.literal("Размеры мапарта"), textRenderer), 2);
        adder.add(widthInput);
        adder.add(heightInput);

        return size;
    }

    private ImageAdjustmentSliderWidget createBrightnessSlider() {
        return new ImageAdjustmentSliderWidget(
                150, 15, 0.f, 2.f, true,
                CurrentConversionSettings.brightness,
                value -> {
                    CurrentConversionSettings.brightness = value.floatValue();
                    MapartImageConverter.updateMapart();
                },
                value -> String.format("Яркость: %.2f", value)
        );
    }

    private ImageAdjustmentSliderWidget createContrastSlider() {
        return new ImageAdjustmentSliderWidget(
                150, 15, -255, 255, false,
                CurrentConversionSettings.contrast,
                value -> {
                    CurrentConversionSettings.contrast = value.floatValue();
                    MapartImageConverter.updateMapart();
                },
                value -> String.format("Контраст: %.0f", value)
        );
    }

    private ImageAdjustmentSliderWidget createSaturationSlider() {
        return new ImageAdjustmentSliderWidget(
                150, 15, 0.f, 2.f, true,
                CurrentConversionSettings.saturation,
                value -> {
                    CurrentConversionSettings.saturation = value.floatValue();
                    MapartImageConverter.updateMapart();
                },
                value -> String.format("Насыщенность: %.2f", value)
        );
    }

    private DropdownMenuWidget createImagePreprocessingDropdown() {
        ImageAdjustmentSliderWidget sliderBrightness = createBrightnessSlider();
        ImageAdjustmentSliderWidget sliderContrast = createContrastSlider();
        ImageAdjustmentSliderWidget sliderSaturation = createSaturationSlider();

        ButtonWidget reset = ButtonWidget.builder(
                Text.of("Сброс"),
                (btn) -> {
                    CurrentConversionSettings.brightness = 1.0f;
                    CurrentConversionSettings.contrast = 0.0f;
                    CurrentConversionSettings.saturation = 1.0f;
                    sliderBrightness.setValue(0.5f);
                    sliderContrast.setValue(0.5f);
                    sliderSaturation.setValue(0.5f);
                }
        ).size(80, 20).build();

        DropdownMenuWidget imagePreprocessing = new DropdownMenuWidget(this, 0, 0, 100, 20, 154, Text.of("Предобработка"));
        imagePreprocessing.addEntry(reset);
        imagePreprocessing.addEntry(sliderBrightness);
        imagePreprocessing.addEntry(sliderContrast);
        imagePreprocessing.addEntry(sliderSaturation);

        return imagePreprocessing;
    }

    private DropdownMenuWidget createSaveMapartDropdown() {
        ButtonWidget saveImage = ButtonWidget.builder(
                Text.of("Сохранить PNG"),
                (btn) -> MapartImageConverter.saveMapartImage(CurrentConversionSettings.mapartName)
        ).size(156, 20).build();

        ButtonWidget saveNBT = ButtonWidget.builder(
                Text.of("Сохранить NBT"),
                (btn) -> MapartToNBT.saveNBT(true)
        ).size(156, 20).build();

        ButtonWidget saveSplitNBT = ButtonWidget.builder(
                Text.of("Сохранить NBT каждой карты"),
                (btn) -> MapartToNBT.saveNBT(false)
        ).size(156, 20).build();

        ButtonWidget saveZipNBT = ButtonWidget.builder(
                Text.of("Сохранить NBT в архиве"),
                (btn) -> MapartToNBT.saveNBTAsZip()
        ).size(156, 20).build();

        DropdownMenuWidget saveMapart = new DropdownMenuWidget(this, 0, 0, 20, 20, 160, Text.of("\uD83D\uDDAB"));
        saveMapart.setTooltip(Tooltip.of(Text.of("Сохранить мапарт как...")));
        saveMapart.addEntry(saveImage);
        saveMapart.addEntry(saveNBT);
        saveMapart.addEntry(saveSplitNBT);
        saveMapart.addEntry(saveZipNBT);

        return saveMapart;
    }
}
