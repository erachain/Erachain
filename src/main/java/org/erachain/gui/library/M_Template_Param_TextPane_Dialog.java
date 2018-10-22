package org.erachain.gui.library;

import javax.swing.*;
import java.awt.*;

public class M_Template_Param_TextPane_Dialog extends JDialog {

    public JTextPane tp;

    public M_Template_Param_TextPane_Dialog(String string, Point point) {

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
