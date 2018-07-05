package gui.models;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;


public class Renderer_BigDecimals extends JFormattedTextField implements TableCellRenderer {
    private static final long serialVersionUID = 1L;
    int roww = 0;
    int rowww = 1;
    int n = 0;
    int row1 = 0;
    FontMetrics fontMetrics;
    int rowHeight;
    Boolean[] column_auto_Height;
    static DecimalFormat ff = (DecimalFormat) DecimalFormat.getInstance();

    //   FontMetrics fontMetrics = table.getFontMetrics(table.getFont());
    public Renderer_BigDecimals(Integer scale) {
        //	fontMetrics=fontMetrics1;	
        super(ff);
        ff.setMaximumFractionDigits(scale);
        ff.setMinimumFractionDigits(scale);
        setOpaque(true);
        setBackground(new Color(255, 255, 220));
        setHorizontalAlignment(JLabel.RIGHT);//.RIGHT);
   
    }

    @Override
    public Component getTableCellRendererComponent(JTable table,
                                                   Object value, boolean isSelected, boolean hasFocus, int row, int column) {
       
       
         // using L&F colors
            setForeground(isSelected ?
                UIManager.getColor("Table.selectionForeground") :
                UIManager.getColor("Table.foreground"));
            setBackground(isSelected ?
                UIManager.getColor("Table.selectionBackground") :
                UIManager.getColor("Table.background"));
           
               
     //           BorderFactory.createEmptyBorder(BT, BT, BT, BT));
     //       setIcon(icon);
     //       return this;
            
            
     //   }

        if (hasFocus) {
            BorderFactory.createLineBorder(UIManager.getColor("Table.LineBorder"), 1) ;
        } else {
            setBorder(new LineBorder(null, 0));
        }

        setValue(value);

        return this;
    }
}