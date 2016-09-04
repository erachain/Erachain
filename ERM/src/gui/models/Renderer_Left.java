 package gui.models;

      import java.awt.Color;
      import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.SystemColor;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;

import javax.swing.JLabel;
      import javax.swing.JTable;
      import javax.swing.border.LineBorder;
      import javax.swing.table.TableCellRenderer;

      /**
      * @author Subbotin B.P.
      * @see http://www.sbp-program.ru
      */
      public class Renderer_Left extends JLabel implements TableCellRenderer  
      {
        private static final long serialVersionUID = 1L;
        int roww =0;
        int rowww =1;
        int n = 0;
        int row1=0;
        FontMetrics fontMetrics;
        int rowHeight;
    
       //   FontMetrics fontMetrics = table.getFontMetrics(table.getFont()); 
        public Renderer_Left(FontMetrics fontMetrics1){
        	fontMetrics=fontMetrics1;	
        	 setOpaque(true);
        	 setBackground(new Color(255, 255, 220));
        	 setVerticalAlignment(TOP);
        	 setHorizontalAlignment(JLabel.LEFT);//.RIGHT);
             setHorizontalTextPosition(JLabel.LEFT);//.RIGHT);
        }
        
        @Override
        public Component getTableCellRendererComponent(JTable table,
               Object value, boolean isSelected, boolean hasFocus, int row, int column)
        {
        //	int rowHeight; // = table .getRowHeight();
         // setFont(table.getFont());
         // Font ff = table.getFont();
      //    setBackground(new Color(255, 255, 220));
      //    setOpaque(true);
          
      //    table.setRowHeight(row, 90);
          
          if (value ==null || value =="") value=" ";
       
     	 // String a1 = table.getModel().getValueAt(row, column).toString();
         
      //     double textWidth = fontMetrics.stringWidth( value.toString());// + fontMetrics.stringWidth("w"));
       //    double textHeight = fontMetrics.getHeight();
      //     double componentWidth = table.getColumnModel().getColumn(column).getWidth();
          // double componentHeight = (textWidth * textHeight) / componentWidth;
         //  n = (int) (componentHeight / textHeight) + 1;
         //  n = (int) (fontMetrics.stringWidth( value.toString()) / table.getColumnModel().getColumn(column).getWidth() +1);
         //  if (n <1) n=1;
           rowww = Math.max(rowww, (int) (fontMetrics.stringWidth( value.toString()+"WW") / table.getColumnModel().getColumn(column).getWidth() +1));
          if(row1!=row){
        	  table.setRowHeight((row1), (int) (fontMetrics.getHeight() * rowww));
        	  rowww = 1;
        	  row1=row;
        	 
        	  
        	  
          }
          
          
          
          
          
          
         // setAlignmentY(TOP_ALIGNMENT);
         // setVerticalAlignment(verTOP_ALIGNMEN);
         
          
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

        
   //      setAlignmentX(10);
          setText((value == null) ? "" :  value + "</></>  ");
          
       
          
          
          
          return this;
        }
      }