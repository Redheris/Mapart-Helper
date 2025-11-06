package rh.maparthelper.gui.widget;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.PressableWidget;
import net.minecraft.client.input.AbstractInput;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;

public class DecorativeButtonWidget extends PressableWidget {
    private final PressAction onPress;
    private Identifier texture;

    private DecorativeButtonWidget(int x, int y, int width, int height, Text message, PressAction pressAction) {
        super(x, y, width, height, message);
        this.onPress = pressAction;
    }

    private DecorativeButtonWidget(int x, int y, int width, int height, Identifier texture, PressAction pressAction) {
        super(x, y, width, height, Text.empty());
        this.texture = texture;
        this.onPress = pressAction;
    }

    @Override
    public void onPress(AbstractInput input) {
        this.onPress.onPress(this);
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        MinecraftClient minecraftClient = MinecraftClient.getInstance();
        int i = ColorHelper.withAlpha(this.alpha, this.active ? Colors.WHITE : Colors.LIGHT_GRAY);
        this.drawMessage(context, minecraftClient.textRenderer, i);

        if (texture != null) {
            context.drawTexture(
                    RenderPipelines.GUI_TEXTURED,
                    texture,
                    getX(), getY(),
                    0, 0,
                    width, height,
                    width, height
            );
        }
    }

    @Override
    public void appendClickableNarrations(NarrationMessageBuilder builder) {
        this.appendDefaultNarrations(builder);
    }

    public static DecorativeButtonWidget.Builder builder(Text message, PressAction onPress) {
        return new DecorativeButtonWidget.Builder(message, onPress);
    }

    public static DecorativeButtonWidget.Builder builder(Identifier texture, PressAction onPress) {
        return new DecorativeButtonWidget.Builder(texture, onPress);
    }

    public static class Builder {
        private final Text message;
        private final Identifier texture;
        private final PressAction onPress;
        private int width = 150;
        private int height = 20;
        private int x = 0;
        private int y = 0;

        public Builder(Text message, PressAction onPress) {
            this.message = message;
            this.texture = null;
            this.onPress = onPress;
        }

        public Builder(Identifier texture, PressAction onPress) {
            this.message = null;
            this.texture = texture;
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

        public Builder position(int x, int y) {
            this.x = x;
            this.y = y;
            return this;
        }

        public Builder dimensions(int x, int y, int width, int height) {
            return this.position(x, y).size(width, height);
        }

        public DecorativeButtonWidget build() {
            if (message != null)
                return new DecorativeButtonWidget(x, y, this.width, this.height, this.message, this.onPress);
            return new DecorativeButtonWidget(x, y, this.width, this.height, this.texture, this.onPress);
        }
    }

    @Environment(EnvType.CLIENT)
    public interface PressAction {
        void onPress(DecorativeButtonWidget button);
    }
}
