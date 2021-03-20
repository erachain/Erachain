package org.erachain.gui.items;

import org.erachain.lang.Lang;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

public abstract class ImageCropDialog extends JDialog {

    public ImageCropPanelNavigator2D imageCropPanel;
    public ImageCropDialog(File imageFile, int cropWidth, int cropHeight, TypeOfImage typeOfImage, boolean originalSize) {
        JPanel contentPanel = new JPanel(new BorderLayout());
        imageCropPanel = new ImageCropPanelNavigator2D(imageFile, cropWidth, cropHeight, originalSize, typeOfImage);
        contentPanel.add(imageCropPanel, BorderLayout.CENTER);
        JPanel buttonPanel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        JButton okButton = new JButton("OK");
        okButton.addActionListener(e -> {
            TypeOfImage imgType = imageCropPanel.asGif.isSelected() ? TypeOfImage.GIF : TypeOfImage.JPEG;
            boolean useOriginal = imageCropPanel.useOrig.isSelected();
            BufferedImage snapshot = imageCropPanel.getSnapshot(imgType);
            onFinish(snapshot, imgType, useOriginal);
            dispose();
        });
        buttonPanel.add(okButton, c);
        c.gridx = 1;
        JButton cancelButton = new JButton(Lang.T("Cancel"));
        cancelButton.addActionListener(e ->
                dispose()
        );
        buttonPanel.add(cancelButton, c);
        contentPanel.add(buttonPanel, BorderLayout.SOUTH);
        setContentPane(contentPanel);
        pack();
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);
        setModal(true);
        setVisible(true);
    }

    public ImageCropDialog(ImageIcon image) {
        //setTitle();
        JPanel contentPanel = new JPanel(new BorderLayout());
        imageCropPanel = new ImageCropPanelNavigator2D(image);
        contentPanel.add(imageCropPanel, BorderLayout.CENTER);
        JPanel buttonPanel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        JButton okButton = new JButton("OK");
        okButton.addActionListener(e ->
                dispose()
        );
        buttonPanel.add(okButton, c);
        contentPanel.add(buttonPanel, BorderLayout.SOUTH);
        setContentPane(contentPanel);
        pack();
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(true);
        setModal(true);
        setVisible(true);
    }

    public abstract void onFinish(BufferedImage image, TypeOfImage typeOfImage, boolean useOriginal);
}
