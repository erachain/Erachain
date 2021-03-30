package org.erachain.gui.library;

import org.erachain.api.ApiErrorFactory;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ImagesTools {

    public static byte[] bufferedImageToBytes(final BufferedImage bufferedImage, final String outputFormat)
            throws IOException {
        if (bufferedImage == null) {
            throw new IllegalArgumentException("Buffered image may not be null");
        }

        if (outputFormat == null) {
            throw new IllegalArgumentException("Output format may not be null");
        }

        try (@SuppressWarnings("resource") final ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            if (!ImageIO.write(bufferedImage, outputFormat, outputStream)) {
                throw new IOException(String.format("No appropriate image writer found for format '%s'", outputFormat));
            }

            return outputStream.toByteArray();
        }
    }

    public static BufferedImage imageToBufferedImage(Image im) {
        BufferedImage bi = new BufferedImage(im.getWidth(null), im.getHeight(null), BufferedImage.TYPE_INT_RGB);

        Graphics bg = bi.getGraphics();
        bg.drawImage(im, 0, 0, null);
        bg.dispose();
        return bi;
    }

    // image to byte[] with set Widht scale
    public static byte[] ImageToByte(Image img, int widht) {
        BufferedImage outputImage;

        if (widht != 0) {
            // if scale
            int x = img.getWidth(null);
            int y = img.getHeight(null);
            // calc scale item
            int x1 = widht;
            double k = ((double) x / (double) x1);
            y = (int) ((double) y / k);

            if (y == 0) {
                throw ApiErrorFactory.getInstance().createError(
                        // ApiErrorFactory.ERROR_INVALID_ASSET_ID);
                        "Invalid Image");

            }
            // input buffer +
            BufferedImage inputbuffered = ImagesTools.imageToBufferedImage(img);
            int scaledWidth = x1;
            int scaledHeight = y;
            // creates output image
            outputImage = new BufferedImage(scaledWidth, scaledHeight, inputbuffered.getType());
            // scales the input image to the output image
            Graphics2D g2d = outputImage.createGraphics();
            g2d.drawImage(inputbuffered, 0, 0, scaledWidth, scaledHeight, null);
            g2d.dispose();
        }
        // if not scale
        else {
            outputImage = ImagesTools.imageToBufferedImage(img);
        }

        byte[] b;
        try {
            b = ImagesTools.bufferedImageToBytes(outputImage, "jpg");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            throw ApiErrorFactory.getInstance().createError(
                    // ApiErrorFactory.ERROR_INVALID_ASSET_ID);
                    "Invalid Image");
        }
        return b;
    }

    public static ImageIcon resizeMaxWidth(ImageIcon imageIcon, int preferredWidth) {
        BufferedImage bufferedImage = new BufferedImage(imageIcon.getIconWidth(), imageIcon.getIconHeight(), BufferedImage.TYPE_INT_RGB);
        bufferedImage.getGraphics().drawImage(imageIcon.getImage(), 0, 0, null);

        int bufferedWidth = bufferedImage.getWidth();

        // под размеры поля подгоним чтобы поле не обрезало каритнку
        if (bufferedWidth > preferredWidth) {
            float scaleView = (float) preferredWidth / bufferedWidth;
            Image imagePack = bufferedImage.getScaledInstance(preferredWidth,
                    (int) (scaleView * bufferedImage.getHeight()),
                    Image.SCALE_AREA_AVERAGING);
            return new ImageIcon(imagePack);
        } else {
            return new ImageIcon(bufferedImage);
        }
    }
}

