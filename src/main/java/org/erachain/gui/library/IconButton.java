package org.erachain.gui.library;

import javax.swing.*;

public class IconButton extends JButton {


    public IconButton(int height, String fileImage) {
        super();

        ImageIcon image = new ImageIcon(fileImage);
        int x = image.getIconWidth();
        int y = image.getIconHeight();

        int x1 = height;
        double k = ((double) x / (double) x1);
        y = (int) ((double) y / k);

        setIcon(new ImageIcon(image.getImage().getScaledInstance(x1, y, 1)));

    }


}
