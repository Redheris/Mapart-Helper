package rh.maparthelper.config.adapter;

import com.google.gson.*;
import net.minecraft.block.MapColor;
import rh.maparthelper.colors.MapColorEntry;

import java.lang.reflect.Type;

public class MapColorEntryAdapter implements JsonSerializer<MapColorEntry>, JsonDeserializer<MapColorEntry> {
    @Override
    public JsonElement serialize(MapColorEntry mapColorEntry, Type type, JsonSerializationContext context) {
        JsonObject obj = new JsonObject();
        obj.addProperty("colorId", mapColorEntry.mapColor().id);
        obj.addProperty("brightnessId", mapColorEntry.brightness().id);
        return obj;
    }

    @Override
    public MapColorEntry deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
        JsonObject obj = json.getAsJsonObject();
        int colorId = obj.get("colorId").getAsInt();
        if (colorId == 0) {
            return MapColorEntry.CLEAR;
        }
        int brightnessId = obj.get("brightnessId").getAsInt();
        return new MapColorEntry(MapColor.get(colorId), MapColor.Brightness.validateAndGet(brightnessId), new int[3]);
    }
}
