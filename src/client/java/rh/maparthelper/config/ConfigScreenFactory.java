package rh.maparthelper.config;

import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.gui.controllers.BooleanController;
import dev.isxander.yacl3.gui.controllers.ColorController;
import dev.isxander.yacl3.gui.controllers.TickBoxController;
import dev.isxander.yacl3.gui.controllers.string.number.IntegerFieldController;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
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
        Set<Option<Boolean>> paletteGenOptions = new HashSet<>();
        OptionEventListener<Boolean> limiterListener = createPaletteGenLimiterListener(paletteGenLimiters, paletteGenOptions);
        AtomicBoolean requestPaletteRegenerate = new AtomicBoolean(false);

        CommonConfiguration.HANDLER.load();
        return YetAnotherConfigLib.create(CommonConfiguration.HANDLER, (defaults, config, builder) -> builder
                .title(Component.literal("Mapart Helper Config"))
                .category(ConfigCategory.createBuilder()
                        .name(Component.literal("Common"))
                        .option(Option.<Color>createBuilder()
                                .name(Component.literal("Map selection color"))
                                .customController(ColorController::new)
                                .binding(
                                        defaults.selectionColor,
                                        () -> config.selectionColor,
                                        value -> config.selectionColor = value
                                )
                                .build())
                        .option(Option.<Color>createBuilder()
                                .name(Component.literal("Highlight color on mapart preview"))
                                .customController(opt -> new ColorController(opt, true))
                                .binding(
                                        defaults.previewHighlightingColor,
                                        () -> config.previewHighlightingColor,
                                        value -> config.previewHighlightingColor = value
                                )
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Component.literal("Create dirs for schematic files"))
                                .customController(TickBoxController::new)
                                .binding(
                                        defaults.createDirsForSchematic,
                                        () -> config.createDirsForSchematic,
                                        value -> config.createDirsForSchematic = value
                                )
                                .build())

                        .option(Option.<Integer>createBuilder()
                                .name(Component.literal("Fake item frames display duration (in ticks)"))
                                .customController(IntegerFieldController::new)
                                .binding(
                                        defaults.fakeItemFramesLiveTime,
                                        () -> config.fakeItemFramesLiveTime,
                                        value -> config.fakeItemFramesLiveTime = value
                                )
                                .build())
                        .group(OptionGroup.createBuilder()
                                .name(Component.literal("Conversion settings"))
                                .option(Option.<Boolean>createBuilder()
                                        .name(Component.literal("Log image conversion duration"))
                                        .customController(opt -> new BooleanController(opt, true))
                                        .binding(
                                                defaults.logConversionTime,
                                                () -> config.logConversionTime,
                                                value -> config.logConversionTime = value
                                        )
                                        .build())
                                .option(Option.<Boolean>createBuilder()
                                        .name(Component.literal("Highlight color on preview when the material is hovered"))
                                        .customController(TickBoxController::new)
                                        .binding(
                                                defaults.previewHighlightOnHover,
                                                () -> config.previewHighlightOnHover,
                                                value -> config.previewHighlightOnHover = value
                                        )
                                        .build())
                                .option(Option.<Integer>createBuilder()
                                        .name(Component.literal("Cached closest colors cache lifetime (in ms)"))
                                        .customController(opt -> new IntegerFieldController(opt, 0, 300000))
                                        .binding(
                                                defaults.colorsCacheLiveTimeMs,
                                                () -> config.colorsCacheLiveTimeMs,
                                                value -> config.colorsCacheLiveTimeMs = value
                                        )
                                        .build())
                                .build())
                        .group(OptionGroup.createBuilder()
                                .name(Component.literal("Elements displaying toggles"))
                                .option(Option.<Boolean>createBuilder()
                                        .name(Component.literal("Show image import button"))
                                        .customController(TickBoxController::new)
                                        .binding(
                                                defaults.showImageImportButton,
                                                () -> config.showImageImportButton,
                                                value -> config.showImageImportButton = value
                                        )
                                        .build())
                                .option(Option.<Boolean>createBuilder()
                                        .name(Component.literal("Display unobtainable mode (staircase style)"))
                                        .customController(TickBoxController::new)
                                        .binding(
                                                defaults.displayUnobtainableMode,
                                                () -> config.displayUnobtainableMode,
                                                value -> config.displayUnobtainableMode = value
                                        )
                                        .build())
                                .option(Option.<Boolean>createBuilder()
                                        .name(Component.literal("Show tooltip for LAB toggle"))
                                        .customController(TickBoxController::new)
                                        .binding(
                                                defaults.showUseLABTooltip,
                                                () -> config.showUseLABTooltip,
                                                value -> config.showUseLABTooltip = value
                                        )
                                        .build())
                                .option(Option.<Boolean>createBuilder()
                                        .name(Component.literal("Show tooltips for staircase styles"))
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
                        .name(Component.literal("Palette Generator"))
                        .option(LabelOption.create(Component.literal("This is category")))
                        .option(Option.<Boolean>createBuilder()
                                .name(Component.literal("§c§nALL§r registered blocks"))
                                .customController(TickBoxController::new)
                                .binding(paletteGenOptionBinding(
                                        defaults.anyBlocks,
                                        () -> config.anyBlocks,
                                        value -> config.anyBlocks = value,
                                        requestPaletteRegenerate))
                                .addListener(limiterListener)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Component.literal("§nOnly§r solid blocks"))
                                .customController(TickBoxController::new)
                                .binding(paletteGenOptionBinding(
                                        defaults.onlySolid,
                                        () -> config.onlySolid,
                                        value -> config.onlySolid = value,
                                        requestPaletteRegenerate))
                                .addListener(limiterListener)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Component.literal("§nOnly§r carpet blocks"))
                                .customController(TickBoxController::new)
                                .binding(paletteGenOptionBinding(
                                        defaults.onlyCarpets,
                                        () -> config.onlyCarpets,
                                        value -> config.onlyCarpets = value,
                                        requestPaletteRegenerate))
                                .addListener(limiterListener)
                                .build())
                        .option(LabelOption.create(Component.literal("More detailed settings:")))
                        .option(Option.<Boolean>createBuilder()
                                .name(Component.literal("Blocks with entities (EntityBlock)"))
                                .customController(TickBoxController::new)
                                .binding(paletteGenOptionBinding(
                                        defaults.entityBlocks,
                                        () -> config.entityBlocks,
                                        value -> config.entityBlocks = value,
                                        requestPaletteRegenerate))
                                .available(paletteGenLimiters.stream().noneMatch(Option::pendingValue))
                                .addListener((option, event) -> {
                                    if (event == OptionEventListener.Event.INITIAL) paletteGenOptions.add(option);
                                })
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Component.literal("Decorative blocks"))
                                .customController(TickBoxController::new)
                                .binding(paletteGenOptionBinding(
                                        defaults.buildDecorBlocks,
                                        () -> config.buildDecorBlocks,
                                        value -> config.buildDecorBlocks = value,
                                        requestPaletteRegenerate))
                                .available(paletteGenLimiters.stream().noneMatch(Option::pendingValue))
                                .addListener((option, event) -> {
                                    if (event == OptionEventListener.Event.INITIAL) paletteGenOptions.add(option);
                                })
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Component.literal("Require being placed in water"))
                                .customController(TickBoxController::new)
                                .binding(paletteGenOptionBinding(
                                        defaults.needWaterBlocks,
                                        () -> config.needWaterBlocks,
                                        value -> config.needWaterBlocks = value,
                                        requestPaletteRegenerate))
                                .available(paletteGenLimiters.stream().noneMatch(Option::pendingValue))
                                .addListener((option, event) -> {
                                    if (event == OptionEventListener.Event.INITIAL) paletteGenOptions.add(option);
                                })
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Component.literal("Creative-only blocks"))
                                .customController(TickBoxController::new)
                                .binding(paletteGenOptionBinding(
                                        defaults.creativeBlocks,
                                        () -> config.creativeBlocks,
                                        value -> config.creativeBlocks = value,
                                        requestPaletteRegenerate))
                                .available(paletteGenLimiters.stream().noneMatch(Option::pendingValue))
                                .addListener((option, event) -> {
                                    if (event == OptionEventListener.Event.INITIAL) paletteGenOptions.add(option);
                                })
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Component.literal("Growable blocks"))
                                .customController(TickBoxController::new)
                                .binding(paletteGenOptionBinding(
                                        defaults.growableBlocks,
                                        () -> config.growableBlocks,
                                        value -> config.growableBlocks = value,
                                        requestPaletteRegenerate))
                                .available(paletteGenLimiters.stream().noneMatch(Option::pendingValue))
                                .addListener((option, event) -> {
                                    if (event == OptionEventListener.Event.INITIAL) paletteGenOptions.add(option);
                                })
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Component.literal("Non-full blocks like tall grass"))
                                .customController(TickBoxController::new)
                                .binding(paletteGenOptionBinding(
                                        defaults.grassLikeBlocks,
                                        () -> config.grassLikeBlocks,
                                        value -> config.grassLikeBlocks = value,
                                        requestPaletteRegenerate))
                                .available(paletteGenLimiters.stream().noneMatch(Option::pendingValue))
                                .addListener((option, event) -> {
                                    if (event == OptionEventListener.Event.INITIAL) paletteGenOptions.add(option);
                                })
                                .build())
                        .build())
                .save(() -> {
                    CommonConfiguration.HANDLER.save();
                    if (requestPaletteRegenerate.get()) {
                        System.out.println("Regenerate is requested");
                        PaletteLoader.requestRegenerate();
                    }
                })
        ).generateScreen(parent);
    }

    private static @NotNull Binding<Boolean> paletteGenOptionBinding(Boolean def, Supplier<Boolean> getter, Consumer<Boolean> setter, AtomicBoolean requestRegen) {
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
