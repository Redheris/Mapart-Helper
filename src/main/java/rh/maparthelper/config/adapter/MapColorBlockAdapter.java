package rh.maparthelper.config.adapter;

import com.google.gson.*;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import rh.maparthelper.colors.MapColors;

import java.lang.reflect.Type;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

public class MapColorBlockAdapter implements JsonSerializer<Map<MapColor, Block>>, JsonDeserializer<Map<MapColor, Block>> {
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
        Map<MapColor, Block> map = new TreeMap<>(Comparator.comparingInt(o -> o.id));
        JsonObject obj = json.getAsJsonObject();
        for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
            MapColor color = MapColors.valueOf(entry.getKey()).color;
            Block block = Registries.BLOCK.get(Identifier.of(entry.getValue().getAsString()));
            map.put(color, block);
        }
        return map;
    }
}
