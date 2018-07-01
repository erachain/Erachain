package gui.library;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.Format;
import java.text.NumberFormat;

import javax.swing.JFormattedTextField;
import javax.swing.JTextField;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.NavigationFilter.FilterBypass;

public class M_DecimalFormatedTextField extends JTextField {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private M_DecimalFormatedTextField th;
    private int scale = 8;
    protected int curr_Dot;
    //  maskType =0 - float (1.02)
    //   maskType =1 - int  (100)
    //  maskType =2 - LONG  (1000000000000000000000000000)
    public int maskType =0;
    public int maskInteger = 1;
    public int maskFloat = 0;
    public int maskLong = 2;
    public M_DecimalFormatedTextField() {
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
