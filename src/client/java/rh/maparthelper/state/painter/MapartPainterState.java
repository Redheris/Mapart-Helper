package rh.maparthelper.state.painter;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.material.MapColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import rh.maparthelper.MapartHelper;
import rh.maparthelper.mapart.MapartSaver;
import rh.maparthelper.mixin.NativeImageAccessor;
import rh.maparthelper.painter.PainterProject;
import rh.maparthelper.painter.drawing.DrawingEngine;
import rh.maparthelper.painter.layer.DynamicTextureLayer;
import rh.maparthelper.painter.layer.DynamicTextureLayerFactory;
import rh.maparthelper.painter.surface.NativeImageSurface;
import rh.maparthelper.palette.PaletteDataManager;
import rh.maparthelper.palette.RegisteredPalettePreset;
import rh.maparthelper.util.FileUtils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class MapartPainterState {
    private static final MapartPainterState INSTANCE = new MapartPainterState();
    public static final Path PAINTER_PNG_OUTPUT_DIR = MapartSaver.SAVED_MAPS_DIR.resolve("painter");
    @Nullable
    private PainterProject<NativeImageSurface, DynamicTextureLayer> currentPainterProject;
    private ColorPickerState colorPickerState = ColorPickerState.PRESET;
    @Nullable
    private Path imageOutputPath;

    private MapartPainterState() {}

    public static MapartPainterState getInstance() {
        return INSTANCE;
    }

    public void newPainterProject(@NotNull NativeImage original) {
        DynamicTextureLayerFactory layerFactory = new DynamicTextureLayerFactory();
        DynamicTextureLayer layer = layerFactory.createFromImage(
                original,
                Component.translatable("maparthelper.gui.mapart_painter.background").getString()
        );

        this.setCurrentPainterProject(new PainterProject<>(layer, layerFactory), true);
    }

    public void newPainterProject(int mapsWidth, int mapsHeight) {
        DynamicTextureLayerFactory layerFactory = new DynamicTextureLayerFactory();
        DynamicTextureLayer layer = layerFactory.createEmpty(mapsWidth * 128, mapsHeight * 128, 1);
        layer.setName(Component.translatable("maparthelper.gui.mapart_painter.background").getString());

        this.setCurrentPainterProject(new PainterProject<>(layer, layerFactory), true);
    }

    public void setCurrentPainterProject(@Nullable PainterProject<NativeImageSurface, DynamicTextureLayer> painterProject,
                                         boolean setDefaultDrawingColors
    ) {
        closeCurrentProject();
        this.imageOutputPath = null;
        this.currentPainterProject = painterProject;
        if (currentPainterProject != null && setDefaultDrawingColors) {
            defaultDrawingColors(currentPainterProject.getDrawingEngine());
        }
    }

    private void defaultDrawingColors(DrawingEngine<?> drawingEngine) {
        RegisteredPalettePreset preset = PaletteDataManager.getInstance().getPresetsHandler().getSelectedPreset();
        MapColor mainColor;
        MapColor secondaryColor;
        if (preset == null) {
            mainColor = MapColor.COLOR_BLACK;
            secondaryColor = MapColor.SNOW;
        } else {
            Iterator<MapColor> presetColors = preset.getMapColors().iterator();
            mainColor = presetColors.next();
            secondaryColor = presetColors.next();
        }
        drawingEngine.setMainColor(mainColor.calculateARGBColor(MapColor.Brightness.NORMAL));
        drawingEngine.setSecondaryColor(secondaryColor.calculateARGBColor(MapColor.Brightness.NORMAL));
    }

    private void closeCurrentProject() {
        if (currentPainterProject != null) {
            currentPainterProject.close();
        }
    }

    public @Nullable Path saveProjectAsPNG(String projectName) {
        if (currentPainterProject == null) return null;
        if (imageOutputPath == null) {
            String uniqueFilename = FileUtils.makeUniqueFilename(PAINTER_PNG_OUTPUT_DIR, projectName, "png");
            imageOutputPath = PAINTER_PNG_OUTPUT_DIR.resolve(uniqueFilename);
        }
        DynamicTextureLayer flattened = currentPainterProject.getLayerManager().flattenLayers();
        DynamicTexture texture = flattened.getTexture();
        MapartSaver.saveMapartImage(imageOutputPath, texture, Minecraft.getInstance().player);
        return imageOutputPath;
    }

    public @Nullable Path saveLayersAsPNGsZip(String projectName) {
        if (currentPainterProject == null) return null;
        String uniqueFilename = FileUtils.makeUniqueFilename(PAINTER_PNG_OUTPUT_DIR, projectName, "zip");
        Path filepath = PAINTER_PNG_OUTPUT_DIR.resolve(uniqueFilename);
        try (FileOutputStream fos = new FileOutputStream(filepath.toFile());
             ZipOutputStream zipOut = new ZipOutputStream(fos)
        ) {
            List<DynamicTextureLayer> layers = currentPainterProject.getLayerManager().getLayers();
            for (int i = 0; i < layers.size(); i++) {

                DynamicTextureLayer layer = layers.get(i);

                ZipEntry zipEntry = new ZipEntry((i + 1) + "_" + layer.getName() + ".png");
                zipOut.putNextEntry(zipEntry);
                @SuppressWarnings("ConstantConditions")
                NativeImageAccessor image = (NativeImageAccessor) (Object) layer.getTexture().getPixels();
                image.maparthelper$writeToChannel(Channels.newChannel(zipOut));
            }
            return filepath;
        } catch (Exception e) {
            MapartHelper.LOGGER.error("An error occurred during saving zip", e);
        }
        return null;
    }

    public @Nullable PainterProject<NativeImageSurface, DynamicTextureLayer> getPainterProject() {
        return currentPainterProject;
    }

    public boolean painterProjectExists() {
        return currentPainterProject != null;
    }

    public ColorPickerState getColorPickerState() {
        return colorPickerState;
    }

    public void setColorPickerState(ColorPickerState colorPickerState) {
        this.colorPickerState = colorPickerState;
    }

    public void initOutputFolder() {
        try {
            if (!Files.exists(PAINTER_PNG_OUTPUT_DIR)) {
                Files.createDirectories(PAINTER_PNG_OUTPUT_DIR);
            }
        } catch (IOException e) {
            MapartHelper.LOGGER.error("Error occurred while creating directories \"{}\"", PAINTER_PNG_OUTPUT_DIR, e);
        }
    }

    public enum ColorPickerState {
        PRESET(-1),
        FLAT_1COLOR(1),
        STAIRCASE_3COLORS(3),
        UNOBTAINABLE_4COLORS(4);

        public final int huesCount;

        ColorPickerState(int huesCount) {
            this.huesCount = huesCount;
        }
    }
}
