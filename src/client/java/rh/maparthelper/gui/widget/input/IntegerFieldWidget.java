package rh.maparthelper.gui.widget.input;

import net.minecraft.client.gui.Font;
import rh.maparthelper.gui.input.TextFieldPredicates;

import java.util.function.Consumer;

public class IntegerFieldWidget extends AdjEditBox {
    private int intValue;
    private final int min;
    private final int max;
    private Consumer<Integer> integerConsumer;

    public IntegerFieldWidget(Font textRenderer, int width, int height, int initialValue, int min, int max) {
        super(textRenderer, width, height, false);
        this.intValue = initialValue;
        this.min = min;
        this.max = max;
        setFilter(TextFieldPredicates.positiveInt());
        setValue(Integer.toString(initialValue));
    }

    @Override
    protected void onValueChange(String newText) {
        if (newText.isEmpty()) {
            super.onValueChange(newText);
            return;
        }
        int parsed = Integer.parseInt(newText);
        intValue = Math.clamp(parsed, min, max);
        value = Integer.toString(intValue);
        if (newText.length() != value.length()) {
            setCursorPosition(value.length());
            setHighlightPos(value.length());
        }
        super.onValueChange(value);
        if (integerConsumer != null) {
            integerConsumer.accept(intValue);
        }
    }

    public void setIntegerValueConsumer(Consumer<Integer> valueConsumer) {
        this.integerConsumer = valueConsumer;
    }

    //? if <=1.21.8 {
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 1) {
            setValue("");
            return super.mouseClicked(mouseX, mouseY, 0);
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
    //?}

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        intValue = Math.clamp((int) (intValue + scrollY), min, max);
        setValue(Integer.toString(intValue));
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }
}
