package rh.maparthelper.conversion.palette.gson;

import com.google.gson.*;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class MapColorEntryAdapter implements JsonSerializer<Map<MapColor, Block>>, JsonDeserializer<Map<MapColor, Block>> {
    @Override
    public JsonElement serialize(Map<MapColor, Block> src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject obj = new JsonObject();
        for (Map.Entry<MapColor, Block> entry : src.entrySet()) {
            String key = MapColors.findByMapColor(entry.getKey()).name();
            obj.addProperty(key, Registries.BLOCK.getId(entry.getValue()).toString());
        }
        return obj;
    }

    @Override
    public Map<MapColor, Block> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        Map<MapColor, Block> map = new HashMap<>();
        JsonObject obj = json.getAsJsonObject();
        for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
            MapColor color = MapColors.valueOf(entry.getKey()).color;
            Block block = Registries.BLOCK.get(Identifier.of(entry.getValue().getAsString()));
            map.put(color, block);
        }
        return map;
    }

    private enum MapColors {
        CLEAR(MapColor.CLEAR),
        PALE_GREEN(MapColor.PALE_GREEN),
        PALE_YELLOW(MapColor.PALE_YELLOW),
        WHITE_GRAY(MapColor.WHITE_GRAY),
        BRIGHT_RED(MapColor.BRIGHT_RED),
        PALE_PURPLE(MapColor.PALE_PURPLE),
        IRON_GRAY(MapColor.IRON_GRAY),
        DARK_GREEN(MapColor.DARK_GREEN),
        WHITE(MapColor.WHITE),
        LIGHT_BLUE_GRAY(MapColor.LIGHT_BLUE_GRAY),
        DIRT_BROWN(MapColor.DIRT_BROWN),
        STONE_GRAY(MapColor.STONE_GRAY),
        WATER_BLUE(MapColor.WATER_BLUE),
        OAK_TAN(MapColor.OAK_TAN),
        OFF_WHITE(MapColor.OFF_WHITE),
        ORANGE(MapColor.ORANGE),
        MAGENTA(MapColor.MAGENTA),
        LIGHT_BLUE(MapColor.LIGHT_BLUE),
        YELLOW(MapColor.YELLOW),
        LIME(MapColor.LIME),
        PINK(MapColor.PINK),
        GRAY(MapColor.GRAY),
        LIGHT_GRAY(MapColor.LIGHT_GRAY),
        CYAN(MapColor.CYAN),
        PURPLE(MapColor.PURPLE),
        BLUE(MapColor.BLUE),
        BROWN(MapColor.BROWN),
        GREEN(MapColor.GREEN),
        RED(MapColor.RED),
        BLACK(MapColor.BLACK),
        GOLD(MapColor.GOLD),
        DIAMOND_BLUE(MapColor.DIAMOND_BLUE),
        LAPIS_BLUE(MapColor.LAPIS_BLUE),
        EMERALD_GREEN(MapColor.EMERALD_GREEN),
        SPRUCE_BROWN(MapColor.SPRUCE_BROWN),
        DARK_RED(MapColor.DARK_RED),
        TERRACOTTA_WHITE(MapColor.TERRACOTTA_WHITE),
        TERRACOTTA_ORANGE(MapColor.TERRACOTTA_ORANGE),
        TERRACOTTA_MAGENTA(MapColor.TERRACOTTA_MAGENTA),
        TERRACOTTA_LIGHT_BLUE(MapColor.TERRACOTTA_LIGHT_BLUE),
        TERRACOTTA_YELLOW(MapColor.TERRACOTTA_YELLOW),
        TERRACOTTA_LIME(MapColor.TERRACOTTA_LIME),
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
        DULL_RED(MapColor.DULL_RED),
        DULL_PINK(MapColor.DULL_PINK),
        DARK_CRIMSON(MapColor.DARK_CRIMSON),
        TEAL(MapColor.TEAL),
        DARK_AQUA(MapColor.DARK_AQUA),
        DARK_DULL_PINK(MapColor.DARK_DULL_PINK),
        BRIGHT_TEAL(MapColor.BRIGHT_TEAL),
        DEEPSLATE_GRAY(MapColor.DEEPSLATE_GRAY),
        RAW_IRON_PINK(MapColor.RAW_IRON_PINK),
        LICHEN_GREEN(MapColor.LICHEN_GREEN);

        final MapColor color;

        MapColors(MapColor color) {
            this.color = color;
        }

        static MapColors findByMapColor(MapColor color) {
            for (var e : values()) {
                if (e.color == color)
                    return e;
            }
            return CLEAR;
        }
    }
}
