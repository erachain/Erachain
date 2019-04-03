package org.erachain.gui.library;

import java.math.BigDecimal;

import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.text.PlainDocument;

public class MTextFieldOnlyBigDecimal extends JTextField {

    /**
     * TODO Insert only Bigdecimal number
     */
    private static final long serialVersionUID = 1L;
    private MTextFieldOnlyBigDecimal th;

    public MTextFieldOnlyBigDecimal(){
        super();
        th = this;
        PlainDocument doc = (PlainDocument) getDocument();
        doc.setDocumentFilter(new BigDecimalFilter());
    }
    
    class BigDecimalFilter extends DocumentFilter {
        @Override
        public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {

            try {
                
                String str = new StringBuffer(th.getText()).insert(offset, string).toString();
                 new BigDecimal(str);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                //e.printStackTrace();
                return;
            }
                super.insertString(fb, offset, string, attr);
           
        }

        @Override
        public void replace(FilterBypass fb, int offset, int length, String string, AttributeSet attrs) throws BadLocationException {
           
            try {
                
                String str = new StringBuffer(th.getText()).insert(offset, string).toString();
                new BigDecimal(str);
            } catch (Exception e) {
                // TODO Auto-generated catch block
               // e.printStackTrace();
                return;
            }
                super.replace(fb, offset, length, string, attrs);
           
        }
    }
}
