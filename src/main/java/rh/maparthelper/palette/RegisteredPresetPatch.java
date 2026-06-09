package rh.maparthelper.palette;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.MapColor;
import rh.maparthelper.util.FileUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RegisteredPresetPatch {
    private final RegisteredPalettePreset origin;

    private PatchTypes changeState;
    private boolean toRemove;
    private boolean colorsChanged;

    private final UUID uuid;
    private final Map<MapColor, Block> colors = new HashMap<>();
    private String filename;
    private String presetName;

    public RegisteredPresetPatch() {
        this.origin = null;
        this.uuid = UUID.randomUUID();
        this.changeState = PatchTypes.CREATED;
        this.presetName = "New empty preset";
        this.filename = FileUtils.makeUniqueFilename(PaletteDataManager.PRESETS_PATH, presetName, "json");
    }

    public RegisteredPresetPatch(Map<MapColor, Block> copyFrom, String filename, String presetName) {
        this.changeState = PatchTypes.CREATED;
        this.origin = null;
        this.uuid = UUID.randomUUID();
        this.colors.putAll(copyFrom);
        this.filename = filename;
        this.presetName = presetName;
    }

    public RegisteredPresetPatch(RegisteredPalettePreset origin) {
        this.changeState = PatchTypes.UNCHANGED;
        this.origin = origin;
        this.uuid = origin.uuid();
        this.colors.putAll(origin.colors());
        this.filename = origin.filename();
        this.presetName = origin.presetName();
    }

    public static RegisteredPresetPatch duplicate(RegisteredPresetPatch origin) {
        var duplicate = new RegisteredPresetPatch(origin.colors, origin.filename, origin.presetName);
        String simpleFilename = origin.filename.substring(0, origin.filename.length() - 5);
        duplicate.filename = FileUtils.makeUniqueFilename(PaletteDataManager.PRESETS_PATH, simpleFilename, "json");
        return duplicate;
    }

    public void setFilename(String fileName) {
        if (toRemove) return;
        this.filename = fileName;
        updateChangedState(false);
    }

    public void setPresetName(String presetName) {
        if (toRemove) return;
        this.presetName = presetName;
        updateChangedState(false);
    }

    public void updateEntry(MapColor mapColor, Block block) {
        if (toRemove) return;
        this.colors.put(mapColor, block);
        updateChangedState(true);
    }

    public void removeColor(MapColor mapColor) {
        if (toRemove) return;
        this.colors.remove(mapColor);
        updateChangedState(true);
    }

    public void toggleToRemove() {
        this.toRemove = !toRemove;
    }

    public String getPresetName() {
        return presetName;
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

    public Block getBlockOfMapColor(MapColor mapColor) {
        return colors.get(mapColor);
    }

    public RegisteredPalettePreset build() {
        return new RegisteredPalettePreset(uuid, filename, presetName, colors);
    }

    private void updateChangedState(boolean colorsChangedEvent) {
        if (toRemove || origin == null || changeState == PatchTypes.CREATED) return;

        if (colorsChangedEvent) {
            this.colorsChanged = !colors.equals(origin.colors());
        }

        if (!filename.equals(origin.filename()) || !presetName.equals(origin.presetName()) || colorsChanged) {
            this.changeState = PatchTypes.CHANGED;
        } else {
            this.changeState = PatchTypes.UNCHANGED;
        }
    }
}
