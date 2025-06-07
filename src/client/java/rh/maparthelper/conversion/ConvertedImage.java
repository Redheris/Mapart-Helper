package rh.maparthelper.conversion;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.MapColor;
import net.minecraft.util.Pair;
import rh.maparthelper.MapartHelper;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

public class ConvertedImage {
    private final static Path TEMP_ARTS_DIR = FabricLoader.getInstance().getGameDir().resolve("saved_maps").resolve("temp");
    private BufferedImage image;

    public ConvertedImage(Path path) {
        try {
            Files.copy(path, TEMP_ARTS_DIR.resolve("original.png"));
            BufferedImage readImage = ImageIO.read(path.toFile());
            image = new BufferedImage(readImage.getWidth(), readImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = image.createGraphics();
            g.drawImage(readImage, 0, 0, null);
            g.dispose();
        }
        catch (Exception e) {
            MapartHelper.LOGGER.error("Error occurred while reading an image:\n{}", e.toString());
            throw new RuntimeException(e);
        }
    }

    public ConvertedImage(BufferedImage image) {
        this.image = image;
    }

    public void saveImage(String filename) {
        try {
            Path imagePath = TEMP_ARTS_DIR.getParent().resolve(filename + ".png");
            ImageIO.write(image, "png", imagePath.toFile());
            MapartHelper.LOGGER.info("Image successfully saved as {}", imagePath);
        }
        catch (Exception e) {
            MapartHelper.LOGGER.error("Error occurred while saving an image:\n{}", e.toString());
            throw new RuntimeException(e);
        }
    }

    // Saves temporary file of the image after converting image colors
    private void saveTemp() {
        try {
            Path imagePath = TEMP_ARTS_DIR.resolve("converted.png");
            ImageIO.write(image, "png", imagePath.toFile());
            MapartHelper.LOGGER.info("Temporary image successfully saved as {}", imagePath);
        }
        catch (Exception e) {
            MapartHelper.LOGGER.error("Error occurred while saving an image:\n{}", e.toString());
            throw new RuntimeException(e);
        }
    }

    /** Creates new Thread for computing new image with pixels adapted to the current blocks palette colors
     * */
    public void convertToBlocksPalette(boolean use3D, Consumer<ConvertedImage> callback) {
        new Thread(() -> {
            long startTime = System.currentTimeMillis();
            BufferedImage converted = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);

            for (int y = 0; y < image.getHeight(); y++) {
                for (int x = 0; x < image.getWidth(); x++) {
                    int argb = image.getRGB(x, y);
                    int newArgb;
                    if (use3D) {
                        Pair<MapColor, MapColor.Brightness> color = BlocksPalette.getClosestColor3D(argb);
                        newArgb = color.getLeft().getRenderColor(color.getRight());
                    } else {
                        MapColor color = BlocksPalette.getClosestColor2D(argb);
                        newArgb = color.getRenderColor(MapColor.Brightness.NORMAL);
                    }
                converted.setRGB(x, y, newArgb);
                }
            }
            double timeLeft = (System.currentTimeMillis() - startTime) / 1000.0;
            MapartHelper.LOGGER.info("[{}] Conversion took {} seconds", use3D ? "3D" : "2D", timeLeft);

            ConvertedImage result = new ConvertedImage(converted);
            callback.accept(result);
            result.saveTemp();
        }).start();
    }

    public ConvertedImage scaleToMapSize(int mapsX, int mapsY, int stretchingMode) {
        int width = 128 * mapsX;
        int height = 128 * mapsY;

        Image scaled = image.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        g2d.drawImage(scaled, 0, 0, null);
        g2d.dispose();

        return this;
    }
}
