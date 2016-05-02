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
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

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
		
		//ICON
		List<Image> icons = new ArrayList<Image>();
		icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon16.png"));
		icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon32.png"));
		icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon64.png"));
		icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon128.png"));
		
		this.setBorder(new EmptyBorder(10, 10, 10, 10));
	//	MainFrame mainFram = new MainFrame();
	
	//	this.setVisible(true);
//		this.setMaximizable(true);
//		this.setTitle(Lang.getInstance().translate("Persons"));
//		this.setClosable(true);
//		this.setResizable(true);
		
	
		
		this.setLocation(50, 20);
	//	this.setIconImages(icons);
		
		//CLOSE
//		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		//LAYOUT
		this.setLayout(new GridBagLayout());
		
		//PADDING
//		((JComponent) this.getContentPane()).setBorder(new EmptyBorder(5, 5, 5, 5));
		
		//SEACH LABEL GBC
		GridBagConstraints searchLabelGBC = new GridBagConstraints();
		searchLabelGBC.insets = new Insets(0, 5, 5, 0);
		searchLabelGBC.fill = GridBagConstraints.HORIZONTAL;   
		searchLabelGBC.anchor = GridBagConstraints.NORTHWEST;
		searchLabelGBC.weightx = 0;	
		searchLabelGBC.gridwidth = 1;
		searchLabelGBC.gridx = 0;
		searchLabelGBC.gridy = 0;
		
		//SEACH GBC
		GridBagConstraints searchGBC = new GridBagConstraints();
		searchGBC.insets = new Insets(0, 5, 5, 0);
		searchGBC.fill = GridBagConstraints.HORIZONTAL;   
		searchGBC.anchor = GridBagConstraints.NORTHWEST;
		searchGBC.weightx = 1;	
		searchGBC.gridwidth = 1;
		searchGBC.gridx = 1;
		searchGBC.gridy = 0;
	
		
		//SEACH GBC
				GridBagConstraints ToolB = new GridBagConstraints();
				searchGBC.insets = new Insets(0, 5, 5, 0);
				searchGBC.fill = GridBagConstraints.HORIZONTAL;   
				searchGBC.anchor = GridBagConstraints.NORTHWEST;
				searchGBC.weightx = 1;	
				searchGBC.gridwidth = 1;
				searchGBC.gridx = 1;
				searchGBC.gridy = 0;
		
		
		
		//CREATE TABLE
		this.tableModelPersons = new TableModelPersons();
		final JTable personsTable = new JTable(this.tableModelPersons);
		
		TableColumnModel columnModel = personsTable.getColumnModel(); // read column model
		columnModel.getColumn(0).setMaxWidth((100));
		//columnModel.getColumn(1).setMaxWidth((250));
		//columnModel.getColumn(2).setMaxWidth((120));//sizeWidthToFit();
		//Custom renderer for the String column;
		personsTable.setDefaultRenderer(Long.class, new Renderer_Right()); // set renderer
		personsTable.setDefaultRenderer(String.class, new Renderer_Right()); // set renderer
	//	TableCellRenderer a = personsTable.getDefaultRenderer(Long.class);
	
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
	
		
		personsTable.setFillsViewportHeight(true);
		personsTable.setAutoCreateRowSorter(true);
		
		
				  
		  
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
				tableModelPersons.getSortableList().setFilter(search);
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
		Address1.setText("<HTML>Select person"); // Document text is provided below.
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
						Tuple3<Integer, Integer, byte[]> t3 = DBSet.getInstance().getPersonStatusMap().getItem(person.getKey()); //(Long) personsTable.getValueAt(personsTable.getSelectedRow(),0));
				// преобразование в дату
				
				
						if (t3 != null){
							Date_Acti = formatDate.format( new Date(Long.valueOf(t3.a.toString())));
						} else
						{
							Date_Acti =Lang.getInstance().translate("Not found!");
						};
						if (person.isConfirmed()){
							Date_birs=  formatDate.format(new Date(Long.valueOf(person.getBirthday())));
							 message ="<html><div></div><div> <p><b>" + Lang.getInstance().translate("Key")+":"   + person.getKey()        			+ "</p>"
							+ "<p> <b> "  + Lang.getInstance().translate("Name")+":"       			  + person.getName().toString()		+ "</p>" 
					        + "<p>  "  + Lang.getInstance().translate("To do")  +":"        		  + Date_Acti			+"</p>"
					        + "<p> "  + Lang.getInstance().translate("Birthday")  +":"        	      + Date_birs			+"</p>";
							 // Читаем адреса клиента
							 TreeMap<String, java.util.Stack<Tuple3<Integer, Integer, byte[]>>> Addresses= DBSet.getInstance().getPersonAddressMap().getItems(person.getKey());
							 if ( !Addresses.isEmpty()){
								 message =message + "<p>"  + Lang.getInstance().translate("Account")  +":  <input type='text' size='40' value='"+ Addresses.lastKey() +"' id='iiii' name='nnnn' class= 'cccc' onchange =''><p></div>";
							 }
							 else{
								 message = message + "<p> " +  Lang.getInstance().translate("Account not found!")+ "</p";
								 ConfirmButton.setEnabled(true);
							 }
						}else{
							ConfirmButton.setEnabled(false);
							message = "<html><p>"+ Lang.getInstance().translate("Not found!") +"</></>";	
						}
						message = message + "</html>";
						
							
				Address1.setText(message);
				PersJSpline.setDividerLocation(PersJSpline.getDividerLocation());//.setPreferredSize(new Dimension(100,100));		
			 
			
					
					}
					}
					
					});
				
				
				
	 // tool bar
				// tool bar
				JToolBar tb1 = new JToolBar(" Панель 1"),

						tb2 = new JToolBar(" Панель 2");

						tb1.setRollover(true);

						tb1.add(new JButton(new ImageIcon("Add24.gif"))); tb1.add(new JButton(new ImageIcon("AlignTop24.gif")));

						tb1.add(new JButton(new ImageIcon("About24.gif")));

						tb2.add(new JButton("Первая")); tb2.add(new JButton("Вторая"));

						tb2.add(new JButton("Третья"));

						//add(tb1, BorderLayout.NORTH); 
						//add(tb2, ToolB);

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
	

		this.add(new JLabel(Lang.getInstance().translate("Search") + ":"), searchLabelGBC);
		this.add(txtSearch, searchGBC);
        
        // Create a constraints object, and specify some default values
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH; // components grow in both dimensions
        c.insets = new Insets(0, 5, 5, 0); // 5-pixel margins on all sides
