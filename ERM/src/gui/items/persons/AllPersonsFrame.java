package gui.items.persons;

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
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyVetoException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import gui.CoreRowSorter;
import gui.MainFrame;
import lang.Lang;

import javax.naming.ldap.SortKey;
import javax.swing.DefaultRowSorter;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import org.mapdb.Fun.Tuple3;
import org.mapdb.Queues.Stack;

import core.item.persons.PersonCls;
import database.BlockMap;
import database.DBSet;
import database.ItemPersonMap;
import gui.items.AllItemsFrame;
import gui.models.BlocksTableModel;

@SuppressWarnings("serial")
public class AllPersonsFrame extends JInternalFrame {//extends JFrame { //AllItemsFrame {
	
	private TableModelPersons tableModelPersons;
	private JButton ConfirmButton;
	

	public AllPersonsFrame() {
		
		super(Lang.getInstance().translate("All Persons"));
		
		//ICON
		List<Image> icons = new ArrayList<Image>();
		icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon16.png"));
		icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon32.png"));
		icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon64.png"));
		icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon128.png"));
		
		this.setBorder(new EmptyBorder(10, 10, 10, 10));
	//	MainFrame mainFram = new MainFrame();
	
		this.setVisible(true);
		this.setMaximizable(true);
		this.setTitle(Lang.getInstance().translate("Persons"));
		this.setClosable(true);
		this.setResizable(true);
		
	
		
		this.setLocation(50, 20);
	//	this.setIconImages(icons);
		
		//CLOSE
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		//LAYOUT
		this.setLayout(new GridBagLayout());
		
		//PADDING
		((JComponent) this.getContentPane()).setBorder(new EmptyBorder(5, 5, 5, 5));
		
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
	
		
		
		
		
		//CREATE TABLE
		this.tableModelPersons = new TableModelPersons();
		final JTable personsTable = new JTable(this.tableModelPersons);

		/*
		//CHECKBOX FOR DIVISIBLE
		TableColumn divisibleColumn = personsTable.getColumnModel().getColumn(PersonsTableModel.COLUMN_DIVISIBLE);
		divisibleColumn.setCellRenderer(personsTable.getDefaultRenderer(Boolean.class));
		*/
		
		//BLOCKS SORTER
				
				
			
			
		
		
		//ASSETS SORTER
		
//		TableModel tmod = personsTable.getModel();
		//	Class<?> ss = tmod.getColumnClass(0);
		//	TableRowSorter<TableModel> sorter1=new TableRowSorter<TableModel>(personsTable.getModel()); //Создаем сортировщик
	  //      sorter1..setSortable(0, true); //Указываем, что сортировать будем в первой колонке
	    //    sorter1.setSortable(1, true); // а в других нет
	    //    sorter1.setSortable(2, true);
	       //   sorter1.toggleSortOrder(1);                                  //Сортируем  колонку 2
	  
	        
	    //    sorter1.setSortsOnUpdates(true);                         //Указываем автоматически сортировать
		//	personsTable.                                                                    //при изменении модели данных
	   //     personsTable.setAutoCreateRowSorter(true);                        //Устанавливаем сортировщик в таблицу
		
		
	//	  personsTable.setAutoCreateRowSorter(true); 
		Map<Integer, Integer> indexes = new TreeMap<Integer, Integer>();
		indexes.put(TableModelPersons.COLUMN_KEY, ItemPersonMap.DEFAULT_INDEX); // указываем, что первая колонка состоит из чисел
		CoreRowSorter sorter = new CoreRowSorter(this.tableModelPersons, indexes);
	//	sorter.setSortKeys(1,true);
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
				tableModelPersons.getSortableList().setFilter(search);
				tableModelPersons.fireTableDataChanged();
			}
		});
		
		// select row table persons
		//CREATE SEARCH FIELD
		//		final JLabel Address1 = new JLabel(""); //<HTML><input value = '2222' name = 'nnn' id = 'iii' class='cccc'></HTML>");
		
		JEditorPane Address1 = new JEditorPane();
		Address1.setContentType("text/html");
		Address1.setText("<HTML>Select person"); // Document text is provided below.
		// HTMLDocument d = (HTMLDocument) p.getDocument();
		
		
				Address1.setBackground(new Color(255, 255, 255, 0));
			//	Address1.set
			//	JScrollPane Address1 = new JScrollPane();
		// обработка изменения положения курсора в таблице
				personsTable.getSelectionModel().addListSelectionListener(new ListSelectionListener()  {

			@Override
			public void valueChanged(ListSelectionEvent arg0) {
				String Date_Acti;
				String Date_birs;
				String message;
				// TODO Auto-generated method stub
				// устанавливаем формат даты
				SimpleDateFormat formatDate = new SimpleDateFormat("dd.MM.yyyy"); // HH:mm");
				//создаем объект персоны
				PersonCls person = tableModelPersons.getPerson(personsTable.getSelectedRow());
				//читаем таблицу персон.
				Tuple3<Integer, Integer, byte[]> t3 = DBSet.getInstance().getPersonStatusMap().getItem(person.getKey()); //(Long) personsTable.getValueAt(personsTable.getSelectedRow(),0));
				// преобразование в дату
				
				
				if (t3 != null){
				Date_Acti = formatDate.format( new Date(Long.valueOf(t3.a.toString())));
				} else
				{
					Date_Acti =Lang.getInstance().translate("Not found!");}
				
				
				
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
				}
				}else{
				message = "<html><p>"+ Lang.getInstance().translate("Not found!") +"</></>";	
				}
				message = message + "</html>";
						
				
					//	personsTable.getValueAt(personsTable.getSelectedRow(),0).toString() +"  " +  personsTable.getValueAt(personsTable.getSelectedRow(),1).toString() 
					//	+ " Статус:" + formatDate.format(d));	
		//	AllPersonsFrame.this.resize(AllPersonsFrame.this.getSize());
			
				
		//	String a = person.getName().toString();
			
				Address1.setText(message);
				AllPersonsFrame.this.setSize(AllPersonsFrame.this.getSize());//.setPreferredSize(new Dimension(100,100));
			//.get
			
			
			}

			
	      //  SimpleDateFormat formatDate = new SimpleDateFormat("dd.MM.yyyy"); // HH:mm");
	       
					
				});
				
				
				// 
		//	
		
		
