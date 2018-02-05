package gui.items;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class ImageCropDisplayPanel extends JPanel {
    private final int cropX;
    private final int cropY;
    private final int cropWidth;
    private final int cropHeight;
    private BufferedImage image;
    private int imageX;
    private int imageY;
    private int imageDragX = 0;
    private int imageDragY = 0;
    private double zoom = 1;
    private java.util.List<ChangeListener> zoomListeners = new ArrayList<>();


    public ImageCropDisplayPanel(File imageFile, int cropWidth, int cropHeight) {
        super();

        setPreferredSize(new Dimension(600, 500));

        this.cropWidth = cropWidth;
        this.cropHeight = cropHeight;
        this.cropX = getPreferredSize().width / 2 - cropWidth / 2;
        this.cropY = getPreferredSize().height / 2 - cropHeight / 2;

        try {
            image = ImageIO.read(imageFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

        imageX = -image.getWidth() / 2 + cropX + cropWidth / 2;
        imageY = -image.getHeight() / 2 + cropY + cropHeight / 2;

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                imageDragX = e.getX();
                imageDragY = e.getY();
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                setCursor(new Cursor(Cursor.MOVE_CURSOR));
            }


            @Override
            public void mouseDragged(MouseEvent e) {
                moveImageBy((e.getX() - imageDragX) / zoom, (e.getY() - imageDragY) / zoom);
                imageDragX = e.getX();
                imageDragY = e.getY();
            }
        });

        addMouseWheelListener(e -> {
            zoom += -e.getWheelRotation() / 10d;
            zoomListeners.forEach(changeListener -> changeListener.stateChanged(new ChangeEvent(this)));
            moveImageBy(0, 0);
        });
    }


    private void moveImageBy(double deltaX, double deltaY)
    {
        imageX += deltaX;
        imageY += deltaY;
//        imageX = between(imageX + deltaX, (cropX + cropWidth - image.getWidth()) / zoom, cropX / zoom);
//        imageY = between(imageY + deltaY, (cropY + cropHeight - image.getHeight()) / zoom, cropY / zoom);
        repaint();
    }


    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        // Backup original transform
        AffineTransform originalTransform = g2d.getTransform();

        g2d.scale(zoom, zoom);

        // paint the image here with no scaling
        g2d.drawImage(image, imageX, imageY, this);

        // Restore original transform
        g2d.setTransform(originalTransform);

        drawFrame(g2d);
    }


    private void drawFrame(Graphics2D g2d)
    {
        drawRect(g2d, cropX, cropY, cropWidth, cropHeight, Color.BLACK);
        drawRect(g2d, cropX - 1, cropY - 1, cropWidth + 2, cropHeight + 2, Color.WHITE);
        drawRect(g2d, cropX - 2, cropY - 2, cropWidth + 4, cropHeight + 4, Color.WHITE);
        drawRect(g2d, cropX - 3, cropY - 3, cropWidth + 6, cropHeight + 6, Color.WHITE);
        drawRect(g2d, cropX - 4, cropY - 4, cropWidth + 8, cropHeight + 8, Color.BLACK);
    }


    private void drawRect(Graphics2D g2d, int x, int y, int width, int height, Color color)
    {
        g2d.setColor(color);
        g2d.drawRect(x, y, width, height);
    }


    public void setZoom(double zoom)
    {
        this.zoom = zoom;
        moveImageBy(0, 0);
    }


    public BufferedImage getSnapshot() {
        BufferedImage snapshot = new BufferedImage(Math.max(image.getWidth(), cropX + cropWidth), Math.max(image.getHeight(), cropY + cropHeight), image.getType());
        Graphics2D g2d = (Graphics2D)snapshot.getGraphics();
        g2d.scale(zoom, zoom);
        g2d.drawImage(image, imageX, imageY, this);
        return snapshot.getSubimage(cropX, cropY, cropWidth, cropHeight);
    }


    private int between(double value, double min, double max)
    {
        return (int)Math.min(Math.max(value, min), max);
    }


    public void addZoomListener(ChangeListener listener) {
        zoomListeners.add(listener);
    }


    public double getZoom() {
        return zoom;
    }
}
