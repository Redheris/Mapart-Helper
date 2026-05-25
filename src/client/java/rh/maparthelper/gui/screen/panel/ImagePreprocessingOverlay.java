package rh.maparthelper.gui.screen.panel;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.network.chat.Component;
import rh.maparthelper.conversion.CurrentConversionSettings;
import rh.maparthelper.conversion.MapartImageUpdater;
import rh.maparthelper.gui.widget.ImageAdjustmentSliderWidget;
import rh.maparthelper.gui.widget.layout.OverlayLayout;
import rh.maparthelper.gui.widget.layout.OverlayLayoutFactory;
import rh.maparthelper.mapart.MapartProcessing;

public class ImagePreprocessingOverlay {
    public static OverlayLayout create(MapartProcessing mapart, int contentWidth) {
        LinearLayout content = LinearLayout.vertical().spacing(-2);
        content.defaultCellSetting().padding(-1, 2, 0, 2);

        ImageAdjustmentSliderWidget sliderBrightness = createBrightnessSlider(mapart, contentWidth);
        ImageAdjustmentSliderWidget sliderContrast = createContrastSlider(mapart, contentWidth);
        ImageAdjustmentSliderWidget sliderSaturation = createSaturationSlider(mapart, contentWidth);
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

        content.addChild(reset);
        content.addChild(sliderBrightness);
        content.addChild(sliderContrast);
        content.addChild(sliderSaturation);
        content.arrangeElements();

        return OverlayLayoutFactory.defaultOverlay(content, 124, contentWidth + 4);
    }

    private static ImageAdjustmentSliderWidget createBrightnessSlider(MapartProcessing mapart, int width) {
        Component brightness = Component.translatable("maparthelper.gui.brightness");
        return new ImageAdjustmentSliderWidget(
                width, 15, 0.f, 2.f, true,
                CurrentConversionSettings.brightness,
                value -> {
                    CurrentConversionSettings.brightness = value.floatValue();
                    MapartImageUpdater.updateMapart(mapart);
                },
                value -> String.format(brightness.getString() + ": %.2f", value)
        );
    }

    private static ImageAdjustmentSliderWidget createContrastSlider(MapartProcessing mapart, int width) {
        Component contrast = Component.translatable("maparthelper.gui.contrast");
        return new ImageAdjustmentSliderWidget(
                width, 15, -255, 255, false,
                CurrentConversionSettings.contrast,
                value -> {
                    CurrentConversionSettings.contrast = value.floatValue();
                    MapartImageUpdater.updateMapart(mapart);
                },
                value -> String.format(contrast.getString() + ": %.0f", value)
        );
    }

    private static ImageAdjustmentSliderWidget createSaturationSlider(MapartProcessing mapart, int width) {
        Component saturation = Component.translatable("maparthelper.gui.saturation");
        return new ImageAdjustmentSliderWidget(
                width, 15, 0.f, 2.f, true,
                CurrentConversionSettings.saturation,
                value -> {
                    CurrentConversionSettings.saturation = value.floatValue();
                    MapartImageUpdater.updateMapart(mapart);
                },
                value -> String.format(saturation.getString() + ": %.2f", value)
        );
    }
}
