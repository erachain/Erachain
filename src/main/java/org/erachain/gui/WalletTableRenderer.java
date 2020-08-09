package org.erachain.gui;

import org.erachain.gui.items.accounts.AccountsTransactionsTableModel;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class WalletTableRenderer extends DefaultTableCellRenderer {

    private final DefaultTableCellRenderer adaptee = new DefaultTableCellRenderer();

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
        } else if (value instanceof Number) {
            JLabel c = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, false, row, column);
            c.setHorizontalAlignment(SwingConstants.RIGHT);
        }

        JLabel c = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, false, row, column);

        Object isUnViewed = table.getValueAt(row, AccountsTransactionsTableModel.COLUMN_UN_VIEWED);
        if (isUnViewed != null && (boolean) isUnViewed) {
            Font font = c.getFont();
            font = new Font(font.getName(), Font.BOLD, font.getSize());
            c.setFont(font);
        } else {
            c.setBackground(adaptee.getBackground());
            c.setFont(adaptee.getFont());
        }

        Object isOutcome = table.getValueAt(row, AccountsTransactionsTableModel.COLUMN_IS_OUTCOME);
        if (isOutcome != null && (boolean) isOutcome) {
            c.setForeground(Color.RED);
        } else {
            JLabel label = new JLabel();
            c.setForeground(label.getForeground());
        }

        return c;
    }

    @Override
    protected void setValue(Object value) {

        if (value != null && value instanceof Iconable) {
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
        super.setValue(value);
    }

}
