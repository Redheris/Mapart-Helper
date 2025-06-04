package rh.maparthelper.conversion;

import net.fabricmc.loader.api.FabricLoader;
import rh.maparthelper.MapartHelper;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;

public class ImageConverter {
    private final static Path SAVE_MAPS_DIR = FabricLoader.getInstance().getGameDir().resolve("saved_maps");
    private final Path imagePath;
    private BufferedImage image;

    public ImageConverter(Path path) {
        this.imagePath = path;

        try {
            BufferedImage readImage = ImageIO.read(path.toFile());
            image = new BufferedImage(readImage.getWidth(), readImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = image.createGraphics();
            g.drawImage(readImage, 0, 0, null);
            g.dispose();
        }
        catch (Exception e) {
            MapartHelper.LOGGER.error("Error occurred while reading an image:\n{}", e.toString());
        }
    }

    public void saveImage() {
        try {
            Files.copy(imagePath, SAVE_MAPS_DIR.resolve(imagePath.getFileName()));
        }
        catch (Exception e) {
            MapartHelper.LOGGER.error("Error occurred while saving an image:\n{}", e.toString());
        }
    }

    public ImageConverter scaleToMapSize(int mapsX, int mapsY, int stretchingMode) {
        int width = 128 * mapsX;
        int height = 128 * mapsY;

        Image scaled = image.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        BufferedImage newImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = newImage.createGraphics();
        g2d.drawImage(scaled, 0, 0, null);
        g2d.dispose();

        return this;
    }
}
