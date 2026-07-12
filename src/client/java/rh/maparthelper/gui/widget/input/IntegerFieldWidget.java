package rh.maparthelper.gui.widget.input;

import net.minecraft.client.gui.Font;
import org.jetbrains.annotations.Nullable;
import rh.maparthelper.gui.input.TextFieldPredicates;

//? if >=1.21.10 {
/*import org.jetbrains.annotations.NotNull;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
*///?}

import java.util.function.Consumer;

public class IntegerFieldWidget extends AdjEditBox {
    private int intValue;
    private final int min;
    private final int max;
    @Nullable
    private Consumer<Integer> integerConsumer;

    public IntegerFieldWidget(Font textRenderer, int width, int height, int initialValue, int min, int max) {
        super(textRenderer, width, height, false);
        this.intValue = initialValue;
        this.min = min;
        this.max = max;
        setFilter(TextFieldPredicates.rangeInt(min, max));
        setIntValue(initialValue);
    }

    public void setIntValue(int intValue) {
        intValue = Math.clamp(intValue, min, max);
        this.intValue = intValue;
        this.value = Integer.toString(intValue);

        this.moveCursorToEnd(false);
        this.setHighlightPos(getCursorPosition());
        if (integerConsumer != null) {
            integerConsumer.accept(intValue);
        }
    }

    public int getIntValue() {
        return intValue;
    }

    @Override
    protected void onValueChange(String newText) {
        if (newText.isEmpty()) {
            super.onValueChange(newText);
            setSuggestion(Integer.toString(intValue));
            return;
        }
        setSuggestion(null);
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

    //~ widget_events
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 1) {
            setValue("");
            setSuggestion(Integer.toString(intValue));
            //? if <=1.21.8 {
            return super.mouseClicked(mouseX, mouseY, 0);
            //?} else {
            /*MouseButtonInfo mouseButtonInfo = new MouseButtonInfo(0, mouseEvent.modifiers());
            mouseEvent = new MouseButtonEvent(mouseX, mouseY, mouseButtonInfo);
            return super.mouseClicked(mouseX, mouseY, button);
            *///?}
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
    //~ !widget_events

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        intValue = Math.clamp((int) (intValue + scrollY), min, max);
        setIntValue(intValue);
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }
}
