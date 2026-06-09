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
    private boolean autoFilename = true;

    private final UUID uuid;
    private final Map<MapColor, Block> colors = new HashMap<>();
    private String shortFilename;
    private String presetName;

    public RegisteredPresetPatch() {
        this.origin = null;
        this.uuid = UUID.randomUUID();
        this.changeState = PatchTypes.CREATED;
        this.presetName = "New empty preset";
        this.shortFilename = FileUtils.makeUniqueFilename(PaletteDataManager.PRESETS_PATH, presetName, "json")
                .replaceAll("\\.json$", "");
    }

    public RegisteredPresetPatch(Map<MapColor, Block> copyFrom, String shortFilename, String presetName) {
        this.changeState = PatchTypes.CREATED;
        this.origin = null;
        this.uuid = UUID.randomUUID();
        this.colors.putAll(copyFrom);
        this.shortFilename = shortFilename;
        this.presetName = presetName;
    }

    public RegisteredPresetPatch(RegisteredPalettePreset origin) {
        this.changeState = PatchTypes.UNCHANGED;
        this.autoFilename = false;
        this.origin = origin;
        this.uuid = origin.uuid();
        this.colors.putAll(origin.colors());
        this.shortFilename = origin.filename().replaceAll("\\.json$", "");
        this.presetName = origin.presetName();
    }

    public static RegisteredPresetPatch duplicate(RegisteredPresetPatch origin) {
        return new RegisteredPresetPatch(origin.colors, origin.shortFilename + " (Copy)", origin.presetName + " (Copy)");
    }

    public void setShortFilename(String fileName) {
        if (toRemove) return;
        this.autoFilename = false;
        this.shortFilename = fileName;
        updateChangedState(false);
    }

    public void setPresetName(String presetName) {
        if (toRemove) return;
        this.presetName = presetName;
        if (autoFilename) {
            this.shortFilename = presetName.replaceAll("[\\\\/:*?\"<>|]", "");
        }
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

    public String getShortFilename() {
        return shortFilename;
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
        return new RegisteredPalettePreset(uuid, shortFilename + ".json", presetName, colors);
    }

    public RegisteredPalettePreset build(String filename) {
        return new RegisteredPalettePreset(uuid, filename, presetName, colors);
    }

    private void updateChangedState(boolean colorsChangedEvent) {
        if (toRemove || origin == null || changeState == PatchTypes.CREATED) return;

        if (colorsChangedEvent) {
            this.colorsChanged = !colors.equals(origin.colors());
        }

        if (!shortFilename.equals(origin.filename()) || !presetName.equals(origin.presetName()) || colorsChanged) {
            this.changeState = PatchTypes.CHANGED;
        } else {
            this.changeState = PatchTypes.UNCHANGED;
        }
    }
}
