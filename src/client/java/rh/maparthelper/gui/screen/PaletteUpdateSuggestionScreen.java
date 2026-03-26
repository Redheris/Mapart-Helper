package rh.maparthelper.gui.screen;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.NotNull;
import rh.maparthelper.config.palette.PaletteConfigManager;
import rh.maparthelper.util.RenderUtils;

public class PaletteUpdateSuggestionScreen extends Screen {
    private final Screen parent;
    private int boxX;
    private int boxY;
    private final int boxWidth = 300;
    private final int boxHeight = 100;

    public PaletteUpdateSuggestionScreen(Screen parent) {
        super(Component.nullToEmpty("Palette update suggestion"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        String paletteGameVersion = PaletteConfigManager.completePalette.getGameVersion();
        MutableComponent unknownLabel = Component.translatable("maparthelper.gui.for_unknown_game_version");
        MutableComponent gameVersionName;
        if (paletteGameVersion == null)
            gameVersionName = unknownLabel;
        else
            gameVersionName = Component.literal(paletteGameVersion);
        Component gameVersion = gameVersionName.withStyle(ChatFormatting.DARK_AQUA);
        Component suggestionText = Component.translatable(
                "maparthelper.gui.ask_to_update_palette",
                gameVersion
        );
        boxX = (width - boxWidth) / 2;
        boxY = (height - boxHeight) / 2;

        MultiLineTextWidget suggestionLabel = new MultiLineTextWidget(suggestionText, font)
                .setCentered(true)
                .setMaxWidth(boxWidth - 16);
        suggestionLabel.setPosition(
                boxX + (boxWidth - suggestionLabel.getWidth()) / 2,
                boxY + (boxHeight - suggestionLabel.getHeight() - 10) / 2
        );
        this.addRenderableWidget(suggestionLabel);

        int optionBtnWidth = 130;
        Component keepOption = Component.translatable("maparthelper.gui.keep_current_palette");
        Button deny = Button.builder(
                        keepOption,
                        btn -> {
                            PaletteConfigManager.bumpPaletteGameVersion();
                            Minecraft.getInstance().setScreen(new MapartEditorScreen());
                        }
                )
                .pos(boxX + 1, boxY + boxHeight - 21)
                .size(optionBtnWidth, 20)
                .build();

        Component regenerateOption = Component.translatable("maparthelper.gui.regenerate_palette");
        Button update = Button.builder(
                        regenerateOption,
                        btn -> {
                            PaletteConfigManager.regenerateCompletePalette();
                            Minecraft.getInstance().setScreen(new MapartEditorScreen());
                        }
                )
                .pos(boxX + boxWidth - optionBtnWidth - 1, boxY + boxHeight - 21)
                .size(optionBtnWidth, 20)
                .build();
        this.addRenderableWidget(deny);
        this.addRenderableWidget(update);
    }

    @Override
    public void renderBackground(@NotNull GuiGraphics context, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(context, mouseX, mouseY, partialTick);

        context.fill(boxX, boxY, boxX + boxWidth, boxY + boxHeight, 0x77000000);
        RenderUtils.renderOutline(context, boxX - 1, boxY - 1, boxWidth + 2, boxHeight + 2, 0x22FFFFFF);
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(parent);
    }
}
