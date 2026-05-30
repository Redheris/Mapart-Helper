package rh.maparthelper.gui.widget.dropdown;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import rh.maparthelper.gui.widget.ImageAdjustmentSliderWidget;

import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class ImageAdjustmentsDropdownWidget extends DropdownOverlayWidget {
    protected boolean isDefaultState = true;

    public ImageAdjustmentsDropdownWidget(@NotNull Screen screen, int width, int height,
                                          int overlayWidth, int overlayHeight, Component message) {
        super(screen, null, width, height, message);

        initOverlay(overlayHeight, overlayWidth);
        updateStateAndMessage();
    }

    protected abstract void initOverlay(int overlayHeight, int overlayWidth);

    protected abstract void updateDefaultState();

    protected void updateStateAndMessage() {
        updateDefaultState();
        this.setMessage(getMessage().plainCopy().withColor(isDefaultState ? -1 : 16755200));
    }

    protected ImageAdjustmentSliderWidget createDecimalSliderSetting(
            int width, Component text, Supplier<Double> getter, Consumer<Double> setter
    ) {
        return new ImageAdjustmentSliderWidget(
                width, 15, 0.f, 2.f, true,
                getter.get(),
                setter,
                value -> String.format(text.getString() + ": %.2f", value)
        );
    }

    protected ImageAdjustmentSliderWidget createIntegerSliderSetting(
            int width, Component text, Supplier<Double> getter, Consumer<Double> setter
    ) {
        return new ImageAdjustmentSliderWidget(
                width, 15, -255, 255, false,
                getter.get(),
                setter,
                value -> String.format(text.getString() + ": %.0f", value)
        );
    }
}
