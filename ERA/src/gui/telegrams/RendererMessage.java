package gui.telegrams;

import java.awt.Color;
import java.awt.Component;
import java.awt.SystemColor;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.border.LineBorder;
import javax.swing.table.TableCellRenderer;

import core.transaction.R_Send;


public class RendererMessage extends JLabel implements TableCellRenderer {
    private static final long serialVersionUID = 1L;
   
    //   FontMetrics fontMetrics = table.getFontMetrics(table.getFont());
    public RendererMessage() {
      
    }

    @Override
    public Component getTableCellRendererComponent(JTable table,
                                                   Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        

            setHorizontalAlignment(JLabel.RIGHT);
            
            WalletTelegramsFilterTableModel model = (WalletTelegramsFilterTableModel)table.getModel(); 
            if (column == WalletTelegramsFilterTableModel.COLUMN_MESSAGE){
            R_Send tr = (R_Send) model.getTelegramMessage(row);
            String a1 = model.getSender();
            String a2 = tr.getCreator().getAddress();
           
            if(model.getSender().equals(tr.getCreator().getAddress())){
                setHorizontalAlignment(JLabel.LEFT);
                value = "<HTML><span style='font-size:10px;color:green'> Sender: " + value + "</span><br>" + tr.viewData();
            }else{
                setHorizontalAlignment(JLabel.RIGHT);
                value = "<HTML><span style='font-size:10px;color:blue'> Sender: " + value + "</span><br>" + tr.viewData();
            }
       
            }


        if (isSelected) {
            setBackground(SystemColor.blue);
        //    value = "<HTML><p style='color:#ffffff'><b>" + "&nbsp;&nbsp;&nbsp;" + value;
        } else {
            setBackground(new Color(255, 255, 220));
        //    value = "<HTML><p style='color:#000000'>" + "&nbsp;&nbsp;&nbsp;" + value;
        }

        if (hasFocus) {
            setBorder(new LineBorder(new Color(99, 130, 191)));
        } else {
            setBorder(new LineBorder(null, 0));
        }


        //      setAlignmentX(10);
        setText((value == null) ? "" : value+"");


        return this;
    }
}