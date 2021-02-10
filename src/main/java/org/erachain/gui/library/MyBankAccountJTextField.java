package org.erachain.gui.library;

import org.apache.commons.lang3.math.NumberUtils;
import org.erachain.lang.Lang;
import org.erachain.utils.MenuPopupUtil;

import javax.swing.*;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import java.awt.*;

public class MyBankAccountJTextField extends JTextField {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private MyBankAccountJTextField th;
    private Color text_Color;

    public MyBankAccountJTextField() {
        super();
        th = this;
        text_Color = this.getForeground();
        th.setForeground(Color.RED);
        th.setToolTipText(Lang.T("Must be 20 numbers"));
        MenuPopupUtil.installContextMenu(this);
        addCaretListener(new CaretListener() {

            @Override
            public void caretUpdate(CaretEvent arg0) {


                if (!NumberUtils.isNumber(th.getText())) {
                    th.setForeground(Color.RED);
                    return;
                }

                if (th.getText().length() != 20) {

                    th.setForeground(Color.RED);
                    return;
                }
                th.setForeground(text_Color);
            }


        });

    }

}
