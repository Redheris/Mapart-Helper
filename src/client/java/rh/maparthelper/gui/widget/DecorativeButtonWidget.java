package rh.maparthelper.gui.widget;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.CommonColors;
import org.jetbrains.annotations.NotNull;

public class DecorativeButtonWidget extends AbstractButton {
    private final PressAction onPress;
    private final Identifier textureDefault;
    private final Identifier textureHighlighted;

    private DecorativeButtonWidget(int x, int y, int width, int height, Component message, PressAction pressAction) {
        super(x, y, width, height, message);
        this.onPress = pressAction;
        this.textureDefault = null;
        this.textureHighlighted = null;
    }

    private DecorativeButtonWidget(int x, int y, int width, int height, Identifier texture, PressAction pressAction) {
        super(x, y, width, height, Component.empty());
        this.textureDefault = texture;
        this.textureHighlighted = texture;
        this.onPress = pressAction;
    }

    private DecorativeButtonWidget(int x, int y, int width, int height, Identifier textureDefault, Identifier textureHighlighted, PressAction pressAction) {
        super(x, y, width, height, Component.empty());
        this.textureDefault = textureDefault;
        this.textureHighlighted = textureHighlighted;
        this.onPress = pressAction;
    }

    @Override
    public void onPress() {
        this.onPress.onPress(this);
    }

    @Override
    protected void renderWidget(@NotNull GuiGraphics context, int mouseX, int mouseY, float deltaTicks) {
        Minecraft minecraftClient = Minecraft.getInstance();
        int i = ARGB.color(this.alpha, this.active ? CommonColors.WHITE : CommonColors.LIGHT_GRAY);
        this.renderString(context, minecraftClient.font, i);

        if (textureDefault != null) {
            if (isHovered) {
                context.blit(
                        RenderPipelines.GUI_TEXTURED,
                        textureHighlighted,
                        getX(), getY(),
                        0, 0,
                        width, height,
                        width, height
                );
            } else {
                context.blit(
                        RenderPipelines.GUI_TEXTURED,
                        textureDefault,
                        getX(), getY(),
                        0, 0,
                        width, height,
                        width, height
                );
            }
        }
    }

    @Override
    public void updateWidgetNarration(@NotNull NarrationElementOutput builder) {
        this.defaultButtonNarrationText(builder);
    }

    public static DecorativeButtonWidget.Builder builder(Component message, PressAction onPress) {
        return new DecorativeButtonWidget.Builder(message, onPress);
    }

    public static DecorativeButtonWidget.Builder builder(Identifier texture, PressAction onPress) {
        return new DecorativeButtonWidget.Builder(texture, onPress);
    }

    public static class Builder {
        private final Component message;
        private final Identifier textureDefault;
        private Identifier textureHighlighted;
        private final PressAction onPress;
        private int width = 150;
        private int height = 20;
        private int x = 0;
        private int y = 0;

        public Builder(Component message, PressAction onPress) {
            this.message = message;
            this.textureDefault = null;
            this.textureHighlighted = null;
            this.onPress = onPress;
        }

        public Builder(Identifier texture, PressAction onPress) {
            this.message = null;
            this.textureDefault = texture;
            this.onPress = onPress;
        }

        public Builder highlightedTexture(Identifier textureHighlighted) {
            this.textureHighlighted = textureHighlighted;
            return this;
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
            if (textureHighlighted != null)
                return new DecorativeButtonWidget(x, y, this.width, this.height, this.textureDefault, this.textureHighlighted, this.onPress);
            return new DecorativeButtonWidget(x, y, this.width, this.height, this.textureDefault, this.onPress);
        }
    }

    @Environment(EnvType.CLIENT)
    public interface PressAction {
        void onPress(DecorativeButtonWidget button);
    }
}
