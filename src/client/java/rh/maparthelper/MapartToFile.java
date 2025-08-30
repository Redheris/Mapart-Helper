package rh.maparthelper;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.MapColor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.map.MapState;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import rh.maparthelper.command.ClientCommandsContext;
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
            if (Files.exists(SAVE_MAPS_DIR.resolve("temp"))) return;

            Files.createDirectories(SAVE_MAPS_DIR.resolve("temp"));
            MapartHelper.LOGGER.info("Created a directory for saved maps: \"{}\"", SAVE_MAPS_DIR);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void saveImageFromItemFramesArea(PlayerEntity player, World world, String filename) {
        Vec3d pos1 = ClientCommandsContext.selectedPos1;
        Vec3d pos2 = ClientCommandsContext.selectedPos2;

        int width = ClientCommandsContext.selectionWidth;
        int height = ClientCommandsContext.selectionHeight;
        int size = width * height;

        Box area = new Box(pos1, pos2);

        List<ItemFrameEntity> itemFrames = world.getEntitiesByType(EntityType.ITEM_FRAME, area, ItemFrameEntity::containsMap);
        itemFrames.addAll(world.getEntitiesByType(EntityType.GLOW_ITEM_FRAME, area, ItemFrameEntity::containsMap));

        if (itemFrames.isEmpty()) {
            player.sendMessage(Text.translatable("maparthelper.selection_has_no_maps").formatted(Formatting.RED), true);
            return;
        }
        if (itemFrames.size() != size) {
            player.sendMessage(Text.translatable("maparthelper.selection_has_empty_places").formatted(Formatting.RED), true);
            return;
        }

        itemFrames = itemFrames.stream().sorted((if1, if2) -> {
            BlockPos p1 = if1.getBlockPos();
            BlockPos p2 = if2.getBlockPos();

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

                    mapart.setColorArgb(resultX, resultY, MapColor.getRenderColor(map[j]));
                }
            }

            ClientCommandsContext.resetSelection();
            saveMapartFile(player, filename, mapart);

        } catch (InvalidPathException e) {
            player.sendMessage(Text.translatable("maparthelper.saving_path_error").formatted(Formatting.RED), false);
            MapartHelper.LOGGER.error("Invalid path for saving the map:\n{}", e.toString());
        }
        catch (Exception e) {
            player.sendMessage(Text.translatable("maparthelper.saving_error").formatted(Formatting.RED), false);
            MapartHelper.LOGGER.error("An error occurred during saving the map:\n{}", e.toString());
        }

    }

    public static void saveImageFromMapColors(PlayerEntity player, byte[] mapColors, String filename) {
        try (NativeImage image = new NativeImage(128, 128, false)) {

            for (int i = 0; i < mapColors.length; i++)
                image.setColorArgb(i % 128, i / 128, MapColor.getRenderColor(mapColors[i]));

            saveMapartFile(player, filename, image);

        } catch (Exception e) {
            MapartHelper.LOGGER.error("An error occurred during saving the map:\n{}", e.toString());
            throw new RuntimeException(e);
        }
    }

    private static void saveMapartFile(PlayerEntity player, String filename, NativeImage image) throws IOException {
        filename = Utils.makeUniqueFilename(SAVE_MAPS_DIR, filename, "png");

        Path filePath = SAVE_MAPS_DIR.resolve(filename);
        image.writeTo(filePath);

        Text mapartFile = Text.literal(filename)
                .styled(style -> style
                        .withColor(Formatting.GREEN)
                        .withClickEvent(new ClickEvent.OpenFile(filePath.toAbsolutePath().toString()))
                        .withHoverEvent(new HoverEvent.ShowText(Text.translatable("maparthelper.open_image_file")))
                        .withUnderline(true)
                );

        player.sendMessage(Text.translatable("maparthelper.mapart_saved", mapartFile).formatted(Formatting.GREEN), false);
    }

    public static void saveImageFromMapColors(PlayerEntity player, byte[] mapColors) {
        saveImageFromMapColors(player, mapColors, "New map");
    }

    public static byte[] getMapColorsFromItemFrame(ItemFrameEntity itemFrame) {
        if (!itemFrame.containsMap())
            return null;
        MapState mapState = FilledMapItem.getMapState(itemFrame.getHeldItemStack(), itemFrame.getWorld());
        assert mapState != null;
        return rotateMap(mapState.colors.clone(), itemFrame.getRotation());
    }

    public static byte[] getMapColorsFromItemFrame() {
        HitResult target = MinecraftClient.getInstance().crosshairTarget;

        if (!(target instanceof EntityHitResult entity))
            return null;

        if (entity.getEntity() instanceof ItemFrameEntity itemFrame) {
            if (!itemFrame.containsMap())
                return null;
            MapState mapState = FilledMapItem.getMapState(itemFrame.getHeldItemStack(), itemFrame.getWorld());
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
                    case 0 ->
                            toIndex = y * 128 + x;
                    case 1 ->
                            toIndex = x * 128 + (127 - y);
                    case 2 ->
                            toIndex = (127 - y) * 128 + (127 - x);
                    case 3 ->
                            toIndex = (127 - x) * 128 + y;
                    default -> throw new IllegalArgumentException("Invalid rotation");
                }

                output[toIndex] = input[fromIndex];
            }
        }

        return output;
    }
}
