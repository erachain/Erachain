package org.erachain.gui;

import org.erachain.gui.library.MTable;
import org.erachain.gui.models.TimerTableModelCls;
import org.erachain.settings.Settings;
import org.erachain.utils.GUIUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class WalletTableRenderer extends DefaultTableCellRenderer {

    private final DefaultTableCellRenderer adaptee = new DefaultTableCellRenderer();
    static Logger LOGGER = LoggerFactory.getLogger(WalletTableRenderer.class.getName());

    Color color1 = new Color(255, 215, 84, 255);
    public static boolean markIncome = Settings.getInstance().markIncome();
    public final static Color FORE_COLOR = Settings.getInstance().markColorObj();
    public final static Color FORE_COLOR_SELECTED = Settings.getInstance().markColorSelectedObj();

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus, int row, int column) {

        adaptee.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        Component cell;

        if (value instanceof Boolean) {
            Boolean selected = (Boolean) value;

            int sizeRow = table.getRowHeight();

            cell = new JCheckBox(null, GUIUtils.createIcon(sizeRow, selected ?
                    Color.PINK
                    : Color.GRAY, null), selected);
            ((JCheckBox) cell).setHorizontalAlignment(JLabel.CENTER);
            ((JCheckBox) cell).setOpaque(true);
            ((JCheckBox) cell).setBorderPainted(false);
            ((JCheckBox) cell).setContentAreaFilled(true);

        } else {
            cell = super.getTableCellRendererComponent(table, value, isSelected, false, row, column);
        }

        cell.setBackground(adaptee.getBackground());

        if (value instanceof Number) {
            ((JLabel) cell).setHorizontalAlignment(SwingConstants.RIGHT);
        }

        Object isUnViewed = table.getValueAt(row, TimerTableModelCls.COLUMN_UN_VIEWED);
        if (isUnViewed instanceof Boolean && (boolean) isUnViewed) {
            Font font = adaptee.getFont();
            font = new Font(font.getName(), Font.BOLD, font.getSize());
            cell.setFont(font);
        } else {
            cell.setFont(adaptee.getFont());
        }

        Object isOutcome = table.getValueAt(row, TimerTableModelCls.COLUMN_IS_OUTCOME);
        Color color;

        if (isOutcome instanceof Boolean && !(boolean) isOutcome ^ markIncome) {
            if (isSelected) {
                color = FORE_COLOR_SELECTED;
            } else {
                color = FORE_COLOR;
            }
        } else {
            color = adaptee.getForeground();
        }

        if (color != null)
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
                } else if (confirmations < 33) {
                    setIcon(GUIUtils.createIcon(sizeRow, color, null));
                } else {
                    setIcon(null);
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

                ImageIcon image = iconable.getImageIcon();
                if (image != null) {
                    int rowSize = getFont().getSize() + 4;
                    setIcon(new ImageIcon(image.getImage().getScaledInstance(rowSize, rowSize, 1)));
                }

            } else {
                setIcon(null);
            }

        }
        super.setValue(value);
    }

}
