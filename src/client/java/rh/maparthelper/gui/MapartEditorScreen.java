package rh.maparthelper.gui;

import me.shedaniel.autoconfig.AutoConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.*;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import rh.maparthelper.MapartHelper;
import rh.maparthelper.MapartHelperClient;
import rh.maparthelper.config.ConversionConfiguration;
import rh.maparthelper.config.MapartHelperConfig;
import rh.maparthelper.conversion.CurrentConversionSettings;
import rh.maparthelper.conversion.MapartImageConverter;
import rh.maparthelper.conversion.schematic.MapartToNBT;
import rh.maparthelper.conversion.staircases.StaircaseStyles;
import rh.maparthelper.gui.widget.ImageAdjustmentSliderWidget;
import rh.maparthelper.gui.widget.MapartPreviewWidget;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

@Environment(EnvType.CLIENT)
public class MapartEditorScreen extends Screen {
    DirectionalLayoutWidget settings;
    TextFieldWidget selectedTextWidget;
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
                Text.of("Кадрирование: " + getNameOfCroppingMode(CurrentConversionSettings.cropMode)),
                btn -> {
                    CurrentConversionSettings.cropMode = (CurrentConversionSettings.cropMode + 2) % 3 - 1;
                    btn.setMessage(Text.of("Кадрирование: " + getNameOfCroppingMode(CurrentConversionSettings.cropMode)));
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
                    AutoConfig.getConfigHolder(MapartHelperConfig.class).save();
                    MapartImageConverter.updateMapart();
                }
        ).size(80, 20).tooltip(Tooltip.of(Text.of("Повышает точность подбора цветов. Может увеличить время обработки"))).build();
        settings.add(useLAB, positioner.copy());

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
        settings.add(reset);
        settings.add(sliderBrightness);
        settings.add(sliderContrast);
        settings.add(sliderSaturation);

        ButtonWidget submit = ButtonWidget.builder(
                Text.of("Применить изменения"),
                (btn) -> MapartImageConverter.updateMapart()
        ).size(130, 20).build();
        settings.add(submit, positioner.copy().alignHorizontalCenter());

        ButtonWidget saveNBT = ButtonWidget.builder(
                Text.of("Сохранить NBT"),
                (btn) -> MapartToNBT.saveNBT(true)
        ).size(150, 20).build();
        settings.add(saveNBT, positioner.copy().alignHorizontalCenter());

        ButtonWidget saveSplitNBT = ButtonWidget.builder(
                Text.of("Сохранить NBT каждой карты"),
                (btn) -> MapartToNBT.saveNBT(false)
        ).size(150, 20).build();
        settings.add(saveSplitNBT, positioner.copy().alignHorizontalCenter());

        ButtonWidget saveZipNBT = ButtonWidget.builder(
                Text.of("Сохранить NBT в архиве"),
                (btn) -> MapartToNBT.saveNBTAsZip()
        ).size(150, 20).build();
        settings.add(saveZipNBT, positioner.copy().alignHorizontalCenter());

        settings.refreshPositions();
        settings.forEachChild(this::addDrawableChild);


        mapartPreview = new MapartPreviewWidget(220, 20, this.width - 230, this.height - 40);
        this.addDrawableChild(mapartPreview);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
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
            return false;
        }
        Element element = optional.get();
        if (!element.isFocused() && element instanceof TextFieldWidget textField) {
            selectedTextWidget = textField;
            textField.setSelectionStart(0);
            textField.setSelectionEnd(textField.getText().length());
            this.setFocused(textField);
            return true;

        }

        return super.mouseClicked(mouseX, mouseY, button);
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

    private static String getNameOfCroppingMode(int mode) {
        return switch (mode) {
            case MapartImageConverter.NO_CROP -> "выкл";
            case MapartImageConverter.AUTO_CROP -> "auto";
            case MapartImageConverter.USER_CROP -> "manual";
            default -> "?";
        };
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
}
