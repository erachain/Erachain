package org.erachain.gui.models;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.TableCellRenderer;
import java.awt.*;


public class Renderer_Boolean extends JCheckBox implements TableCellRenderer {

    private static final Border noFocusBorder = new EmptyBorder(1, 1, 1, 1);

    public Renderer_Boolean() {
        setLayout(new GridBagLayout());
        setMargin(new Insets(0, 0, 0, 0));
        setHorizontalAlignment(JLabel.CENTER);
        setBorder(null);
        setSize(40, 50);
        setBorderPaintedFlat(true);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        if (value instanceof Boolean) {
            setSelected((Boolean) value);
        }
        //  setCursor(new Cursor(Cursor.HAND_CURSOR));
        if (!isSelected) {
            setBackground(new Color(255, 255, 220));
            //setBackground(new Color(0xFFFFFF));
            // if (row % 2 == 1) {
            //     setBackground(new Color(0xE8F2FE)); //light blue
            // }
            // setForeground(Color.black);
        } else {
            setBackground(SystemColor.blue);

            //       setForeground(table.getSelectionForeground());
            //       setBackground(table.getSelectionBackground());
        }
        if (hasFocus) {
            setBorder(new LineBorder(new Color(99, 130, 191)));
            //  setBorder(UIManager.getBorder("Table.focusCellHighlightBorder"));
        } else {
            setBorder(new LineBorder(null, 0));
            // setBorder(noFocusBorder);
        }
        return this;
    }
}