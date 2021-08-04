package org.erachain.gui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

@SuppressWarnings("serial")
public class AboutPanel extends JPanel {
    private static final Logger LOGGER = LoggerFactory.getLogger(AboutPanel.class);
    private BufferedImage image;

    public AboutPanel() {
        try {
            image = ImageIO.read(new File("images/about.png"));
        } catch (IOException ex) {
            LOGGER.error(ex.getMessage() + " - " + new File("images/about.png").getAbsolutePath());
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(image, 0, 0, null);
    }
}
