package rh.maparthelper.config.palette;

import net.minecraft.MinecraftVersion;
import net.minecraft.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class CompletePalette {
    @Nullable
    private String gameVersion;
    public final Map<Integer, List<Block>> palette = new TreeMap<>();

    @Nullable
    public String getGameVersion() {
        return gameVersion;
    }

    void bumpGameVersion() {
        gameVersion = MinecraftVersion.CURRENT.name();
    }

    static CompletePalette generate() {
        CompletePalette generated = new CompletePalette();
        PaletteGenerator.initColors(generated.palette);
        generated.bumpGameVersion();
        return generated;
    }
}
