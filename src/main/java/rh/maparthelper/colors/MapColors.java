package rh.maparthelper.colors;

import net.minecraft.world.level.material.MapColor;

public enum MapColors {
    CLEAR(MapColor.NONE),
    PALE_GREEN(MapColor.GRASS),
    PALE_YELLOW(MapColor.SAND),
    WHITE_GRAY(MapColor.WOOL),
    BRIGHT_RED(MapColor.FIRE),
    PALE_PURPLE(MapColor.ICE),
    IRON_GRAY(MapColor.METAL),
    DARK_GREEN(MapColor.PLANT),
    WHITE(MapColor.SNOW),
    LIGHT_BLUE_GRAY(MapColor.CLAY),
    DIRT_BROWN(MapColor.DIRT),
    STONE_GRAY(MapColor.STONE),
    WATER_BLUE(MapColor.WATER),
    OAK_TAN(MapColor.WOOD),
    OFF_WHITE(MapColor.QUARTZ),
    ORANGE(MapColor.COLOR_ORANGE),
    MAGENTA(MapColor.COLOR_MAGENTA),
    LIGHT_BLUE(MapColor.COLOR_LIGHT_BLUE),
    YELLOW(MapColor.COLOR_YELLOW),
    LIME(MapColor.COLOR_LIGHT_GREEN),
    PINK(MapColor.COLOR_PINK),
    GRAY(MapColor.COLOR_GRAY),
    LIGHT_GRAY(MapColor.COLOR_LIGHT_GRAY),
    CYAN(MapColor.COLOR_CYAN),
    PURPLE(MapColor.COLOR_PURPLE),
    BLUE(MapColor.COLOR_BLUE),
    BROWN(MapColor.COLOR_BROWN),
    GREEN(MapColor.COLOR_GREEN),
    RED(MapColor.COLOR_RED),
    BLACK(MapColor.COLOR_BLACK),
    GOLD(MapColor.GOLD),
    DIAMOND_BLUE(MapColor.DIAMOND),
    LAPIS_BLUE(MapColor.LAPIS),
    EMERALD_GREEN(MapColor.EMERALD),
    SPRUCE_BROWN(MapColor.PODZOL),
    DARK_RED(MapColor.NETHER),
    TERRACOTTA_WHITE(MapColor.TERRACOTTA_WHITE),
    TERRACOTTA_ORANGE(MapColor.TERRACOTTA_ORANGE),
    TERRACOTTA_MAGENTA(MapColor.TERRACOTTA_MAGENTA),
    TERRACOTTA_LIGHT_BLUE(MapColor.TERRACOTTA_LIGHT_BLUE),
    TERRACOTTA_YELLOW(MapColor.TERRACOTTA_YELLOW),
    TERRACOTTA_LIME(MapColor.TERRACOTTA_LIGHT_GREEN),
    TERRACOTTA_PINK(MapColor.TERRACOTTA_PINK),
    TERRACOTTA_GRAY(MapColor.TERRACOTTA_GRAY),
    TERRACOTTA_LIGHT_GRAY(MapColor.TERRACOTTA_LIGHT_GRAY),
    TERRACOTTA_CYAN(MapColor.TERRACOTTA_CYAN),
    TERRACOTTA_PURPLE(MapColor.TERRACOTTA_PURPLE),
    TERRACOTTA_BLUE(MapColor.TERRACOTTA_BLUE),
    TERRACOTTA_BROWN(MapColor.TERRACOTTA_BROWN),
    TERRACOTTA_GREEN(MapColor.TERRACOTTA_GREEN),
    TERRACOTTA_RED(MapColor.TERRACOTTA_RED),
    TERRACOTTA_BLACK(MapColor.TERRACOTTA_BLACK),
    DULL_RED(MapColor.CRIMSON_NYLIUM),
    DULL_PINK(MapColor.CRIMSON_STEM),
    DARK_CRIMSON(MapColor.CRIMSON_HYPHAE),
    TEAL(MapColor.WARPED_NYLIUM),
    DARK_AQUA(MapColor.WARPED_STEM),
    DARK_DULL_PINK(MapColor.WARPED_HYPHAE),
    BRIGHT_TEAL(MapColor.WARPED_WART_BLOCK),
    DEEPSLATE_GRAY(MapColor.DEEPSLATE),
    RAW_IRON_PINK(MapColor.RAW_IRON),
    LICHEN_GREEN(MapColor.GLOW_LICHEN);

    public final MapColor color;

    MapColors(MapColor color) {
        this.color = color;
    }

    public static MapColors findByMapColor(MapColor color) {
        for (var e : values()) {
            if (e.color == color)
                return e;
        }
        return CLEAR;
    }
}
