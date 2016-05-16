package gui.items.unions;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import gui.CoreRowSorter;
import gui.items.notes.TableModelNotes;
import gui.models.Renderer_Boolean;
import gui.models.Renderer_Right;
import lang.Lang;

import javax.naming.ldap.SortKey;
import javax.swing.DefaultRowSorter;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.RowFilter;
import javax.swing.RowSorter;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableRowSorter;

import org.mapdb.Fun.Tuple3;
import core.item.unions.UnionCls;
import core.item.statuses.StatusCls;
//import database.AddressUnionMap;
import database.DBSet;
import database.ItemUnionMap;



@SuppressWarnings("serial")
public class AllUnionsPanel extends JPanel {
	
	private TableModelUnions tableModelUnions;
	

	public AllUnionsPanel() {
		
		GridBagConstraints gridBagConstraints;
				
		//LAYOUT
		this.setLayout(new GridBagLayout());
			
		//CREATE TABLE
		this.tableModelUnions = new TableModelUnions();
		final JTable unionsTable = new JTable(this.tableModelUnions);
		
		TableColumnModel columnModel = unionsTable.getColumnModel(); // read column model
		columnModel.getColumn(0).setMaxWidth((100));

		//Custom renderer for the String column;
		unionsTable.setDefaultRenderer(Long.class, new Renderer_Right()); // set renderer
		unionsTable.setDefaultRenderer(String.class, new Renderer_Right()); // set renderer
		unionsTable.setDefaultRenderer(Boolean.class, new Renderer_Boolean()); // set renderer
	
	//	cellRenderer render = new CellRenderer();
	//	columnModel.getColumn(0).setCellRenderer(cellRenderer);

		/*
		//CHECKBOX FOR DIVISIBLE
		TableColumn divisibleColumn = unionsTable.getColumnModel().getColumn(UnionsTableModel.COLUMN_DIVISIBLE);
		divisibleColumn.setCellRenderer(unionsTable.getDefaultRenderer(Boolean.class));
		*/
		
		//CHECKBOX FOR FAVORITE
	//	TableColumn favoriteColumn = unionsTable.getColumnModel().getColumn(TableModelUnions.COLUMN_FAVORITE);
	//	favoriteColumn.setCellRenderer(unionsTable.getDefaultRenderer(Boolean.class));

		//BLOCKS SORTER
	//	Map<Integer, Integer> indexes = new TreeMap<Integer, Integer>();
	//	indexes.put(TableModelUnions.COLUMN_KEY, ItemUnionMap.DEFAULT_INDEX);// указываем, что первая колонка состоит из чисел
	//	CoreRowSorter sorter = new CoreRowSorter(this.tableModelUnions, indexes);
	//	unionsTable.setRowSorter(sorter);
	
		
	//	unionsTable.setFillsViewportHeight(true);
	//	unionsTable.setAutoCreateRowSorter(true);
		
		RowSorter sorter =   new TableRowSorter(this.tableModelUnions);

		unionsTable.setRowSorter(sorter);	
		
				  
		  
		//CREATE SEARCH FIELD
		final JTextField txtSearch = new JTextField();

		// UPDATE FILTER ON TEXT CHANGE
		txtSearch.getDocument().addDocumentListener(new DocumentListener() {
			public void changedUpdate(DocumentEvent e) {
				onChange();
			}

			public void removeUpdate(DocumentEvent e) {
				onChange();
			}

			public void insertUpdate(DocumentEvent e) {
				onChange();
			}

			public void onChange() {

				// GET VALUE
				String search = txtSearch.getText();

			 	// SET FILTER
		//		tableModelUnions.getSortableList().setFilter(search);
				tableModelUnions.fireTableDataChanged();
				
				RowFilter filter = RowFilter.regexFilter(".*" + search + ".*", 1);
				((DefaultRowSorter) sorter).setRowFilter(filter);
				
				tableModelUnions.fireTableDataChanged();
				
			}
		});
		
				
		// select row table unions
				
		JEditorPane Address1 = new JEditorPane();
		Address1.setContentType("text/html");
		Address1.setText("<HTML>" + Lang.getInstance().translate("Select union")); // Document text is provided below.
		Address1.setBackground(new Color(255, 255, 255, 0));
		
		
		JSplitPane PersJSpline = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,true,new JScrollPane(unionsTable),new JScrollPane(Address1)); 
		 
