package rh.maparthelper.gui.widget.input;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.util.CommonColors;

import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
import java.util.function.Predicate;

//? if >=1.21.10 {
/*import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
*///?}
//? if >=26.1 {
/*import java.util.Objects;
import net.minecraft.util.StringUtil;
*///?}

public class AdjEditBox extends EditBox {
    private Predicate<String> valueValidator;
    private Consumer<String> valueConsumer;
    private int baseTextColor = -1;
    private final boolean overrideOnClick;
    //? >=26.1
    //private Predicate<String> filter = Objects::nonNull;

    public AdjEditBox(Font textRenderer, int width, int height, String initialValue, boolean overrideOnClick) {
        super(textRenderer, width, height, Component.empty());
        setValue(initialValue);
        this.overrideOnClick = overrideOnClick;
    }

    public AdjEditBox(Font textRenderer, int width, int height, String initialValue) {
        this(textRenderer, width, height, initialValue, true);
    }

    public AdjEditBox(Font textRenderer, int width, int height, boolean overrideOnClick) {
        super(textRenderer, width, height, Component.empty());
        this.overrideOnClick = overrideOnClick;
    }

    //~ widget_events
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        //? if <=1.21.8 {
        if (!overrideOnClick) return super.mouseClicked(mouseX, mouseY, button);
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
        //?}

        return super.mouseClicked(mouseX, mouseY, button);
    }
    //~ !widget_events

    //? if >=26.1 {
    /*@Override
    public void setValue(@NotNull String value) {
        if (this.filter.test(value))
            super.setValue(value);
    }

    @Override
    public void insertText(@NotNull String input) {
        if (filter.test(StringUtil.filterText(input)))
            super.insertText(input);
    }

    public void setFilter(final Predicate<String> filter) {
        this.filter = filter;
    }
    *///?}

    @Override
    public void setFocused(boolean focused) {
        super.setFocused(focused);
        if (!focused) {
            setCursorPosition(getCursorPosition());
            setHighlightPos(getCursorPosition());
        }
    }

    @Override
    public void setTextColor(int baseTextColor) {
        super.setTextColor(baseTextColor);
        this.baseTextColor = baseTextColor;
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
    public void setResponder(@NotNull Consumer<String> changedListener) {}

    private void updateChangedListener() {
        super.setResponder(value -> {
            setTextColor(baseTextColor);
            if (value.isEmpty()) return;
            if (valueValidator != null && !valueValidator.test(value)) super.setTextColor(CommonColors.SOFT_RED);
            else if (valueConsumer != null) valueConsumer.accept(value);
        });
    }
}
