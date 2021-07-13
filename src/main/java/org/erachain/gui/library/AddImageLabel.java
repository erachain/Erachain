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
import javax.ws.rs.core.MediaType;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
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
    private JScrollPane jScrollImage = new JScrollPane();

    public JTextField externalURL = new JTextField();
    public JComboBox externalURLType = new JComboBox(new String[]{Lang.T("Image"), Lang.T("Video"), Lang.T("Audio")});

    private boolean editable = true;
    Image emptyImage;

    public AddImageLabel(String text, int baseWidth, int baseHeight, int minSize, int maxSize, int initialWidth,
                         int initialHeight, boolean originalSize, boolean useExtURL, Image emptyImage) {

        this.emptyImage = emptyImage;

        JPanel panelScroll = new JPanel();
        panelScroll.add(mainLabel);

        //mainLabel.setMinimumSize(new Dimension(initialWidth - 50, initialHeight - 50));
        //mainLabel.setPreferredSize(new Dimension(initialWidth + 50, initialHeight + 50));
        //mainLabel.setMaximumSize(new Dimension(initialWidth << 1, initialHeight << 1));
        //jScrollImage.setMinimumSize(new Dimension(initialWidth - 50, initialHeight - 50));
        //jScrollImage.setPreferredSize(new Dimension(initialWidth + 250, initialHeight + 250));
        //jScrollImage.setMaximumSize(new Dimension(initialWidth << 1, initialHeight << 1));
        jScrollImage.setViewportView(panelScroll);

        //setLayout(new BorderLayout());
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        //panelTop.setCursor(new Cursor(Cursor.HAND_CURSOR));
        this.text = text;
        label = new JLabel(text, SwingConstants.CENTER);
        add(label);
        add(jScrollImage);
        add(labelSize);

        JButton externalURLCheck = new JButton(Lang.T("Check URL"));
        if (useExtURL) {
            JPanel panelURLLine1 = new JPanel();
            panelURLLine1.setLayout(new BoxLayout(panelURLLine1, BoxLayout.X_AXIS));

            panelURLLine1.add(new JLabel(Lang.T("Use URL") + ":"));
            panelURLLine1.add(externalURLType);
            add(panelURLLine1);

            externalURL.setToolTipText(Lang.T("AddImageLabel.externalURL.tip"));
            add(externalURL);
            add(externalURLCheck);

        }

        this.baseWidth = baseWidth;
        this.baseHeight = baseHeight;
        this.initialWidth = initialWidth;
        this.initialHeight = initialHeight;
        mainLabel.setIcon(createEmptyImage(Color.WHITE, this.initialWidth, this.initialHeight));

        setBorder(BorderFactory.createEtchedBorder());
        mainLabel.setVerticalAlignment(SwingConstants.TOP);
        mainLabel.setHorizontalAlignment(SwingConstants.CENTER);

        jScrollImage.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (editable) {
                    if (e.getButton() == MouseEvent.BUTTON1) {
                        addImage(minSize, maxSize, originalSize);
                    }
                }
            }
        });
        jScrollImage.addMouseListener(new MouseAdapter() {
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
                    JOptionPane.showMessageDialog(new JFrame(), Lang.T("Invalid URL")
                            + " '" + e.getMessage() + "'!", Lang.T("Error"), JOptionPane.ERROR_MESSAGE);
                    return;
                }

                MediaType mediaTypeResp;
                int mediaTypeRespInt;
                try {
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod("GET");
                    //int result = con.getResponseCode();
                    mediaTypeResp = MediaType.valueOf(con.getHeaderField("Content-Type"));
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(new JFrame(), Lang.T("Connection error")
                            + " '" + e.getMessage() + "'!", Lang.T("Error"), JOptionPane.ERROR_MESSAGE);
                    return;
                }

                String mediaTypeMainResp = mediaTypeResp.getType();
                if (mediaTypeMainResp.equals("video")) {
                    mediaTypeRespInt = ItemCls.MEDIA_TYPE_VIDEO;
                } else if (mediaTypeMainResp.equals("image")) {
                    mediaTypeRespInt = ItemCls.MEDIA_TYPE_IMG;
                } else if (mediaTypeMainResp.equals("audio")) {
                    mediaTypeRespInt = ItemCls.MEDIA_TYPE_AUDIO;
                } else {
                    JOptionPane.showMessageDialog(new JFrame(), Lang.T("Invalid type") + "! "
                            + Lang.T("Need # нужно") + ": .jpg, .gif, .png, .mp4, .mp3", Lang.T("Error"), JOptionPane.ERROR_MESSAGE);
                    return;
                }

                externalURLType.setSelectedIndex(mediaTypeRespInt);

                JOptionPane.showMessageDialog(new JFrame(), Lang.T("URL is valid") + ": "
                        + mediaTypeResp.toString(), Lang.T("Message"), JOptionPane.INFORMATION_MESSAGE);

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
        if (true) {
            return new ImageIcon(emptyImage);
        } else {
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics = image.createGraphics();
            graphics.setPaint(color);
            graphics.fillRect(0, 0, width, height);
            return new ImageIcon(image);
        }
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
                        //preferredWidth = 250;

                        ImageIcon imageIcon;
                        // под размеры поля подгоним чтобы поле не обрезало картинку
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

                            labelSize.setText(Lang.T("Size") + ": " + (mediaBytes.length >> 10) + " kB");

                        } catch (Exception e) {
                            logger.error("Can not write image in ImageCropDialog dialog onFinish method", e);
                        }

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
