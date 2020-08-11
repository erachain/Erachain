package org.erachain.utils;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class GUIUtils {

    public static ImageIcon createIcon(int size, Color colorOfIcon, Color backgroundColor) {
        //CREATE IMAGE
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();

        //AA
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        //SET COLOR
        g.setColor(colorOfIcon);

        //CREATE CIRCLE
        g.fillOval(0, 0, size - 1, size - 1);

        //SET BACKGROUND
        g.setBackground(backgroundColor);

        //CONVERT TO ICON
        return new ImageIcon(image);
    }

    public static ImageIcon createIconArc(int size, int sector, Color colorOfIcon) {
        //CREATE IMAGE
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();

        //AA
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        //SET COLOR
        g.setColor(colorOfIcon);

        //CREATE CIRCLE
        g.drawOval(0, 0, size - 2, size - 2);
        g.fillArc(0, 0, size - 2, size - 2, 90, -120 * sector);

        //CONVERT TO ICON
        return new ImageIcon(image);
    }

}
