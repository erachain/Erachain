package org.erachain.gui.library;

import org.apache.commons.lang3.math.NumberUtils;
import org.erachain.lang.Lang;
import org.erachain.utils.MenuPopupUtil;

import javax.swing.*;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import java.awt.*;

public class MyINNJTextField extends JTextField {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private MyINNJTextField th;
    private Color text_Color;

    public MyINNJTextField() {
        super();
        th = this;
        th.setToolTipText(Lang.T("Must be 10 or 12 numbers"));
        text_Color = this.getForeground();
        th.setForeground(Color.RED);
        MenuPopupUtil.installContextMenu(this);

        addCaretListener(new CaretListener() {

            @Override
            public void caretUpdate(CaretEvent arg0) {
                if (!NumberUtils.isNumber(th.getText())) {
                    th.setForeground(Color.RED);
                    return;
                }
                if (th.getText().length() != 10 && th.getText().length() != 12) {

                    th.setForeground(Color.RED);
                    return;
                }
                th.setForeground(text_Color);
            }


        });


    }

}
