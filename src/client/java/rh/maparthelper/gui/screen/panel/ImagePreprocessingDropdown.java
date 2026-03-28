package rh.maparthelper.gui.screen.panel;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.AbstractLayout;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import rh.maparthelper.conversion.CurrentConversionSettings;
import rh.maparthelper.conversion.MapartImageUpdater;
import rh.maparthelper.gui.widget.DropdownMenuWidget;
import rh.maparthelper.gui.widget.ImageAdjustmentSliderWidget;
import rh.maparthelper.mapart.MapartProcessing;

import java.util.function.Consumer;

public class ImagePreprocessingDropdown extends AbstractLayout {
    private final MapartProcessing mapart;
    private final DropdownMenuWidget imagePreprocessing;
    private final int contentWidth;

    public ImagePreprocessingDropdown(Screen screen, MapartProcessing mapart, int btnWidth, int contentWidth) {
        super(0, 0, btnWidth, 20);
        this.mapart = mapart;
        this.contentWidth = contentWidth;
        this.imagePreprocessing = new DropdownMenuWidget(
                screen, 0, 0, 100, 20, contentWidth + 4, -1,
                Component.translatable("maparthelper.gui.image_preprocessing")
        );
        initDropdown();
    }

    @Override
    public void visitChildren(Consumer<LayoutElement> consumer) {
        consumer.accept(imagePreprocessing);
    }

    private void initDropdown() {
        ImageAdjustmentSliderWidget sliderBrightness = createBrightnessSlider();
        ImageAdjustmentSliderWidget sliderContrast = createContrastSlider();
        ImageAdjustmentSliderWidget sliderSaturation = createSaturationSlider();
        Button reset = Button.builder(
                Component.translatable("maparthelper.gui.reset"),
                (btn) -> {
                    CurrentConversionSettings.brightness = 1.0f;
                    CurrentConversionSettings.contrast = 0.0f;
                    CurrentConversionSettings.saturation = 1.0f;
                    sliderBrightness.setValue(0.5f);
                    sliderContrast.setValue(0.5f);
                    sliderSaturation.setValue(0.5f);
                }
        ).size(80, 20).build();
        imagePreprocessing.addEntry(reset);
        imagePreprocessing.addEntry(sliderBrightness);
        imagePreprocessing.addEntry(sliderContrast);
        imagePreprocessing.addEntry(sliderSaturation);
    }

    private ImageAdjustmentSliderWidget createBrightnessSlider() {
        Component brightness = Component.translatable("maparthelper.gui.brightness");
        return new ImageAdjustmentSliderWidget(
                contentWidth, 15, 0.f, 2.f, true,
                CurrentConversionSettings.brightness,
                value -> {
                    CurrentConversionSettings.brightness = value.floatValue();
                    MapartImageUpdater.updateMapart(mapart);
                },
                value -> String.format(brightness.getString() + ": %.2f", value)
        );
    }

    private ImageAdjustmentSliderWidget createContrastSlider() {
        Component contrast = Component.translatable("maparthelper.gui.contrast");
        return new ImageAdjustmentSliderWidget(
                contentWidth, 15, -255, 255, false,
                CurrentConversionSettings.contrast,
                value -> {
                    CurrentConversionSettings.contrast = value.floatValue();
                    MapartImageUpdater.updateMapart(mapart);
                },
                value -> String.format(contrast.getString() + ": %.0f", value)
        );
    }

    private ImageAdjustmentSliderWidget createSaturationSlider() {
        Component saturation = Component.translatable("maparthelper.gui.saturation");
        return new ImageAdjustmentSliderWidget(
                contentWidth, 15, 0.f, 2.f, true,
                CurrentConversionSettings.saturation,
                value -> {
                    CurrentConversionSettings.saturation = value.floatValue();
                    MapartImageUpdater.updateMapart(mapart);
                },
                value -> String.format(saturation.getString() + ": %.2f", value)
        );
    }
}
