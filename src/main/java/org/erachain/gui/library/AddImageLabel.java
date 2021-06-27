package org.erachain.gui.library;

import org.erachain.core.item.ItemCls;
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
    private byte[] mediaBytes;
    private int mediaType;
    private int baseWidth;
    private int baseHeight;
    private Logger logger = LoggerFactory.getLogger(getClass());
    private JLabel label;
    private JLabel labelSize = new JLabel();
    private JLabel mainLabel = new JLabel();
    public JTextField externalURL = new JTextField();
    public JComboBox externalURLType = new JComboBox(new String[]{Lang.T("Image"), Lang.T("Video")});

    private boolean editable = true;

    public AddImageLabel(String text, int baseWidth, int baseHeight, int minSize, int maxSize, int initialWidth, int initialHeight, boolean originalSize, boolean useExtURL) {
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

        JButton externalURLCheck = new JButton(Lang.T("Check URL"));
        if (useExtURL) {
            JPanel panelCenter = new JPanel();
            panelCenter.setLayout(new BorderLayout());
            add(panelCenter, BorderLayout.CENTER);

            panelCenter.add(new JLabel(Lang.T("Use URL") + ":"), BorderLayout.NORTH);
            panelCenter.add(externalURLType, BorderLayout.EAST);
            externalURL.setToolTipText(Lang.T("AddImageLabel.externalURL.tip"));
            panelCenter.add(externalURL, BorderLayout.CENTER);
            panelCenter.add(externalURLCheck, BorderLayout.SOUTH);
        }

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

                mediaBytes = null;
                labelSize.setText(Lang.T("Source by URL") + " - 0kB");
                reset();

                String urlTxt = externalURL.getText();
                try {
                    url = new URL(urlTxt);
                } catch (MalformedURLException e) {
                    JOptionPane.showMessageDialog(new JFrame(), Lang.T("Invalid URL") + "!", Lang.T("Error"), JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (urlTxt.toLowerCase().endsWith(".mp4")) {
                    externalURLType.setSelectedIndex(ItemCls.MEDIA_TYPE_VIDEO);
                } else if (urlTxt.toLowerCase().endsWith(".mp3")) {
                    externalURLType.setSelectedIndex(ItemCls.MEDIA_TYPE_AUDIO);
                } else if (urlTxt.toLowerCase().endsWith(".gif")
                        || urlTxt.toLowerCase().endsWith(".png")
                        || urlTxt.toLowerCase().endsWith(".jpg")) {
                    externalURLType.setSelectedIndex(ItemCls.MEDIA_TYPE_IMG);
                } else {
                    JOptionPane.showMessageDialog(new JFrame(), Lang.T("Invalid type") + "! "
                            + Lang.T("Need # нужно") + ": .jpg, .gif, .png, .mp4, .mp3", Lang.T("Error"), JOptionPane.ERROR_MESSAGE);
                    return;
                }

                JOptionPane.showMessageDialog(new JFrame(), Lang.T("URL is valid") + ". "
                        + Lang.T("Please set valid media type"), Lang.T("Message"), JOptionPane.INFORMATION_MESSAGE);

                if (false) {
                    // иногда ссылка не читается даже у JPG
                    if (externalURLType.getSelectedIndex() == 0) {
                        if (externalURL.getText().toLowerCase().endsWith(".gif")
                                || externalURL.getText().toLowerCase().endsWith(".png")) {
                            mainLabel.setIcon(new ImageIcon(url));
                        } else {
                            mainLabel.setIcon(ImagesTools.resizeMaxWidth(new ImageIcon(url), 250));
                        }
                    } else {
                        mainLabel.setIcon(createEmptyImage(Color.WHITE, initialWidth, initialHeight));
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
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Media", "mp4", "png", "jpg", "gif", "mp3");
        chooser.setFileFilter(filter);
        chooser.setDialogTitle(Lang.T("Open Media") + "...");
        int returnVal = chooser.showOpenDialog(getParent());
        if (returnVal == JFileChooser.APPROVE_OPTION) {

            File file = new File(chooser.getSelectedFile().getPath());
            if (file.getName().toLowerCase().endsWith("mp4")) {
                // VIDEO
                try {
                    mediaBytes = Files.readAllBytes(chooser.getSelectedFile().toPath());
                    mediaType = ItemCls.MEDIA_TYPE_VIDEO;
                    labelSize.setText(Lang.T("Size") + ": " + (mediaBytes.length >> 10) + " kB");
                    mainLabel.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage("images/icons/video.png")));
                } catch (Exception e) {
                }
            } else if (file.getName().toLowerCase().endsWith("mp3")) {
                // AUDIO
                try {
                    mediaBytes = Files.readAllBytes(chooser.getSelectedFile().toPath());
                    mediaType = ItemCls.MEDIA_TYPE_AUDIO;
                    labelSize.setText(Lang.T("Size") + ": " + (mediaBytes.length >> 10) + " kB");
                    mainLabel.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage("images/icons/audio.png")));
                } catch (Exception e) {
                }

            } else {
                // IMAGE
                mediaType = ItemCls.MEDIA_TYPE_IMG;
                new ImageCropDialog(file, baseWidth, baseHeight,
                        file.getName().toLowerCase().endsWith("gif") || file.getName().toLowerCase().endsWith("png") ?
                                TypeOfImage.GIF : TypeOfImage.JPEG,
                        originalSize) {
                    @Override
                    public void onFinish(BufferedImage bufferedImage, TypeOfImage typeOfImage, boolean useOriginal) {

                        externalURL.setText("");

                        if (useOriginal) {
                            URL url;
                            try {
                                url = new URL("file", "", chooser.getSelectedFile().getPath());
                                mediaBytes = Files.readAllBytes(chooser.getSelectedFile().toPath());
                                labelSize.setText(Lang.T("Size") + ": " + (mediaBytes.length >> 10) + " kB");
                            } catch (Exception e) {
                                url = null;
                            }

                            if (chooser.getSelectedFile().getPath().toLowerCase().endsWith(".gif")
                                    || chooser.getSelectedFile().getPath().toLowerCase().endsWith(".png")) {
                                mainLabel.setIcon(new ImageIcon(url));
                                ///mainLabel.setPreferredSize(new Dimension(100, 100));
                            } else {
                                mainLabel.setIcon(ImagesTools.resizeMaxWidth(new ImageIcon(url), 250));
                            }
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

                            mediaBytes = imageStream.toByteArray();

                            int templWidth = bufferedImage.getWidth();
                            int templHeight = bufferedImage.getHeight();
                            int counter = 0;

                            if (false && minSize > 0 && mediaBytes.length < minSize) {
                                while (mediaBytes.length < minSize && counter++ < 100) {
                                    imageStream.reset();
                                    templWidth *= 1.2;
                                    templHeight *= 1.2;
                                    Image scaledImage = bufferedImage.getScaledInstance(templWidth, templHeight, Image.SCALE_AREA_AVERAGING);
                                    writeImage(imageStream, templWidth, templHeight, scaledImage, typeOfImage);
                                }

                            } else if (false && maxSize > 0 && mediaBytes.length > maxSize) {
                                while (mediaBytes.length > maxSize && counter++ < 100) {
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

                            labelSize.setText(Lang.T("Size") + ": " + (mediaBytes.length >> 10) + " kB");

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
                        mediaBytes = imageStream.toByteArray();
                    }
                };
            }
        }
    }

    public void reset() {
        mediaBytes = null;
        mainLabel.setIcon(createEmptyImage(Color.WHITE, initialWidth, initialHeight));
    }

    public void set(byte[] imgBytes) {
        this.mediaBytes = imgBytes;
        mainLabel.setIcon(new ImageIcon(imgBytes));
    }

    public boolean isInternalMedia() {
        return externalURL.getText().isEmpty();
    }

    public byte[] getMediaBytes() {
        if (isInternalMedia())
            return mediaBytes;
        return externalURL.getText().getBytes(StandardCharsets.UTF_8);
    }

    public int getMediaType() {
        if (isInternalMedia())
            return mediaType;
        return externalURLType.getSelectedIndex();
    }

}
