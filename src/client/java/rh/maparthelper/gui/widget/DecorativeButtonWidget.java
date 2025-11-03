package rh.maparthelper.gui.widget;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.math.ColorHelper;

public class DecorativeButtonWidget extends ButtonWidget {
    private DecorativeButtonWidget(int x, int y, int width, int height, Text message, PressAction pressAction) {
        super(x, y, width, height, message, pressAction, DEFAULT_NARRATION_SUPPLIER);
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        MinecraftClient minecraftClient = MinecraftClient.getInstance();
        int i = ColorHelper.withAlpha(this.alpha, this.active ? Colors.WHITE : Colors.LIGHT_GRAY);
        this.drawMessage(context, minecraftClient.textRenderer, i);
    }

    public static DecorativeButtonWidget.Builder builder(Text message, ButtonWidget.PressAction onPress) {
        return new DecorativeButtonWidget.Builder(message, onPress);
    }

    public static class Builder extends ButtonWidget.Builder {
        private final Text message;
        private final PressAction onPress;
        private int width = 150;
        private int height = 20;

        public Builder(Text message, PressAction onPress) {
            super(message, onPress);
            this.message = message;
            this.onPress = onPress;
        }

        public Builder width(int width) {
            this.width = width;
            return this;
        }

        public Builder size(int width, int height) {
            this.width = width;
            this.height = height;
            return this;
        }

        public DecorativeButtonWidget build() {
            return new DecorativeButtonWidget(0, 0, this.width, this.height, this.message, this.onPress);
        }
    }
}
