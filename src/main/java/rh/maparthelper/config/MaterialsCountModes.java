package rh.maparthelper.config;

import net.minecraft.network.chat.Component;

public enum MaterialsCountModes {
    FULL(Component.translatable("maparthelper.gui.countMode.perBlock_tooltip")),
    PER_MAP(Component.translatable("maparthelper.gui.countMode.full_tooltip"));

    private final Component description;

    MaterialsCountModes(Component description) {
        this.description = description;
    }

    public Component getDescription() {
        return description;
    }

    public static MaterialsCountModes nextMode(MaterialsCountModes mode) {
        return values()[(mode.ordinal() + 1) % values().length];
    }
}
