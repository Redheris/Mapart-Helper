package rh.maparthelper.gui;

import me.shedaniel.autoconfig.AutoConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.*;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import rh.maparthelper.MapartHelper;
import rh.maparthelper.MapartHelperClient;
import rh.maparthelper.config.ConversionConfiguration;
import rh.maparthelper.config.MapartHelperConfig;
import rh.maparthelper.conversion.CroppingMode;
import rh.maparthelper.conversion.CurrentConversionSettings;
import rh.maparthelper.conversion.MapartImageConverter;
import rh.maparthelper.conversion.dithering.DitheringAlgorithms;
import rh.maparthelper.conversion.schematic.MapartToNBT;
import rh.maparthelper.conversion.staircases.StaircaseStyles;
import rh.maparthelper.gui.widget.DropdownMenuWidget;
import rh.maparthelper.gui.widget.ImageAdjustmentSliderWidget;
import rh.maparthelper.gui.widget.MapartPreviewWidget;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Environment(EnvType.CLIENT)
public class MapartEditorScreen extends Screen {
    private final List<Drawable> drawables = new ArrayList<>();

    DirectionalLayoutWidget settings;
    TextFieldWidget selectedTextWidget;
    DropdownMenuWidget selectedDropdownMenu;
    MapartPreviewWidget mapartPreview;

    public MapartEditorScreen() {
        super(Text.translatable("maparthelper.mapart_editor_screen"));
    }

    @Override
    protected void init() {
        settings = DirectionalLayoutWidget.vertical();
        settings.setPosition(15, 25);
        Positioner positioner = settings.getMainPositioner().margin(0, 5, 0, 0);

        TextFieldWidget mapartName = createTextInputFieldWidget(150, CurrentConversionSettings.mapartName, -1);
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
        settings.add(new TextWidget(Text.literal("Название мапарта"), textRenderer));
        settings.add(mapartName, positioner.copy().marginTop(0));

        GridWidget size = createSizeSettingsGrid();
        settings.add(size);

        ButtonWidget croppingMode = ButtonWidget.builder(
                Text.of("Кадрирование: " + CurrentConversionSettings.cropMode.name()),
                btn -> {
                    int nextMode = (CurrentConversionSettings.cropMode.ordinal() + 1) % CroppingMode.values().length;
                    CurrentConversionSettings.cropMode = CroppingMode.values()[nextMode];
                    btn.setMessage(Text.of("Кадрирование: " + CurrentConversionSettings.cropMode.name()));
                    MapartImageConverter.updateMapart();
                }
        ).size(150, 20).build();
        settings.add(croppingMode);

        ButtonWidget staircaseStyle = ButtonWidget.builder(
                Text.of("Ступенчатость: " + MapartHelperClient.conversionConfig.staircaseStyle.name()),
                btn -> {
                    ConversionConfiguration config = MapartHelperClient.conversionConfig;
                    int nextStyle = (config.staircaseStyle.ordinal() + 1) % StaircaseStyles.values().length;
                    boolean was3D = config.use3D();
                    config.staircaseStyle = StaircaseStyles.values()[nextStyle];
                    if (was3D != config.use3D())
                        MapartImageConverter.updateMapart();
                    btn.setMessage(Text.of("Ступенчатость: " + config.staircaseStyle.name()));
                    AutoConfig.getConfigHolder(MapartHelperConfig.class).save();
                }
        ).size(150, 20).build();
        settings.add(staircaseStyle);

        ButtonWidget ditheringAlg = ButtonWidget.builder(
                Text.of("Дизеринг: " + MapartHelperClient.conversionConfig.ditheringAlgorithm.name()),
                btn -> {
                    ConversionConfiguration config = MapartHelperClient.conversionConfig;
                    int nextAlg = (config.ditheringAlgorithm.ordinal() + 1) % DitheringAlgorithms.values().length;
                    config.ditheringAlgorithm = DitheringAlgorithms.values()[nextAlg];
                    MapartImageConverter.updateMapart();
                    btn.setMessage(Text.of("Дизеринг: " + config.ditheringAlgorithm.name()));
                    AutoConfig.getConfigHolder(MapartHelperConfig.class).save();
                }
        ).size(150, 20).build();
        settings.add(ditheringAlg);


        ButtonWidget showGridButton = ButtonWidget.builder(
                CurrentConversionSettings.doShowGrid ? Text.of("Сетка: вкл") : Text.of("Сетка: выкл"),
                (btn) -> {
                    CurrentConversionSettings.doShowGrid = !CurrentConversionSettings.doShowGrid;
                    btn.setMessage(CurrentConversionSettings.doShowGrid ? Text.of("Сетка: вкл") :
                            Text.of("Сетка: выкл"));
                }
        ).size(80, 20).build();
        settings.add(showGridButton, positioner.copy());

        ButtonWidget useLAB = ButtonWidget.builder(
                MapartHelper.config.conversionSettings.useLAB ? Text.of("LAB: вкл") : Text.of("LAB: выкл"),
                (btn) -> {
                    MapartHelper.config.conversionSettings.useLAB = !MapartHelper.config.conversionSettings.useLAB;
                    btn.setMessage(MapartHelper.config.conversionSettings.useLAB ? Text.of("LAB: вкл") :
                            Text.of("LAB: выкл"));
                    MapartImageConverter.updateMapart();
                }
        ).size(80, 20).tooltip(Tooltip.of(Text.of("Улучшает подбор цветов. Заметно влияет на скорость обработки, поэтому рекомендуется применять §cпосле настройки остальных параметров"))).build();
        settings.add(useLAB, positioner.copy());

        DropdownMenuWidget imagePreprocessing = createImagePreprocessingDropdown();
        imagePreprocessing.addSelectableEntries(this::addSelectableChild);
        settings.add(imagePreprocessing);

        ButtonWidget submit = ButtonWidget.builder(
                Text.of("Применить изменения"),
                (btn) -> MapartImageConverter.updateMapart()
        ).size(130, 20).build();
        settings.add(submit, positioner.copy().alignHorizontalCenter());

        DropdownMenuWidget saveMapart = createSaveMapartDropdown();
        saveMapart.addSelectableEntries(this::addSelectableChild);
        settings.add(saveMapart);

        settings.refreshPositions();
        settings.forEachChild(this::addDrawableChild);
        imagePreprocessing.refreshPositions();
        saveMapart.refreshPositions();


        mapartPreview = new MapartPreviewWidget(220, 20, this.width - 230, this.height - 40);
        this.addDrawableChild(mapartPreview);
    }

