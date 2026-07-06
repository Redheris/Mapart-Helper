package rh.maparthelper.config;

import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.gui.controllers.BooleanController;
import dev.isxander.yacl3.gui.controllers.ColorController;
import dev.isxander.yacl3.gui.controllers.TickBoxController;
import dev.isxander.yacl3.gui.controllers.string.number.IntegerFieldController;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import rh.maparthelper.MapartHelper;
import rh.maparthelper.event.PaletteLoader;

import java.awt.*;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ConfigScreenFactory {
    public static Screen getConfigScreen(Screen parent) {
        Set<Option<Boolean>> paletteGenLimiters = new HashSet<>();
        Set<Option<Boolean>> paletteGenFilters = new HashSet<>();
        OptionEventListener<Boolean> limiterListener = createPaletteGenLimiterListener(paletteGenLimiters, paletteGenFilters);
        AtomicBoolean requestPaletteRegenerate = new AtomicBoolean(false);

        CommonConfiguration.HANDLER.load();
        return YetAnotherConfigLib.create(CommonConfiguration.HANDLER, (defaults, config, builder) -> builder
                .title(Component.translatable("maparthelper.config.title"))
                .category(ConfigCategory.createBuilder()
                        .name(Component.translatable("maparthelper.config.category.common"))
                        .option(Option.<Color>createBuilder()
                                .name(Component.translatable("maparthelper.config.common.mapSelectionColor"))
                                .description(OptionDescription.of(Component.translatable("maparthelper.config.common.description.mapSelectionColor")))
                                .customController(ColorController::new)
                                .binding(
                                        defaults.selectionColor,
                                        () -> config.selectionColor,
                                        value -> config.selectionColor = value
                                )
                                .build())
                        .option(Option.<Color>createBuilder()
                                .name(Component.translatable("maparthelper.config.common.previewHighlightingColor"))
                                .description(OptionDescription.of(Component.translatable("maparthelper.config.common.description.previewHighlightingColor")))
                                .customController(opt -> new ColorController(opt, true))
                                .binding(
                                        defaults.previewHighlightingColor,
                                        () -> config.previewHighlightingColor,
                                        value -> config.previewHighlightingColor = value
                                )
                                .build())
                        .option(Option.<Integer>createBuilder()
                                .name(Component.translatable("maparthelper.config.common.fakeItemFramesLiveTime"))
                                .customController(IntegerFieldController::new)
                                .binding(
                                        defaults.fakeItemFramesLiveTime,
                                        () -> config.fakeItemFramesLiveTime,
                                        value -> config.fakeItemFramesLiveTime = value
                                )
                                .build())
                        .group(OptionGroup.createBuilder()
                                .name(Component.translatable("maparthelper.config.common.group.conversionSettings"))
                                .option(Option.<Boolean>createBuilder()
                                        .name(Component.translatable("maparthelper.config.common.logConversionTime"))
                                        .description(OptionDescription.of(Component.translatable("maparthelper.config.common.description.logConversionTime")))
                                        .customController(opt -> new BooleanController(opt, true))
                                        .binding(
                                                defaults.logConversionTime,
                                                () -> config.logConversionTime,
                                                value -> config.logConversionTime = value
                                        )
                                        .build())
                                .option(Option.<Boolean>createBuilder()
                                        .name(Component.translatable("maparthelper.config.common.previewHighlightOnHover"))
                                        .description(OptionDescription.of(Component.translatable("maparthelper.config.common.description.previewHighlightOnHover")))
                                        .customController(TickBoxController::new)
                                        .binding(
                                                defaults.previewHighlightOnHover,
                                                () -> config.previewHighlightOnHover,
                                                value -> config.previewHighlightOnHover = value
                                        )
                                        .build())
                                .option(Option.<Integer>createBuilder()
                                        .name(Component.translatable("maparthelper.config.common.colorsCacheLiveTimeMs"))
                                        .description(OptionDescription.of(Component.translatable("maparthelper.config.common.description.colorsCacheLiveTimeMs")))
                                        .customController(opt -> new IntegerFieldController(opt, 0, 300000))
                                        .binding(
                                                defaults.colorsCacheLiveTimeMs,
                                                () -> config.colorsCacheLiveTimeMs,
                                                value -> config.colorsCacheLiveTimeMs = value
                                        )
                                        .build())
                                .option(Option.<Boolean>createBuilder()
                                        .name(Component.translatable("maparthelper.config.experimental")
                                                .append(" ")
                                                .append(Component.translatable("maparthelper.config.common.multithreadColorConversion")))
                                        .description(OptionDescription.of(Component.translatable("maparthelper.config.description.experimental")
                                                .append("\n\n")
                                                .append(Component.translatable("maparthelper.config.common.description.multithreadColorConversion"))))
                                        .customController(TickBoxController::new)
                                        .binding(
                                                defaults.multithreadColorConversion,
                                                () -> config.multithreadColorConversion,
                                                value -> config.multithreadColorConversion = value
                                        )
                                        .build())
                                .build())
                        .group(OptionGroup.createBuilder()
                                .name(Component.translatable("maparthelper.config.common.group.schematicSettings"))
                                .option(Option.<Boolean>createBuilder()
                                        .name(Component.translatable("maparthelper.config.common.createDirsForSchematic"))
                                        .description(OptionDescription.of(Component.translatable("maparthelper.config.common.description.createDirsForSchematic")))
                                        .customController(TickBoxController::new)
                                        .binding(
                                                defaults.createDirsForSchematic,
                                                () -> config.createDirsForSchematic,
                                                value -> config.createDirsForSchematic = value
                                        )
                                        .build())
                                .option(Option.<Boolean>createBuilder()
                                        .name(Component.translatable("maparthelper.config.common.addPlatformLayerAuxBlocks"))
                                        .description(OptionDescription.of(Component.translatable("maparthelper.config.common.description.addPlatformLayerAuxBlocks")))
                                        .customController(TickBoxController::new)
                                        .binding(
                                                defaults.addPlatformLayerAuxBlocks,
                                                () -> config.addPlatformLayerAuxBlocks,
                                                value -> config.addPlatformLayerAuxBlocks = value
                                        )
                                        .build())
                                .build())
                        .group(OptionGroup.createBuilder()
                                .name(Component.translatable("maparthelper.config.common.group.elementsDisplaySettings"))
                                .option(Option.<Boolean>createBuilder()
                                        .name(Component.translatable("maparthelper.config.common.showImageImportButton"))
                                        .customController(TickBoxController::new)
                                        .binding(
                                                defaults.showImageImportButton,
                                                () -> config.showImageImportButton,
                                                value -> config.showImageImportButton = value
                                        )
                                        .build())
                                .option(Option.<Boolean>createBuilder()
                                        .name(Component.translatable("maparthelper.config.common.displayUnobtainableMode"))
                                        .description(OptionDescription.of(Component.translatable("maparthelper.config.common.description.displayUnobtainableMode")))
                                        .customController(TickBoxController::new)
                                        .binding(
                                                defaults.displayUnobtainableMode,
                                                () -> config.displayUnobtainableMode,
                                                value -> config.displayUnobtainableMode = value
                                        )
                                        .build())
                                .option(Option.<Boolean>createBuilder()
                                        .name(Component.translatable("maparthelper.config.common.showUseLABTooltip"))
                                        .customController(TickBoxController::new)
                                        .binding(
                                                defaults.showUseLABTooltip,
                                                () -> config.showUseLABTooltip,
                                                value -> config.showUseLABTooltip = value
                                        )
                                        .build())
                                .option(Option.<Boolean>createBuilder()
                                        .name(Component.translatable("maparthelper.config.common.showStaircaseTooltips"))
                                        .customController(TickBoxController::new)
                                        .binding(
                                                defaults.showStaircaseTooltips,
                                                () -> config.showStaircaseTooltips,
                                                value -> config.showStaircaseTooltips = value
                                        )
                                        .build())
                                .build())
                        .build())
                .category(ConfigCategory.createBuilder()
                        .name(Component.translatable("maparthelper.config.category.paletteGenerator"))
                        .option(LabelOption.create(Component.translatable("maparthelper.config.paletteGenerator.regenerationWarningLabel")
                                .withStyle(ChatFormatting.GOLD)))
                        .option(LabelOption.create(Component.translatable("maparthelper.config.paletteGenerator.headLabel")))
                        .option(Option.<Boolean>createBuilder()
                                .name(Component.translatable("maparthelper.config.paletteGenerator.onlyVanillaBlocks"))
                                .description(OptionDescription.of(Component.translatable("maparthelper.config.description.paletteGenerator.onlyVanillaBlocks")))
                                .customController(TickBoxController::new)
                                .binding(paletteGenFilterBinding(
                                        defaults.useInPalette.onlyVanillaBlocks,
                                        () -> config.useInPalette.onlyVanillaBlocks,
                                        value -> config.useInPalette.onlyVanillaBlocks = value,
                                        requestPaletteRegenerate))
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Component.translatable("maparthelper.config.paletteGenerator.anyBlocks"))
                                .description(OptionDescription.of(Component.translatable("maparthelper.config.description.paletteGenerator.anyBlocks")))
                                .customController(TickBoxController::new)
                                .binding(paletteGenFilterBinding(
                                        defaults.useInPalette.anyBlocks,
                                        () -> config.useInPalette.anyBlocks,
                                        value -> config.useInPalette.anyBlocks = value,
                                        requestPaletteRegenerate))
                                .addListener(limiterListener)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Component.translatable("maparthelper.config.paletteGenerator.onlySolid"))
                                .customController(TickBoxController::new)
                                .binding(paletteGenFilterBinding(
                                        defaults.useInPalette.onlySolid,
                                        () -> config.useInPalette.onlySolid,
                                        value -> config.useInPalette.onlySolid = value,
                                        requestPaletteRegenerate))
                                .addListener(limiterListener)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Component.translatable("maparthelper.config.paletteGenerator.onlyCarpets"))
                                .customController(TickBoxController::new)
                                .binding(paletteGenFilterBinding(
                                        defaults.useInPalette.onlyCarpets,
                                        () -> config.useInPalette.onlyCarpets,
                                        value -> config.useInPalette.onlyCarpets = value,
                                        requestPaletteRegenerate))
                                .addListener(limiterListener)
                                .build())
                        .option(LabelOption.create(Component.translatable("maparthelper.config.paletteGenerator.detailedSettingsLabel")))
                        .option(createPaletteGenFilter(
                                Component.translatable("maparthelper.config.paletteGenerator.candles"),
                                defaults.useInPalette.candles,
                                () -> config.useInPalette.candles,
                                value -> config.useInPalette.candles = value,
                                paletteGenLimiters, paletteGenFilters, requestPaletteRegenerate)
                                .build())
                        .option(createPaletteGenFilter(
                                Component.translatable("maparthelper.config.paletteGenerator.entityBlocks"),
                                defaults.useInPalette.entityBlocks,
                                () -> config.useInPalette.entityBlocks,
                                value -> config.useInPalette.entityBlocks = value,
                                paletteGenLimiters, paletteGenFilters, requestPaletteRegenerate)
                                .description(OptionDescription.of(Component.translatable("maparthelper.config.description.paletteGenerator.entityBlocks")))
                                .build())
                        .option(createPaletteGenFilter(
                                Component.translatable("maparthelper.config.paletteGenerator.buildDecorBlocks"),
                                defaults.useInPalette.buildDecorBlocks,
                                () -> config.useInPalette.buildDecorBlocks,
                                value -> config.useInPalette.buildDecorBlocks = value,
                                paletteGenLimiters, paletteGenFilters, requestPaletteRegenerate)
                                .description(OptionDescription.of(Component.translatable("maparthelper.config.description.paletteGenerator.buildDecorBlocks")))
                                .build())
                        .option(createPaletteGenFilter(
                                Component.translatable("maparthelper.config.paletteGenerator.creativeBlocks"),
                                defaults.useInPalette.creativeBlocks,
                                () -> config.useInPalette.creativeBlocks,
                                value -> config.useInPalette.creativeBlocks = value,
                                paletteGenLimiters, paletteGenFilters, requestPaletteRegenerate)
                                .build())
                        .option(createPaletteGenFilter(
                                Component.translatable("maparthelper.config.paletteGenerator.growableBlocks"),
                                defaults.useInPalette.growableBlocks,
                                () -> config.useInPalette.growableBlocks,
                                value -> config.useInPalette.growableBlocks = value,
                                paletteGenLimiters, paletteGenFilters, requestPaletteRegenerate)
                                .description(OptionDescription.of(Component.translatable("maparthelper.config.description.paletteGenerator.growableBlocks")))
                                .build())
                        .option(createPaletteGenFilter(
                                Component.translatable("maparthelper.config.paletteGenerator.grassLikeBlocks"),
                                defaults.useInPalette.grassLikeBlocks,
                                () -> config.useInPalette.grassLikeBlocks,
                                value -> config.useInPalette.grassLikeBlocks = value,
                                paletteGenLimiters, paletteGenFilters, requestPaletteRegenerate)
                                .description(OptionDescription.of(Component.translatable("maparthelper.config.description.paletteGenerator.grassLikeBlocks")))
                                .build())
                        .build())
                .save(() -> {
                    CommonConfiguration.HANDLER.save();
                    if (requestPaletteRegenerate.get()) {
                        MapartHelper.LOGGER.info("Palette generation settings are changed. Requesting a regeneration...");
                        PaletteLoader.requestRegenerate();
                    }
                })
        ).generateScreen(parent);
    }

    private static @NotNull Option.Builder<Boolean> createPaletteGenFilter(Component optionName,
                                                                           Boolean def, Supplier<Boolean> getter, Consumer<Boolean> setter,
                                                                           Set<Option<Boolean>> paletteGenLimiters,
                                                                           Set<Option<Boolean>> paletteGenFilters,
                                                                           AtomicBoolean requestRegen) {
        return Option.<Boolean>createBuilder()
                .name(optionName)
                .customController(TickBoxController::new)
                .binding(paletteGenFilterBinding(def, getter, setter, requestRegen))
                .available(paletteGenLimiters.stream().noneMatch(Option::pendingValue))
                .addListener((option, event) -> {
                    if (event == OptionEventListener.Event.INITIAL) paletteGenFilters.add(option);
                });
    }

    private static @NotNull Binding<Boolean> paletteGenFilterBinding(Boolean def, Supplier<Boolean> getter, Consumer<Boolean> setter, AtomicBoolean requestRegen) {
        return Binding.generic(
                def,
                getter,
                value -> {
                    if (getter.get() == value) return;
                    setter.accept(value);
                    requestRegen.set(true);
                }
        );
    }

    private static @NotNull OptionEventListener<Boolean> createPaletteGenLimiterListener(Set<Option<Boolean>> paletteGenLimiters, Set<Option<Boolean>> paletteGenOptions) {
        AtomicBoolean updatingLimiters = new AtomicBoolean(false);
        return (option, event) -> {
            if (event == OptionEventListener.Event.INITIAL) paletteGenLimiters.add(option);
            else if (event == OptionEventListener.Event.STATE_CHANGE) {
                if (updatingLimiters.get()) return; // Preventing recursion

                updatingLimiters.set(true);

                paletteGenLimiters.forEach(opt -> {
                    if (opt != option) opt.requestSet(false);
                });
                paletteGenOptions.forEach(opt -> opt.setAvailable(!option.pendingValue()));

                updatingLimiters.set(false);
            }
        };
    }
}
