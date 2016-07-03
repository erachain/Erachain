package gui;

import java.awt.Component;
import java.awt.Font;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;

import javax.persistence.Column;
import javax.swing.JTable;

public class Table_Formats {

	public Table_Formats(){
	
	
	
	
	
	
	
	
	}
	
	
	
	public void Table_Row_Auto_Height(JTable table){
		
		//	if (st==st) return;
			Font ff = table.getFont();
		/*	
			 for (int i = 0; i < table .getColumnCount(); i++) {
			      DefaultTableColumnModel colModel = (DefaultTableColumnModel) table .getColumnModel();
			      TableColumn col = colModel.getColumn(i);
			      int width = 0;

			      TableCellRenderer renderer = col.getHeaderRenderer();
			      for (int r = 0; r < table .getRowCount(); r++) {
			        renderer = table .getCellRenderer(r, i);
			        Component comp = renderer.getTableCellRendererComponent(table , table .getValueAt(r, i),
			            false, false, r, i);
			        width = Math.max(width, comp.getPreferredSize().width);
			      }
			      col.setPreferredWidth(width + 2);
			    } 
	*/
			    for (int row = 0; row < table .getRowCount(); row++)
			    {
			        int rowHeight = table .getRowHeight();
			        int roww =0;
			     //   JLabel label = new JLabel("Test label");
			     //   Graphics g = label.getGraphics();
			     //   FontMetrics fm = g.getFontMetrics();
			        
			      //  TextLayout tl = new TextLayout(text, font, new FontRenderContext(null, true, true)); 
			        
			        for (int column = 0; column < table .getColumnCount(); column++)
			        {
			           
			        	if (column ==3){
			        		column=column;
			        		
			        		
			        	}
			        	// Component comp = table .prepareRenderer(table .getCellRenderer(row, column), row, column);
			           // rowHeight = Math.max(rowHeight, comp.getPreferredSize().height);
			        	// читаем данные из ячейки таблицы 
			        	String a = table.getModel().getValueAt(row, column).toString();
			        	if (a ==null || a =="") a=" ";
			        	// читаем фонт ячейки таблицы
			        	
			        	
			        	
			        	
			        	// вычисляем длину текста в пикселях
			        	 TextLayout tl1 = new TextLayout(a, ff, new FontRenderContext(null, true, true)); 
			        	 Rectangle2D ss = tl1.getBounds();
			        	 
			        	// берем длину ячейки в пикселях
			        	 Component comp = table .prepareRenderer(table .getCellRenderer(table.convertRowIndexToModel(row), column), row, column);
			        	 int ww = table.getColumnModel().getColumn(column).getWidth()-10;
			        	 //int ww = comp.getWidth();//.getSize().width;//.getPreferredSize().width;
			        	 // вычисляем количество строк
			        	//  double ssh = ss.getWidth();
			        	   int roww1 = (int)Math.ceil(ss.getWidth()/ww);
			        	   if (roww1 <1) roww1=1;
			        	// определяем тип ячейки
				        	// читаем данные из ячейки таблицы
			        	   String colType = table.getModel().getColumnClass(column).getTypeName();
				        	if( colType.contains("Boolean")){
				        		
				        		roww1 =1;
				        	}
			        	   
			        	   
			        	   roww = Math.max(roww, roww1);
			        	  
			        	 
			        }

			        table .setRowHeight(table.convertRowIndexToModel(row), rowHeight * roww);
			    }
			
			
			
		}



}
