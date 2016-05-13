package gui.items.persons;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.DefaultRowSorter;
import javax.swing.JInternalFrame;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.RowSorter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableRowSorter;

import core.item.persons.PersonCls;
import gui.Frame_All;
import gui.MainFrame;
import gui.Main_Internal_Frame;
import gui.Menu;
import gui.Split_Panel;
import gui.models.Renderer_Right;
import gui.models.WalletItemPersonsTableModel;
import lang.Lang;

public class MainPersonsFrame extends Main_Internal_Frame{ //Frame_All{ //JInternalFrame {
/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
private TableModelPersons tableModelPersons;

public MainPersonsFrame(){
// not show buttons main Toolbar
	 this.jButton2_jToolBar.setVisible(false);
	 this.jButton3_jToolBar.setVisible(false);
// buttun1
	 this.jButton1_jToolBar.setText(Lang.getInstance().translate("Issue Person"));
	 
	 this.jButton1_jToolBar.addActionListener(new ActionListener()
		{
		    public void actionPerformed(ActionEvent e)
		    {
		    	 Menu.selectOrAdd( new IssuePersonFrame(), MainFrame.desktopPane.getAllFrames());
		    }

			
		});	
	 
// all persons 
	Split_Panel search_Person_SplitPanel = new Split_Panel();
	search_Person_SplitPanel.setName("Search Persons");
	search_Person_SplitPanel.searthLabel_SearchToolBar_LeftPanel.setText(Lang.getInstance().translate("Search") +":  ");
	// not show buttons
	search_Person_SplitPanel.button1_ToolBar_LeftPanel.setVisible(false);
	search_Person_SplitPanel.button2_ToolBar_LeftPanel.setVisible(false);
	search_Person_SplitPanel.jButton1_jToolBar_RightPanel.setVisible(false);
	search_Person_SplitPanel.jButton2_jToolBar_RightPanel.setVisible(false);
	//CREATE TABLE
		this.tableModelPersons = new TableModelPersons();
		final JTable personsTable = new JTable(this.tableModelPersons);
		TableColumnModel columnModel = personsTable.getColumnModel(); // read column model
		columnModel.getColumn(0).setMaxWidth((100));
	//Custom renderer for the String column;
		personsTable.setDefaultRenderer(Long.class, new Renderer_Right()); // set renderer
		personsTable.setDefaultRenderer(String.class, new Renderer_Right()); // set renderer
	//CHECKBOX FOR FAVORITE
		TableColumn favoriteColumn = personsTable.getColumnModel().getColumn(TableModelPersons.COLUMN_FAVORITE);
		favoriteColumn.setCellRenderer(personsTable.getDefaultRenderer(Boolean.class));
	//Sorter
		RowSorter sorter =   new TableRowSorter(this.tableModelPersons);
		personsTable.setRowSorter(sorter);	
		// UPDATE FILTER ON TEXT CHANGE
		
		search_Person_SplitPanel.searchTextField_SearchToolBar_LeftPanel.getDocument().addDocumentListener(new DocumentListener() {
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
						String search = search_Person_SplitPanel.searchTextField_SearchToolBar_LeftPanel.getText();

					 	// SET FILTER
				//		tableModelPersons.getSortableList().setFilter(search);
						tableModelPersons.fireTableDataChanged();
						
						RowFilter filter = RowFilter.regexFilter(".*" + search + ".*", 1);
						((DefaultRowSorter) sorter).setRowFilter(filter);
						
						tableModelPersons.fireTableDataChanged();
						
					}
				});
		
		
		
		

	// set video			
				//jTable_jScrollPanel_Panel2_Tabbed_Panel_Left_Panel.setModel(this.tableModelPersons);
		search_Person_SplitPanel.jTable_jScrollPanel_LeftPanel.setModel(this.tableModelPersons);
				//jTable_jScrollPanel_Panel2_Tabbed_Panel_Left_Panel = personsTable;
		search_Person_SplitPanel.jTable_jScrollPanel_LeftPanel = personsTable;
				//jScrollPanel_Panel2_Tabbed_Panel_Left_Panel.setViewportView(jTable_jScrollPanel_Panel2_Tabbed_Panel_Left_Panel); // personsTable; 
		search_Person_SplitPanel.jScrollPanel_LeftPanel.setViewportView(search_Person_SplitPanel.jTable_jScrollPanel_LeftPanel);
	// select row table persons

		
		Person_Info info = new Person_Info(); 
		//		
	// обработка изменения положения курсора в таблице
				 //jTable_jScrollPanel_Panel2_Tabbed_Panel_Left_Panel.getSelectionModel().addListSelectionListener(new ListSelectionListener()  {
				 search_Person_SplitPanel.jTable_jScrollPanel_LeftPanel.getSelectionModel().addListSelectionListener(new ListSelectionListener()  {
					@SuppressWarnings("deprecation")
					@Override
					public void valueChanged(ListSelectionEvent arg0) {
						PersonCls person = null;
						if (personsTable.getSelectedRow() >= 0 ) person = tableModelPersons.getPerson(personsTable.convertRowIndexToModel(personsTable.getSelectedRow()));
						info.show_001(person);
						
						search_Person_SplitPanel.jSplitPanel.setDividerLocation(search_Person_SplitPanel.jSplitPanel.getDividerLocation());	
						search_Person_SplitPanel.searchTextField_SearchToolBar_LeftPanel.setEnabled(true);
					}
				});
		
		
		
		
//		Person_Info info = new Person_Info();
//		info.show_001(null);
		//this.Panel_Right_Panel.add(info);
		 //this.jScrollPane_Panel_Right_Panel.setViewportView(info);
				 search_Person_SplitPanel.jScrollPane_jPanel_RightPanel.setViewportView(info);
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

