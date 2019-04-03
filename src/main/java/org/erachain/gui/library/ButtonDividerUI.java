package org.erachain.gui.library;

import javax.swing.*;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;
import java.awt.*;

class ButtonDividerUI extends BasicSplitPaneUI {
    protected JButton button;
    protected JButton button1;
    protected GridBagConstraints gridBagConstraints;
    protected GridBagConstraints gridBagConstraints1;
    private BasicSplitPaneDivider divider1;
    private int wight_Div;
    private JButton button_work;

    public ButtonDividerUI(JButton button, JButton button1, int wight_Div, JButton button_work) {
        this.button = button;
        this.button1 = button1;
        this.wight_Div = wight_Div;
        this.button_work = button_work;
    }

    public BasicSplitPaneDivider createDefaultDivider() {
        divider1 = new BasicSplitPaneDivider(this) {


            public int getDividerSize() {


                if (getOrientation() == JSplitPane.HORIZONTAL_SPLIT) {
                    divider1.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
                    button1.setPreferredSize(new Dimension(wight_Div, wight_Div * 2));
                    button.setPreferredSize(new Dimension(wight_Div, wight_Div * 2));
                    button_work.setPreferredSize(new Dimension(wight_Div, wight_Div * 2));
                    return button.getPreferredSize().width;
                }
                divider1.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
                button1.setPreferredSize(new Dimension(wight_Div * 2, wight_Div));
                button.setPreferredSize(new Dimension(wight_Div * 2, wight_Div));
                button_work.setPreferredSize(new Dimension(wight_Div * 2, wight_Div));
                return button.getPreferredSize().height;
            }
        };


        divider1.add(button_work);
        divider1.add(button1);
        divider1.add(button);
        return divider1;
    }
}
