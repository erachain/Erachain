package gui.items;

import javax.swing.*;

import lang.Lang;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

public abstract class ImageCropDialog extends JDialog {
    public ImageCropDialog(File imageFile, int cropWidth, int cropHeight) {
        super();

        JPanel contentPanel = new JPanel(new BorderLayout());
        ImageCropPanel imageCropPanel = new ImageCropPanel(imageFile, cropWidth, cropHeight);
        contentPanel.add(imageCropPanel, BorderLayout.CENTER);
        JPanel buttonPanel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        JButton okButton = new JButton("OK");
        okButton.addActionListener(e -> {
            onFinish(imageCropPanel.getSnapshot());
            dispose();
        });
        buttonPanel.add(okButton, c);
        c.gridx = 1;
        JButton cancelButton = new JButton(Lang.getInstance().translate("Cancel"));
        cancelButton.addActionListener(e ->
            dispose()
        );
        buttonPanel.add(cancelButton, c);
        contentPanel.add(buttonPanel, BorderLayout.SOUTH);

        setContentPane(contentPanel);

        pack();
        setResizable(false);
        setModal(true);
        setVisible(true);
    }


    public abstract void onFinish(BufferedImage image);
}
