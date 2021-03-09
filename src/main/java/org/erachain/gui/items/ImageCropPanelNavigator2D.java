package org.erachain.gui.items;

import org.erachain.lang.Lang;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

public class ImageCropPanelNavigator2D extends JPanel {
    private ImageCropDisplayPanelNavigator2D imageCropDisplayPanelNavigator2D;

    public JSlider sizeSlider;
    public JSlider zoomSlider;
    public JSlider frameSlider;

    public JCheckBox asGif = new JCheckBox(Lang.T("as GIF/PNG with transparent background"));

    private boolean originalSize;

    public ImageCropPanelNavigator2D(File imageFile, int cropWidth, int cropHeight, boolean originalSize, TypeOfImage typeOfImage) {
        setLayout(new BorderLayout());

        this.originalSize = originalSize;
        this.asGif.setSelected(typeOfImage == TypeOfImage.GIF);

        imageCropDisplayPanelNavigator2D = new ImageCropDisplayPanelNavigator2D(this, imageFile, cropWidth, cropHeight);

        if (originalSize) {
            JPanel sliderPanelLeft = new JPanel(new BorderLayout());
            add(sliderPanelLeft, BorderLayout.WEST);
            sizeSlider = new JSlider(JSlider.VERTICAL, 0, 100, 100);
            sizeSlider.setMajorTickSpacing(20);
            sizeSlider.setMinorTickSpacing(5);
            sizeSlider.setPaintTicks(true);
            sizeSlider.addChangeListener(e -> imageCropDisplayPanelNavigator2D.setImgSize(sizeSlider.getValue() / 100d));
            add(sizeSlider, BorderLayout.WEST);
        }

        add(imageCropDisplayPanelNavigator2D, BorderLayout.CENTER);

        JPanel sliderPanel = new JPanel(new BorderLayout());
        add(sliderPanel, BorderLayout.SOUTH);

        add(asGif, BorderLayout.NORTH);

        zoomSlider = new JSlider(JSlider.HORIZONTAL, 0, 200, 100);
        zoomSlider.setMajorTickSpacing(50);
        zoomSlider.setMinorTickSpacing(10);
        zoomSlider.setPaintTicks(true);
        zoomSlider.addChangeListener(e -> imageCropDisplayPanelNavigator2D.setZoom(zoomSlider.getValue() / 100d));
        imageCropDisplayPanelNavigator2D.addListener(e -> zoomSlider.setValue((int) (imageCropDisplayPanelNavigator2D.getZoom() * 100)));
        sliderPanel.add(zoomSlider, BorderLayout.NORTH);

        frameSlider = new JSlider(JSlider.HORIZONTAL, 0, 200, 100);
        frameSlider.setMajorTickSpacing(50);
        frameSlider.setMinorTickSpacing(10);
        frameSlider.setPaintTicks(true);
        frameSlider.addChangeListener(e -> imageCropDisplayPanelNavigator2D.setFrameRate(frameSlider.getValue()));
        sliderPanel.add(frameSlider, BorderLayout.SOUTH);

    }


    public BufferedImage getSnapshot(TypeOfImage typeOfImage) {
        return imageCropDisplayPanelNavigator2D.getSnapshot(typeOfImage, originalSize);
    }
}