					    	PersonConfirmFrame fm = new PersonConfirmFrame(MainPersonsFrame.this, person);	
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

							PersonSetStatusFrame fm = new PersonSetStatusFrame(MainPersonsFrame.this, person);	
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
				 
				 
				 
				 
				 
		
// My persons		
		
	Split_Panel my_Person_SplitPanel = new Split_Panel();
	my_Person_SplitPanel.setName("My Persons");
	my_Person_SplitPanel.searthLabel_SearchToolBar_LeftPanel.setText(Lang.getInstance().translate("Search") +":  ");
	// not show buttons
	my_Person_SplitPanel.button1_ToolBar_LeftPanel.setVisible(false);
	my_Person_SplitPanel.button2_ToolBar_LeftPanel.setVisible(false);
	my_Person_SplitPanel.jButton1_jToolBar_RightPanel.setVisible(false);
	my_Person_SplitPanel.jButton2_jToolBar_RightPanel.setVisible(false);
	
	//TABLE
			final WalletItemPersonsTableModel personsModel = new WalletItemPersonsTableModel();
			final JTable table = new JTable(personsModel);
			TableRowSorter sorter1 = new TableRowSorter(personsModel);
			table.setRowSorter(sorter1);
			table.getRowSorter();
			personsModel.fireTableDataChanged();
			
			//CHECKBOX FOR CONFIRMED
			TableColumn confirmedColumn = table.getColumnModel().getColumn(WalletItemPersonsTableModel.COLUMN_CONFIRMED);
			confirmedColumn.setCellRenderer(table.getDefaultRenderer(Boolean.class));
			
			//CHECKBOX FOR FAVORITE
			favoriteColumn = table.getColumnModel().getColumn(WalletItemPersonsTableModel.COLUMN_FAVORITE);
			favoriteColumn.setCellRenderer(table.getDefaultRenderer(Boolean.class));

			
			 columnModel = table.getColumnModel(); // read column model
			columnModel.getColumn(0).setMaxWidth((100));
			
			//Custom renderer for the String column;
					table.setDefaultRenderer(Long.class, new Renderer_Right()); // set renderer
					table.setDefaultRenderer(String.class, new Renderer_Right()); // set renderer
					table.setDefaultRenderer(Boolean.class, new Renderer_Right()); // set renderer
			
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
							personsModel.fireTableDataChanged();
							
							RowFilter filter = RowFilter.regexFilter(".*" + search + ".*", 1);
							((DefaultRowSorter) sorter1).setRowFilter(filter);
							
							personsModel.fireTableDataChanged();
							
						}
					});
	
	

					// set video			
					//jTable_jScrollPanel_Panel2_Tabbed_Panel_Left_Panel.setModel(this.tableModelPersons);
			my_Person_SplitPanel.jTable_jScrollPanel_LeftPanel.setModel(personsModel);
					//jTable_jScrollPanel_Panel2_Tabbed_Panel_Left_Panel = personsTable;
			my_Person_SplitPanel.jTable_jScrollPanel_LeftPanel = table;
					//jScrollPanel_Panel2_Tabbed_Panel_Left_Panel.setViewportView(jTable_jScrollPanel_Panel2_Tabbed_Panel_Left_Panel); // personsTable; 
			my_Person_SplitPanel.jScrollPanel_LeftPanel.setViewportView(my_Person_SplitPanel.jTable_jScrollPanel_LeftPanel);		
					
					
					
					// select row table persons
					
					 Person_Info info1 = new Person_Info();
					// JSplitPane PersJSpline = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,true,new JScrollPane(table),new JScrollPane(info1)); 
					 
				//	 my_Person_SplitPanel.jTable_jScrollPanel_LeftPanel = table;
					
					 
					 
					// обработка изменения положения курсора в таблице
					table.getSelectionModel().addListSelectionListener(new ListSelectionListener()  {
						@SuppressWarnings("deprecation")
						@Override
						public void valueChanged(ListSelectionEvent arg0) {
							PersonCls person =null;
							if (table.getSelectedRow() >= 0 )person = personsModel.getItem(table.convertRowIndexToModel(table.getSelectedRow()));
							info1.show_002(person);
						//	PersJSpline.setDividerLocation(PersJSpline.getDividerLocation());
							my_Person_SplitPanel.jSplitPanel.setDividerLocation(my_Person_SplitPanel.jSplitPanel.getDividerLocation());	
							my_Person_SplitPanel.searchTextField_SearchToolBar_LeftPanel.setEnabled(true);
						}
					});
	
	
	
					 my_Person_SplitPanel.jScrollPane_jPanel_RightPanel.setViewportView(info1);
	
	
	
	
	
	
	
	
	this.jTabbedPane.add(search_Person_SplitPanel);
	this.jTabbedPane.add(my_Person_SplitPanel);
	
	
	
	
	//Frame_All frame = new Frame_All();
