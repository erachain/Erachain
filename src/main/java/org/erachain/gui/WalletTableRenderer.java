package org.erachain.gui;

import org.erachain.gui.library.MTable;
import org.erachain.gui.models.TimerTableModelCls;
import org.erachain.utils.GUIUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class WalletTableRenderer extends DefaultTableCellRenderer {

    private final DefaultTableCellRenderer adaptee = new DefaultTableCellRenderer();
    static Logger LOGGER = LoggerFactory.getLogger(WalletTableRenderer.class.getName());

    public final static Color FORE_COLOR = new Color(0, 120, 0, 255);

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus, int row, int column) {

        adaptee.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        Component cell;

        if (value instanceof Boolean) {
            Boolean selected = (Boolean) value;

            int sizeRow = table.getRowHeight();

            cell = new JCheckBox(null, GUIUtils.createIcon(sizeRow, selected ?
                    //new Color(38, 90, 30, 255)
                    Color.PINK
                    : Color.GRAY, null), selected);
            //((JCheckBox)cell).setSelected((Boolean) value);
            ((JCheckBox) cell).setHorizontalAlignment(JLabel.CENTER);
            ((JCheckBox) cell).setBorderPainted(true);

        } else {
            cell = super.getTableCellRendererComponent(table, value, isSelected, false, row, column);
        }

        cell.setBackground(adaptee.getBackground());

        if (value instanceof Number) {
            ((JLabel) cell).setHorizontalAlignment(SwingConstants.RIGHT);
        }

        Object isUnViewed = table.getValueAt(row, TimerTableModelCls.COLUMN_UN_VIEWED);
        if (isUnViewed != null && (boolean) isUnViewed) {
            Font font = adaptee.getFont();
            font = new Font(font.getName(), Font.BOLD, font.getSize());
            cell.setFont(font);
        } else {
            cell.setFont(adaptee.getFont());
        }

        Object isOutcome = table.getValueAt(row, TimerTableModelCls.COLUMN_IS_OUTCOME);
        Color color;
        if (isOutcome != null && !(boolean) isOutcome) {
            color = FORE_COLOR;
        } else {
            color = adaptee.getForeground();
        }

        cell.setForeground(color);

        if (column == 0) {
            Integer confirmations = (Integer) table.getValueAt(row, TimerTableModelCls.COLUMN_CONFIRMATIONS);
            if (confirmations != null) {
                int sizeRow = ((MTable) table).iconSize;
                // ● ⚫ ◆ █ ▇ ■ ◢ ◤ ◔ ◑ ◕ ⬛ ⬜ ⬤ ⛃
                if (confirmations == 0) {
                    setIcon(GUIUtils.createIconArc(sizeRow, 0, color));
                } else if (confirmations < 2) {
                    setIcon(GUIUtils.createIconArc(sizeRow, 1, color));
                } else if (confirmations < 6) {
                    setIcon(GUIUtils.createIconArc(sizeRow, 2, color));
                } else {
                    setIcon(GUIUtils.createIcon(sizeRow, color, null));
                }
            }
        }

        return cell;
    }

    @Override
    protected void setValue(Object value) {

        if (value != null) {
            if (value instanceof Iconable) {
                // Get icon to use for the list item value
                Iconable iconable = (Iconable) value;

                byte[] iconBytes = iconable.getIcon();
                if (iconBytes != null && iconBytes.length > 0) {
                    int rowSize = getFont().getSize() + 4;
                    ImageIcon image = new ImageIcon(iconBytes);
                    setIcon(new ImageIcon(image.getImage().getScaledInstance(rowSize, rowSize, 1)));
                }

            } else {
                setIcon(null);
            }

        }
        super.setValue(value);
    }

}