/*
        // Create and add a bunch of buttons, specifying different grid
        // position, and size for each.
        // Give the first button a resize weight of 1.0 and all others
        // a weight of 0.0. The first button will get all extra space.
        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 4;
        c.gridheight = 6;
        c.weightx = c.weighty = 1.0;
        this.add(new JScrollPane(personsTable), c);

        c.gridx = 4;
        c.gridy = 1;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = c.weighty = 0.0;
        this.add(this.ConfirmButton, c);

        
        c.gridx = 4;
        c.gridy = 2;
        c.gridwidth = 1;
        c.gridheight = GridBagConstraints.REMAINDER; //3;
        c.fill = GridBagConstraints.BOTH;//.HORIZONTAL;
        c.anchor = GridBagConstraints.NORTH;
        c.weightx = c.weighty = 0;
       
        this.add(new JScrollPane(Address1), c);
*/
        c.gridx = 0;
        c.gridy = 2;
        c.gridwidth = 4;
        c.gridheight = 2;
        c.weightx = c.weighty = 1.0;
       // this.add(new JButton("Button #4"), c);
        
        
   //     JSplitPane PersJSpline = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,true,new JScrollPane(personsTable),new JScrollPane(Address1)); 
        PersJSpline.setDividerLocation(700);
        this.add(PersJSpline,c);

        
//		setPreferredSize(new Dimension(1000, 600));
		//PACK
//		this.pack();
		//this.setSize(500, this.getHeight());
//		this.setResizable(true);
	//	this.setLocationRelativeTo(null);
		this.setVisible(true);
		
	}
	
	



   
	


}
