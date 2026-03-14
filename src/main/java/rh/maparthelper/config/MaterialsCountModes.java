package rh.maparthelper.config;

import net.minecraft.text.Text;

public enum MaterialsCountModes {
    FULL(Text.translatable("maparthelper.gui.countMode.perBlock_tooltip")),
    PER_MAP(Text.translatable("maparthelper.gui.countMode.full_tooltip"));

    private final Text description;

    MaterialsCountModes(Text description) {
        this.description = description;
    }

    public Text getDescription() {
        return description;
    }

    public static MaterialsCountModes nextMode(MaterialsCountModes mode) {
        return values()[(mode.ordinal() + 1) % values().length];
    }
}
