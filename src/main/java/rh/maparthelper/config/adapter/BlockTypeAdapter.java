package rh.maparthelper.config.adapter;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;

import java.io.IOException;

public class BlockTypeAdapter extends TypeAdapter<Block> {
    @Override
    public void write(JsonWriter writer, Block block) throws IOException {
        writer.value(BuiltInRegistries.BLOCK.getKey(block).toString());
    }

    @Override
    public Block read(JsonReader in) throws IOException {
        return BuiltInRegistries.BLOCK.getValue(Identifier.parse(in.nextString()));
    }
}
