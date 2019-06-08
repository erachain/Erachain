package org.erachain.gui.models;

import org.erachain.core.item.ItemCls;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;


public class RendererIcon extends DefaultTableCellRenderer {
    private static final long serialVersionUID = 1L;

    /*

    тут при выделении не перекрашивает

    @Override
    public Component getTableCellRendererComponent(JTable table,
                                                   Object value,
                                                   boolean isSelected,
                                                   boolean hasFocus,
                                                   int row,
                                                   int column) {
        // Get icon to use for the list item value
        ItemCls item = (ItemCls)value;
        byte[] iconBytes = item.getIcon();
        if (iconBytes != null && iconBytes.length > 0) {
            ImageIcon image = new ImageIcon(iconBytes);
            Image Im = image.getImage().getScaledInstance(20, 20, 1);
            setIcon(new ImageIcon(Im));
        }

        setText(item.viewName());

        return this;

    }
    */

    @Override
    protected void setValue(Object value) {

        // Get icon to use for the list item value
        ItemCls item = (ItemCls)value;
        byte[] iconBytes = item.getIcon();
        if (iconBytes != null && iconBytes.length > 0) {
            ImageIcon image = new ImageIcon(iconBytes);
            setIcon(new ImageIcon(image.getImage().getScaledInstance(20, 20, 1)));
        }

        setText(item.viewName());

    }

}
