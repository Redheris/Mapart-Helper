package rh.maparthelper.gui.widget.input;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class AdjTextFieldWidget extends TextFieldWidget {
    private Predicate<String> valueValidator;
    private Consumer<String> valueConsumer;

    public AdjTextFieldWidget(TextRenderer textRenderer, int width, int height, String initialValue, String narrationTitle) {
        super(textRenderer, width, height, Text.of(narrationTitle));
        setText(initialValue);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 1) {
            this.setText("");
            return super.mouseClicked(mouseX, mouseY, 0);
        }
        if (button == 0 && !isFocused()) {
            super.mouseClicked(mouseX, mouseY, button);
            setSelectionStart(0);
            setSelectionEnd(getText().length());
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void setFocused(boolean focused) {
        super.setFocused(focused);
        if (!focused) {
            setSelectionStart(getCursor());
            setSelectionEnd(getCursor());
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
    public void setChangedListener(Consumer<String> changedListener) {
    }

    private void updateChangedListener() {
        super.setChangedListener(value -> {
            setEditableColor(Colors.WHITE);
            if (value.isEmpty()) return;
            if (valueValidator != null && !valueValidator.test(value)) setEditableColor(Colors.LIGHT_RED);
            else if (valueConsumer != null) valueConsumer.accept(value);
        });
    }
}
