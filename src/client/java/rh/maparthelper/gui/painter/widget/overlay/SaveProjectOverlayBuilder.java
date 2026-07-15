package rh.maparthelper.gui.painter.widget.overlay;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import rh.maparthelper.conversion.CurrentConversionSettings;
import rh.maparthelper.conversion.MapartImageUpdater;
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
    // TODO: Localize
    public static OverlayLayout create(LayerManager<NativeImageSurface, DynamicTextureLayer> layerManager) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_hh.mm.ss");

        MapartPainterState painterState = MapartPainterState.getInstance();

        Button savePNG = Button.builder(
                Component.literal("Save as PNG"),
                btn -> painterState.saveProjectAsPNG(LocalDateTime.now().format(formatter))
        ).size(140, 20).build();

        Button saveZip = Button.builder(
                Component.literal("Save every layer as PNG"),
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
        ).size(140, 20).build();

        Button passToMapartEditor = Button.builder(
                Component.literal("Save and set as mapart"),
                btn -> {
                    Path filepath = painterState.saveProjectAsPNG(LocalDateTime.now().format(formatter));
                    if (filepath != null) {
                        CurrentConversionSettings.mapart.setMapartSize(
                                layerManager.getWidth() / 128,
                                layerManager.getHeight() / 128
                        );
                        MapartImageUpdater.readAndUpdateMapartImage(
                                CurrentConversionSettings.mapart,
                                filepath
                        );
                        Minecraft.getInstance().setScreen(new MapartEditorScreen());
                    }
                }
        ).size(140, 20).build();

        return OverlayLayoutFactory.listMenu(
                80, 150,
                savePNG,
                saveZip,
                passToMapartEditor
        );
    }
}
