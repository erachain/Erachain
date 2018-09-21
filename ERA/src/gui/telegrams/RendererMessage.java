package gui.telegrams;

import java.awt.Color;
import java.awt.Component;
import java.awt.SystemColor;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.border.LineBorder;
import javax.swing.table.TableCellRenderer;

import org.mapdb.Fun.Tuple3;

import core.transaction.R_Send;
import core.transaction.Transaction;
import utils.DateTimeFormat;

public class RendererMessage extends JLabel implements TableCellRenderer {
    private static final long serialVersionUID = 1L;

    // FontMetrics fontMetrics = table.getFontMetrics(table.getFont());
    public RendererMessage() {

    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
            int row, int column) {

        setHorizontalAlignment(JLabel.RIGHT);

        WalletTelegramsFilterTableModel model = (WalletTelegramsFilterTableModel) table.getModel();
        if (column == WalletTelegramsFilterTableModel.COLUMN_MESSAGE) {
           
            Tuple3<String, String, Transaction> val = (Tuple3<String,String,Transaction>)value;
            if (model.getSender() == null ) return this;
            if (model.getSender().equals(val.a)) {
                setHorizontalAlignment(JLabel.LEFT);
                value = "<HTML><span style='font-size:10px;color:green'> Sender: " + val.a 
                        + "  Recipient: " + val.b + "</span>"
                      + "<br>" +(( R_Send)val.c).viewData();
            } else {
                setHorizontalAlignment(JLabel.RIGHT);
                value = "<HTML><span style='font-size:10px;color:blue'> Sender: " + val.a  
                        + " Recipient: " + val.b + "</span>"
                      + "<br>" +(( R_Send)val.c).viewData();
                this.setBackground(SystemColor.LIGHT_GRAY);
            }
            
            
        }
        if (column == WalletTelegramsFilterTableModel.COLUMN_DATE) {
            value = "<HTML>" + DateTimeFormat.timestamptoString((long) value);
        }
        if (isSelected) {
            setBackground(SystemColor.blue);
            // value = "<HTML><p style='color:#ffffff'><b>" +
            // "&nbsp;&nbsp;&nbsp;" + value;
        } else {
     //       setBackground(new Color(255, 255, 220));
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

        return this;
    }
}