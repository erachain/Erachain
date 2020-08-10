package org.erachain.gui.library;

import org.erachain.utils.GUIUtils;

import javax.swing.*;
import java.awt.*;

public class MIcon extends JCheckBox {

    Color color;
    ImageIcon imageIcon;
    byte[] imageBytes;

    MIcon(Color color) {
        super();
        this.color = color;
        imageIcon = GUIUtils.createIcon(getFont().getSize(), color, this.getBackground());
    }

    @Override
    public ImageIcon getIcon() {
        return imageIcon;
    }

}
