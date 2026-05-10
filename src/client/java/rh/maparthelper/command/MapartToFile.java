package rh.maparthelper.command;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import rh.maparthelper.MapartHelper;
import rh.maparthelper.maps.ImageFromMapItems;
import rh.maparthelper.maps.MapartImageData;
import rh.maparthelper.maps.SelectionIsEmptyException;
import rh.maparthelper.maps.SelectionNotFullException;
import rh.maparthelper.state.FramesAreaSelectionState;
import rh.maparthelper.util.CompatUtils;
import rh.maparthelper.util.FileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MapartToFile {
    private final static Path SAVED_MAPS_DIR = FabricLoader.getInstance().getGameDir().resolve("saved_maps");
    private static final ExecutorService mapartDataSaver = Executors.newSingleThreadExecutor(
            new ThreadFactoryBuilder().setNameFormat("Mapart Helper/Mapart Saver").build()
    );

    public static void initializeSavesDir() {
        try {
            if (Files.exists(SAVED_MAPS_DIR)) return;

            Files.createDirectories(SAVED_MAPS_DIR);
            MapartHelper.LOGGER.info("Created a directory for saved maps: \"{}\"", SAVED_MAPS_DIR);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void saveMapart(Player player, String mapartName, MapartImageData imageData) {
        mapartDataSaver.execute(() -> {
            String filename = FileUtils.makeUniqueFilename(SAVED_MAPS_DIR, mapartName, "png");
            Path filePath = SAVED_MAPS_DIR.resolve(filename);

            imageData.buildNativeImage(image -> {
                try {
                    image.writeToFile(filePath);
                    Component mapartFile = Component.literal(filename)
                            .withStyle(style -> style
                                    .withColor(ChatFormatting.GREEN)
                                    .withClickEvent(new ClickEvent.OpenFile(filePath.toAbsolutePath().toString()))
                                    .withHoverEvent(new HoverEvent.ShowText(Component.translatable("maparthelper.open_image_file")))
                                    .withUnderlined(true)
                            );

                    CompatUtils.sendMessage(player, Component.translatable("maparthelper.mapart_saved", mapartFile)
                            .withStyle(ChatFormatting.GREEN), false);
                } catch (InvalidPathException e) {
                    CompatUtils.sendMessage(player, Component.translatable("maparthelper.saving_path_error").withStyle(ChatFormatting.RED), false);
                    MapartHelper.LOGGER.error("Invalid path for saving the map", e);
                } catch (Exception e) {
                    CompatUtils.sendMessage(player, Component.translatable("maparthelper.saving_error").withStyle(ChatFormatting.RED), false);
                    MapartHelper.LOGGER.error("An error occurred during saving the map", e);
                }
            });
        });
    }

    public static void saveMapImageFromHand(Player player, String mapartName) {
        ItemStack itemStack = player.getMainHandItem();

        if (!(itemStack.getItem() instanceof MapItem))
            itemStack = player.getOffhandItem();
        if (!(itemStack.getItem() instanceof MapItem)) {
            CompatUtils.sendMessage(player,
                    Component.translatable("map_frames_selection.is_holding_filled_map").withStyle(ChatFormatting.RED),
                    true
            );
        }

        MapartImageData mapData = ImageFromMapItems.getMapartFromItem(itemStack, player.level());
        if (mapData == MapartImageData.INVALID) {
            sendInvalidFilledMapMessage(player);
            return;
        }

        saveMapart(player, mapartName, mapData);
    }

    public static void saveMapImageFromItemFrame(Player player, String mapartName) {
        HitResult target = Minecraft.getInstance().hitResult;
        if (target instanceof EntityHitResult result && result.getEntity() instanceof ItemFrame itemFrame) {
            if (itemFrame.hasFramedMap()) {
                MapartImageData mapData = ImageFromMapItems.getMapartFromItemFrame(
                        itemFrame,
                        player.level(),
                        Direction.fromYRot(player.getYHeadRot())
                );
                if (mapData == MapartImageData.INVALID) {
                    sendInvalidFilledMapMessage(player);
                    return;
                }
                saveMapart(player, mapartName, mapData);
                return;
            }
        }

        CompatUtils.sendMessage(player,
                Component.translatable("map_frames_selection.is_looking_at_frame_with_map").withStyle(ChatFormatting.RED),
                true
        );
    }

    public static void saveImageFromItemFramesArea(Player player, String mapartName) {
        try {
            MapartImageData imageData = ImageFromMapItems.getMapartFromItemFramesArea(
                    player.level(), Direction.fromYRot(player.getYHeadRot())
            );
            saveMapart(player, mapartName, imageData);
            FramesAreaSelectionState.getInstance().resetSelection();
        } catch (SelectionIsEmptyException e) {
            CompatUtils.sendMessage(player, Component.translatable("map_frames_selection.selection_has_no_maps")
                    .withStyle(ChatFormatting.RED), true);
        } catch (SelectionNotFullException e) {
            CompatUtils.sendMessage(player, Component.translatable("map_frames_selection.selection_has_empty_places")
                    .withStyle(ChatFormatting.RED), true);
        }
    }

    private static void sendInvalidFilledMapMessage(Player player) {
        CompatUtils.sendMessage(player,
                Component.translatable("maparthelper.invalid_filled_map").withStyle(ChatFormatting.RED),
                true
        );
    }
}
