package rh.maparthelper.palette;

import rh.maparthelper.util.FileUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class PalettePresetsHandler {
    private UUID selectedPreset;
    private final Map<String, UUID> registeredFilenames = new LinkedHashMap<>();
    private transient final Map<UUID, RegisteredPalettePreset> presets = new LinkedHashMap<>();

    public Map<UUID, RegisteredPalettePreset> getPresets() {
        return Collections.unmodifiableSequencedMap((LinkedHashMap<UUID, RegisteredPalettePreset>) presets);
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

    private void toDefaultState(CompletePalette palette) {
        RegisteredPalettePreset defaultPreset = new RegisteredPalettePreset(
                UUID.randomUUID(),
                FileUtils.makeUniqueFilename(PaletteDataManager.PRESETS_PATH, "Default preset", "json"),
                "Default preset",
                PaletteGenerator.generateDefaultPreset(palette.palette)
        );
        this.presets.clear();
        this.presets.put(defaultPreset.uuid(), defaultPreset);
        this.selectedPreset = defaultPreset.uuid();
        updatePresetFilenamesMap();
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

    public Map<UUID, RegisteredPresetPatch> createPresetPatches() {
        Map<UUID, RegisteredPresetPatch> patches = new LinkedHashMap<>();
        presets.forEach(((uuid, preset) ->
                patches.put(uuid, new RegisteredPresetPatch(preset))
        ));
        return patches;
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

    /**
     * Updates handler's state by applying and registration preset patches. After applying patches validates
     * the handler's state
     *
     * @param palette       Object of {@link CompletePalette} to be able to create a default preset
     * @param presetPatches Set of patches containing changes to presets: deletions, creations and content changes
     * @return {@link PaletteDataManager.DataPatchRequest} object containing info about changes to apply them in the {@link PaletteDataManager}
     */
    public PaletteDataManager.DataPatchRequest applyPresetPatches(CompletePalette palette, Collection<RegisteredPresetPatch> presetPatches) {
        Set<RegisteredPalettePreset> presetsToCreate = new HashSet<>();
        Set<RegisteredPalettePreset> presetsToUpdate = new HashSet<>();
        Set<String> filesToRemove = new HashSet<>();

        Map<PatchTypes, List<RegisteredPresetPatch>> patchGroups = presetPatches.stream()
                .collect(Collectors.groupingBy(RegisteredPresetPatch::getState));


        Set<String> newFilenames = new HashSet<>();
        Predicate<String> uniqueFilenamePredicate = name ->
                !filesToRemove.contains(name) && Files.exists(PaletteDataManager.PRESETS_PATH.resolve(name))
                        || newFilenames.contains(name);

        if (patchGroups.containsKey(PatchTypes.REMOVED)) {
            patchGroups.get(PatchTypes.REMOVED).forEach(patch -> {
                presets.remove(patch.getUUID());
                filesToRemove.add(patch.getShortFilename() + ".json");
            });
        }

        if (patchGroups.containsKey(PatchTypes.CREATED)) {
            patchGroups.get(PatchTypes.CREATED).forEach(patch -> {
                String uniqueFilename = FileUtils.makeUniqueName(
                        uniqueFilenamePredicate, patch.getShortFilename(), "json", "%s (%d)"
                );
                RegisteredPalettePreset preset = patch.build(uniqueFilename);
                presetsToCreate.add(preset);
                newFilenames.add(preset.filename());
                presets.put(preset.uuid(), preset);
            });
        }

        if (patchGroups.containsKey(PatchTypes.CHANGED)) {
            patchGroups.get(PatchTypes.CHANGED).forEach(patch -> {
                RegisteredPalettePreset oldPreset = presets.get(patch.getUUID());
                RegisteredPalettePreset newPreset;
                if (!oldPreset.filename().equals(patch.getShortFilename() + ".json")) {
                    String uniqueFilename = FileUtils.makeUniqueName(
                            uniqueFilenamePredicate, patch.getShortFilename(), "json", "%s (%d)"
                    );
                    newPreset = patch.build(uniqueFilename);
                    newFilenames.add(newPreset.filename());
                    filesToRemove.add(oldPreset.filename());
                } else {
                    newPreset = patch.build();
                }
                presetsToUpdate.add(newPreset);
                presets.put(newPreset.uuid(), newPreset);
            });
        }

        if (presets.isEmpty()) {
            String uniqueFilename = FileUtils.makeUniqueName(
                    uniqueFilenamePredicate, "Default preset", "json", "%s (%d)"
            );
            RegisteredPalettePreset defaultPreset = new RegisteredPalettePreset(
                    UUID.randomUUID(),
                    uniqueFilename,
                    "Default preset",
                    PaletteGenerator.generateDefaultPreset(palette.palette)
            );
            presetsToCreate.add(defaultPreset);
            presets.put(defaultPreset.uuid(), defaultPreset);
            selectedPreset = defaultPreset.uuid();
        }
        if (!presets.containsKey(selectedPreset)) {
            selectedPreset = presets.keySet().iterator().next();
        }

        updatePresetFilenamesMap();

        return new PaletteDataManager.DataPatchRequest(presetsToCreate, presetsToUpdate, filesToRemove);
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
