package org.erachain.gui.items;

import org.erachain.lang.Lang;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

public abstract class ImageCropDialog extends JDialog {


    public ImageCropDialog(File imageFile, int cropWidth, int cropHeight, TypeOfImage typeOfImage, boolean originalSize) {
        JPanel contentPanel = new JPanel(new BorderLayout());
        ImageCropPanelNavigator2D imageCropPanel = new ImageCropPanelNavigator2D(imageFile, cropWidth, cropHeight, originalSize);
        contentPanel.add(imageCropPanel, BorderLayout.CENTER);
        JPanel buttonPanel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        JButton okButton = new JButton("OK");
        okButton.addActionListener(e -> {
            BufferedImage snapshot = imageCropPanel.getSnapshot(typeOfImage);
            onFinish(snapshot);
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


    public abstract void onFinish(BufferedImage image);
}
