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

    public AddImageLabel(String text, int bezelWidth, int bezelHeight, int minSize, int maxSize, int initialWidth, int initialHeight, boolean originalSize) {
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
        mainLabel.setIcon(createEmptyImage(Color.WHITE, this.initialWidth, this.initialHeight));
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setBorder(BorderFactory.createEtchedBorder());
        mainLabel.setVerticalAlignment(SwingConstants.TOP);
        mainLabel.setHorizontalAlignment(SwingConstants.CENTER);
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (editable) {
                    if (e.getButton() == MouseEvent.BUTTON1) {
                        addImage(minSize, maxSize, originalSize);
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

    private ImageIcon createEmptyImage(Color color, int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setPaint(color);
        graphics.fillRect(0, 0, width, height);
        return new ImageIcon(image);
    }

    private void addImage(int minSize, int maxSize, boolean originalSize) {
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
            new ImageCropDialog(file, bezelWidth, bezelHeight,
                    file.getName().endsWith("jpg") || file.getName().endsWith("jpeg") ?
                            TypeOfImage.JPEG : TypeOfImage.GIF,
                    originalSize) {
                @Override
                public void onFinish(BufferedImage bufferedImage, TypeOfImage typeOfImage) {
                    if (bufferedImage == null) {
                        logger.error("Image does not setup");
                        return;
                    }

                    int bufferedWidth = bufferedImage.getWidth();
                    int preferredWidth = mainLabel.getPreferredSize().width;
                    preferredWidth = 250;

                    ImageIcon imageIcon;
                    // под размеры поля подгоним чтобы поле не обрезало каритнку
                    if (bufferedWidth > preferredWidth) {
                        float scaleView = (float) preferredWidth / bufferedWidth;
                        Image imagePack = bufferedImage.getScaledInstance(preferredWidth,
                                (int) (scaleView * bufferedImage.getHeight()),
                                Image.SCALE_AREA_AVERAGING);
                        imageIcon = new ImageIcon(imagePack);
                    } else {
                        imageIcon = new ImageIcon(bufferedImage);
                    }

                    mainLabel.setIcon(imageIcon);

                    ByteArrayOutputStream imageStream = new ByteArrayOutputStream();

                    try {
                        if (typeOfImage == TypeOfImage.GIF) {
                            ImageIO.write(bufferedImage, "gif", imageStream);
                        } else {
                            ImageIO.write(bufferedImage, "jpeg", imageStream);
                        }

                        imgBytes = imageStream.toByteArray();

                        int templWidth = bufferedImage.getWidth();
                        int templHeight = bufferedImage.getHeight();
                        int counter = 0;

                        if (false && minSize > 0 && imgBytes.length < minSize) {
                            while (imgBytes.length < minSize && counter++ < 100) {
                                imageStream.reset();
                                templWidth *= 1.2;
                                templHeight *= 1.2;
                                Image scaledImage = bufferedImage.getScaledInstance(templWidth, templHeight, Image.SCALE_AREA_AVERAGING);
                                writeImage(imageStream, templWidth, templHeight, scaledImage, typeOfImage);
                            }

                        } else if (false && maxSize > 0 && imgBytes.length > maxSize) {
                            while (imgBytes.length > maxSize && counter++ < 100) {
                                imageStream.reset();
                                templWidth /= 1.2;
                                templHeight /= 1.2;
                                Image scaledImage = bufferedImage.getScaledInstance(templWidth, templHeight, Image.SCALE_AREA_AVERAGING);
                                writeImage(imageStream, templWidth, templHeight, scaledImage, typeOfImage);
                            }

                        } else if (false && typeOfImage == TypeOfImage.JPEG) {
                            // преобразуем GIF с прозрачным фоном в непрозрачный если надо
                            // это может понадобиться если Оригинальный размер картинки взяли и не было преобразования в snapshot в ImageCropDisplayPanelNavigator2D.getSnapshot
                            Image scaledImage = bufferedImage.getScaledInstance(templWidth, templHeight, Image.SCALE_AREA_AVERAGING);
                            writeImage(imageStream, templWidth, templHeight, scaledImage, typeOfImage);
                        }

                        label.setText(Lang.T("Size") + ": " + (imgBytes.length >> 10) + " kB");

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
                        image.getGraphics().drawImage(scaledImage, 0, 0, Color.WHITE, null);
                        ImageIO.write(image, "jpeg", imageStream);
                    }
                    imgBytes = imageStream.toByteArray();
                }
            };
        }
    }

    public void reset() {
        imgBytes = null;
        mainLabel.setIcon(createEmptyImage(Color.WHITE, initialWidth, initialHeight));
    }

    public void set(byte[] imgBytes) {
        this.imgBytes = imgBytes;
        mainLabel.setIcon(new ImageIcon(imgBytes));
    }

    public byte[] getImgBytes() {
        return imgBytes;
    }
}
