package org.erachain.gui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class WalletTableRenderer extends DefaultTableCellRenderer {

    private final DefaultTableCellRenderer adaptee = new DefaultTableCellRenderer();
    static Logger LOGGER = LoggerFactory.getLogger(WalletTableRenderer.class.getName());

    public static final int COLUMN_NUMBER_SCALE = -3;
    public static final int COLUMN_IS_OUTCOME = -2;
    public static final int COLUMN_UN_VIEWED = -1;

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus, int row, int column) {

        adaptee.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        if (value instanceof Boolean) {
            JCheckBox checkbox = new JCheckBox();
            checkbox.setSelected((Boolean) value);
            checkbox.setHorizontalAlignment(JLabel.CENTER);
            checkbox.setBorderPainted(true);

            checkbox.setForeground(adaptee.getForeground());
            checkbox.setBackground(adaptee.getBackground());
            checkbox.setFont(adaptee.getFont());
            return checkbox;

        }

        JLabel cell = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, false, row, column);
        cell.setBackground(adaptee.getBackground());
        cell.setFont(adaptee.getFont());

        if (value instanceof Number) {
            cell.setHorizontalAlignment(SwingConstants.RIGHT);
        }

        Object isUnViewed = table.getValueAt(row, COLUMN_UN_VIEWED);
        if (isUnViewed != null && (boolean) isUnViewed) {
            Font font = cell.getFont();
            font = new Font(font.getName(), Font.BOLD, font.getSize());
            cell.setFont(font);
        } else {
            cell.setBackground(adaptee.getBackground());
            cell.setFont(adaptee.getFont());
        }

        Object isOutcome = table.getValueAt(row, COLUMN_IS_OUTCOME);
        if (isOutcome != null && (boolean) isOutcome) {
            cell.setForeground(Color.RED);
        } else {
            JLabel label = new JLabel();
            cell.setForeground(label.getForeground());
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
                    ImageIcon image = new ImageIcon(iconBytes);
                    setIcon(new ImageIcon(image.getImage().getScaledInstance(20, 20, 1)));
                }

            } else {
                setIcon(null);
            }

        }
        super.setValue(value);
    }

}
