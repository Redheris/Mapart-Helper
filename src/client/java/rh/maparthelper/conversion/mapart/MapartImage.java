package rh.maparthelper.conversion.mapart;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.texture.NativeImage;

import java.awt.image.BufferedImage;
import java.nio.file.Path;

public abstract class MapartImage {
    protected final static Path SAVED_MAPS_DIR = FabricLoader.getInstance().getGameDir().resolve("saved_maps");
    protected ColorsCounter colorsCounter = new ColorsCounter();
    protected NativeImage nativeImage;
    protected BufferedImage original;
    protected Path imagePath;

    public String mapartName = "New mapart";
    protected int width = 1;
    protected int height = 1;

    protected final CroppingFrame croppingFrame = new CroppingFrame();
    protected BufferedImage scaledImage;
    protected int insertionX = 0;
    protected int insertionY = 0;

    public MapartImage() {

    }

    public MapartImage(MapartImage mapart) {
        this.colorsCounter = new ColorsCounter(mapart.colorsCounter);
        this.nativeImage = mapart.nativeImage;
        this.original = mapart.original;
        this.imagePath = mapart.imagePath;
        this.mapartName = mapart.mapartName;
        this.width = mapart.width;
        this.height = mapart.height;
        this.insertionX = mapart.insertionX;
        this.insertionY = mapart.insertionY;
        this.scaledImage = mapart.scaledImage;

        this.croppingFrame.x = mapart.croppingFrame.x;
        this.croppingFrame.y = mapart.croppingFrame.y;
        this.croppingFrame.width = mapart.croppingFrame.width;
        this.croppingFrame.height = mapart.croppingFrame.height;
    }

    public ColorsCounter getColorsCounter() {
        return colorsCounter;
    }

    public NativeImage getNativeImage() {
        return nativeImage;
    }

    public BufferedImage getOriginal() {
        return original;
    }

    public Path getImagePath() {
        return imagePath;
    }

    public CroppingFrame getCroppingFrame() {
        return croppingFrame;
    }

    public BufferedImage getScaledImage() {
        return scaledImage;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getInsertionX() {
        return insertionX;
    }

    public int getInsertionY() {
        return insertionY;
    }


    public class CroppingFrame {
        private int x = 0;
        private int y = 0;
        private int width = 1;
        private int height = 1;

        public int getX() {
            return x;
        }

        protected void setX(int x) {
            if (x < 0 || x > original.getWidth()) return;
            this.x = x;
        }

        public int getY() {
            return y;
        }

        protected void setY(int y) {
            if (y < 0 || y > original.getHeight()) return;
            this.y = y;
        }

        public int getWidth() {
            return width;
        }

        protected void setWidth(int width) {
            this.width = width;
        }

        public int getHeight() {
            return height;
        }

        protected void setHeight(int height) {
            this.height = height;
        }
    }
}
