package rh.maparthelper.gui.widget;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import org.jetbrains.annotations.NotNull;

//? if >=1.21.10 {
/*import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.util.CommonColors;
*///?} else
import net.minecraft.client.Minecraft;

public class DecorativeButtonWidget extends AbstractButton {
    private static final WidgetSprites DEFAULT_SPRITES = new WidgetSprites(
            Identifier.withDefaultNamespace("widget/button"),
            Identifier.withDefaultNamespace("widget/button_disabled"),
            Identifier.withDefaultNamespace("widget/button_highlighted")
    );

    private final PressAction onPress;
    private WidgetSprites customSprites;
    private final boolean renderVanillaBackground;

    protected int textureWidth;
    protected int textureHeight;

    private int defaultTextureColor = -1;
    private int hoveredTextureColor = -1;
    private int textColorActive = -1;

    public DecorativeButtonWidget(boolean vanillaBackground, int textColorActive,
                                  int x, int y, int width, int height,
                                  Component message, PressAction pressAction
    ) {
        super(x, y, width, height, message);
        this.customSprites = null;
        this.onPress = pressAction;
        this.renderVanillaBackground = vanillaBackground;
        this.textColorActive = textColorActive;
        this.textureWidth = 16;
        this.textureHeight = 16;
    }

    public DecorativeButtonWidget(boolean vanillaBackground, WidgetSprites widgetSprites,
                                  int x, int y, int width, int height,
                                  PressAction pressAction
    ) {
        super(x, y, width, height, Component.empty());
        this.customSprites = widgetSprites;
        this.onPress = pressAction;
        this.renderVanillaBackground = vanillaBackground;
        this.textureWidth = width;
        this.textureHeight = height;
    }

    public void setCustomSprites(WidgetSprites customSprites) {
        this.customSprites = customSprites;
    }

    public void setTextColorActive(int color) {
        this.textColorActive = color;
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
        if (renderVanillaBackground) {
            graphics.blitSprite(
                    RenderPipelines.GUI_TEXTURED,
                    DEFAULT_SPRITES.get(this.active, this.isHoveredOrFocused()),
                    this.getX(),
                    this.getY(),
                    this.getWidth(),
                    this.getHeight(),
                    ARGB.white(this.alpha)
            );
        }
        if (customSprites != null) {
            graphics.blit(
                    RenderPipelines.GUI_TEXTURED,
                    customSprites.get(this.active, this.isHoveredOrFocused()),
                    getX() + (width - textureWidth) / 2, getY() + (height - textureHeight) / 2,
                    0, 0,
                    textureWidth, textureHeight,
                    textureWidth, textureHeight,
                    ARGB.color(this.alpha, this.isHovered() ? hoveredTextureColor : defaultTextureColor)
            );
        } else {
            int textColor = ARGB.color(this.alpha, this.active ? textColorActive : -6250336);
            //? if >=1.21.11 {
            /*//~ if >=26.1 'renderDefaultLabel(' -> 'extractDefaultLabel('
            this.renderDefaultLabel(graphics.textRenderer(
                    GuiGraphics.HoveredTextEffects.NONE,
                    style -> style.withColor(CommonColors.LIGHT_GRAY))
            );
            *///?} else <=1.21.8
            this.renderString(graphics, Minecraft.getInstance().font, textColor);
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

    public static DecorativeButtonWidget.Builder builder(WidgetSprites customSprites, PressAction onPress) {
        return new DecorativeButtonWidget.Builder(customSprites, onPress);
    }

    public static DecorativeButtonWidget.Builder builder(Identifier defaultTex, Identifier disabledTeX, PressAction onPress) {
        return new DecorativeButtonWidget.Builder(
                new WidgetSprites(defaultTex, disabledTeX, defaultTex),
                onPress
        );
    }

    public static DecorativeButtonWidget.Builder builderSimpleTexture(Identifier texture, PressAction onPress) {
        return builder(
                new WidgetSprites(texture, texture),
                onPress
        );
    }

    public static DecorativeButtonWidget.Builder builderHighlightable(Identifier defaultTex, Identifier highlightedTex, PressAction onPress) {
        return builder(
                new WidgetSprites(defaultTex, defaultTex, highlightedTex),
                onPress
        );
    }

    public static class Builder {
        private final Component message;
        private final WidgetSprites customSprites;
        private final PressAction onPress;
        private boolean renderVanillaBackground;
        private int width = 150;
        private int height = 20;
        private int textureWidth = 16;
        private int textureHeight = 16;
        private int x = 0;
        private int y = 0;
        private int textColorActive;

        public Builder(Component message, PressAction onPress) {
            this.renderVanillaBackground = true;
            this.message = message;
            this.customSprites = null;
            this.onPress = onPress;
        }

        public Builder(WidgetSprites customSprites, PressAction onPress) {
            this.renderVanillaBackground = false;
            this.message = null;
            this.customSprites = customSprites;
            this.onPress = onPress;
        }

        public Builder textColorActive(int color) {
            this.textColorActive = color;
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

        public Builder textureSize(int width, int height) {
            this.textureWidth = width;
            this.textureHeight = height;
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

        public Builder vanillaButtonBackground(boolean doRender) {
            this.renderVanillaBackground = doRender;
            return this;
        }

        public DecorativeButtonWidget build() {
            if (message != null) {
                return new DecorativeButtonWidget(
                        renderVanillaBackground, textColorActive, x, y, width, height, message, onPress
                );
            }
            DecorativeButtonWidget button = new DecorativeButtonWidget(
                    renderVanillaBackground, customSprites, x, y, width, height, onPress
            );
            button.textureWidth = textureWidth;
            button.textureHeight = textureHeight;
            return button;
        }
    }

    @Environment(EnvType.CLIENT)
    public interface PressAction {
        void onPress(DecorativeButtonWidget button);
    }
}