//Address1.setText( personsTable.getValueAt(personsTable.getSelectedRow(),0).toString());
		// MENU
	/*
	JPopupMenu nameSalesMenu = new JPopupMenu();
		JMenuItem details = new JMenuItem(Lang.getInstance().translate("Details"));
		details.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int row = personsTable.getSelectedRow();
				row = personsTable.convertRowIndexToModel(row);

				PersonCls person = tableModelPersons.getPerson(row);
				new PersonFrame(person);
			}
		});
		nameSalesMenu.add(details);

		personsTable.setComponentPopupMenu(nameSalesMenu);
*/		
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
	
// button confirm
		  
      //  buttonGBC.gridy = gridy;
        this.ConfirmButton = new JButton(Lang.getInstance().translate("Confirm"));
        this.ConfirmButton.setPreferredSize(new Dimension(100, 25));
        this.ConfirmButton.addActionListener(new ActionListener()
		{
		    public void actionPerformed(ActionEvent e)
		    {
		//        onIssueClick();
		    	//selectOrAdd( new PersonConfirm(), MainFrame.desktopPane.getAllFrames());
		    	 new PersonConfirm();
		    }
		});
    	
        
        		
        
		
		this.add(new JLabel(Lang.getInstance().translate("Search") + ":"), searchLabelGBC);
		this.add(txtSearch, searchGBC);

	//	this.add(new JScrollPane(personsTable), tableGBC);
	//	this.add(this.ConfirmButton, Con_ButtonConfirm);
	//	this.add(new JScrollPane(Address1), tableAccount);
//		this.add(new JLabel(" 1 "), C_empty);
        
        // Create a constraints object, and specify some default values
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH; // components grow in both dimensions
        c.insets = new Insets(0, 5, 5, 0); // 5-pixel margins on all sides

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

       // c.gridx = 4;
       // c.gridy = 2;
       // c.gridwidth = 1;
       // c.gridheight = 2;
       // this.add(new JButton("Button #4"), c);

        
		setPreferredSize(new Dimension(1000, 600));
		//PACK
		this.pack();
		//this.setSize(500, this.getHeight());
		this.setResizable(true);
	//	this.setLocationRelativeTo(null);
		this.setVisible(true);
	}
	
	// подпрограмма выводит в панели окно или передает фокус если окно уже открыто
	// item открываемое окно
	// массив всех открытых окон в панели
	void selectOrAdd(JInternalFrame item, JInternalFrame[] a ){
		    		
		//проверка если уже открыто такое окно то передаем только фокус на него
		int k= -1;
		for (int i=0 ; i < a.length; i=i+1) {
//			String s = a[i].getClass().getName();
			if (a[i].getClass().getName() == item.getClass().getName()){
				k=i;
			}
			
		};
		if (k==-1){
		MainFrame.desktopPane.add(item);
		 try {
			 item.setSelected(true);
	        } catch (java.beans.PropertyVetoException e1) {}
		}
		else {
			try {
				a[k].setSelected(true);
			} catch (PropertyVetoException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}	

}}
