package rh.maparthelper.gui.widget;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.CommonColors;
import org.jetbrains.annotations.NotNull;

//? if >=1.21.10 {
/*import net.minecraft.client.input.InputWithModifiers;
 *///?} else
import net.minecraft.client.Minecraft;

public class DecorativeButtonWidget extends AbstractButton {
    private final PressAction onPress;
    private final Identifier textureDefault;
    private final Identifier textureHighlighted;
    private int defaultTextureColor = -1;
    private int hoveredTextureColor = -1;

    private DecorativeButtonWidget(int x, int y, int width, int height, Component message, PressAction pressAction) {
        super(x, y, width, height, message);
        this.onPress = pressAction;
        this.textureDefault = null;
        this.textureHighlighted = null;
    }

    private DecorativeButtonWidget(int x, int y, int width, int height, Identifier textureDefault, Identifier textureHighlighted, PressAction pressAction) {
        super(x, y, width, height, Component.empty());
        this.textureDefault = textureDefault;
        this.textureHighlighted = textureHighlighted;
        this.onPress = pressAction;
    }

    public void setTextureColor(int color) {
        this.setDefaultTextureColor(color);
        this.setHoveredTextureColor(color);
    }

    public void setDefaultTextureColor(int color) {
        this.defaultTextureColor = color;
    }

    public void setHoveredTextureColor(int color) {
        this.hoveredTextureColor = color;
    }

    @Override
    public void onPress(/*? if >=1.21.10 {*/ /*@NotNull InputWithModifiers input *//*?}*/) {
        this.onPress.onPress(this);
    }

    //~ render_button_contents
    @Override
    protected void renderWidget(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (textureDefault != null) {
            Identifier texture = isHovered && textureHighlighted != null ? textureHighlighted : textureDefault;
            int textureColor = isHovered ? hoveredTextureColor : defaultTextureColor;
            graphics.blit(
                    RenderPipelines.GUI_TEXTURED,
                    texture,
                    getX(), getY(),
                    0, 0,
                    width, height,
                    width, height,
                    textureColor
            );
        } else {
            //? if >=1.21.11 {
            /*//~ if >=26.1 'renderDefaultLabel(' -> 'extractDefaultLabel('
            this.renderDefaultLabel(graphics.textRenderer(
                    GuiGraphics.HoveredTextEffects.NONE,
                    style -> style.withColor(CommonColors.LIGHT_GRAY))
            );
            *///?} else <=1.21.8
            this.renderString(graphics, Minecraft.getInstance().font, CommonColors.LIGHT_GRAY);
        }
    }
    //~ !render_button_contents

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
            return new DecorativeButtonWidget(x, y, this.width, this.height, this.textureDefault, this.textureHighlighted, this.onPress);
        }
    }

    @Environment(EnvType.CLIENT)
    public interface PressAction {
        void onPress(DecorativeButtonWidget button);
    }
}