/*	
	this.setTitle(Lang.getInstance().translate("Persons"));
	this.search_Label_Panel1_Tabbed_Panel_Left_Panel.setText(Lang.getInstance().translate("Search") +":"); 
	this.search_Label_Panel2_Tabbed_Panel_Left_Panel.setText(Lang.getInstance().translate("Search") +":");
	this.Search_jLabel.setText(Lang.getInstance().translate("Search") +":");
	

	
	
	
	//CREATE TABLE
	this.tableModelPersons = new TableModelPersons();
	final JTable personsTable = new JTable(this.tableModelPersons);
	TableColumnModel columnModel = personsTable.getColumnModel(); // read column model
	columnModel.getColumn(0).setMaxWidth((100));
//Custom renderer for the String column;
	personsTable.setDefaultRenderer(Long.class, new Renderer_Right()); // set renderer
	personsTable.setDefaultRenderer(String.class, new Renderer_Right()); // set renderer
//CHECKBOX FOR FAVORITE
	TableColumn favoriteColumn = personsTable.getColumnModel().getColumn(TableModelPersons.COLUMN_FAVORITE);
	favoriteColumn.setCellRenderer(personsTable.getDefaultRenderer(Boolean.class));
//Sorter
	RowSorter sorter =   new TableRowSorter(this.tableModelPersons);
	personsTable.setRowSorter(sorter);	
	// UPDATE FILTER ON TEXT CHANGE
			this.search_TextField_Panel2_Tabbed_Panel_Left_Panel.getDocument().addDocumentListener(new DocumentListener() {
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
					String search = search_TextField_Panel2_Tabbed_Panel_Left_Panel.getText();

				 	// SET FILTER
			//		tableModelPersons.getSortableList().setFilter(search);
					tableModelPersons.fireTableDataChanged();
					
					RowFilter filter = RowFilter.regexFilter(".*" + search + ".*", 1);
					((DefaultRowSorter) sorter).setRowFilter(filter);
					
					tableModelPersons.fireTableDataChanged();
					
				}
			});
	
	
	
	

// set video			
			jTable_jScrollPanel_Panel2_Tabbed_Panel_Left_Panel.setModel(this.tableModelPersons);
			jTable_jScrollPanel_Panel2_Tabbed_Panel_Left_Panel = personsTable;
			jScrollPanel_Panel2_Tabbed_Panel_Left_Panel.setViewportView(jTable_jScrollPanel_Panel2_Tabbed_Panel_Left_Panel); // personsTable; 
// select row table persons
			 Person_Info info = new Person_Info(); 
	//		
// обработка изменения положения курсора в таблице
			 jTable_jScrollPanel_Panel2_Tabbed_Panel_Left_Panel.getSelectionModel().addListSelectionListener(new ListSelectionListener()  {
				@SuppressWarnings("deprecation")
				@Override
				public void valueChanged(ListSelectionEvent arg0) {
					PersonCls person = null;
					if (personsTable.getSelectedRow() >= 0 ) person = tableModelPersons.getPerson(personsTable.convertRowIndexToModel(personsTable.getSelectedRow()));
					info.show_001(person);
					
					jSplitPane2.setDividerLocation(jSplitPane2.getDividerLocation());	
				}
			});
	
	
	
	
//	Person_Info info = new Person_Info();
//	info.show_001(null);
	//this.Panel_Right_Panel.add(info);
	 this.jScrollPane_Panel_Right_Panel.setViewportView(info);
	
	
	
	
	
	
	
	this.jSplitPane2.setDividerLocation(700);
*/	
	this.pack();
	this.setSize(800,600);
	this.setMaximizable(true);
	
	this.setClosable(true);
	this.setResizable(true);
//	this.setSize(new Dimension( (int)parent.getSize().getWidth()-80,(int)parent.getSize().getHeight()-150));
	this.setLocation(20, 20);
//	this.setIconImages(icons);
	//CLOSE
	setDefaultCloseOperation(JInternalFrame.DISPOSE_ON_CLOSE);
    this.setResizable(true);
//    splitPane_1.setDividerLocation((int)((double)(this.getHeight())*0.7));//.setDividerLocation(.8);
    //my_person_panel.requestFocusInWindow();
    this.setVisible(true);
	
}

}
