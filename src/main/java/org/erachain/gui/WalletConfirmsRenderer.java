package org.erachain.gui;

import org.erachain.utils.GUIUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class WalletConfirmsRenderer extends DefaultTableCellRenderer {

    private final DefaultTableCellRenderer adaptee = new DefaultTableCellRenderer();

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus, int row, int column) {

        adaptee.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        JLabel cell = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, false, row, column);

        cell.setHorizontalAlignment(SwingConstants.CENTER);
        cell.setBackground(adaptee.getBackground());
        //cell.set

        Object isOutcome = table.getValueAt(row, WalletTableRenderer.COLUMN_IS_OUTCOME);
        Color color;
        if (isOutcome != null && (boolean) isOutcome) {
            color = adaptee.getForeground();
        } else {
            color = WalletTableRenderer.FORE_COLOR;
        }

        if (value != null) {
            int sizeRow = table.getRowHeight();
            setHorizontalAlignment(JLabel.CENTER);
            Integer intValue = (Integer) value;
            if (intValue == 0) {
                setIcon(GUIUtils.createIconArc(sizeRow, 0, color));
            } else if (intValue < 2) {
                setIcon(GUIUtils.createIconArc(sizeRow, 1, color));
            } else if (intValue < 6) {
                setIcon(GUIUtils.createIconArc(sizeRow, 2, color));
            } else {
                setIcon(GUIUtils.createIcon(sizeRow, color, null));
            }

        }

        return cell;
    }

    @Override
    protected void setValue(Object value) {
    }

}
