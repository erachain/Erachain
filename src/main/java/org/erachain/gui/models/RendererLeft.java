package org.erachain.gui.models;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

public class RendererLeft extends JLabel implements TableCellRenderer {
    private static final long serialVersionUID = 1L;

    public RendererLeft() {
        setOpaque(true);
        setBackground(new Color(255, 255, 220));
        setVerticalAlignment(TOP);
        setHorizontalAlignment(JLabel.LEFT);
        setHorizontalTextPosition(JLabel.LEFT);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table,
                                                   Object value, boolean isSelected, boolean hasFocus, int row, int column) {

        if (value == null || value == "") value = " ";

        if (isSelected) {
            setBackground(SystemColor.blue);
            value = "<HTML><p style='color:#ffffff'><b>" + "&nbsp;&nbsp;&nbsp;" + value;
        } else {
            setBackground(new Color(255, 255, 220));
            value = "<HTML><p style='color:#000000'>" + "&nbsp;&nbsp;&nbsp;" + value;
        }

        if (hasFocus) {
            setBorder(new LineBorder(new Color(99, 130, 191)));
        } else {
            setBorder(new LineBorder(null, 0));
        }

        setText((value == null) ? "" : value + "</></>  ");

        return this;
    }
}