		// обработка изменения положения курсора в таблице
		unionsTable.getSelectionModel().addListSelectionListener(new ListSelectionListener()  {
					
			@SuppressWarnings("deprecation")
			@Override
			public void valueChanged(ListSelectionEvent arg0) {
				String dateAlive;
				String date_birthday;
				String message;
				// TODO Auto-generated method stub
				// устанавливаем формат даты
				SimpleDateFormat formatDate = new SimpleDateFormat("dd.MM.yyyy"); // HH:mm");
				//создаем объект персоны
				UnionCls union;
						
			
				if (unionsTable.getSelectedRow() >= 0 ){
					union = tableModelUnions.getUnion(unionsTable.convertRowIndexToModel(unionsTable.getSelectedRow()));
				

				if (union.isConfirmed()){
					date_birthday=  formatDate.format(new Date(Long.valueOf(union.getBirthday())));
					message ="<html><div>#" + "<b>" + union.getKey() + " : " + date_birthday + "</b>"
					+ "<br>" + union.getName().toString() + "</div>";

					message += "<h2>"+ "Statuses" +"</h2>";
					// GETT UNION STATUS for ALIVE
					Tuple3<Long, Integer, byte[]> t3Alive = null; //DBSet.getInstance().getUnionStatusMap().getItem(union.getKey());
			
					if (t3Alive != null){
						if (t3Alive.a == null) dateAlive = "active";
						else dateAlive = formatDate.format( new Date(t3Alive.a));
					} else
					{
						dateAlive = Lang.getInstance().translate("unknown");
					}
					message += "<div>" + Lang.getInstance().translate("ALIVE")+": <b>" + dateAlive +"</b></div>";

					// GETT UNION STATUS for DEAD
					Tuple3<Long, Integer, byte[]> t3Dead = DBSet.getInstance().getUnionStatusMap().getItem(union.getKey(), StatusCls.DEAD_KEY);
			
					if (t3Dead != null){
						if (t3Dead.a == null) dateAlive = "yes";
						else dateAlive = formatDate.format( new Date(t3Dead.a ));
					} else
					{
						dateAlive = Lang.getInstance().translate("unknown");
					}
					message += "<div>" + Lang.getInstance().translate("DEAD")+": <b>" + dateAlive +"</b></div>";

					// GET CERTIFIED ACCOUNTS
					message += "<h2>"+ "Accounts" +"</h2>";
					TreeMap<String, java.util.Stack<Tuple3<Integer, Integer, byte[]>>> addresses= null; //DBSet.getInstance().getUnionAddressMap().getItems(union.getKey());
					if ( !addresses.isEmpty()){
						// for each account seek active date
						String active_date_str;
						for( Map.Entry<String, java.util.Stack<Tuple3<Integer, Integer, byte[]>>> e : addresses.entrySet())
						{
							Tuple3<Integer, Integer, byte[]> active_date = e.getValue().peek();
							if (active_date.a == 0) active_date_str = "active";
							else active_date_str = formatDate.format( new Date(active_date.a * (long)86400000));
							
							message += "<div><input type='text' size='33' value='"+ e.getKey() +"' disabled='disabled' class='disabled' onchange =''>"
									+ " -> <b>" + active_date_str +"</b></div>";
						}
					}
					else{
						message += "<p> " +  Lang.getInstance().translate("Account not found!")+ "</p";
					}					
				} else {
					message = "<html><p>"+ Lang.getInstance().translate("Not found!") +"</p>";	
				}
				message = message + "</html>";
				
					
				Address1.setText(message);
				PersJSpline.setDividerLocation(PersJSpline.getDividerLocation());//.setPreferredSize(new Dimension(100,100));		
					
				}
			}
		});
				
		// MENU
	
		JPopupMenu all_Unions_Table_menu = new JPopupMenu();
		JMenuItem confirm_Menu = new JMenuItem(Lang.getInstance().translate("Confirm"));
		confirm_Menu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				/*
				int row = unionsTable.getSelectedRow();
				row = unionsTable.convertRowIndexToModel(row);

				UnionCls union = tableModelUnions.getUnion(row);
				new UnionFrame(union);
				*/
				// открываем диалоговое окно ввода данных для подтверждения персоны 
				UnionCls union = tableModelUnions.getUnion(unionsTable.getSelectedRow());

		    	UnionConfirmFrame fm = new UnionConfirmFrame(AllUnionsPanel.this, union);	
		    	// обрабатываем полученные данные от диалогового окна
		    	//if(fm.isOK()){
                //    JOptionPane.showMessageDialog(Form1.this, "OK");
                //}
			
			}
		});
		all_Unions_Table_menu.add(confirm_Menu);

		JMenuItem setStatus_Menu = new JMenuItem(Lang.getInstance().translate("Set Status"));
		setStatus_Menu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				UnionCls union = tableModelUnions.getUnion(unionsTable.getSelectedRow());

				UnionSetStatusFrame fm = new UnionSetStatusFrame(AllUnionsPanel.this, union);	
		    	// обрабатываем полученные данные от диалогового окна
		    	//if(fm.isOK()){
                //    JOptionPane.showMessageDialog(Form1.this, "OK");
                //}
			
			}
		});
		all_Unions_Table_menu.add(setStatus_Menu);

		unionsTable.setComponentPopupMenu(all_Unions_Table_menu);
		
		unionsTable.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				Point p = e.getPoint();
				int row = unionsTable.rowAtPoint(p);
				unionsTable.setRowSelectionInterval(row, row);
				
				if(e.getClickCount() == 2)
				{
					row = unionsTable.convertRowIndexToModel(row);
					UnionCls union = tableModelUnions.getUnion(row);
		//			new UnionFrame(union);
					
				}
			}
		});
		
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(8, 10, 8, 8);
        this.add(new JLabel(Lang.getInstance().translate("Search") + ":"), gridBagConstraints);

      
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 8;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(8, 0, 9, 10);
       
        this.add(txtSearch, gridBagConstraints);
        
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 9;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 10, 6, 10);

		
        PersJSpline.setDividerLocation(700);
        this.add(PersJSpline, gridBagConstraints);

        
	}
	
	



   
	


}
