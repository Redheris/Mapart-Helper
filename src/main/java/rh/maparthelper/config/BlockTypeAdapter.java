package rh.maparthelper.config;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import net.minecraft.block.Block;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.io.IOException;

public class BlockTypeAdapter extends TypeAdapter<Block> {
    @Override
    public void write(JsonWriter writer, Block block) throws IOException {
        writer.value(Registries.BLOCK.getId(block).toString());
    }

    @Override
    public Block read(JsonReader in) throws IOException {
        return Registries.BLOCK.get(Identifier.of(in.nextString()));
    }
}
