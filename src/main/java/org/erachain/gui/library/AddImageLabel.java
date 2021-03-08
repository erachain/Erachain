package org.erachain.gui.library;

import org.erachain.gui.items.ImageCropDialog;
import org.erachain.gui.items.TypeOfImage;
import org.erachain.lang.Lang;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;


public class AddImageLabel extends JPanel {

    private static final long serialVersionUID = 1L;
    private final int initialWidth;
    private final int initialHeight;
    private final String text;
    private byte[] imgBytes;
    private int bezelWidth;
    private int bezelHeight;
    private Logger logger = LoggerFactory.getLogger(getClass());
    private JLabel label;
    private JLabel mainLabel = new JLabel();
    private boolean editable = true;

    public AddImageLabel(String text, int bezelWidth, int bezelHeight, TypeOfImage typeOfImage, int minSize, int maxSize, int initialWidth, int initialHeight) {
        setLayout(new BorderLayout());
        this.text = text;
        label = new JLabel("The Label", SwingConstants.CENTER);
        label.setText(this.text);
        add(label, BorderLayout.NORTH);
        add(mainLabel, BorderLayout.CENTER);
        this.bezelWidth = bezelWidth;
        this.bezelHeight = bezelHeight;
        this.initialWidth = initialWidth;
        this.initialHeight = initialHeight;
        mainLabel.setIcon(createImageIcon(Color.WHITE, this.initialWidth, this.initialHeight));
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setBorder(BorderFactory.createEtchedBorder());
        mainLabel.setVerticalAlignment(SwingConstants.TOP);
        mainLabel.setHorizontalAlignment(SwingConstants.CENTER);
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (editable) {
                    if (e.getButton() == MouseEvent.BUTTON1) {
                        addImage(typeOfImage, minSize, maxSize);
                    }
                }
            }
        });


        JPopupMenu menu = new JPopupMenu();
        JMenuItem resetMenu = new JMenuItem(Lang.T("Reset"));
        resetMenu.addActionListener(e -> reset());
        menu.add(resetMenu);
        setComponentPopupMenu(menu);
        validate();
    }

    public void setEditable(boolean value) {
        this.editable = value;
        if (value) {
            setCursor(new Cursor(Cursor.HAND_CURSOR));
        } else {
            setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        }
    }

    public void setImageHorizontalAlignment(int alig) {
        mainLabel.setHorizontalAlignment(alig);
        label.setHorizontalAlignment(alig);
    }

    private ImageIcon createImageIcon(Color color, int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setPaint(color);
        graphics.fillRect(0, 0, width, height);
        return new ImageIcon(image);
    }

    private void addImage(TypeOfImage typeOfImage, int minSize, int maxSize) {
        // открыть диалог для файла
        FileChooser chooser = new FileChooser();
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setMultiSelectionEnabled(false);
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Image", "png", "jpg", "gif");
        chooser.setFileFilter(filter);
        chooser.setDialogTitle(Lang.T("Open Image") + "...");
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
                    ImageIcon imageIcon = new ImageIcon(image);
                    mainLabel.setIcon(imageIcon);
                    mainLabel.setPreferredSize(new Dimension(initialWidth, initialHeight));
                    mainLabel.setMaximumSize(new Dimension(initialWidth, initialHeight));

                    ByteArrayOutputStream imageStream = new ByteArrayOutputStream();
                    try {
                        if (typeOfImage == TypeOfImage.GIF) {
                            ImageIO.write(image, "gif", imageStream);
                        } else if (typeOfImage == TypeOfImage.JPEG) {
                            ImageIO.write(image, "jpeg", imageStream);
                        }

                        imgBytes = imageStream.toByteArray();
                        if (minSize > 0) {
                            int templWidth = image.getWidth();
                            int templHeight = image.getHeight();
                            int counter = 0;
                            while (imgBytes.length < minSize && counter++ < 100) {
                                imageStream.reset();
                                templWidth *= 1.2;
                                templHeight *= 1.2;
                                Image scaledImage = image.getScaledInstance(templWidth, templHeight, Image.SCALE_AREA_AVERAGING);
                                writeImage(imageStream, templWidth, templHeight, scaledImage, typeOfImage);
                            }
                        }
                        if (maxSize > 0) {
                            int templWidth = image.getWidth();
                            int templHeight = image.getHeight();
                            int counter = 0;
                            while (imgBytes.length > maxSize && counter++ < 100) {
                                imageStream.reset();
                                templWidth /= 1.2;
                                templHeight /= 1.2;
                                Image scaledImage = image.getScaledInstance(templWidth, templHeight, Image.SCALE_AREA_AVERAGING);
                                writeImage(imageStream, templWidth, templHeight, scaledImage, typeOfImage);
                            }
                        }
                    } catch (Exception e) {
                        logger.error("Can not write image in ImageCropDialog dialog onFinish method", e);
                    }
                }

                private void writeImage(ByteArrayOutputStream imageStream, int templWidth, int templHeight, Image scaledImage, TypeOfImage typeOfImage) throws IOException {
                    BufferedImage image;
                    if (typeOfImage == TypeOfImage.GIF) {
                        image = new BufferedImage(templWidth, templHeight, BufferedImage.TYPE_INT_ARGB);
                        image.getGraphics().drawImage(scaledImage, 0, 0, null);
                        ImageIO.write(image, "gif", imageStream);
                    } else {
                        image = new BufferedImage(templWidth, templHeight, BufferedImage.TYPE_INT_RGB);
                        image.getGraphics().drawImage(scaledImage, 0, 0, null);
                        ImageIO.write(image, "jpeg", imageStream);
                    }
                    imgBytes = imageStream.toByteArray();
                }
            };
        }
    }

    public void reset() {
        imgBytes = null;
        mainLabel.setIcon(createImageIcon(Color.WHITE, initialWidth, initialHeight));
    }

    public void set(byte[] imgBytes) {
        this.imgBytes = imgBytes;
        mainLabel.setIcon(new ImageIcon(imgBytes));
        mainLabel.setPreferredSize(new Dimension(initialWidth, initialHeight));
        mainLabel.setMaximumSize(new Dimension(initialWidth, initialHeight));
    }

    public byte[] getImgBytes() {
        return imgBytes;
    }
}
