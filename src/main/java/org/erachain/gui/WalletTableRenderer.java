package org.erachain.gui;

import org.erachain.gui.items.accounts.AccountsTransactionsTableModel;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class WalletTableRenderer extends DefaultTableCellRenderer {
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus, int row, int column) {
        JLabel c = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, false, row, column);

        //if (column == 1) c.setHorizontalAlignment(CENTER);
        //else c.setHorizontalAlignment(LEFT);

        Object isUnViewed = table.getValueAt(row, AccountsTransactionsTableModel.COLUMN_UN_VIEWED);
        if (isUnViewed != null && (boolean) isUnViewed) {
            Font font = c.getFont();
            font = new Font(font.getName(), Font.BOLD, font.getSize());
            c.setFont(font);
        } else {
            JLabel label = new JLabel();
            c.setForeground(label.getForeground());
            c.setFont(label.getFont());
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
