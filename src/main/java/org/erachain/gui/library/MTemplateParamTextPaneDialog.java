package org.erachain.gui.library;

import javax.swing.*;
import java.awt.*;

public class MTemplateParamTextPaneDialog extends JDialog {

    public JTextPane tp;

    public MTemplateParamTextPaneDialog(String string, Point point) {

        setModal(true);
        tp = new JTextPane();
        this.setLocationRelativeTo(null);
        tp.setText(string);
        add(tp);
//	setLocation(point);
        setSize(new Dimension(600, 400));
        setVisible(true);


    }
}
