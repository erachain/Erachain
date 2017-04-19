 package gui.models;

      import java.awt.Color;
      import java.awt.Component;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.SystemColor;

import javax.swing.JLabel;
      import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.border.LineBorder;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

      /**
      * @author Subbotin B.P.
      * @see http://www.sbp-program.ru
      */
      public class Renderer_TextArea extends JTextArea implements TableCellRenderer  
      {
        private static final long serialVersionUID = 1L;
        
        public Renderer_TextArea(){
        setLineWrap(true);
        setWrapStyleWord(true);
        setOpaque(true);
        }

      //  @Override
      //  public Component getTableCellRendererComponent(JTable table,
      //         Object value, boolean isSelected, boolean hasFocus, int row, int column)
      //  {
        	
        	 public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column){
       //   setFont(table.getFont());
      //    setBackground(new Color(255, 255, 220));
      //    setOpaque(true);
          
          
          
          setText((value == null) ? "" : value.toString());
    //      setSize(table.getColumnModel().getColumn(column).getWidth(),
    //          getPreferredSize().height);
    //  if (table.getRowHeight(row) != getPreferredSize().height) {  
    //          table.setRowHeight(row, getPreferredSize().height);  
    //      }
      
      
    //  Class<?> colum = table.getColumnClass(column);
    //  cell = colum.getr
          
          Graphics g = getGraphics();
      
       FontMetrics fm = g.getFontMetrics(this.getFont());// g.getFontMetrics();
      int wightString = fm.stringWidth(value.toString());
      // rows
      int rowCount = getRows();
      // ширина в пикселях  
      int wightCell = wightString/rowCount; 
      table.setRowHeight(row, wightCell);
//	return table;
          
          
          
 /*         
          if(isSelected)
          {
            setBackground(SystemColor.blue);
            value = "<HTML><p style='color:#ffffff'><b>" + "&nbsp;&nbsp;&nbsp;" + value;
          }
          else
          {
            setBackground(new Color(255, 255, 220));
            value = "<HTML><p style='color:#000000'>" + "&nbsp;&nbsp;&nbsp;" + value;
          }

          if (hasFocus) 
          {
            setBorder(new LineBorder(new Color(99, 130, 191)));
          }
          else
          {
            setBorder(new LineBorder(null, 0));
          }
*/
     //   setAlignmentX(JTextArea.LEFT_ALIGNMENT);
     //     setHorizontalAlignment(JTextArea.LEFT_ALIGNMENT);//.RIGHT);
   //      setHorizontalTextPosition(JLabel.LEFT);//.RIGHT);
   //      setAlignmentX(10);
   //       setText((value == null) ? "" :  value + "</></>  ");
   //   setText((value == null) ? "" : value.toString());
          return this;
        }
      }