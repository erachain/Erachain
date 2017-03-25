package gui.library;

import java.awt.Component;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.swing.DefaultRowSorter;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import org.mapdb.Fun.Tuple2;

import com.toedter.calendar.JCalendar;

import gui.items.persons.TableModelPersons;
import gui.models.Renderer_Right;



public class MTable extends JTable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public TableModel model;
	public TableRowSorter search_Sorter;
	HashMap<Integer,Tuple2< Object,RowFilter<TableModel, Integer>>> filters;
	MouseListener mouseListener;
	private RowFilter<TableModel, Integer> filter;

	private String[] items;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public MTable(TableModel model) {
		super();
		
		filters = new HashMap();
	// set model
		this.model = model;
		setModel(this.model);
	// height row in table	
		setRowHeight( (int) (getFontMetrics(getFont()).getHeight()));
	// set renders
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		 search_Sorter = new TableRowSorter(this.model);
			this.setRowSorter(search_Sorter);	
			
			
			 items = new String[5];
		        for(int j = 0; j < items.length; j++)
		        {
		            items[j] = "item " + j;
		        }
			
			
			
			mouseListener = new MouseListener(){

				

				private TableCellRenderer rend;

				@Override
				public void mouseClicked(MouseEvent arg0) {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void mouseEntered(MouseEvent arg0) {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void mouseExited(MouseEvent arg0) {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void mousePressed(MouseEvent arg0) {
					// TODO Auto-generated method stub
					if(arg0.getButton() == arg0.BUTTON3) {
				    	JTableHeader th = (JTableHeader)arg0.getSource();
				        Point p = arg0.getPoint();
				        int col = getColumn(th, p);
				        TableColumn column = th.getColumnModel().getColumn(col);
				        if (rend == null)  rend = column.getHeaderRenderer();
				        String ss = "****";
						if (model.getColumnClass(col)== Integer.class ) {
							ss  = "Number";
						}
				        if (model.getColumnClass(col) == String.class)
				        {
				     // Диалоговое окно с полем ввода, инициализируемое initialSelectionValue
				        	String sss = "";
				        	if (filters.get(col) != null){
				        	    	if (filters.get(col).a != null) sss = filters.get(col).a.toString();
				        	}
				        	Object str = JOptionPane.showInputDialog (th, "filter col. = " + column.getHeaderValue(), sss );
				        	if(str != null)
				        	{
				        		if (!str.toString().equals(""))	{
				        			column.setHeaderRenderer(new Renderer_Right());//.setHeaderValue("<HTML><B>" + column.getHeaderValue());
				        			filters.put(col,new Tuple2(str, RowFilter.regexFilter(".*"+  str.toString()  +".*",col)));
				        		}
				        		else {
				        			filters.remove(col);
				        			column.setHeaderRenderer(null);
				        		}
				        	}
				   //     	else filters.clear();           
				        	
				        	
				      //  filters.put(2,RowFilter.regexFilter(".*Отп.*"));
				        }
						
				        if (model.getColumnClass(col) == Date.class){
				        	ss = "Date";
				        	Object sss = null;
				        	if (filters.get(col) != null){
				        	    	if (filters.get(col).a != null) sss = filters.get(col).a;
				        	}
				        	Object str = JOptionPane.showInputDialog (th, "filter col. = " + column.getHeaderValue(),new JCalendar((Date) sss) );
				        	if(str != null)
				        	{
				        		if (!str.toString().equals(""))	filters.put(col,new Tuple2((Date)str, RowFilter.regexFilter(".*"+  str.toString()  +".*",col)));
				        		else filters.remove(col);
				        	}
				 //       	else filters.clear();    
				        	
				        	
				        }
				        String oldValue = (String)column.getHeaderValue() + " " + ss;
				 //       Object value = showEditor(th, col, oldValue);
				        Iterator<Tuple2<Object, RowFilter<TableModel, Integer>>> s = filters.values().iterator();
				        List <RowFilter<TableModel, Integer>> rowfolters = new ArrayList();
				        while(s.hasNext()){
				        	Tuple2<Object, RowFilter<TableModel, Integer>> a = s.next();
				        	rowfolters.add(a.b);
				        }
				        filter = RowFilter.andFilter(rowfolters);
				//        column.setHeaderValue("<HTML><B>" + value);
				        set_Filter();
				        th.resizeAndRepaint();
				    	}	
				}

				@Override
				public void mouseReleased(MouseEvent arg0) {
					// TODO Auto-generated method stub
					
				}
				
			};
			
	//		Date d1 = (Date) model.getValueAt(0, 3);
	 //       Date d2 = (Date) model.getValueAt(model.getRowCount() - 2, 3);
	  //      RowFilter<TableModel, Integer> low = RowFilter.dateFilter(RowFilter.ComparisonType.AFTER, d1, 3);
	  //      RowFilter<TableModel, Integer> high = RowFilter.dateFilter(RowFilter.ComparisonType.BEFORE, d2, 3);
	  //      RowFilter<TableModel, Integer> num = RowFilter.numberFilter(RowFilter.ComparisonType.BEFORE, (Number) 2, 1);
	   //     List<RowFilter<TableModel, Integer>> filters = Arrays.asList(num);//low, high);
	   //     final RowFilter<TableModel, Integer> filter = RowFilter.andFilter(filters);
			
			
/*	
		setDefaultRenderer(Integer.class, new Renderer_Right()); // set renderer
	setDefaultRenderer(String.class, new Renderer_Left()); // set renderer
		setDefaultRenderer(Long.class, new Renderer_Right()); // set renderer
		setDefaultRenderer(Boolean.class, new Renderer_Boolean()); // set renderer
		setDefaultRenderer(Tuple2.class, new Renderer_Left()); // set renderer
		setDefaultRenderer(Date.class, new Renderer_Right()); // set renderer
		setDefaultRenderer(PublicKeyAccount.class, new Renderer_Left()); // set renderer
		//RenderingHints.
		setDefaultRenderer(Double.class, new Renderer_Right()); // set renderer
		
	*/		
		
		
	
	}
	
	public void  show_search(boolean bl){
		
		if (bl)this.getTableHeader().addMouseListener(mouseListener);	
		else this.getTableHeader().removeMouseListener(mouseListener);
		
	}


  
    private int getColumn(JTableHeader th, Point p)
    {
        TableColumnModel model = th.getColumnModel();
        for(int col = 0; col < model.getColumnCount(); col++)
            if(th.getHeaderRect(col).contains(p))
                return col;
        return -1;
    }
    @SuppressWarnings("unchecked")
	private void set_Filter(){

	//	RowFilter filter = RowFilter.regexFilter(".*" + "2" + ".*", 1);
	//	((DefaultRowSorter) this.search_Sorter).setRowFilter(filter);
    //	 this.search_Sorter.setRowFilter(RowFilter.regexFilter(".*Create.*"));
		
		this.search_Sorter.setRowFilter(filter);
		this.setRowSorter( this.search_Sorter);
	//	this.model.fireTableDataChanged();
    	
    	
    	
    	
    }
    
    public Object showEditor(Component parent, int col, String currentValue)
    {
        items[0] = currentValue;
        String message = "select value for column " + (col + 1) + ":";
        Object retVal = JOptionPane.showInputDialog(parent,
                                                    message,
                                                    "table header editor",
                                                    JOptionPane.INFORMATION_MESSAGE,
                                                    null,
                                                    items,
                                                    items[0]);
        if(retVal == null)
            retVal = currentValue;
        return retVal;
    }
    
    
  
  
   
  
   
}
