package org.erachain.gui.library;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * https://www.sql.ru/forum/542056/swing-kak-realizovat-vsplyvaushhie-podskazki
 */
public class AuxiliaryToolTip extends JToolTip {
    public static final String IGNORE_TOOLTIP = "#IGNORE_TOOLTIP#";

    private JLabel displayLabel;
    private int fixedHeight = -1;
    private String key;
    private Point storedLocation;

    private boolean redispatchFlag = false;

    public AuxiliaryToolTip() {
        super();
        displayLabel = new JLabel();
        displayLabel.setMinimumSize(new Dimension(300, 200));
        displayLabel.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                redispatchMouseEvent(e);
            }

            public void mousePressed(MouseEvent e) {
                redispatchMouseEvent(e);
            }

            public void mouseReleased(MouseEvent e) {
                redispatchMouseEvent(e);
            }
        });
        displayLabel.setOpaque(true);
        displayLabel.setBackground(getBackground());

        setLayout(new BorderLayout());
        add(displayLabel, BorderLayout.CENTER);
    }

    public int getFixedHeight() {
        return fixedHeight;
    }

    public void setFixedHeight(int fixedHeight) {
        this.fixedHeight = fixedHeight;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Point getStoredLocation() {
        return storedLocation;
    }

    public void setStoredLocation(Point storedLocation) {
        this.storedLocation = storedLocation;
    }

    public void decorateLike(JLabel label) {
        displayLabel.setIcon(label.getIcon());
        displayLabel.setIconTextGap(label.getIconTextGap());
        displayLabel.setText(label.getText());
        displayLabel.setHorizontalAlignment(label.getHorizontalAlignment());
        displayLabel.setHorizontalTextPosition(label.getHorizontalTextPosition());
        displayLabel.setVerticalAlignment(label.getVerticalAlignment());
        displayLabel.setVerticalTextPosition(label.getVerticalTextPosition());
        displayLabel.setFont(label.getFont());

        Insets insets = label.getInsets();
        displayLabel.setBorder(insets == null ? BorderFactory.createEmptyBorder()
                : BorderFactory.createEmptyBorder(insets.top, insets.left, insets.bottom, insets.right));
    }

    public Dimension getPreferredSize() {
        return addInsets(displayLabel.getPreferredSize());
    }

    public Dimension getMinimumSize() {
        return addInsets(displayLabel.getMinimumSize());
    }

    public Dimension getMaximumSize() {
        return addInsets(displayLabel.getMaximumSize(), -1);
    }

    private Dimension addInsets(Dimension d) {
        return addInsets(d, getFixedHeight());
    }

    private Dimension addInsets(Dimension d, int fixedH) {
        if (d == null) {
            return null;
        }

        Insets insets = this.getInsets();
        if (insets == null) {
            return d;
        }

        return new Dimension(d.width + insets.left + insets.right, (fixedH < 0 ? d.height : fixedH) + insets.top + insets.bottom);
    }

    private void redispatchMouseEvent(MouseEvent e) {
        if (redispatchFlag) {
            return;
        }

        redispatchFlag = true;
        try {
            redispatchMouseEventImpl(e);
        } finally {
            redispatchFlag = false;
        }
    }

    private void redispatchMouseEventImpl(MouseEvent e) {
        JComponent destination = getComponent();
        if (destination == null) {
            return;
        }

        if (!(e.getSource() instanceof Component)) {
            return;
        }

        Component source = (Component) e.getSource();
        Point p = SwingUtilities.convertPoint(source, e.getX(), e.getY(), destination);
        if ((p == null) || (p.x < 0) || (p.x >= destination.getWidth()) || (p.y < 0) || (p.y >= destination.getHeight())) {
            return;
        }

        boolean popupTrigger = e.isPopupTrigger();
        if ((e.getID() == MouseEvent.MOUSE_PRESSED) && SwingUtilities.isRightMouseButton(e) && (e.getClickCount() <= 1)) {
            popupTrigger = true;
        }
        MouseEvent fakeEvent = new MouseEvent(destination, e.getID(), e.getWhen(), e.getModifiers(),
                p.x, p.y,
                e.getXOnScreen(),
                e.getYOnScreen(),
                e.getClickCount(),
                popupTrigger,
                e.getButton());
        destination.dispatchEvent(fakeEvent);
    }
}