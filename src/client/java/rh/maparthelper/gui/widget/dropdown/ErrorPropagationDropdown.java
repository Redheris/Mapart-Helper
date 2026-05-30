package rh.maparthelper.gui.widget.dropdown;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import rh.maparthelper.conversion.CurrentConversionSettings;
import rh.maparthelper.conversion.MapartImageUpdater;
import rh.maparthelper.gui.widget.ImageAdjustmentSliderWidget;
import rh.maparthelper.gui.widget.layout.OverlayLayoutFactory;
import rh.maparthelper.mapart.MapartProcessing;

public class ErrorPropagationDropdown extends ImageAdjustmentsDropdown {
    private final MapartProcessing mapart;

    private ImageAdjustmentSliderWidget sliderRed;
    private ImageAdjustmentSliderWidget sliderGreen;
    private ImageAdjustmentSliderWidget sliderBlue;

    public ErrorPropagationDropdown(@NotNull Screen screen, MapartProcessing mapart, int width, int height,
                                    int overlayWidth, int overlayHeight, Component message) {
        super(screen, width, height, overlayWidth, overlayHeight, message);
        this.mapart = mapart;
    }

    @Override
    protected void initOverlay(int overlayHeight, int overlayWidth) {
        LinearLayout content = LinearLayout.vertical().spacing(-2);
        content.defaultCellSetting().padding(-1, 2, 0, 2);

        sliderRed = createDecimalSliderSetting(
                overlayWidth - 4,
                Component.translatable("maparthelper.gui.red"),
                () -> (double) CurrentConversionSettings.redPropagation,
                value -> {
                    CurrentConversionSettings.redPropagation = value.floatValue();
                    updateStateAndMessage();
                    MapartImageUpdater.updateMapart(mapart);
                }
        );
        sliderGreen = createDecimalSliderSetting(
                overlayWidth - 4,
                Component.translatable("maparthelper.gui.green"),
                () -> (double) CurrentConversionSettings.greenPropagation,
                value -> {
                    CurrentConversionSettings.greenPropagation = value.floatValue();
                    updateStateAndMessage();
                    MapartImageUpdater.updateMapart(mapart);
                }
        );
        sliderBlue = createDecimalSliderSetting(
                overlayWidth - 4,
                Component.translatable("maparthelper.gui.blue"),
                () -> (double) CurrentConversionSettings.bluePropagation,
                value -> {
                    CurrentConversionSettings.bluePropagation = value.floatValue();
                    updateStateAndMessage();
                    MapartImageUpdater.updateMapart(mapart);
                }
        );

        Button reset = Button.builder(
                Component.translatable("maparthelper.gui.reset"),
                (btn) -> {
                    CurrentConversionSettings.redPropagation = 1.0f;
                    CurrentConversionSettings.greenPropagation = 1.0f;
                    CurrentConversionSettings.bluePropagation = 1.0f;
                    sliderRed.setValue(0.5f);
                    sliderGreen.setValue(0.5f);
                    sliderBlue.setValue(0.5f);
                    updateStateAndMessage();
                }
        ).size(80, 20).build();

        content.addChild(reset);
        content.addChild(sliderRed);
        content.addChild(sliderGreen);
        content.addChild(sliderBlue);
        content.arrangeElements();

        setOverlay(OverlayLayoutFactory.defaultOverlay(content, overlayHeight, overlayWidth));
    }

    @Override
    protected void updateDefaultState() {
        isDefaultState = sliderRed.isMiddleValue() && sliderGreen.isMiddleValue() && sliderBlue.isMiddleValue();
    }
}
