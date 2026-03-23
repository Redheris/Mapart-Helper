package rh.maparthelper.gui.widget.input;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.util.CommonColors;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class AdjTextFieldWidget extends EditBox {
    private Predicate<String> valueValidator;
    private Consumer<String> valueConsumer;

    public AdjTextFieldWidget(Font textRenderer, int width, int height, String initialValue, String narrationTitle) {
        super(textRenderer, width, height, Component.nullToEmpty(narrationTitle));
        setValue(initialValue);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 1) {
            this.setValue("");
            return super.mouseClicked(mouseX, mouseY, 0);
        }
        if (button == 0 && !isFocused()) {
            super.mouseClicked(mouseX, mouseY, button);
            setCursorPosition(0);
            setHighlightPos(getValue().length());
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void setFocused(boolean focused) {
        super.setFocused(focused);
        if (!focused) {
            setCursorPosition(getCursorPosition());
            setHighlightPos(getCursorPosition());
        }
    }

    public void setValueValidator(Predicate<String> valueValidator) {
        this.valueValidator = valueValidator;
        updateChangedListener();
    }

    public void setValueConsumer(Consumer<String> valueConsumer) {
        this.valueConsumer = valueConsumer;
        updateChangedListener();
    }

    /**
     * Use {@link #setValueValidator} and {@link #setValueConsumer} instead
     */
    @Deprecated
    @Override
    public void setResponder(Consumer<String> changedListener) {
    }

    private void updateChangedListener() {
        super.setResponder(value -> {
            setTextColor(CommonColors.WHITE);
            if (value.isEmpty()) return;
            if (valueValidator != null && !valueValidator.test(value)) setTextColor(CommonColors.SOFT_RED);
            else if (valueConsumer != null) valueConsumer.accept(value);
        });
    }
}
