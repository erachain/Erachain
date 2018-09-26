package gui.telegrams;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.border.LineBorder;
import javax.swing.table.TableCellRenderer;
import javax.swing.text.View;

import org.mapdb.Fun.Tuple3;

import core.transaction.R_Send;
import core.transaction.Transaction;
import settings.Settings;
import utils.DateTimeFormat;

public class RendererMessage extends JLabel implements TableCellRenderer {
    private static final long serialVersionUID = 1L;
    private static JLabel resizer;
    FontMetrics fontMetrics;
    int row1 =0;
    int rowww =1;
    
 //   JTextPane jtp;
 //   JTextArea ta;
    
    public RendererMessage() {
       //setContentType("text/html");
       // ta = new JTextArea();
     //   jtp = new JTextPane();
     //   setViewportView(jtp);
     //   setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED); 
        resizer = new JLabel();
       
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
            int row, int column) {

        
        fontMetrics = table.getFontMetrics(UIManager.getFont("Label.font"));
        String color;
        WalletTelegramsFilterTableModel model = (WalletTelegramsFilterTableModel) table.getModel();
       
           
            Tuple3<String, String, Transaction> val = (Tuple3<String,String,Transaction>)value;
            if (model.getSender() == null ) return this;
            String image;
            if (model.getSender().equals(val.a)) {
                resizer.setHorizontalAlignment(LEFT);
                color = "Green";
                image = Settings.getInstance().getUserPath() + "images/messages/send.png";
            } else {
                color = "Blue";
                resizer.setHorizontalAlignment(RIGHT);
                image = Settings.getInstance().getUserPath() + "images/messages/receive.png";
            }
            
           value = "<HTML><p>&nbsp;&nbsp;<img src='file:"+ image +"'>&nbsp;<span style='font-size:10px;font-family:" + UIManager.getFont("Label.font").getFamily() + ";color:"+ color   + "'>"
                   + " DateTime: " + DateTimeFormat.timestamptoString(val.c.getTimestamp()) + "</span></p>"
                   + "<p style='font-size:10px;font-family:" + UIManager.getFont("Label.font").getFamily() + ";color:"+ color   + "'>&nbsp;&nbsp;Sender: " + val.a   + " &nbsp;&nbsp; Recipient: " + val.b + "</p>"
                  + "&nbsp;&nbsp;<p>" + "<span style='font-size:" + UIManager.getFont("Label.font").getSize() + "px;font-family:"
                    + UIManager.getFont("Label.font").getFamily() + "'>" + (( R_Send)val.c).viewData() + "</p></HTML>";
            
         
       

           if (isSelected) {
            resizer.setBackground(Color.MAGENTA);//.UIManager.getColor("Table.selectionBackground"));
         } else {
            resizer.setBackground(UIManager.getColor("Table.background"));
        }

           if (hasFocus) {
               resizer.setBorder(new LineBorder(new Color(99, 130, 191)));
               //  setBorder(UIManager.getBorder("Table.focusCellHighlightBorder"));
           } else {
               resizer.setBorder(new LineBorder(null, 0));
               // setBorder(noFocusBorder);
           }
          // calc height
           Dimension prefSize = getPreferredSize1((String) value, true, table.getWidth()-10);
         // set hight cell table
        table.setRowHeight((row), (int) (prefSize.getHeight()+30));
        
        return resizer;
    }
    
    
    public static java.awt.Dimension getPreferredSize1(String html, boolean width, int prefSize) {

        resizer.setText(html);
        View view = (View) resizer.getClientProperty(javax.swing.plaf.basic.BasicHTML.propertyKey);

        view.setSize(width ? prefSize : 0, width ? 0 : prefSize);

        float w = view.getPreferredSpan(View.X_AXIS);
        float h = view.getPreferredSpan(View.Y_AXIS);

        return new java.awt.Dimension((int) Math.ceil(w), (int) Math.ceil(h));
    }
    
}