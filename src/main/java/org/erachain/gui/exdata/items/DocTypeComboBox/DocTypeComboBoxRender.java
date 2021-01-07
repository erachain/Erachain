package org.erachain.gui.exdata.items.DocTypeComboBox;

import org.erachain.core.exdata.exLink.ExLink;
import org.erachain.lang.Lang;

import javax.swing.*;
import java.awt.*;

public class DocTypeComboBoxRender  extends DefaultListCellRenderer {

    public DocTypeComboBoxRender() {
    }

    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {

        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        String text = "";
        if (value != null) {
            text = Lang.T(ExLink.viewTypeName((int) value, true));
        }
        this.setText(text);
        return this;

    }
}
