package org.erachain.gui.items;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

public class ImageCropPanelNavigator2D extends JPanel {
    private ImageCropDisplayPanelNavigator2D imageCropDisplayPanelNavigator2D;


    public ImageCropPanelNavigator2D(File imageFile, int cropWidth, int cropHeight) {
        setLayout(new BorderLayout());
        imageCropDisplayPanelNavigator2D = new ImageCropDisplayPanelNavigator2D(imageFile, cropWidth, cropHeight);
        add(imageCropDisplayPanelNavigator2D, BorderLayout.CENTER);
    }


    public BufferedImage getSnapshot(TypeOfImage typeOfImage) {
        return imageCropDisplayPanelNavigator2D.getSnapshot(typeOfImage);
    }
}
