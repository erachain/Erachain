package org.erachain.gui.items;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

public class ImageCropPanelNavigator2D extends JPanel {
    private ImageCropDisplayPanelNavigator2D imageCropDisplayPanelNavigator2D;

    public JSlider zoomSlider;
    public JSlider frameSlider;


    public ImageCropPanelNavigator2D(File imageFile, int cropWidth, int cropHeight) {
        setLayout(new BorderLayout());
        imageCropDisplayPanelNavigator2D = new ImageCropDisplayPanelNavigator2D(this, imageFile, cropWidth, cropHeight);
        add(imageCropDisplayPanelNavigator2D, BorderLayout.CENTER);

        JPanel sliderPanel = new JPanel(new BorderLayout());
        add(sliderPanel, BorderLayout.SOUTH);
        zoomSlider = new JSlider(JSlider.HORIZONTAL, 0, 200, 100);
        zoomSlider.setMajorTickSpacing(50);
        zoomSlider.setMinorTickSpacing(10);
        zoomSlider.setPaintTicks(true);
        zoomSlider.addChangeListener(e -> imageCropDisplayPanelNavigator2D.setZoom(zoomSlider.getValue() / 100d));
        imageCropDisplayPanelNavigator2D.addZoomListener(e -> zoomSlider.setValue((int) (imageCropDisplayPanelNavigator2D.getZoom() * 100)));
        sliderPanel.add(zoomSlider, BorderLayout.NORTH);
        frameSlider = new JSlider(JSlider.HORIZONTAL, 0, 200, 100);
        frameSlider.setMajorTickSpacing(10);
        frameSlider.setMinorTickSpacing(1);
        frameSlider.setPaintTicks(true);
        frameSlider.addChangeListener(e -> imageCropDisplayPanelNavigator2D.setFrameRate(frameSlider.getValue()));
        sliderPanel.add(frameSlider, BorderLayout.SOUTH);


    }


    public BufferedImage getSnapshot(TypeOfImage typeOfImage) {
        return imageCropDisplayPanelNavigator2D.getSnapshot(typeOfImage);
    }
}
