package rh.maparthelper.conversion.mapart;

import net.minecraft.client.texture.NativeImage;

import java.awt.image.BufferedImage;
import java.nio.file.Path;

public class ProcessingMapartImage extends MapartImage {

    public ProcessingMapartImage(MapartImage mapart) {
        super(mapart);
    }

    public ConvertedMapartImage release(ConvertedMapartImage mapart) {
        return mapart.update(this);
    }

    public void centerCroppingFrame() {
        int imageWidth = original.getWidth();
        int imageHeight = original.getHeight();
        double mapartAspect = (double) width / height;
        double imageAspect = (double) imageWidth / imageHeight;

        int frameWidth, frameHeight;
        int frameX, frameY;

        if (imageAspect > mapartAspect) {
            frameHeight = imageHeight;
            frameY = 0;
            frameWidth = (int) (frameHeight * mapartAspect);
            frameX = (imageWidth - frameWidth) / 2;
        } else {
            frameWidth = imageWidth;
            frameX = 0;
            frameHeight = (int) (frameWidth / mapartAspect);
            frameY = (imageHeight - frameHeight) / 2;
        }
        croppingFrame.setX(frameX);
        croppingFrame.setY(frameY);
        croppingFrame.setWidth(frameWidth);
        croppingFrame.setHeight(frameHeight);
        insertionX = 0;
        insertionY = 0;
    }

    public void scaleToCenter(double scale) {
        scaleToPoint(0.5, 0.5, scale);
    }

    public void scaleToPoint(double pointX, double pointY, double scale) {
        int imageWidth = original.getWidth();
        int imageHeight = original.getHeight();
        double mapartAspect = (double) width / height;
        double imageAspect = (double) imageWidth / imageHeight;

        int delta = (int) scale * 5;

        int minSize = Math.min(imageWidth, Math.min(imageHeight, 64));
        int cropWidth = croppingFrame.getWidth();
        int cropHeight = croppingFrame.getHeight();
        int centerX = (int) (croppingFrame.getX() + cropWidth * pointX);
        int centerY = (int) (croppingFrame.getY() + cropHeight * pointY);

        if (imageAspect < mapartAspect) {
            cropWidth = Math.max(cropWidth - 2 * delta, minSize);
            cropHeight = (int) Math.round(cropWidth / mapartAspect);
        } else {
            cropHeight = Math.max(cropHeight - 2 * delta, minSize);
            cropWidth = (int) Math.round(cropHeight * mapartAspect);
        }
        int mapartW = width * 128;
        int mapartH = height * 128;
        int mapartScaledW = (int) Math.min(mapartW, Math.round(mapartW * ((double) imageWidth / cropWidth)));
        int mapartScaledH = (int) Math.min(mapartH, Math.round(mapartH * ((double) imageHeight / cropHeight)));
        setInsertionX(Math.max(0, (mapartW - mapartScaledW) / 2));
        setInsertionY(Math.max(0, (mapartH - mapartScaledH) / 2));

        int frameX = Math.clamp(centerX - (int) (cropWidth * pointX), 0, Math.max(imageWidth - cropWidth, 0));
        int frameY = Math.clamp(centerY - (int) (cropHeight * pointY), 0, Math.max(imageHeight - cropHeight, 0));

        croppingFrame.setX(frameX);
        croppingFrame.setY(frameY);
        croppingFrame.setWidth(cropWidth);
        croppingFrame.setHeight(cropHeight);
    }

    public boolean moveCroppingFrame(int dx, int dy) {
        if (original == null) return false;

        boolean needRescale = false;
        int imageWidth = original.getWidth();
        int imageHeight = original.getHeight();
        int mapartWidth = width * 128;
        int mapartHeight = height * 128;
        if (scaledImage.getWidth() < mapartWidth)
            insertionX = Math.clamp(insertionX + dx, 0, mapartWidth - scaledImage.getWidth());
        else {
            insertionX = 0;
            croppingFrame.setX(Math.clamp(croppingFrame.getX() - dx, 0, imageWidth - croppingFrame.getWidth()));
            needRescale = true;
        }
        if (scaledImage.getHeight() < mapartHeight)
            insertionY = Math.clamp(insertionY + dy, 0, mapartHeight - scaledImage.getHeight());
        else {
            insertionY = 0;
            croppingFrame.setY(Math.clamp(croppingFrame.getY() - dy, 0, imageHeight - croppingFrame.getHeight()));
            needRescale = true;
        }
        return needRescale;
    }

    public void setNativeImage(NativeImage nativeImage) {
        this.nativeImage = nativeImage;
    }

    public void setImagePath(Path imagePath) {
        this.imagePath = imagePath;
    }

    public void setOriginal(BufferedImage image) {
        this.original = image;
    }

    public void setScaledImage(BufferedImage scaledImage) {
        this.scaledImage = scaledImage;
    }

    public void setScale(double scale) {
        this.scale = scale;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void setInsertionX(int insertionX) {
        this.insertionX = insertionX;
    }

    public void setInsertionY(int insertionY) {
        this.insertionY = insertionY;
    }

}
