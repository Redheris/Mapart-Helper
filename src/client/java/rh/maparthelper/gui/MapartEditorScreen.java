package rh.maparthelper.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.DirectionalLayoutWidget;
import net.minecraft.client.gui.widget.Positioner;
import net.minecraft.text.Text;
import rh.maparthelper.conversion.CurrentConversionSettings;
import rh.maparthelper.conversion.MapartImageConverter;
import rh.maparthelper.gui.widget.ImageAdjustmentSliderWidget;
import rh.maparthelper.gui.widget.MapartPreviewWidget;

@Environment(EnvType.CLIENT)
public class MapartEditorScreen extends Screen {
    DirectionalLayoutWidget settings;

    public MapartEditorScreen() {
        super(Text.translatable("maparthelper.mapart_editor_screen"));
    }

    @Override
    protected void init() {
        settings = DirectionalLayoutWidget.vertical();
        settings.setPosition(15, 25);
        Positioner positioner = settings.getMainPositioner().margin(0, 5, 0, 0);

        ButtonWidget showGridButton = ButtonWidget.builder(
                CurrentConversionSettings.doShowGrid ? Text.of("Сетка: вкл") :
                        Text.of("Сетка: выкл"),
                (btn) -> {
                    CurrentConversionSettings.doShowGrid = !CurrentConversionSettings.doShowGrid;
                    btn.setMessage(CurrentConversionSettings.doShowGrid ? Text.of("Сетка: вкл") :
                            Text.of("Сетка: выкл"));
                }
        ).size(80, 20).build();
        settings.add(showGridButton);

        ImageAdjustmentSliderWidget sliderBrightness = new ImageAdjustmentSliderWidget(
                150, 15, 0.f, 2.f, true,
                CurrentConversionSettings.brightness,
                value -> {
                    CurrentConversionSettings.brightness = value.floatValue();
                    MapartImageConverter.readAndUpdateMapartImage(CurrentConversionSettings.imagePath);
                },
                value -> String.format("Яркость: %.2f", value)
        );

        ImageAdjustmentSliderWidget sliderContrast = new ImageAdjustmentSliderWidget(
                150, 15, -255, 255, false,
                CurrentConversionSettings.contrast,
                value -> {
                    CurrentConversionSettings.contrast = value.floatValue();
                    MapartImageConverter.readAndUpdateMapartImage(CurrentConversionSettings.imagePath);
                },
                value -> String.format("Контраст: %.0f", value)
        );

        ImageAdjustmentSliderWidget sliderSaturation = new ImageAdjustmentSliderWidget(
                150, 15, 0.f, 2.f, true,
                CurrentConversionSettings.saturation,
                value -> {
                    CurrentConversionSettings.saturation = value.floatValue();
                    MapartImageConverter.readAndUpdateMapartImage(CurrentConversionSettings.imagePath);
                },
                value -> String.format("Насыщенность: %.2f", value)
        );

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
                Text.of("Применить"),
                (btn) -> MapartImageConverter.readAndUpdateMapartImage(CurrentConversionSettings.imagePath)
        ).size(80, 20).build();
        settings.add(submit, positioner.alignHorizontalCenter());

        settings.refreshPositions();
        settings.forEachChild(this::addDrawableChild);

        MapartPreviewWidget mapartPreview = new MapartPreviewWidget(220, 20, this.width - 230, this.height - 40);
        this.addDrawableChild(mapartPreview);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return super.mouseClicked(mouseX, mouseY, button);
    }
}
