package rh.maparthelper.gui.tooltip;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

//? if >=26.1 {
/*import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

import java.util.Optional;
*///?}

public class MultilineTooltip extends Tooltip {
    private final List<Component> lines;
    @Nullable
    private Language splitWithLanguage;

    protected MultilineTooltip(@NotNull List<Component> lines) {
        super(Component.empty(), Component.empty()/*? if >=26.1 {*//*, Optional.empty(), null*//*?}*/);
        this.lines = lines;
    }

    protected MultilineTooltip(@NotNull Component... lines) {
        this(new ArrayList<>(List.of(lines)));
    }

    protected MultilineTooltip(@NotNull Component message, @Nullable Component narration) {
        super(message, narration/*? if >=26.1 {*//*, Optional.empty(), null*//*?}*/);
        this.lines = new ArrayList<>(List.of(message));
    }

    //? if >=26.1 {
    /*protected MultilineTooltip(@NotNull Component message, @Nullable Component narration, Optional<TooltipComponent> component, @Nullable Identifier style) {
        super(message, narration, component, style);
        this.lines = new ArrayList<>(List.of(message));
    }
    *///?}

    public static @NotNull MultilineTooltip createMultiline(@NotNull List<Component> lines) {
        return new MultilineTooltip(lines);
    }

    public static @NotNull MultilineTooltip createMultiline(@NotNull Component... lines) {
        return new MultilineTooltip(lines);
    }

    public static @NotNull MultilineTooltip create(@NotNull Component message, @Nullable Component narration) {
        return new MultilineTooltip(message, narration);
    }

    public static @NotNull MultilineTooltip create(@NotNull Component message) {
        return new MultilineTooltip(message, message);
    }

    @Override
    public @NotNull List<FormattedCharSequence> toCharSequence(@NotNull Minecraft minecraft) {
        Language language = Language.getInstance();
        if (this.cachedTooltip == null || language != this.splitWithLanguage) {
            List<FormattedCharSequence> tooltipContent = new ArrayList<>();
            for (Component line : lines) {
                List<FormattedCharSequence> splitLine = splitTooltip(minecraft, line);
                tooltipContent.addAll(splitLine);
            }
            this.cachedTooltip = tooltipContent;
            this.splitWithLanguage = language;
        }

        return this.cachedTooltip;
    }
}
