package org.erachain.gui.library;

import org.erachain.gui.items.ImageCropDialog;
import org.erachain.gui.items.TypeOfImage;
import org.erachain.gui.items.assets.CreateOrderPanel;
import org.erachain.lang.Lang;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;


public class AddImageLabel extends JLabel {

    private static final long serialVersionUID = 1L;
    private byte[] imgBytes;
    private int bezelWidth;
    private int bezelHeight;
    private Logger logger = LoggerFactory.getLogger(getClass().getName());
    private JLabel label = new JLabel();

    public AddImageLabel(String text, int bezelWidth, int bezelHeight, TypeOfImage typeOfImage) {
        setLayout(new BorderLayout());
        label.setText(text);
        add(label, BorderLayout.NORTH);
        this.bezelWidth = bezelWidth;
        this.bezelHeight = bezelHeight;
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setBorder(BorderFactory.createEtchedBorder());
        setVerticalAlignment(SwingConstants.TOP);
        setHorizontalAlignment(SwingConstants.CENTER);
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    addImage(typeOfImage);
                }
            }
        });
        JPopupMenu menu = new JPopupMenu();
        JMenuItem copyAddress = new JMenuItem(Lang.getInstance().translate("Reset"));
        copyAddress.addActionListener(e -> reset());
        menu.add(copyAddress);
        setComponentPopupMenu(menu);
    }


    private void addImage(TypeOfImage typeOfImage) {
        // открыть диалог для файла
        fileChooser chooser = new fileChooser();
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setMultiSelectionEnabled(false);
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Image", "png", "jpg", "gif");
        chooser.setFileFilter(filter);
        chooser.setDialogTitle(Lang.getInstance().translate("Open Image") + "...");
        int returnVal = chooser.showOpenDialog(getParent());
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = new File(chooser.getSelectedFile().getPath());
            new ImageCropDialog(file, bezelWidth, bezelHeight, typeOfImage) {
                @Override
                public void onFinish(BufferedImage image) {
                    if (image == null) {
                        logger.error("Image does not setup");
                        return;
                    }
                    setIcon(new ImageIcon(image));
                    setPreferredSize(new Dimension(image.getWidth(), image.getHeight()));
                    ByteArrayOutputStream imageStream = new ByteArrayOutputStream();
                    try {
                        if (typeOfImage == TypeOfImage.GIF) {
                            ImageIO.write(image, "gif", imageStream);
                        } else if (typeOfImage == TypeOfImage.JPEG) {
                            ImageIO.write(image, "jpeg", imageStream);
                        }
                        imgBytes = imageStream.toByteArray();
                    } catch (Exception e) {
                        logger.error("Can not write image in ImageCropDialog dialog onFinish method", e);
                    }
                }
            };
        }
    }

    public void reset() {
        imgBytes = null;
        setIcon(null);
    }

    public byte[] getImgBytes() {
        return imgBytes;
    }
}
