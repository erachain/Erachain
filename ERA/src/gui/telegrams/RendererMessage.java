package gui.telegrams;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.SystemColor;

import javax.swing.JTable;
import javax.swing.JTextPane;
import javax.swing.UIManager;
import javax.swing.border.LineBorder;
import javax.swing.table.TableCellRenderer;

import org.mapdb.Fun.Tuple3;

import core.transaction.R_Send;
import core.transaction.Transaction;
import utils.DateTimeFormat;

public class RendererMessage extends JTextPane implements TableCellRenderer {
    private static final long serialVersionUID = 1L;
    FontMetrics fontMetrics;
    int row1 =0;
    int rowww =1;
    
    public RendererMessage() {
       setContentType("text/html");
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
            int row, int column) {

        fontMetrics = table.getFontMetrics(UIManager.getFont("Label.font"));
     //   setHorizontalAlignment(JLabel.RIGHT);
        String color = "green";
        WalletTelegramsFilterTableModel model = (WalletTelegramsFilterTableModel) table.getModel();
        if (column == WalletTelegramsFilterTableModel.COLUMN_MESSAGE) {
           
            Tuple3<String, String, Transaction> val = (Tuple3<String,String,Transaction>)value;
            if (model.getSender() == null ) return this;
            if (model.getSender().equals(val.a)) {
        //        setHorizontalAlignment(JLabel.LEFT);
               color = "green";
            } else {
       //         setHorizontalAlignment(JLabel.RIGHT);
               color = "blue";
                this.setBackground(SystemColor.LIGHT_GRAY);
            }
            
           
            
            value = "<HTML><span style='font-size:10px;font-family:"
                    + UIManager.getFont("Label.font").getFamily() + "color:"+ color   + "'> DateTime: " + DateTimeFormat.timestamptoString(val.c.getTimestamp()) + "Sender: " + val.a 
                    + "  Recipient: " + val.b + "</span>"
                  + "<br>" + "<span style='font-size:" + UIManager.getFont("Label.font").getSize() + "px;font-family:"
                    + UIManager.getFont("Label.font").getFamily() + "'>" + (( R_Send)val.c).viewData() + "</HTML>";
        }
         String k = value.toString();
        int a = fontMetrics.stringWidth( value.toString());
        int b = table.getColumnModel().getColumn(column).getWidth();
         rowww = Math.max(rowww, (int) (fontMetrics.stringWidth( value.toString()+"           ") / table.getColumnModel().getColumn(column).getWidth())+1);
       // if(row1!=row){
            
      //    System.out.println("row0"+row+" row1:"+row1+" hight:"+(int) (fontMetrics.getHeight() * rowww));
            
         //   table.setRowHeight((row), (int) (fontMetrics.getHeight() * rowww));
           
            
            
            rowww = 1;
         //   row1=row;
           
            
            
      //  }
        
        if (isSelected) {
            setBackground(SystemColor.blue);
            // value = "<HTML><p style='color:#ffffff'><b>" +
            // "&nbsp;&nbsp;&nbsp;" + value;
        } else {
            setBackground(new Color(255, 255, 220));
            // value = "<HTML><p style='color:#000000'>" + "&nbsp;&nbsp;&nbsp;"
            // + value;
        }

        if (hasFocus) {
            setBorder(new LineBorder(new Color(99, 130, 191)));
        } else {
            setBorder(new LineBorder(null, 0));
        }

        // setAlignmentX(10);
        setText((value == null) ? "" : value + "");
         Dimension d = getPreferredSize();
         table.setRowHeight((row), d.height);
        return this;
    }
}