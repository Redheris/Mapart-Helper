package rh.maparthelper.gui.screen;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.MultilineTextWidget;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import rh.maparthelper.config.palette.PaletteConfigManager;

public class PaletteUpdateSuggestionScreen extends Screen {
    private final Screen parent;
    private int boxX;
    private int boxY;
    private final int boxWidth = 300;
    private final int boxHeight = 100;

    public PaletteUpdateSuggestionScreen(Screen parent) {
        super(Text.of("Palette update suggestion"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        String paletteGameVersion = PaletteConfigManager.completePalette.getGameVersion();
        MutableText unknownLabel = Text.translatable("maparthelper.gui.for_unknown_game_version");
        MutableText gameVersionName;
        if (paletteGameVersion == null)
            gameVersionName = unknownLabel;
        else
            gameVersionName = Text.literal(paletteGameVersion);
        Text gameVersion = gameVersionName.formatted(Formatting.DARK_AQUA);
        Text suggestionText = Text.translatable(
                "maparthelper.gui.ask_to_update_palette",
                gameVersion
        );
        boxX = (width - boxWidth) / 2;
        boxY = (height - boxHeight) / 2;

        MultilineTextWidget suggestionLabel = new MultilineTextWidget(suggestionText, textRenderer)
                .setCentered(true)
                .setMaxWidth(boxWidth - 16);
        suggestionLabel.setPosition(
                boxX + (boxWidth - suggestionLabel.getWidth()) / 2,
                boxY + (boxHeight - suggestionLabel.getHeight() - 10) / 2
        );
        this.addDrawableChild(suggestionLabel);

        int optionBtnWidth = 130;
        Text keepOption = Text.translatable("maparthelper.gui.keep_current_palette");
        ButtonWidget deny = ButtonWidget.builder(
                        keepOption,
                        btn -> {
                            PaletteConfigManager.bumpPaletteGameVersion();
                            MinecraftClient.getInstance().setScreen(new MapartEditorScreen());
                        }
                )
                .position(boxX + 1, boxY + boxHeight - 21)
                .size(optionBtnWidth, 20)
                .build();

        Text regenerateOption = Text.translatable("maparthelper.gui.regenerate_palette");
        ButtonWidget update = ButtonWidget.builder(
                        regenerateOption,
                        btn -> {
                            PaletteConfigManager.regenerateCompletePalette();
                            MinecraftClient.getInstance().setScreen(new MapartEditorScreen());
                        }
                )
                .position(boxX + boxWidth - optionBtnWidth - 1, boxY + boxHeight - 21)
                .size(optionBtnWidth, 20)
                .build();
        this.addDrawableChild(deny);
        this.addDrawableChild(update);
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        super.renderBackground(context, mouseX, mouseY, deltaTicks);

        context.fill(boxX, boxY, boxX + boxWidth, boxY + boxHeight, 0x77000000);
        context.drawBorder(boxX - 1, boxY - 1, boxWidth + 2, boxHeight + 2, 0x22FFFFFF);
    }

    @Override
    public void close() {
        MinecraftClient.getInstance().setScreen(parent);
    }
}