    @Override
    protected <T extends Element & Drawable & Selectable> T addDrawableChild(T drawableElement) {
        this.drawables.add(drawableElement);
        return super.addDrawableChild(drawableElement);
    }

    @Override
    protected void clearChildren() {
        super.clearChildren();
        this.drawables.clear();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);

        for (Drawable drawable : this.drawables) {
            if (selectedDropdownMenu != null && selectedDropdownMenu.isExpanded && selectedDropdownMenu.isMouseOverMenu(mouseX, mouseY)) {
                if (drawable == selectedDropdownMenu)
                    drawable.render(context, mouseX, mouseY, delta);
                else
                    drawable.render(context, 0, 0, delta);
            } else
                drawable.render(context, mouseX, mouseY, delta);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (selectedTextWidget != null) {
            selectedTextWidget.setSelectionStart(0);
            selectedTextWidget.setSelectionEnd(0);
            selectedTextWidget = null;
        }
        Optional<Element> optional = this.hoveredElement(mouseX, mouseY);
        if (optional.isEmpty()) {
            this.setFocused(null);
            if (selectedDropdownMenu != null && !selectedDropdownMenu.isMouseOver(mouseX, mouseY)) {
                collapseDropdown();
            }
            return false;
        }
        Element element = optional.get();

        if (element instanceof DropdownMenuWidget dropMenu) {
            if (element != selectedDropdownMenu) {
                if (selectedDropdownMenu == null || !selectedDropdownMenu.isMouseOverMenu(mouseX, mouseY)) {
                    collapseDropdown();
                    selectedDropdownMenu = dropMenu;
                } else if (selectedDropdownMenu.isMouseOverMenu(mouseX, mouseY))
                    return false;
            }
            return dropMenu.mouseClicked(mouseX, mouseY, button);
        }
        if (selectedDropdownMenu != null) {
            if (selectedDropdownMenu.isMouseOverMenu(mouseX, mouseY)) {
                if (selectedDropdownMenu.isChild((ClickableWidget) element)) {
                    this.setFocused(element);
                    this.setDragging(true);
                    return selectedDropdownMenu.mouseClicked(mouseX, mouseY, button);
                } else
                    return false;
            } else {
                collapseDropdown();
            }
        }

        if (!element.isFocused() && element instanceof TextFieldWidget textField) {
            selectedTextWidget = textField;
            textField.setSelectionStart(0);
            textField.setSelectionEnd(textField.getText().length());
            this.setFocused(textField);
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void collapseDropdown() {
        if (this.selectedDropdownMenu != null) {
            this.selectedDropdownMenu.switchExpanded(false);
            this.selectedDropdownMenu = null;
        }
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
        GridWidget.Adder adder = size.createAdder(3);

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
        adder.add(new TextWidget(Text.literal("Размеры мапарта"), textRenderer), 3);
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
        ).size(150, 20).build();

        ButtonWidget saveNBT = ButtonWidget.builder(
                Text.of("Сохранить NBT"),
                (btn) -> MapartToNBT.saveNBT(true)
        ).size(150, 20).build();

        ButtonWidget saveSplitNBT = ButtonWidget.builder(
                Text.of("Сохранить NBT каждой карты"),
                (btn) -> MapartToNBT.saveNBT(false)
        ).size(150, 20).build();

        ButtonWidget saveZipNBT = ButtonWidget.builder(
                Text.of("Сохранить NBT в архиве"),
                (btn) -> MapartToNBT.saveNBTAsZip()
        ).size(150, 20).build();

        DropdownMenuWidget saveMapart = new DropdownMenuWidget(this, 0, 0, 100, 20, 154, Text.of("Сохранить"));
        saveMapart.addEntry(saveImage);
        saveMapart.addEntry(saveNBT);
        saveMapart.addEntry(saveSplitNBT);
        saveMapart.addEntry(saveZipNBT);

        return saveMapart;
    }
}
