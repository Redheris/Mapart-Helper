package rh.maparthelper.conversion;

import rh.maparthelper.conversion.mapart.ConvertedMapartImage;
import rh.maparthelper.conversion.mapart.ProcessingMapartImage;

import java.awt.*;
import java.awt.image.BufferedImage;

public class MapartImageResizer {
    public static BufferedImage adjustToMapartSize(ProcessingMapartImage mapart) {
        BufferedImage image = mapart.getOriginal();
        int imageW = image.getWidth();
        int imageH = image.getHeight();
        ConvertedMapartImage.CroppingFrame frame = mapart.getCroppingFrame();

        int visibleW = frame.getWidth();
        int visibleH = frame.getHeight();
        image = image.getSubimage(frame.getX(), frame.getY(), Math.min(imageW, visibleW), Math.min(imageH, visibleH));

        int mapartW = mapart.getWidth() * 128;
        int mapartH = mapart.getHeight() * 128;
        int mapartScaledW = (int) Math.min(mapartW, Math.round(mapartW * ((double) imageW / visibleW)));
        int mapartScaledH = (int) Math.min(mapartH, Math.round(mapartH * ((double) imageH / visibleH)));
        mapart.setScaledImage(scaleImage(image, mapartScaledW, mapartScaledH));

        return placeOnMapartCanvas(mapart, mapartW, mapartH);
    }

    public static BufferedImage placeOnMapartCanvas(ProcessingMapartImage mapart, int mapartWidth, int mapartHeight) {
        BufferedImage mapartImage = new BufferedImage(mapartWidth, mapartHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = mapartImage.createGraphics();

        BufferedImage scaled = mapart.getScaledImage();
        g2d.drawImage(scaled, mapart.getInsertionX(), mapart.getInsertionY(), null);
        g2d.dispose();
        return mapartImage;
    }

    public static BufferedImage scaleImage(ProcessingMapartImage mapart, int width, int height) {
        BufferedImage image = mapart.getOriginal();
        image = scaleImage(image, width, height);
        mapart.setScaledImage(image);
        return image;
    }

    public static BufferedImage scaleImage(BufferedImage image, int width, int height) {
        boolean scaleUp = width > image.getWidth() || height > image.getHeight();
        BufferedImage scaledImage;
        if (scaleUp) {
            scaledImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = scaledImage.createGraphics();

            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.drawImage(image, 0, 0, width, height, null);
            g2.dispose();
        } else {
            Image scaled = image.getScaledInstance(width, height, Image.SCALE_SMOOTH);
            scaledImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

            Graphics2D g2d = scaledImage.createGraphics();
            g2d.drawImage(scaled, 0, 0, null);
            g2d.dispose();
        }
        return scaledImage;
    }
}
