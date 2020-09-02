package org.erachain.gui.models;

import org.erachain.core.item.ItemCls;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;


public class RendererIcon extends DefaultTableCellRenderer {
    private static final long serialVersionUID = 1L;

    @Override
    protected void setValue(Object value) {

        // Get icon to use for the list item value
        ItemCls item = (ItemCls) value;
        if (item == null)
            return;

        byte[] iconBytes = item.getIcon();
        if (iconBytes != null && iconBytes.length > 0) {
            int size = getFont().getSize() + 2;
            ImageIcon image = new ImageIcon(iconBytes);
            setIcon(new ImageIcon(image.getImage().getScaledInstance(size, size, 1)));
        } else {
            setIcon(new ImageIcon());
        }

        setText(item.viewName());

    }

}
