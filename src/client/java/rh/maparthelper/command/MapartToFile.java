package rh.maparthelper.command;

import com.mojang.blaze3d.platform.NativeImage;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import rh.maparthelper.MapartHelper;
import rh.maparthelper.util.Utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

public class MapartToFile {
    private final static Path SAVE_MAPS_DIR = FabricLoader.getInstance().getGameDir().resolve("saved_maps");

    public static void initializeSavesDir() {
        try {
            if (Files.exists(SAVE_MAPS_DIR)) return;

            Files.createDirectories(SAVE_MAPS_DIR);
            MapartHelper.LOGGER.info("Created a directory for saved maps: \"{}\"", SAVE_MAPS_DIR);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void saveImageFromItemFramesArea(Player player, Level world, String filename) {
        Vec3 pos1 = ClientCommandsContext.selectedPos1;
        Vec3 pos2 = ClientCommandsContext.selectedPos2;

        int width = ClientCommandsContext.selectionWidth;
        int height = ClientCommandsContext.selectionHeight;
        int size = width * height;

        AABB area = new AABB(pos1, pos2);

        List<ItemFrame> itemFrames = world.getEntities(EntityType.ITEM_FRAME, area, ItemFrame::hasFramedMap);
        itemFrames.addAll(world.getEntities(EntityType.GLOW_ITEM_FRAME, area, ItemFrame::hasFramedMap));

        if (itemFrames.isEmpty()) {
            player.displayClientMessage(Component.translatable("maparthelper.selection_has_no_maps").withStyle(ChatFormatting.RED), true);
            return;
        }
        if (itemFrames.size() != size) {
            player.displayClientMessage(Component.translatable("maparthelper.selection_has_empty_places").withStyle(ChatFormatting.RED), true);
            return;
        }

        itemFrames = itemFrames.stream().sorted((if1, if2) -> {
            BlockPos p1 = if1.blockPosition();
            BlockPos p2 = if2.blockPosition();

            if (ClientCommandsContext.selectedDirection.getAxis() != Direction.Axis.Y) {
                int heightCompare = Integer.compare(p2.getY(), p1.getY());
                if (heightCompare != 0) return heightCompare;
            }

            return switch (ClientCommandsContext.selectedDirection) {
                case EAST -> Integer.compare(p2.getZ(), p1.getZ());
                case WEST -> Integer.compare(p1.getZ(), p2.getZ());
                case NORTH -> Integer.compare(p2.getX(), p1.getX());
                case SOUTH -> Integer.compare(p1.getX(), p2.getX());
                case UP, DOWN -> {
                    int zCompare;
                    if (ClientCommandsContext.selectedDirection == Direction.DOWN)
                        zCompare = Integer.compare(p2.getZ(), p1.getZ());
                    else
                        zCompare = Integer.compare(p1.getZ(), p2.getZ());
                    if (zCompare != 0) yield zCompare;

                    yield Integer.compare(p1.getX(), p2.getX());
                }
            };
        }).toList();

        int mapartWidth = width * 128;
        int mapartHeight = height * 128;
        try (NativeImage mapart = new NativeImage(mapartWidth, mapartHeight, false)) {
            for (int ind = 0; ind < itemFrames.size(); ind++) {
                byte[] mapColors = getMapColorsFromItemFrame(itemFrames.get(ind));
                byte[] map = (mapColors != null) ? mapColors : new byte[16384];
                if (mapColors == null)
                    Arrays.fill(map, (byte) 0);

                int mapX = ind % width;
                int mapY = ind / width;

                for (int j = 0; j < map.length; j++) {
                    int localX = j % 128;
                    int localY = j / 128;

                    int resultX = mapX * 128 + localX;
                    int resultY = mapY * 128 + localY;

                    mapart.setPixel(resultX, resultY, MapColor.getColorFromPackedId(map[j]));
                }
            }

            ClientCommandsContext.resetSelection();
            saveMapartFile(player, filename, mapart);

        } catch (InvalidPathException e) {
            player.displayClientMessage(Component.translatable("maparthelper.saving_path_error").withStyle(ChatFormatting.RED), false);
            MapartHelper.LOGGER.error("Invalid path for saving the map:\n{}", e.toString());
        } catch (Exception e) {
            player.displayClientMessage(Component.translatable("maparthelper.saving_error").withStyle(ChatFormatting.RED), false);
            MapartHelper.LOGGER.error("An error occurred during saving the map:\n{}", e.toString());
        }

    }

    public static void saveImageFromMapColors(Player player, byte[] mapColors, String filename) {
        try (NativeImage image = new NativeImage(128, 128, false)) {

            for (int i = 0; i < mapColors.length; i++)
                image.setPixel(i % 128, i / 128, MapColor.getColorFromPackedId(mapColors[i]));

            saveMapartFile(player, filename, image);

        } catch (Exception e) {
            MapartHelper.LOGGER.error("An error occurred during saving the map:\n{}", e.toString());
            throw new RuntimeException(e);
        }
    }

    private static void saveMapartFile(Player player, String filename, NativeImage image) throws IOException {
        filename = Utils.makeUniqueFilename(SAVE_MAPS_DIR, filename, "png");

        Path filePath = SAVE_MAPS_DIR.resolve(filename);
        image.writeToFile(filePath);

        Component mapartFile = Component.literal(filename)
                .withStyle(style -> style
                        .withColor(ChatFormatting.GREEN)
                        .withClickEvent(new ClickEvent.OpenFile(filePath.toAbsolutePath().toString()))
                        .withHoverEvent(new HoverEvent.ShowText(Component.translatable("maparthelper.open_image_file")))
                        .withUnderlined(true)
                );

        player.displayClientMessage(Component.translatable("maparthelper.mapart_saved", mapartFile).withStyle(ChatFormatting.GREEN), false);
    }

    public static void saveImageFromMapColors(Player player, byte[] mapColors) {
        saveImageFromMapColors(player, mapColors, "New map");
    }

    public static byte[] getMapColorsFromItemFrame(ItemFrame itemFrame) {
        if (!itemFrame.hasFramedMap())
            return null;
        MapItemSavedData mapState = MapItem.getSavedData(itemFrame.getItem(), itemFrame.level());
        assert mapState != null;
        return rotateMap(mapState.colors.clone(), itemFrame.getRotation());
    }

    public static byte[] getMapColorsFromItemFrame() {
        HitResult target = Minecraft.getInstance().hitResult;

        if (!(target instanceof EntityHitResult entity))
            return null;

        if (entity.getEntity() instanceof ItemFrame itemFrame) {
            if (!itemFrame.hasFramedMap())
                return null;
            MapItemSavedData mapState = MapItem.getSavedData(itemFrame.getItem(), itemFrame.level());
            assert mapState != null;
            return rotateMap(mapState.colors.clone(), itemFrame.getRotation());
        }

        return null;
    }

    private static byte[] rotateMap(byte[] input, int rotation) {
        byte[] output = new byte[128 * 128];

        for (int y = 0; y < 128; y++) {
            for (int x = 0; x < 128; x++) {
                int fromIndex = y * 128 + x;
                int toIndex;

                switch (rotation % 4) {
                    case 0 -> toIndex = y * 128 + x;
                    case 1 -> toIndex = x * 128 + (127 - y);
                    case 2 -> toIndex = (127 - y) * 128 + (127 - x);
                    case 3 -> toIndex = (127 - x) * 128 + y;
                    default -> throw new IllegalArgumentException("Invalid rotation");
                }

                output[toIndex] = input[fromIndex];
            }
        }

        return output;
    }
}
