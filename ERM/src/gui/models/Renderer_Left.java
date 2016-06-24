 package gui.models;

      import java.awt.Color;
      import java.awt.Component;
import java.awt.Font;
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

        @Override
        public Component getTableCellRendererComponent(JTable table,
               Object value, boolean isSelected, boolean hasFocus, int row, int column)
        {
          setFont(table.getFont());
          setBackground(new Color(255, 255, 220));
          setOpaque(true);
         // setAlignmentY(TOP_ALIGNMENT);
         // setVerticalAlignment(verTOP_ALIGNMEN);
          setVerticalAlignment(TOP);
          
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

        setHorizontalAlignment(JLabel.LEFT);//.RIGHT);
         setHorizontalTextPosition(JLabel.LEFT);//.RIGHT);
   //      setAlignmentX(10);
          setText((value == null) ? "" :  value + "</></>  ");
          
       
          
          
          
          return this;
        }
      }