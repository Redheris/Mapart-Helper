package rh.maparthelper.palette;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.MapColor;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

public record RegisteredPalettePreset(UUID uuid, String filename, PalettePreset preset) {
    public RegisteredPalettePreset(UUID uuid, String filename, PalettePreset preset) {
        this.uuid = uuid != null ? uuid : UUID.randomUUID();
        this.filename = filename;
        this.preset = preset;
    }

    public RegisteredPalettePreset(UUID uuid, String filename, String presetName, Map<MapColor, Block> colors) {
        this(uuid, filename, new PalettePreset(presetName, colors));
    }

    public Map<MapColor, Block> colors() {
        return preset.colors();
    }

    public String presetName() {
        return preset.presetName();
    }

    public Set<MapColor> getMapColors() {
        return preset.getMapColors();
    }

    public Block getBlockOfMapColor(MapColor mapColor) {
        return preset.getBlockOfMapColor(mapColor);
    }
}
