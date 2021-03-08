package org.erachain.gui.items;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.RasterFormatException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class ImageCropDisplayPanelNavigator2D extends JPanel {
    private int cropY;
    private final int cropHeight;
    private int cropX;
    private int cropWidth;
    private BufferedImage image;

    private java.util.List<ChangeListener> zoomListeners = new ArrayList<>();

    private double zoom = 1;
    private final int originalCropWidth;

    private AffineTransform currentTransform = new AffineTransform();
    private Point currentPoint = new Point();


    private Logger logger = LoggerFactory.getLogger(ImageCropDisplayPanelNavigator2D.class);
    private boolean flag = false;

    public ImageCropDisplayPanelNavigator2D(ImageCropPanelNavigator2D parent, File imageFile, int cropWidth, int cropHeight) {

        setPreferredSize(new Dimension((int) (cropWidth * 2.0f), (int) (cropHeight * 1.5f)));
        this.cropWidth = cropWidth;
        this.originalCropWidth = cropWidth;
        this.cropHeight = cropHeight;
        cropX = getPreferredSize().width / 2 - cropWidth / 2;
        cropY = getPreferredSize().height / 2 - cropHeight / 2;
        try {
            image = ImageIO.read(imageFile);
        } catch (IOException e) {
            logger.error("Error read image File in crop component", e);
        }
        AffineTransform newTransformBegin = new AffineTransform();
        newTransformBegin.concatenate(AffineTransform.getTranslateInstance(
                -image.getWidth() / 2 + cropX + cropWidth / 2,
                -image.getHeight() / 2 + cropY + cropHeight / 2));
        newTransformBegin.concatenate(currentTransform);
        currentTransform = newTransformBegin;
        addMouseListener(new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON3) {
                    flag = true;
                    return;
                }
                currentPoint = e.getPoint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON3) {
                    flag = false;
                    return;
                }
                int button = e.getButton();
                Point newPoint = e.getPoint();
                currentPoint = newPoint;
                // сброс только масштаба по средней кнопке мыши
                if (button == MouseEvent.BUTTON2) {
                    AffineTransform newTransform = new AffineTransform();
                    newTransform.concatenate(AffineTransform.getTranslateInstance(newPoint.getX(), newPoint.getY()));
                    newTransform.concatenate(AffineTransform.getScaleInstance(1d / currentTransform.getScaleX(), 1d / currentTransform.getScaleY()));
                    newTransform.concatenate(AffineTransform.getTranslateInstance(-newPoint.getX(), -newPoint.getY()));
                    newTransform.concatenate(currentTransform);
                    currentTransform = newTransform;
                }
            }
        });
        addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (flag) {
                    return;
                }
                Point newPoint = e.getPoint();
                Point2D deltaPoint = new Point2D.Double(newPoint.getX() - currentPoint.getX(), newPoint.getY() - currentPoint.getY());
                currentPoint = newPoint;
                AffineTransform newTransform = new AffineTransform();
                newTransform.concatenate(AffineTransform.getTranslateInstance(deltaPoint.getX(), deltaPoint.getY()));
                newTransform.concatenate(currentTransform);
                currentTransform = newTransform;
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                if (flag) {
                    return;
                }
                try {
                    currentPoint = e.getPoint();
                } catch (Throwable exception) {
                    logger.error("Mouse moved error", exception);
                }
            }
        });
        addMouseWheelListener(e -> {
            try {
                int wheelRotation = e.getWheelRotation();
                double scale;
                if (wheelRotation > 0) {
                    scale = 0.9d;
                } else if (wheelRotation < 0) {
                    scale = 1.1d;
                } else {
                    scale = 1d;
                }
                if (zoom < 0.05 && wheelRotation > 0) {
                    return;
                }
                zoom *= scale;
                parent.zoomSlider.setValue((int) (zoom * 100d));

                // тут смещаем из центра - мышка это центр
                AffineTransform newTransform = new AffineTransform();
                newTransform.concatenate(AffineTransform.getTranslateInstance(currentPoint.getX(), currentPoint.getY()));
                newTransform.concatenate(AffineTransform.getScaleInstance(scale, scale));
                newTransform.concatenate(AffineTransform.getTranslateInstance(-currentPoint.getX(), -currentPoint.getY()));
                newTransform.concatenate(currentTransform);
                currentTransform = newTransform;
            } catch (Throwable exception) {
                logger.error("Wheel rotation error", exception);
            }
        });
        Timer timer = new Timer(100, e -> {
            repaint();
        });
        timer.setRepeats(true);
        timer.start();
    }


    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D graphics2D = (Graphics2D) g;
        graphics2D.transform(currentTransform);
        graphics2D.drawImage(image, 0, 0, null);
        graphics2D.setTransform(new AffineTransform());
        drawFrame(graphics2D);
    }

    private void drawFrame(Graphics2D g2d) {
        drawRect(g2d, cropX, cropY, cropWidth, cropHeight, Color.BLACK);
        drawRect(g2d, cropX - 1, cropY - 1, cropWidth + 2, cropHeight + 2, Color.WHITE);
        drawRect(g2d, cropX - 2, cropY - 2, cropWidth + 4, cropHeight + 4, Color.WHITE);
        drawRect(g2d, cropX - 3, cropY - 3, cropWidth + 6, cropHeight + 6, Color.WHITE);
        drawRect(g2d, cropX - 4, cropY - 4, cropWidth + 8, cropHeight + 8, Color.BLACK);
    }


    private void drawRect(Graphics2D g2d, int x, int y, int width, int height, Color color) {
        g2d.setColor(color);
        g2d.drawRect(x, y, width, height);
    }


    public BufferedImage getSnapshot(TypeOfImage typeOfImage) {
        Point2D.Double pointSrcRightBottomImage = new Point2D.Double(image.getWidth(), image.getHeight());
        Point2D.Double pointDstRightBottomImage = new Point2D.Double();
        currentTransform.transform(pointSrcRightBottomImage, pointDstRightBottomImage);
        int type = -1;
        if (typeOfImage == TypeOfImage.JPEG) {
            type = BufferedImage.TYPE_INT_RGB;
        } else if (typeOfImage == TypeOfImage.GIF) {
            type = BufferedImage.TYPE_INT_ARGB;
        }
        BufferedImage snapshot = new BufferedImage((int) pointDstRightBottomImage.getX(), (int) pointDstRightBottomImage.getY(), type);
        Graphics2D g2d = (Graphics2D) snapshot.getGraphics();
        g2d.transform(currentTransform);
        if (typeOfImage == TypeOfImage.JPEG) {
            g2d.drawImage(image, 0, 0, Color.WHITE, this);
        } else if (typeOfImage == TypeOfImage.GIF) {
            g2d.drawImage(image, 0, 0, this);
        }
        Point2D.Double zeroPoint = new Point2D.Double(0, 0);
        Point2D.Double pointZeroDst = new Point2D.Double();
        currentTransform.transform(zeroPoint, pointZeroDst);

        Point2D.Double cropPointRightBottom = new Point2D.Double(cropX + cropWidth, cropY + cropHeight);
        Point2D.Double pointCropDstRightBottom = new Point2D.Double();
        currentTransform.transform(cropPointRightBottom, pointCropDstRightBottom);
        int shift = 2;
        try {

            int poinX = cropX;
            int poinY = cropY;
            int width = cropWidth;
            int height = cropHeight;

            if (pointZeroDst.x > cropX) {
                poinX = (int) pointZeroDst.x;
                width = cropWidth - poinX + cropX;
            }
            if (pointZeroDst.y > cropY) {
                poinY = (int) pointZeroDst.y;
                height = cropHeight - poinY + cropY;
            }

            if (pointDstRightBottomImage.x < poinX + width)
                width = (int) pointDstRightBottomImage.x - poinX;

            if (pointDstRightBottomImage.y < poinY + height)
                height = (int) pointDstRightBottomImage.y - poinY;

            return snapshot.getSubimage(poinX, poinY, width, height);

        } catch (RasterFormatException e) {
            logger.error("Error size of sub image", e);
            return snapshot.getSubimage((int) pointZeroDst.x + shift, (int) pointZeroDst.y + shift,
                    snapshot.getWidth() - (int) pointZeroDst.x - shift,
                    snapshot.getHeight() - (int) pointZeroDst.y - shift);
        }

    }

    public void setFrameRate(int value) {
        cropWidth = originalCropWidth - originalCropWidth * value / 100;
        cropX = getPreferredSize().width / 2 - cropWidth / 2;
        //moveImageBy(0, 0);
    }


    public double getZoom() {
        return zoom;
    }

    public void setZoom(double new_zoom) {
        if (new_zoom < 0.05) {
            new_zoom = 0.05;
        }

        double scale = new_zoom / this.zoom;
        this.zoom = new_zoom;

        // тут не смещаем из центра
        AffineTransform newTransform = new AffineTransform();
        newTransform.concatenate(AffineTransform.getTranslateInstance(getPreferredSize().width / 2, getPreferredSize().height / 2));
        newTransform.concatenate(AffineTransform.getScaleInstance(scale, scale));
        newTransform.concatenate(AffineTransform.getTranslateInstance(-getPreferredSize().width / 2, -getPreferredSize().height / 2));
        newTransform.concatenate(currentTransform);
        currentTransform = newTransform;
        repaint();
    }

    public void addZoomListener(ChangeListener listener) {
        zoomListeners.add(listener);
    }

}
