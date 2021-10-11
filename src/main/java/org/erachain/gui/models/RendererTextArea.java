package org.erachain.gui.models;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;


public class RendererTextArea extends JTextArea implements TableCellRenderer {
    private static final long serialVersionUID = 1L;

    public RendererTextArea() {
        setLineWrap(true);
        setWrapStyleWord(true);
        setOpaque(true);
    }

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

        setText((value == null) ? "" : value.toString());

        Graphics g = getGraphics();

        FontMetrics fm = g.getFontMetrics(this.getFont());// g.getFontMetrics();
        int wightString = fm.stringWidth(value.toString());
        // rows
        int rowCount = getRows();
        // ширина в пикселях
        int wightCell = wightString / rowCount;
        table.setRowHeight(row, wightCell);

        return this;
    }
}