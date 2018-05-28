package gui.library;

import lang.Lang;
import org.apache.commons.lang3.math.NumberUtils;
import utils.MenuPopupUtil;

import javax.swing.*;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import java.awt.*;

public class My_Int_Long_JTextField extends JTextField {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private My_Int_Long_JTextField th;
    private Color text_Color;

    public My_Int_Long_JTextField() {
        super();
        th = this;
        th.setToolTipText(Lang.getInstance().translate("Must be digital"));
        text_Color = this.getForeground();
        MenuPopupUtil.installContextMenu(this);
        th.setForeground(Color.RED);

        addCaretListener(new CaretListener() {

            @Override
            public void caretUpdate(CaretEvent arg0) {


                if (!NumberUtils.isNumber(th.getText())) {
                    th.setForeground(Color.RED);
                    return;
                }
                th.setForeground(text_Color);
            }


        });

    }

}
