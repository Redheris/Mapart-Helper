package rh.maparthelper.palette;

import rh.maparthelper.util.FileUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class PalettePresetsHandler {
    private UUID selectedPreset;
    private final Map<String, UUID> registeredFilenames = new LinkedHashMap<>();
    private transient final Map<UUID, RegisteredPalettePreset> presets = new LinkedHashMap<>();

    public Map<String, UUID> getRegisteredFilenames() {
        return Map.copyOf(registeredFilenames);
    }

    public Map<UUID, RegisteredPalettePreset> getPresets() {
        return Map.copyOf(presets); // TODO: check it
    }

    public boolean unregisterPresetsByFilenames(Set<String> filenames) {
        boolean anyRemoved = registeredFilenames.keySet().removeIf(filenames::contains);
        if (anyRemoved) {
            presets.values().removeIf(preset -> !registeredFilenames.containsKey(preset.filename()));
        }
        return anyRemoved;
    }

    public boolean setPresets(Map<String, PalettePreset> newPresets) {
        presets.clear();
        // Preregistration of presets with only metadata
        registeredFilenames.forEach((filename, uuid) -> presets.put(uuid, null));

        boolean configFileChanged = false;

        for (var entry : newPresets.entrySet()) {
            String filename = entry.getKey();
            PalettePreset preset = entry.getValue();
            UUID registry = registeredFilenames.get(filename);
            if (registry == null) {
                registry = UUID.randomUUID();
                configFileChanged = true;
            }
            presets.put(registry, new RegisteredPalettePreset(registry, filename, preset));
        }
        presets.values().removeIf(Objects::isNull);

        updatePresetFilenamesMap();

        return configFileChanged;
    }

    private void updatePresetFilenamesMap() {
        registeredFilenames.clear();
        presets.forEach((uuid, preset) ->
                registeredFilenames.put(preset.filename(), uuid)
        );
    }

    private static String makeUniqueFilename(String filename) {
        return FileUtils.makeUniqueFilename(PaletteDataManager.PRESETS_PATH, filename, "json");
    }

    private PalettePresetsHandler toDefaultState(CompletePalette palette) {
        RegisteredPalettePreset defaultPreset = new RegisteredPalettePreset(
                UUID.randomUUID(),
                makeUniqueFilename("New preset"),
                "New preset",
                PaletteGenerator.generateDefaultPreset(palette.palette)
        );
        this.presets.clear();
        this.presets.put(defaultPreset.uuid(), defaultPreset);
        this.selectedPreset = defaultPreset.uuid();
        updatePresetFilenamesMap();
        return this;
    }

    public static PalettePresetsHandler createDefault(CompletePalette palette) {
        return new PalettePresetsHandler().toDefaultState(palette);
    }

    public boolean shouldConvertWithSelectedPreset() {
        var colors = presets.get(selectedPreset).colors();
        return !colors.isEmpty() && !PaletteColors.excludingColors.containsAll(colors.keySet());
    }

    public RegisteredPalettePreset getSelectedPreset() {
        return presets.get(selectedPreset);
    }

    public void setSelectedPreset(UUID presetUUID) {
        if (presets.containsKey(presetUUID)) selectedPreset = presetUUID;
    }

    public Set<String> getPresetNames() {
        // TODO: do I need this?
        return presets.values().stream().map(RegisteredPalettePreset::presetName).collect(Collectors.toSet());
    }

    /**
     * Updates handler's state by applying and registration preset patches
     *
     * @param palette       Object of {@link CompletePalette} to be able to create a default preset
     * @param presetPatches Set of patches containing changes to presets: deletions, creations and content changes
     * @return Record object containing info about deletions and filename changes to apply them in the {@link PaletteDataManager}
     */
    public PaletteDataManager.DataPatchRequest patchPresets(CompletePalette palette, Set<RegisteredPresetPatch> presetPatches) {
        Map<RegisteredPalettePreset, PatchTypes> presetsToUpdate = new HashMap<>();
        Set<String> filesToRemove = new HashSet<>();
        Map<String, String> fileRenames = new HashMap<>();

        for (RegisteredPresetPatch patch : presetPatches) {
            switch (patch.getState()) {
                case CREATED, CHANGED -> {
                    RegisteredPalettePreset oldPreset = presets.get(patch.getUUID());
                    boolean filenameChanged = !oldPreset.filename().equals(patch.getFilename());

                    patch.filename(makeUniqueFilename(patch.getFilename()));
                    if (filenameChanged) {
                        fileRenames.put(oldPreset.filename(), patch.getFilename());
                    }

                    RegisteredPalettePreset preset = patch.build();
                    presets.put(preset.uuid(), preset);
                    presetsToUpdate.put(preset, patch.getState());
                }
                case REMOVED -> {
                    presets.remove(patch.getUUID());
                    filesToRemove.add(patch.getFilename());
                }
                case UNCHANGED -> {}
            }
        }
        if (validateConfigState(palette) == -1) {
            presetsToUpdate.put(getSelectedPreset(), PatchTypes.CREATED);
        }
        updatePresetFilenamesMap();

        return new PaletteDataManager.DataPatchRequest(presetsToUpdate, filesToRemove, fileRenames);
    }

    /**
     * Validates and fixes an incorrect state of the handler
     *
     * @return {@code 0} - no changes; {@code -1} - presets map was empty; {@code -2} - unregistered preset was selected
     */
    public int validateConfigState(CompletePalette palette) {
        if (presets.isEmpty()) {
            this.toDefaultState(palette);
            return -1;
        } else if (!presets.containsKey(selectedPreset)) {
            selectedPreset = presets.keySet().iterator().next();
            return -2;
        }
        return 0;
    }

    public boolean removeNonexistent() {
        boolean configFileChanged = false;
        var it = registeredFilenames.keySet().iterator();
        while (it.hasNext()) {
            Path presetPath = PaletteDataManager.PRESETS_PATH.resolve(it.next());
            if (Files.notExists(presetPath)) {
                it.remove();
                configFileChanged = true;
            }
        }
        return configFileChanged;
    }
}
