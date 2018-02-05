package gui.items;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

public class ImageCropPanel extends JPanel {
    private ImageCropDisplayPanel imageCropDisplayPanel;
    private JSlider zoomSlider;


    public ImageCropPanel(File imageFile, int cropWidth, int cropHeight) {
        super();

        setLayout(new BorderLayout());
        imageCropDisplayPanel = new ImageCropDisplayPanel(imageFile, cropWidth, cropHeight);
        add(imageCropDisplayPanel, BorderLayout.CENTER);
        zoomSlider = new JSlider(JSlider.HORIZONTAL, 0, 200, 100);
        zoomSlider.setMajorTickSpacing(50);
        zoomSlider.setMinorTickSpacing(10);
        zoomSlider.setPaintTicks(true);
        zoomSlider.addChangeListener(e -> imageCropDisplayPanel.setZoom(zoomSlider.getValue() / 100d));
        imageCropDisplayPanel.addZoomListener(e -> zoomSlider.setValue((int)(imageCropDisplayPanel.getZoom() * 100)));
        add(zoomSlider, BorderLayout.SOUTH);
    }


    public BufferedImage getSnapshot() {
        return imageCropDisplayPanel.getSnapshot();
    }
}
