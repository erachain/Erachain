package gui.items.persons;

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
import core.item.persons.PersonCls;
import core.item.statuses.StatusCls;
import database.AddressPersonMap;
import database.DBSet;
import database.ItemPersonMap;



@SuppressWarnings("serial")
public class AllPersonsPanel extends JPanel {
	
	private TableModelPersons tableModelPersons;
	

	public AllPersonsPanel() {
		
		GridBagConstraints gridBagConstraints;
				
		//LAYOUT
		this.setLayout(new GridBagLayout());
			
		//CREATE TABLE
		this.tableModelPersons = new TableModelPersons();
		final JTable personsTable = new JTable(this.tableModelPersons);
		
		TableColumnModel columnModel = personsTable.getColumnModel(); // read column model
		columnModel.getColumn(0).setMaxWidth((100));

		//Custom renderer for the String column;
		personsTable.setDefaultRenderer(Long.class, new Renderer_Right()); // set renderer
		personsTable.setDefaultRenderer(String.class, new Renderer_Right()); // set renderer

	
	//	cellRenderer render = new CellRenderer();
	//	columnModel.getColumn(0).setCellRenderer(cellRenderer);

		/*
		//CHECKBOX FOR DIVISIBLE
		TableColumn divisibleColumn = personsTable.getColumnModel().getColumn(PersonsTableModel.COLUMN_DIVISIBLE);
		divisibleColumn.setCellRenderer(personsTable.getDefaultRenderer(Boolean.class));
		*/
		
		//CHECKBOX FOR FAVORITE
		TableColumn favoriteColumn = personsTable.getColumnModel().getColumn(TableModelPersons.COLUMN_FAVORITE);
		favoriteColumn.setCellRenderer(personsTable.getDefaultRenderer(Boolean.class));

		//BLOCKS SORTER
	//	Map<Integer, Integer> indexes = new TreeMap<Integer, Integer>();
	//	indexes.put(TableModelPersons.COLUMN_KEY, ItemPersonMap.DEFAULT_INDEX);// указываем, что первая колонка состоит из чисел
	//	CoreRowSorter sorter = new CoreRowSorter(this.tableModelPersons, indexes);
	//	personsTable.setRowSorter(sorter);
	
		
	//	personsTable.setFillsViewportHeight(true);
	//	personsTable.setAutoCreateRowSorter(true);
		
		RowSorter sorter =   new TableRowSorter(this.tableModelPersons);

		personsTable.setRowSorter(sorter);	
		
				  
		  
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
		//		tableModelPersons.getSortableList().setFilter(search);
				tableModelPersons.fireTableDataChanged();
				
				RowFilter filter = RowFilter.regexFilter(".*" + search + ".*", 1);
				((DefaultRowSorter) sorter).setRowFilter(filter);
				
				tableModelPersons.fireTableDataChanged();
				
			}
		});
		
				
		// select row table persons
				
		JEditorPane Address1 = new JEditorPane();
		Address1.setContentType("text/html");
		Address1.setText("<HTML>" + Lang.getInstance().translate("Select person")); // Document text is provided below.
		Address1.setBackground(new Color(255, 255, 255, 0));
		
		
		JSplitPane PersJSpline = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,true,new JScrollPane(personsTable),new JScrollPane(Address1)); 
		 
		// обработка изменения положения курсора в таблице
		personsTable.getSelectionModel().addListSelectionListener(new ListSelectionListener()  {
					
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
				PersonCls person;
						
			
				if (personsTable.getSelectedRow() >= 0 ){
					person = tableModelPersons.getPerson(personsTable.convertRowIndexToModel(personsTable.getSelectedRow()));
				

				if (person.isConfirmed()){
					date_birthday=  formatDate.format(new Date(Long.valueOf(person.getBirthday())));
					message ="<html><div>#" + "<b>" + person.getKey() + " : " + date_birthday + "</b>"
					+ "<br>" + person.getName().toString() + "</div>";

					message += "<h2>"+ "Statuses" +"</h2>";
					// GETT PERSON STATUS for ALIVE
					Tuple3<Integer, Integer, byte[]> t3Alive = DBSet.getInstance().getPersonStatusMap().getItem(person.getKey());
			
					if (t3Alive != null){
						if (t3Alive.a == 0) dateAlive = "active";
						else dateAlive = formatDate.format( new Date(t3Alive.a * (long)86400000));
					} else
					{
						dateAlive = Lang.getInstance().translate("unknown");
					}
					message += "<div>" + Lang.getInstance().translate("ALIVE")+": <b>" + dateAlive +"</b></div>";

					// GETT PERSON STATUS for DEAD
					Tuple3<Integer, Integer, byte[]> t3Dead = DBSet.getInstance().getPersonStatusMap().getItem(person.getKey(), StatusCls.DEAD_KEY);
			
					if (t3Dead != null){
						if (t3Dead.a == 0) dateAlive = "yes";
						else dateAlive = formatDate.format( new Date(t3Dead.a * (long)86400000));
					} else
					{
						dateAlive = Lang.getInstance().translate("unknown");
					}
					message += "<div>" + Lang.getInstance().translate("DEAD")+": <b>" + dateAlive +"</b></div>";

					// GET CERTIFIED ACCOUNTS
					message += "<h2>"+ "Accounts" +"</h2>";
					TreeMap<String, java.util.Stack<Tuple3<Integer, Integer, byte[]>>> addresses= DBSet.getInstance().getPersonAddressMap().getItems(person.getKey());
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
	
		JPopupMenu all_Persons_Table_menu = new JPopupMenu();
		JMenuItem confirm_Menu = new JMenuItem(Lang.getInstance().translate("Confirm"));
		confirm_Menu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				/*
				int row = personsTable.getSelectedRow();
				row = personsTable.convertRowIndexToModel(row);

				PersonCls person = tableModelPersons.getPerson(row);
				new PersonFrame(person);
				*/
				// открываем диалоговое окно ввода данных для подтверждения персоны 
				PersonCls person = tableModelPersons.getPerson(personsTable.getSelectedRow());

		    	PersonConfirmFrame fm = new PersonConfirmFrame(AllPersonsPanel.this, person);	
		    	// обрабатываем полученные данные от диалогового окна
		    	//if(fm.isOK()){
                //    JOptionPane.showMessageDialog(Form1.this, "OK");
                //}
			
			}
		});
		all_Persons_Table_menu.add(confirm_Menu);

		JMenuItem setStatus_Menu = new JMenuItem(Lang.getInstance().translate("Set Status"));
		setStatus_Menu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				PersonCls person = tableModelPersons.getPerson(personsTable.getSelectedRow());

				PersonSetStatusFrame fm = new PersonSetStatusFrame(AllPersonsPanel.this, person);	
		    	// обрабатываем полученные данные от диалогового окна
		    	//if(fm.isOK()){
                //    JOptionPane.showMessageDialog(Form1.this, "OK");
                //}
			
			}
		});
		all_Persons_Table_menu.add(setStatus_Menu);

		personsTable.setComponentPopupMenu(all_Persons_Table_menu);
		
		personsTable.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				Point p = e.getPoint();
				int row = personsTable.rowAtPoint(p);
				personsTable.setRowSelectionInterval(row, row);
				
				if(e.getClickCount() == 2)
				{
					row = personsTable.convertRowIndexToModel(row);
					PersonCls person = tableModelPersons.getPerson(row);
		//			new PersonFrame(person);
					
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
