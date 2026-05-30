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

public class ImagePreprocessingDropdownWidget extends ImageAdjustmentsDropdownWidget {
    private final MapartProcessing mapart;

    private ImageAdjustmentSliderWidget sliderBrightness;
    private ImageAdjustmentSliderWidget sliderContrast;
    private ImageAdjustmentSliderWidget sliderSaturation;

    public ImagePreprocessingDropdownWidget(@NotNull Screen screen, MapartProcessing mapart, int width, int height,
                                            int overlayWidth, int overlayHeight, Component message) {
        super(screen, width, height, overlayWidth, overlayHeight, message);
        this.mapart = mapart;
    }

    @Override
    protected void initOverlay(int overlayHeight, int overlayWidth) {
        LinearLayout content = LinearLayout.vertical().spacing(-2);
        content.defaultCellSetting().padding(-1, 2, 0, 2);

        sliderBrightness = createDecimalSliderSetting(
                overlayWidth - 4,
                Component.translatable("maparthelper.gui.brightness"),
                () -> (double) CurrentConversionSettings.brightness,
                value -> {
                    CurrentConversionSettings.brightness = value.floatValue();
                    updateStateAndMessage();
                    MapartImageUpdater.updateMapart(mapart);
                }
        );
        sliderContrast = createIntegerSliderSetting(
                overlayWidth - 4,
                Component.translatable("maparthelper.gui.contrast"),
                () -> (double) CurrentConversionSettings.contrast,
                value -> {
                    CurrentConversionSettings.contrast = value.floatValue();
                    updateStateAndMessage();
                    MapartImageUpdater.updateMapart(mapart);
                }
        );
        sliderSaturation = createDecimalSliderSetting(
                overlayWidth - 4,
                Component.translatable("maparthelper.gui.saturation"),
                () -> (double) CurrentConversionSettings.saturation,
                value -> {
                    CurrentConversionSettings.saturation = value.floatValue();
                    updateStateAndMessage();
                    MapartImageUpdater.updateMapart(mapart);
                }
        );

        Button reset = Button.builder(
                Component.translatable("maparthelper.gui.reset"),
                (btn) -> {
                    CurrentConversionSettings.brightness = 1.0f;
                    CurrentConversionSettings.contrast = 0.0f;
                    CurrentConversionSettings.saturation = 1.0f;
                    sliderBrightness.setValue(0.5f);
                    sliderContrast.setValue(0.5f);
                    sliderSaturation.setValue(0.5f);
                    updateStateAndMessage();
                }
        ).size(80, 20).build();

        content.addChild(reset);
        content.addChild(sliderBrightness);
        content.addChild(sliderContrast);
        content.addChild(sliderSaturation);
        content.arrangeElements();

        setOverlay(OverlayLayoutFactory.defaultOverlay(content, overlayHeight, overlayWidth));
    }

    @Override
    protected void updateDefaultState() {
        isDefaultState = sliderBrightness.isMiddleValue() && sliderContrast.isMiddleValue() && sliderSaturation.isMiddleValue();
    }
}
