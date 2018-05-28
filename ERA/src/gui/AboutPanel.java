package gui;

import org.apache.log4j.Logger;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

@SuppressWarnings("serial")
public class AboutPanel extends JPanel {
    private static final Logger LOGGER = Logger.getLogger(AboutPanel.class);
    private BufferedImage image;

    public AboutPanel() {
        try {
            image = ImageIO.read(new File("images/about.png"));
        } catch (IOException ex) {
            LOGGER.error(ex);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(image, 0, 0, null);
    }
}
