package rh.maparthelper.conversion;

import java.awt.*;
import java.awt.image.BufferedImage;

public class MapartImageResizer {
    public static BufferedImage adjustToMapartSize(ConvertedMapartImage mapart) {
        BufferedImage image = mapart.original;
        int imageW = image.getWidth();
        int imageH = image.getHeight();
        ConvertedMapartImage.CroppingFrame frame = mapart.croppingFrame;

        int visibleW = frame.getWidth();
        int visibleH = frame.getHeight();
        image = image.getSubimage(frame.getX(), frame.getY(), Math.min(imageW, visibleW), Math.min(imageH, visibleH));

        int mapartW = mapart.getWidth() * 128;
        int mapartH = mapart.getHeight() * 128;
        int mapartScaledW = (int) Math.min(mapartW, Math.round(mapartW * ((double) imageW / visibleW)));
        int mapartScaledH = (int) Math.min(mapartH, Math.round(mapartH * ((double) imageH / visibleH)));
        mapart.scaledImage = scaleImage(image, mapartScaledW, mapartScaledH);

        return placeOnMapartCanvas(mapart, mapartW, mapartH);
    }

    public static BufferedImage placeOnMapartCanvas(ConvertedMapartImage mapart, int mapartWidth, int mapartHeight) {
        BufferedImage mapartImage = new BufferedImage(mapartWidth, mapartHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = mapartImage.createGraphics();

        BufferedImage scaled = mapart.scaledImage;
        g2d.drawImage(scaled, mapart.getInsertionX(), mapart.getInsertionY(), null);
        g2d.dispose();
        return mapartImage;
    }

    public static BufferedImage scaleImage(BufferedImage image, int width, int height) {
        boolean scaleUp = width > image.getWidth() || height > image.getHeight();
        if (scaleUp) {
            BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = resized.createGraphics();

            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.drawImage(image, 0, 0, width, height, null);
            g2.dispose();

            return resized;
        } else {
            Image scaled = image.getScaledInstance(width, height, Image.SCALE_SMOOTH);
            image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

            Graphics2D g2d = image.createGraphics();
            g2d.drawImage(scaled, 0, 0, null);
            g2d.dispose();

            return image;
        }
    }
}
