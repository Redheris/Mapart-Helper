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

public class ErrorPropagationWeightsOverlay {
    public static OverlayLayout create(MapartProcessing mapart, int contentWidth) {
        LinearLayout content = LinearLayout.vertical().spacing(-2);
        content.defaultCellSetting().padding(-1, 2, 0, 2);

        ImageAdjustmentSliderWidget sliderRed = createRedSlider(mapart, contentWidth);
        ImageAdjustmentSliderWidget sliderGreen = createGreenSlider(mapart, contentWidth);
        ImageAdjustmentSliderWidget sliderBlue = createBlueSlider(mapart, contentWidth);
        Button reset = Button.builder(
                Component.translatable("maparthelper.gui.reset"),
                (btn) -> {
                    CurrentConversionSettings.redPropagation = 1.0f;
                    CurrentConversionSettings.greenPropagation = 1.0f;
                    CurrentConversionSettings.bluePropagation = 1.0f;
                    sliderRed.setValue(0.5f);
                    sliderGreen.setValue(0.5f);
                    sliderBlue.setValue(0.5f);
                }
        ).size(80, 20).build();

        content.addChild(reset);
        content.addChild(sliderRed);
        content.addChild(sliderGreen);
        content.addChild(sliderBlue);
        content.arrangeElements();

        return OverlayLayoutFactory.defaultOverlay(content, 124, contentWidth + 4);
    }

    private static ImageAdjustmentSliderWidget createRedSlider(MapartProcessing mapart, int width) {
        Component red = Component.translatable("maparthelper.gui.red");
        return new ImageAdjustmentSliderWidget(
                width, 15, 0.f, 2.f, true,
                CurrentConversionSettings.redPropagation,
                value -> {
                    CurrentConversionSettings.redPropagation = value.floatValue();
                    MapartImageUpdater.updateMapart(mapart);
                },
                value -> String.format(red.getString() + ": %.2f", value)
        );
    }

    private static ImageAdjustmentSliderWidget createGreenSlider(MapartProcessing mapart, int width) {
        Component green = Component.translatable("maparthelper.gui.green");
        return new ImageAdjustmentSliderWidget(
                width, 15, 0.f, 2.f, true,
                CurrentConversionSettings.greenPropagation,
                value -> {
                    CurrentConversionSettings.greenPropagation = value.floatValue();
                    MapartImageUpdater.updateMapart(mapart);
                },
                value -> String.format(green.getString() + ": %.2f", value)
        );
    }

    private static ImageAdjustmentSliderWidget createBlueSlider(MapartProcessing mapart, int width) {
        Component blue = Component.translatable("maparthelper.gui.blue");
        return new ImageAdjustmentSliderWidget(
                width, 15, 0.f, 2.f, true,
                CurrentConversionSettings.bluePropagation,
                value -> {
                    CurrentConversionSettings.bluePropagation = value.floatValue();
                    MapartImageUpdater.updateMapart(mapart);
                },
                value -> String.format(blue.getString() + ": %.2f", value)
        );
    }
}
