package org.erachain.gui.library;

import javax.swing.*;
import javax.swing.text.Caret;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class MDecimalFormatedTextField extends JTextField {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private MDecimalFormatedTextField th;
    private int scale = 8;
    protected int curr_Dot;

    public static int MASK_FLOAT = 0;
    public static int MASK_INTEGER = 1;
    public static int MASK_LONG = 2;

    public int maskType = 0;

    public MDecimalFormatedTextField() {
        super();
        th = this;
        this.addKeyListener(new KeyAdapter() {
            private Caret caret;

            public void keyTyped(KeyEvent e) {
                boolean ret = true;
                
                try {
                    if(maskType == 0)  Double.parseDouble(th.getText() + e.getKeyChar());
                    if(maskType == 1 ) Integer.valueOf(th.getText() + e.getKeyChar());
                    if(maskType == 2 ) Long.valueOf(th.getText() + e.getKeyChar());
                } catch (NumberFormatException ee) {
                    ret = false;
                }
                if (!ret) {
                    e.consume();
                }
            }

            public void keyPressed(KeyEvent event) {

            }

            public void keyReleased(KeyEvent event) {
                
                setMask();
           }
        });
    }

    public void setScale(int scale) {
        th.scale = scale;
        setMask();
        
    }
    private void setMask(){
        String ss = th.getText();
        int in = ss.indexOf(".");
        if (in < 0)
            return;
        // float
        int dd = ss.substring(ss.indexOf(".") + 1).length();
        // int
        int inn = ss.substring(0, ss.indexOf(".")).length();
    
        if (dd > th.scale) {
            // save position
            curr_Dot = th.getCaretPosition();
            // delete last char
            th.setText(ss.substring(0, inn + th.scale + 1));
            // set position
            if (th.getText().length() < curr_Dot)
                curr_Dot = th.getText().length();
            th.setCaretPosition(curr_Dot);
        }
    }
    
    public void  setMaskType(int maskNom){
        maskType = maskNom;
    }
        
}
