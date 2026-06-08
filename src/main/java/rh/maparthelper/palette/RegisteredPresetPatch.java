package rh.maparthelper.palette;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.MapColor;
import rh.maparthelper.util.FileUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RegisteredPresetPatch {
    private PatchTypes changeState;
    private boolean toRemove;

    private final UUID uuid;
    private final Map<MapColor, Block> colors = new HashMap<>();
    private String filename;
    private String presetName;

    public RegisteredPresetPatch() {
        this.uuid = UUID.randomUUID();
        this.changeState = PatchTypes.CREATED;
    }

    public RegisteredPresetPatch(UUID uuid, Map<MapColor, Block> copyFrom, String filename, String presetName) {
        this.changeState = PatchTypes.UNCHANGED;
        this.uuid = uuid;
        this.colors.putAll(copyFrom);
        this.filename = filename;
        this.presetName = presetName;
    }

    public RegisteredPresetPatch(RegisteredPalettePreset origin, boolean createNew) {
        this(createNew ? UUID.randomUUID() : origin.uuid(), origin.colors(), origin.filename(), origin.presetName());
        if (createNew) {
            this.filename = FileUtils.makeUniqueFilename(PaletteDataManager.PRESETS_PATH, filename, "json");
        }
    }

    public RegisteredPresetPatch filename(String fileName) {
        if (toRemove) return this;
        if (this.changeState != PatchTypes.CREATED) this.changeState = PatchTypes.CHANGED;
        this.filename = fileName;
        return this;
    }

    public RegisteredPresetPatch presetName(String presetName) {
        if (toRemove) return this;
        if (this.changeState != PatchTypes.CREATED) this.changeState = PatchTypes.CHANGED;
        this.presetName = presetName;
        return this;
    }

    public RegisteredPresetPatch updateEntry(MapColor mapColor, Block block) {
        if (toRemove) return this;
        if (this.changeState != PatchTypes.CREATED) this.changeState = PatchTypes.CHANGED;
        this.colors.put(mapColor, block);
        return this;
    }

    public RegisteredPresetPatch removeColor(MapColor mapColor) {
        if (toRemove) return this;
        if (this.changeState != PatchTypes.CREATED) this.changeState = PatchTypes.CHANGED;
        this.colors.remove(mapColor);
        return this;
    }

    public RegisteredPresetPatch toggleToRemove() {
        this.toRemove = !toRemove;
        return this;
    }

    public String getFilename() {
        return filename;
    }

    public PatchTypes getState() {
        return toRemove ? PatchTypes.REMOVED : changeState;
    }

    public UUID getUUID() {
        return uuid;
    }

    public RegisteredPalettePreset build() {
        return new RegisteredPalettePreset(uuid, filename, presetName, colors);
    }
}
