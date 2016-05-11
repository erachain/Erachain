 package gui.models;

      import java.awt.Color;
      import java.awt.Component;
import java.awt.SystemColor;

import javax.swing.JLabel;
      import javax.swing.JTable;
      import javax.swing.border.LineBorder;
      import javax.swing.table.TableCellRenderer;

      /**
      * @author Subbotin B.P.
      * @see http://www.sbp-program.ru
      */
      public class Renderer_Right extends JLabel implements TableCellRenderer  
      {
        private static final long serialVersionUID = 1L;

        @Override
        public Component getTableCellRendererComponent(JTable table,
               Object value, boolean isSelected, boolean hasFocus, int row, int column)
        {
          setFont(table.getFont());
          setBackground(new Color(255, 255, 220));
          setOpaque(true);
          
          
          if(isSelected)
          {
            setBackground(SystemColor.blue);
            value = "<HTML><p style='color:#ffffff'><b>" + value;
          }
          else
          {
            setBackground(new Color(255, 255, 220));
            value = "<HTML><p style='color:#000000'>" + value;
          }

          if (hasFocus) 
          {
            setBorder(new LineBorder(new Color(99, 130, 191)));
          }
          else
          {
            setBorder(new LineBorder(null, 0));
          }

        setHorizontalAlignment(JLabel.RIGHT);
         setHorizontalTextPosition(JLabel.RIGHT);
         setAlignmentX(10);
          setText((value == null) ? "" : value +"&nbsp;&nbsp;&nbsp;</></>  ");
          return this;
        }
      }