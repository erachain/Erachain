package gui.library;

import lang.Lang;
import org.apache.commons.lang3.math.NumberUtils;
import utils.MenuPopupUtil;

import javax.swing.*;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import java.awt.*;

public class My_BIK_JTextField extends JTextField {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private My_BIK_JTextField th;
    private Color text_Color;

    public My_BIK_JTextField() {
        super();
        th = this;
        text_Color = this.getForeground();
        th.setForeground(Color.RED);
        th.setToolTipText(Lang.getInstance().translate("Must be 9 numbers"));
        MenuPopupUtil.installContextMenu(this);
        addCaretListener(new CaretListener() {

            @Override
            public void caretUpdate(CaretEvent arg0) {
                if (!NumberUtils.isNumber(th.getText())) {
                    th.setForeground(Color.RED);
                    return;
                }
                if (th.getText().length() != 9) {

                    th.setForeground(Color.RED);
                    return;
                }
                th.setForeground(text_Color);
            }


        });

    }

}
