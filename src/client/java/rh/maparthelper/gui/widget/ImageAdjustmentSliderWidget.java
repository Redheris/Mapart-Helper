package rh.maparthelper.gui.widget;

import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.client.input.KeyCodes;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

public class ImageAdjustmentSliderWidget extends SliderWidget {
    private final float min;
    private final float max;
    private final boolean isDecimalValue;
    private final float diffByKey;
    private final Consumer<Double> onValueChanged;
    private final Function<Double, String> formatter;
    private static boolean shiftPressed = false;

    private static final ScheduledExecutorService EXECUTOR = Executors.newSingleThreadScheduledExecutor();
    private static ScheduledFuture<?> scheduledTask;

    public ImageAdjustmentSliderWidget(
            int width, int height,
            float min, float max, boolean isDecimalValue,
            double initialValue,
            Consumer<Double> onValueChanged,
            Function<Double, String> formatter
    ) {
        super(0, 0, width, height, Text.empty(), (initialValue - min) / (max - min));
        this.min = min;
        this.max = max;
        this.isDecimalValue = isDecimalValue;
        this.diffByKey = (isDecimalValue ? 0.01f : 1.f) / (max - min);
        this.onValueChanged = onValueChanged;
        this.formatter = formatter;
        updateMessage();
    }

    @Override
    protected void updateMessage() {
        setMessage(Text.literal(formatter.apply(getRealValue())));
    }

    @Override
    protected void applyValue() {
        double value = getRealValue();

        if (scheduledTask != null && !scheduledTask.isDone()) {
            scheduledTask.cancel(true);
        }
        scheduledTask = EXECUTOR.schedule(() -> onValueChanged.accept(value), 50, TimeUnit.MILLISECONDS);
    }

    public double getRealValue() {
        double value = min + (max - min) * this.value;
        if (!isDecimalValue)
            return Math.floor(value);
        return (this.value < 0.5 ? Math.floor(value * 100) : Math.ceil(value * 100)) / 100;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (KeyCodes.isToggle(keyCode)) {
            this.setFocused(!this.isFocused());
            return true;
        } else {
            if (keyCode == GLFW.GLFW_KEY_LEFT_SHIFT)
                shiftPressed = true;
            else if (this.isFocused()) {
                boolean bl = keyCode == 263;
                if (bl || keyCode == 262) {
                    float diff = bl ? -diffByKey : diffByKey;
                    if (shiftPressed) diff *= 10;
                    this.setValue(this.value + diff);
                    return true;
                }
            }
            return false;
        }
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_LEFT_SHIFT) {
            shiftPressed = false;
            return true;
        }
        return false;
    }

    public void setValue(double value) {
        double d = this.value;
        this.value = MathHelper.clamp(value, 0.0F, 1.0F);
        if (d != this.value) {
            this.applyValue();
        }

        this.updateMessage();
    }
}
