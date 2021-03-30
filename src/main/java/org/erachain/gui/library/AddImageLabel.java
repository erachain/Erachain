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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;


public class AddImageLabel extends JPanel {

    private static final long serialVersionUID = 1L;
    private final int initialWidth;
    private final int initialHeight;
    private final String text;
    private byte[] imgBytes;
    private int baseWidth;
    private int baseHeight;
    private Logger logger = LoggerFactory.getLogger(getClass());
    private JLabel label;
    private JLabel labelSize = new JLabel();
    private JLabel mainLabel = new JLabel();
    public JTextField externalURL = new JTextField();
    public JComboBox externalURLType = new JComboBox(new String[]{Lang.T("Image"), Lang.T("Video")});

    private boolean editable = true;

    public AddImageLabel(String text, int baseWidth, int baseHeight, int minSize, int maxSize, int initialWidth, int initialHeight, boolean originalSize) {
        setLayout(new BorderLayout());
        JPanel panelTop = new JPanel();
        panelTop.setLayout(new BorderLayout());
        panelTop.setCursor(new Cursor(Cursor.HAND_CURSOR));
        add(panelTop, BorderLayout.NORTH);
        this.text = text;
        label = new JLabel(text, SwingConstants.CENTER);
        panelTop.add(label, BorderLayout.NORTH);
        panelTop.add(mainLabel, BorderLayout.CENTER);
        panelTop.add(labelSize, BorderLayout.SOUTH);

        JPanel panelCenter = new JPanel();
        panelCenter.setLayout(new BorderLayout());
        add(panelCenter, BorderLayout.CENTER);

        panelCenter.add(new JLabel(Lang.T("Use URL") + ":"), BorderLayout.NORTH);
        panelCenter.add(externalURLType, BorderLayout.EAST);
        externalURL.setToolTipText(Lang.T("AddImageLabel.externalURL.tip"));
        panelCenter.add(externalURL, BorderLayout.CENTER);
        JButton externalURLCheck = new JButton(Lang.T("Check URL"));
        panelCenter.add(externalURLCheck, BorderLayout.SOUTH);

        this.baseWidth = baseWidth;
        this.baseHeight = baseHeight;
        this.initialWidth = initialWidth;
        this.initialHeight = initialHeight;
        mainLabel.setIcon(createEmptyImage(Color.WHITE, this.initialWidth, this.initialHeight));

        setBorder(BorderFactory.createEtchedBorder());
        mainLabel.setVerticalAlignment(SwingConstants.TOP);
        mainLabel.setHorizontalAlignment(SwingConstants.CENTER);

        panelTop.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (editable) {
                    if (e.getButton() == MouseEvent.BUTTON1) {
                        addImage(minSize, maxSize, originalSize);
                    }
                }
            }
        });

        externalURLCheck.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                // TODO Auto-generated method stub
                URL url;

                imgBytes = null;
                labelSize.setText("");

                String urlTxt = externalURL.getText();
                try {
                    url = new URL(urlTxt);
                } catch (MalformedURLException e) {
                    reset();
                    JOptionPane.showMessageDialog(new JFrame(), Lang.T("Invalid URL") + "!", Lang.T("Error"), JOptionPane.ERROR_MESSAGE);

                    return;
                }
                ImageIcon imageIcon = new ImageIcon(url);
                BufferedImage bufferedImage = new BufferedImage(imageIcon.getIconWidth(), imageIcon.getIconHeight(), BufferedImage.TYPE_INT_RGB);
                bufferedImage.getGraphics().drawImage(imageIcon.getImage(), 0, 0, null);
                //ImageIO.write(imageIcon, "gif", imageStream);

                int bufferedWidth = bufferedImage.getWidth();
                int preferredWidth = mainLabel.getPreferredSize().width;
                preferredWidth = 250;

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
                mainLabel.setSize(new Dimension(initialWidth, initialHeight));
            }
        });

        JPopupMenu menu = new JPopupMenu();
        JMenuItem resetMenu = new JMenuItem(Lang.T("Reset"));
        resetMenu.addActionListener(e -> reset());
        menu.add(resetMenu);
        setComponentPopupMenu(menu);
        validate();
    }

    public AddImageLabel(Icon image) {

        this.text = "";
        this.initialWidth = this.initialHeight = 1000;

        setLayout(new BorderLayout());
        this.baseHeight = baseHeight;
        mainLabel.setIcon(image);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setBorder(BorderFactory.createEtchedBorder());
        mainLabel.setVerticalAlignment(SwingConstants.TOP);
        mainLabel.setHorizontalAlignment(SwingConstants.CENTER);

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
            new ImageCropDialog(file, baseWidth, baseHeight,
                    file.getName().endsWith("jpg") || file.getName().endsWith("jpeg") ?
                            TypeOfImage.JPEG : TypeOfImage.GIF,
                    originalSize) {
                @Override
                public void onFinish(BufferedImage bufferedImage, TypeOfImage typeOfImage, boolean useOriginal) {

                    externalURL.setText("");

                    if (useOriginal) {
                        URL url;
                        try {
                            url = new URL("file", "", chooser.getSelectedFile().getPath());
                            imgBytes = Files.readAllBytes(chooser.getSelectedFile().toPath());
                            labelSize.setText(Lang.T("Size") + ": " + (imgBytes.length >> 10) + " kB");
                        } catch (Exception e) {
                            url = null;
                        }

                        mainLabel.setIcon(new ImageIcon(url));

                        return;

                    } else {
                        if (bufferedImage == null) {
                            logger.error("Image does not setup");
                            return;
                        }
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

                    try {

                        ByteArrayOutputStream imageStream = new ByteArrayOutputStream();

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

                        labelSize.setText(Lang.T("Size") + ": " + (imgBytes.length >> 10) + " kB");

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
        if (externalURL.getText().isEmpty())
            return imgBytes;
        return externalURL.getText().getBytes(StandardCharsets.UTF_8);
    }
}
