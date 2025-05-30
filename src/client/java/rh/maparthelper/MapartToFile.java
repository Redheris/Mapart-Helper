package rh.maparthelper;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.MapColor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.map.MapState;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;

import java.io.File;
import java.nio.file.Path;

public class MapartToFile {
    private final static Path SAVE_MAPS_DIR = FabricLoader.getInstance().getGameDir().resolve("saved_maps");

    public static void initializeSavesDir() {
        File dir = SAVE_MAPS_DIR.toFile();
        if (dir.mkdirs()) {
            MapartHelper.LOGGER.info("Created folder for saved maps: \"{}\"", dir);
        }
    }

    public static void saveImageFromMapState(MapState mapState, String filename) {
        try (NativeImage image = new NativeImage(128, 128, false)){
            byte[] colors = mapState.colors;

            for (int i = 0; i < colors.length; i++)
                image.setColorArgb(i % 128, i / 128, MapColor.getRenderColor(colors[i]));

            if (SAVE_MAPS_DIR.resolve(filename + ".png").toFile().exists()) {
                int suffix = 1;
                while (SAVE_MAPS_DIR.resolve(filename + " (" + suffix + ").png").toFile().exists())
                    suffix++;
                image.writeTo(SAVE_MAPS_DIR.resolve(filename + " (" + suffix + ").png").toFile());
            }
            else {
                image.writeTo(SAVE_MAPS_DIR.resolve(filename + ".png").toFile());
            }
        } catch (Exception e) {
            MapartHelper.LOGGER.error("An error occurred during saving the map:\n{}", e.toString());
            throw new RuntimeException(e);
        }
    }

    public static void saveImageFromMapState(MapState mapState) {
        saveImageFromMapState(mapState, "New map");
    }

    public static MapState getMapStateFromItemFrame() {
        HitResult target = MinecraftClient.getInstance().crosshairTarget;
        if (!(target instanceof EntityHitResult entity))
            return null;
        if (entity.getEntity() instanceof ItemFrameEntity itemFrame) {
            if (!itemFrame.containsMap())
                return null;
            return FilledMapItem.getMapState(itemFrame.getHeldItemStack(), itemFrame.getWorld());
        }
        return null;
    }
}
