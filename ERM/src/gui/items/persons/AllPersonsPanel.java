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
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableRowSorter;

import org.mapdb.Fun.Tuple3;
import core.item.persons.PersonCls;
import database.AddressPersonMap;
import database.DBSet;
import database.ItemPersonMap;



@SuppressWarnings("serial")
public class AllPersonsPanel extends JPanel {//JInternalFrame {
	
	private TableModelPersons tableModelPersons;
	private JButton ConfirmButton;
	

	public AllPersonsPanel() {
		
//		super(Lang.getInstance().translate("All Persons"));
		GridBagConstraints gridBagConstraints;
		
		//ICON
		List<Image> icons = new ArrayList<Image>();
		icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon16.png"));
		icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon32.png"));
		icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon64.png"));
		icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon128.png"));
		
		this.setBorder(new EmptyBorder(10, 10, 10, 10));
		
		this.setLocation(50, 20);
	//	this.setIconImages(icons);
		
		
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
		
		
		// button confirm
	        ConfirmButton = new JButton(Lang.getInstance().translate("Confirm"));
	        ConfirmButton.setPreferredSize(new Dimension(100, 25));
	        ConfirmButton.setEnabled(false);
	        ConfirmButton.addActionListener(new ActionListener()
			{
			    public void actionPerformed(ActionEvent e)
			    {
			// открываем диалоговое окно ввода данных для подтверждения персоны 
		//	    	PersonConfirm fm = new PersonConfirm(AllPersonsFrame.this);	
			// обрабатываем полученные данные от диалогового окна
			    	//if(fm.isOK()){
	                //    JOptionPane.showMessageDialog(Form1.this, "OK");
	                //}
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
						String Date_Acti;
						String Date_birs;
						String message;
				// TODO Auto-generated method stub
				// устанавливаем формат даты
						SimpleDateFormat formatDate = new SimpleDateFormat("dd.MM.yyyy"); // HH:mm");
				//создаем объект персоны
						PersonCls person;
						
						
						if (personsTable.getSelectedRow() >= 0 ){
							person = tableModelPersons.getPerson(personsTable.convertRowIndexToModel(personsTable.getSelectedRow()));
						
						
						//читаем таблицу персон.
						Tuple3<Integer, Integer, byte[]> t3 = DBSet.getInstance().getPersonStatusMap().getItem(person.getKey());
						// преобразование в дату
				
				
						if (t3 != null){
							if (t3.b == 0) Date_Acti = "+";
							else Date_Acti = formatDate.format( new Date(Long.valueOf(t3.b.toString())));
						} else
						{
							Date_Acti = Lang.getInstance().translate("Not found!");
						}
						
						if (person.isConfirmed()){
							Date_birs=  formatDate.format(new Date(Long.valueOf(person.getBirthday())));
							 message ="<html><div></div><div> <p><b>" + Lang.getInstance().translate("Key")+":"   + person.getKey()        			+ "</p>"
							+ "<p> <b> " + Lang.getInstance().translate("Name")+": " + person.getName().toString() + "</p>" 
					        + "<p>" + Lang.getInstance().translate("Birthday")+": " + Date_birs +"</p>"
					        + "<p><b>" + Lang.getInstance().translate("ALIVE")+": " + Date_Acti +"</b></p>";

							 // Читаем адреса клиента
							 TreeMap<String, java.util.Stack<Tuple3<Integer, Integer, byte[]>>> Addresses= DBSet.getInstance().getPersonAddressMap().getItems(person.getKey());
							 if ( !Addresses.isEmpty()){
								 message += "<p>"  + Lang.getInstance().translate("Account")  +":  <input type='text' size='40' value='"+ Addresses.lastKey() +"' id='iiii' name='nnnn' class= 'cccc' onchange =''><p></div>";
								 
							 }
							 else{
								 message += "<p> " +  Lang.getInstance().translate("Account not found!")+ "</p";
								
							 }
						}else{
							
							message = "<html><p>"+ Lang.getInstance().translate("Not found!") +"</></>";	
						}
						message = message + "</html>";
						
							
						Address1.setText(message);
						PersJSpline.setDividerLocation(PersJSpline.getDividerLocation());//.setPreferredSize(new Dimension(100,100));		
					
					}
				}
				
			});
				
				
				
	/*
				// tool bar
				JToolBar tb1 = new JToolBar(" Панель 1"),

						tb2 = new JToolBar(" Панель 2");

						tb1.setRollover(true);

						tb1.add(new JButton(new ImageIcon("Add24.gif"))); tb1.add(new JButton(new ImageIcon("AlignTop24.gif")));

						tb1.add(new JButton(new ImageIcon("About24.gif")));

						tb2.add(new JButton("Первая")); tb2.add(new JButton("Вторая"));

						tb2.add(new JButton("Третья"));

		*/			

		// MENU
	
	JPopupMenu All_Persons_Table_menu = new JPopupMenu();
		JMenuItem Confirm_Menu = new JMenuItem(Lang.getInstance().translate("Confirm"));
		Confirm_Menu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				/*
				int row = personsTable.getSelectedRow();
				row = personsTable.convertRowIndexToModel(row);

				PersonCls person = tableModelPersons.getPerson(row);
				new PersonFrame(person);
				*/
				// открываем диалоговое окно ввода данных для подтверждения персоны 
				PersonCls person = tableModelPersons.getPerson(personsTable.getSelectedRow());

		    	PersonConfirm fm = new PersonConfirm(AllPersonsPanel.this, person);	
		// обрабатываем полученные данные от диалогового окна
		    	//if(fm.isOK()){
                //    JOptionPane.showMessageDialog(Form1.this, "OK");
                //}
			
			}
		});
		All_Persons_Table_menu.add(Confirm_Menu);

		personsTable.setComponentPopupMenu(All_Persons_Table_menu);
		
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

        
//		setPreferredSize(new Dimension(1000, 600));
		//PACK
//		this.pack();
		//this.setSize(500, this.getHeight());
//		this.setResizable(true);
	//	this.setLocationRelativeTo(null);
		this.setVisible(true);
		
	}
	
	



   
	


}
