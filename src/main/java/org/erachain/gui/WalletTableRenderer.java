package org.erachain.gui;

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
    public static final int COLUMN_NUMBER_SCALE = -3;
    public static final int COLUMN_IS_OUTCOME = -2;
    public static final int COLUMN_UN_VIEWED = -1;

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

        Object isUnViewed = table.getValueAt(row, COLUMN_UN_VIEWED);
        if (isUnViewed != null && (boolean) isUnViewed) {
            Font font = adaptee.getFont();
            font = new Font(font.getName(), Font.BOLD, font.getSize());
            cell.setFont(font);
        } else {
            cell.setFont(adaptee.getFont());
        }

        Object isOutcome = table.getValueAt(row, COLUMN_IS_OUTCOME);
        if (isOutcome != null && (boolean) isOutcome) {
            cell.setForeground(adaptee.getForeground());
        } else {
            cell.setForeground(FORE_COLOR);
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
