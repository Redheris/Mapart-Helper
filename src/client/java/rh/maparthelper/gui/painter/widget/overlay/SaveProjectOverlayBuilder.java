package rh.maparthelper.gui.painter.widget.overlay;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import rh.maparthelper.MapartHelper;
import rh.maparthelper.conversion.CurrentConversionSettings;
import rh.maparthelper.conversion.MapartImageUpdater;
import rh.maparthelper.conversion.staircases.StaircaseStyles;
import rh.maparthelper.gui.screen.MapartEditorScreen;
import rh.maparthelper.gui.widget.layout.OverlayLayout;
import rh.maparthelper.gui.widget.layout.OverlayLayoutFactory;
import rh.maparthelper.painter.layer.DynamicTextureLayer;
import rh.maparthelper.painter.layer.LayerManager;
import rh.maparthelper.painter.surface.NativeImageSurface;
import rh.maparthelper.state.painter.MapartPainterState;
import rh.maparthelper.util.CompatUtils;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class SaveProjectOverlayBuilder {
    public static OverlayLayout create(LayerManager<NativeImageSurface, DynamicTextureLayer> layerManager) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_hh.mm.ss");

        MapartPainterState painterState = MapartPainterState.getInstance();

        Component saveSinglePNG = Component.translatable("maparthelper.gui.mapart_painter.project.save_as_one_png");
        Component saveAllLayersZip = Component.translatable("maparthelper.gui.mapart_painter.project.save_layers_in_zip");
        Component saveAndSetMapart = Component.translatable("maparthelper.gui.mapart_painter.project.save_and_use_as_mapart");

        Font font = Minecraft.getInstance().font;
        int entryWidth = 8 + Math.max(
                font.width(saveSinglePNG),
                Math.max(font.width(saveAllLayersZip), font.width(saveAndSetMapart))
        );

        Button savePNG = Button.builder(
                saveSinglePNG,
                btn -> painterState.saveProjectAsPNG(LocalDateTime.now().format(formatter))
        ).size(entryWidth, 20).build();

        Button saveZip = Button.builder(
                saveAllLayersZip,
                btn -> {
                    Path filepath = painterState.saveLayersAsPNGsZip(LocalDateTime.now().format(formatter));
                    if (filepath != null && Minecraft.getInstance().player != null) {
                        Component mapartFile = Component.literal(filepath.getFileName().toString())
                                .withStyle(style -> style
                                        .withColor(ChatFormatting.GREEN)
                                        .withClickEvent(new ClickEvent.OpenFile(filepath.toFile()))
                                        .withHoverEvent(new HoverEvent.ShowText(Component.translatable("maparthelper.open_file")))
                                        .withUnderlined(true)
                                );
                        CompatUtils.sendMessage(
                                Minecraft.getInstance().player,
                                Component.translatable("maparthelper.file_saved", "saved_maps\\painter", mapartFile)
                                        .withStyle(ChatFormatting.GREEN),
                                false);
                    }
                }
        ).size(entryWidth, 20).build();

        Button passToMapartEditor = Button.builder(
                saveAndSetMapart,
                btn -> {
                    Path filepath = painterState.saveProjectAsPNG(LocalDateTime.now().format(formatter));
                    if (filepath != null) {
                        CurrentConversionSettings.resetMapart();
                        CurrentConversionSettings.mapart.setMapartSize(
                                layerManager.getWidth() / 128,
                                layerManager.getHeight() / 128
                        );
                        StaircaseStyles staircaseStyle = getStaircaseStyle(painterState);
                        MapartHelper.conversionConfig().setStaircaseStyle(staircaseStyle);

                        MapartImageUpdater.readAndUpdateMapartImage(CurrentConversionSettings.mapart, filepath);
                        Minecraft.getInstance().setScreen(new MapartEditorScreen());
                    }
                }
        ).size(entryWidth, 20).build();

        return OverlayLayoutFactory.listMenu(
                80, entryWidth + 10,
                savePNG,
                saveZip,
                passToMapartEditor
        );
    }

    private static StaircaseStyles getStaircaseStyle(MapartPainterState painterState) {
        MapartPainterState.ColorPickerState paletteState = painterState.getColorPickerState();
        return switch (paletteState) {
            case FLAT_1COLOR -> StaircaseStyles.FLAT_2D;
            case STAIRCASE_3COLORS -> StaircaseStyles.VALLEY_3D;
            case UNOBTAINABLE_4COLORS -> StaircaseStyles.UNOBTAINABLE;
            case PRESET -> MapartHelper.conversionConfig().getStaircaseStyle();
        };
    }
}
