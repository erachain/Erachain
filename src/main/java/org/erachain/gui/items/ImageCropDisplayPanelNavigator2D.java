package org.erachain.gui.items;

import com.sun.corba.se.impl.oa.toa.TOAImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.RasterFormatException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class ImageCropDisplayPanelNavigator2D extends JPanel {
    private final int cropY;
    private final int cropHeight;
    private int cropX;
    private int cropWidth;
    private BufferedImage image;


    private AffineTransform currentTransform = new AffineTransform();
    private Point currentPoint = new Point();


    private Logger logger = LoggerFactory.getLogger(ImageCropDisplayPanelNavigator2D.class.getName());

    public ImageCropDisplayPanelNavigator2D(File imageFile, int cropWidth, int cropHeight) {
        setPreferredSize(new Dimension(600, 500));
        this.cropWidth = cropWidth;
        this.cropHeight = cropHeight;
        cropX = getPreferredSize().width / 2 - cropWidth / 2;
        cropY = getPreferredSize().height / 2 - cropHeight / 2;
        try {
            image = ImageIO.read(imageFile);
        } catch (IOException e) {
            logger.error("Error read image File in crop component", e);
        }
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                currentPoint = e.getPoint();
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                int button = e.getButton();
                Point newPoint = e.getPoint();
                currentPoint = newPoint;
                // сброс всего в исходное положение по правой кнопке мыши
                if (button == MouseEvent.BUTTON3) {
                    currentTransform = new AffineTransform();
                }
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

        try {

            if (pointZeroDst.x > cropPointRightBottom.x
                    || pointZeroDst.y > cropPointRightBottom.y
                    || pointDstRightBottomImage.x < cropX
                    || pointDstRightBottomImage.y < cropY) {
                return null;
            }
            if (pointZeroDst.x < cropX && pointZeroDst.y > cropY
                    && pointDstRightBottomImage.x > cropPointRightBottom.x
                    && pointDstRightBottomImage.y < cropPointRightBottom.y) {
                return snapshot.getSubimage(cropX, (int) pointZeroDst.y, cropWidth, (int) (cropPointRightBottom.y - pointZeroDst.y));
            } else if (pointZeroDst.x < cropX && pointZeroDst.y > cropY
                    && pointDstRightBottomImage.x < cropPointRightBottom.x
                    && pointDstRightBottomImage.y < cropPointRightBottom.y) {
                return snapshot.getSubimage(cropX, (int) pointZeroDst.y,
                        (int) (pointDstRightBottomImage.x - cropX), (int) (pointDstRightBottomImage.y - pointZeroDst.y));
            } else if (pointZeroDst.x > cropX && pointZeroDst.y > cropY
                    && pointDstRightBottomImage.x > cropPointRightBottom.x
                    && pointDstRightBottomImage.y < cropPointRightBottom.y) {
                return snapshot.getSubimage((int) pointZeroDst.x, (int) pointZeroDst.y,
                        (int) (cropPointRightBottom.x - pointZeroDst.x), (int) (pointDstRightBottomImage.y - pointZeroDst.y));
            }
            if (pointZeroDst.x > cropX && pointZeroDst.y < cropY
                    && pointDstRightBottomImage.x < cropPointRightBottom.x
                    && pointDstRightBottomImage.y > cropPointRightBottom.y) {
                return snapshot.getSubimage((int) pointZeroDst.x, cropY, (int) (cropPointRightBottom.x - pointZeroDst.x), cropHeight);
            } else if (pointZeroDst.x > cropX && pointZeroDst.y < cropY
                    && pointDstRightBottomImage.x < cropPointRightBottom.x
                    && pointDstRightBottomImage.y < cropPointRightBottom.y) {
                return snapshot.getSubimage((int) pointZeroDst.x, cropY,
                        (int) (pointDstRightBottomImage.x - pointZeroDst.x), (int) (pointDstRightBottomImage.y - cropY));
            } else if (pointZeroDst.x > cropX && pointZeroDst.y > cropY
                    && pointDstRightBottomImage.x < cropPointRightBottom.x
                    && pointDstRightBottomImage.y > cropPointRightBottom.y) {
                return snapshot.getSubimage((int) pointZeroDst.x, (int) pointZeroDst.y,
                        (int) (pointDstRightBottomImage.x - pointZeroDst.x), (int) (cropPointRightBottom.y - pointZeroDst.y));
            }
            if (pointZeroDst.x > cropX && pointZeroDst.y > cropY) {
                return snapshot.getSubimage((int) pointZeroDst.x, (int) pointZeroDst.y,
                        (int) (cropPointRightBottom.x - pointZeroDst.x), (int) (cropPointRightBottom.y - pointZeroDst.y));
            } else if (pointZeroDst.x > cropX) {
                return snapshot.getSubimage((int) pointZeroDst.x, cropY,
                        (int) (cropPointRightBottom.x - pointZeroDst.x), cropHeight);

            } else if (pointZeroDst.y > cropY) {
                return snapshot.getSubimage(cropX, (int) pointZeroDst.y,
                        cropWidth, (int) (cropPointRightBottom.y - pointZeroDst.y));
            }
            if (pointDstRightBottomImage.x < cropPointRightBottom.x && pointDstRightBottomImage.y < cropPointRightBottom.y) {
                return snapshot.getSubimage(cropX, cropY,
                        (int) (pointDstRightBottomImage.x - cropX), (int) (pointDstRightBottomImage.y - cropY));
            } else if (pointDstRightBottomImage.x < cropPointRightBottom.x) {
                return snapshot.getSubimage(cropX, cropY,
                        (int) (pointDstRightBottomImage.x - cropX), cropHeight);

            } else if (pointDstRightBottomImage.y < cropPointRightBottom.y) {
                return snapshot.getSubimage(cropX, cropY,
                        cropWidth, (int) (pointDstRightBottomImage.y - cropY));
            }
            return snapshot.getSubimage(cropX, cropY, cropWidth, cropHeight);
        } catch (RasterFormatException e) {
            logger.info("Error size of sub image");
            return snapshot.getSubimage((int) pointZeroDst.x, (int) pointZeroDst.y,
                    snapshot.getWidth() - (int) pointZeroDst.x, snapshot.getHeight() - (int) pointZeroDst.y);
        }

    }

}
