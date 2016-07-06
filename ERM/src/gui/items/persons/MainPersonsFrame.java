package gui.items.persons;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.DefaultRowSorter;
import javax.swing.JInternalFrame;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.RowSorter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableRowSorter;

import controller.Controller;
import core.item.assets.AssetCls;
import core.item.persons.PersonCls;
import gui.MainFrame;
import gui.Main_Internal_Frame;
import gui.Split_Panel;
import gui.items.assets.IssueAssetPanel;
import gui.models.Renderer_Boolean;
import gui.models.Renderer_Left;
import gui.models.Renderer_Right;
import gui.models.WalletItemAssetsTableModel;
import gui.models.WalletItemPersonsTableModel;
import lang.Lang;

public class MainPersonsFrame extends Main_Internal_Frame{
	private static final long serialVersionUID = 2717571093561259483L;

	private TableModelPersons tableModelPersons;
	private WalletItemPersonsTableModel personsModel;
	
	public MainPersonsFrame(){
	
		// not show buttons main Toolbar
		this.setTitle(Lang.getInstance().translate("Persons"));
		this.jButton2_jToolBar.setVisible(false);
		this.jButton3_jToolBar.setVisible(false);
		this.jToolBar.setVisible(false);
		// buttun1
		this.jButton1_jToolBar.setText(Lang.getInstance().translate("Issue Person"));
		// status panel
		this.jLabel_status_jPanel.setText(Lang.getInstance().translate("Work with persons"));
	 
		this.jButton1_jToolBar.addActionListener(
			new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
			    {
				    // Menu.selectOrAdd( new IssuePersonFrame(), MainFrame.desktopPane.getAllFrames());
			    	 new IssuePersonDialog();
			    }
		
					
			});	
	
		///////////////////////
		// ALL PERSONS
		///////////////////////
		
		Split_Panel search_Person_SplitPanel = new Split_Panel();
		search_Person_SplitPanel.setName(Lang.getInstance().translate("Search Persons"));
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
		personsTable.setDefaultRenderer(String.class, new Renderer_Left()); // set renderer
	
		//CHECKBOX FOR FAVORITE
		TableColumn favoriteColumn = personsTable.getColumnModel().getColumn(TableModelPersons.COLUMN_FAVORITE);	
		favoriteColumn.setCellRenderer(new Renderer_Boolean()); //personsTable.getDefaultRenderer(Boolean.class));
		favoriteColumn.setMinWidth(50);
		favoriteColumn.setMaxWidth(50);
		favoriteColumn.setPreferredWidth(50);//.setWidth(30);
	
		// personsTable.setAutoResizeMode(5);//.setAutoResizeMode(mode);.setAutoResizeMode(0);
		//Sorter
		RowSorter sorter = new TableRowSorter(this.tableModelPersons);
		personsTable.setRowSorter(sorter);	
	
		// UPDATE FILTER ON TEXT CHANGE
	
		search_Person_SplitPanel.searchTextField_SearchToolBar_LeftPanel.getDocument()
			.addDocumentListener(new DocumentListener() {
				
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
					//tableModelPersons.getSortableList().setFilter(search);
					tableModelPersons.fireTableDataChanged();
					
					RowFilter filter = RowFilter.regexFilter(".*" + search + ".*", 1);
					((DefaultRowSorter) sorter).setRowFilter(filter);
					
					tableModelPersons.fireTableDataChanged();
					
				}
			});
	
		// SET VIDEO			
		search_Person_SplitPanel.jTable_jScrollPanel_LeftPanel.setModel(this.tableModelPersons);
		search_Person_SplitPanel.jTable_jScrollPanel_LeftPanel = personsTable;
		search_Person_SplitPanel.jScrollPanel_LeftPanel.setViewportView(search_Person_SplitPanel.jTable_jScrollPanel_LeftPanel);
		
		search_Person_SplitPanel.setRowHeightFormat(true);
	
		// select row table persons as html	
		Person_Info info = new Person_Info(); 
		info.setFocusable(false);
	
		//		
		// Event LISTENER		
		search_Person_SplitPanel.jTable_jScrollPanel_LeftPanel.getSelectionModel().addListSelectionListener(new ListSelectionListener()  {
			@Override
			public void valueChanged(ListSelectionEvent arg0) {
				PersonCls person = null;
				if (personsTable.getSelectedRow() >= 0 ) person = tableModelPersons.getPerson(personsTable.convertRowIndexToModel(personsTable.getSelectedRow()));
				
			//	info.show_001(person);
				
			//	search_Person_SplitPanel.jSplitPanel.setDividerLocation(search_Person_SplitPanel.jSplitPanel.getDividerLocation());	
			//	search_Person_SplitPanel.searchTextField_SearchToolBar_LeftPanel.setEnabled(true);
				Person_info_panel_001 info_panel = new Person_info_panel_001(person, false);
				info_panel.setPreferredSize(new Dimension(search_Person_SplitPanel.jScrollPane_jPanel_RightPanel.getSize().width-50,search_Person_SplitPanel.jScrollPane_jPanel_RightPanel.getSize().height-50));
				search_Person_SplitPanel.jScrollPane_jPanel_RightPanel.setViewportView(info_panel);
			}
		});
		
		//search_Person_SplitPanel.jScrollPane_jPanel_RightPanel.setViewportView(info);
		
		
		//////////////////////////
		// MENU
		JPopupMenu all_Persons_Table_menu = new JPopupMenu();
		JMenuItem favorite = new JMenuItem(Lang.getInstance().translate("Exchange"));
		favorite.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				favorite_all(personsTable);

							
				
				
				
			}
		});
		
		all_Persons_Table_menu.addPopupMenuListener(new PopupMenuListener(){

			@Override
			public void popupMenuCanceled(PopupMenuEvent arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void popupMenuWillBecomeInvisible(PopupMenuEvent arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void popupMenuWillBecomeVisible(PopupMenuEvent arg0) {
				// TODO Auto-generated method stub
				
				int row = personsTable.getSelectedRow();
				row = personsTable.convertRowIndexToModel(row);
				PersonCls asset = tableModelPersons.getPerson(row);
				
				//IF ASSET CONFIRMED AND NOT ERM
				
					favorite.setVisible(true);
					//CHECK IF FAVORITES
					if(Controller.getInstance().isItemFavorite(asset))
					{
						favorite.setText(Lang.getInstance().translate("Remove Favorite"));
					}
					else
					{
						favorite.setText(Lang.getInstance().translate("Add Favorite"));
					}
					/*	
					//this.favoritesButton.setPreferredSize(new Dimension(200, 25));
					this.favoritesButton.addActionListener(new ActionListener()
					{
						public void actionPerformed(ActionEvent e)
						{
							onFavoriteClick();
						}
					});	
					this.add(this.favoritesButton, labelGBC);
					*/
				
			
			
			
			
			}
			
		}
		
		);
		
		
		all_Persons_Table_menu.add(favorite);
		
		
		
		
		
		JMenuItem confirm_Menu = new JMenuItem(Lang.getInstance().translate("Confirm"));
		confirm_Menu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// OPEN FARME FOR CONFIRM PERSON 
				PersonCls person = tableModelPersons.getPerson(personsTable.convertRowIndexToModel(personsTable.getSelectedRow()));
	
		    	PersonConfirmDialog fm = new PersonConfirmDialog(MainPersonsFrame.this, person);	
	
		    	// OK EVENT
		    	//if(fm.isOK()){
	            //    JOptionPane.showMessageDialog(Form1.this, "OK");
	            //}
			
			}
		});
		all_Persons_Table_menu.add(confirm_Menu);
	
		JMenuItem setStatus_Menu = new JMenuItem(Lang.getInstance().translate("Set Status"));
		setStatus_Menu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
	
				PersonCls person = tableModelPersons.getPerson(personsTable.convertRowIndexToModel(personsTable.getSelectedRow()));
	
				PersonSetStatusDialog fm = new PersonSetStatusDialog(MainPersonsFrame.this, person);	
				
		    	// OK EVENT
		    	//if(fm.isOK()){
	            //    JOptionPane.showMessageDialog(Form1.this, "OK");
	            //}
			
			}
		});
		all_Persons_Table_menu.add(setStatus_Menu);
	
		personsTable.setComponentPopupMenu(all_Persons_Table_menu);
		
		// DOUBLE CLICK EVENT
		
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
			
				if(e.getClickCount() == 1 & e.getButton() == e.BUTTON1)
				{
					
					if (personsTable.getSelectedColumn() == TableModelPersons.COLUMN_FAVORITE){
						row = personsTable.convertRowIndexToModel(row);
						PersonCls asset = tableModelPersons.getPerson(row);
						favorite_all( personsTable);	
						
						
						
					}
					
					
				}
			
			
			
			
			
			
			}
			
			
			
			
			
			
		});
		
	 
		//////////////////////////////////////	
		// MY PERSONS
		//////////////////////////////////////
		Split_Panel my_Person_SplitPanel = new Split_Panel();
		my_Person_SplitPanel.setName(Lang.getInstance().translate("My Persons"));
		my_Person_SplitPanel.searthLabel_SearchToolBar_LeftPanel.setText(Lang.getInstance().translate("Search") +":  ");
		// not show buttons
		my_Person_SplitPanel.button1_ToolBar_LeftPanel.setVisible(false);
		my_Person_SplitPanel.button2_ToolBar_LeftPanel.setVisible(false);
		my_Person_SplitPanel.jButton1_jToolBar_RightPanel.setVisible(false);
		my_Person_SplitPanel.jButton2_jToolBar_RightPanel.setVisible(false);
		
		//TABLE
		 personsModel = new WalletItemPersonsTableModel();
		final JTable table = new JTable(personsModel);
		
		columnModel = table.getColumnModel(); // read column model
			columnModel.getColumn(0).setMaxWidth((100));
		
		//Custom renderer for the String column;
		table.setDefaultRenderer(Long.class, new Renderer_Right()); // set renderer
		table.setDefaultRenderer(String.class, new Renderer_Left()); // set renderer
				
				
		TableRowSorter sorter1 = new TableRowSorter(personsModel);
		table.setRowSorter(sorter1);
		table.getRowSorter();
		if (personsModel.getRowCount() > 0) personsModel.fireTableDataChanged();
		
		//CHECKBOX FOR CONFIRMED
		TableColumn confirmedColumn = table.getColumnModel().getColumn(WalletItemPersonsTableModel.COLUMN_CONFIRMED);
		// confirmedColumn.setCellRenderer(table.getDefaultRenderer(Boolean.class));
		confirmedColumn.setCellRenderer(new Renderer_Boolean());
		confirmedColumn.setMinWidth(50);
		confirmedColumn.setMaxWidth(50);
		confirmedColumn.setPreferredWidth(50);//.setWidth(30);
		
		
		//CHECKBOX FOR FAVORITE
		favoriteColumn = table.getColumnModel().getColumn(WalletItemPersonsTableModel.COLUMN_FAVORITE);
		//favoriteColumn.setCellRenderer(table.getDefaultRenderer(Boolean.class));
		favoriteColumn.setCellRenderer(new Renderer_Boolean());
		favoriteColumn.setMinWidth(50);
		favoriteColumn.setMaxWidth(50);
		favoriteColumn.setPreferredWidth(50);//.setWidth(30);
		
		// TableColumn keyColumn = table.getColumnModel().getColumn(WalletItemPersonsTableModel.COLUMN_KEY);
		//keyColumn.setCellRenderer(new Renderer_Right());
		
		//TableColumn nameColumn = table.getColumnModel().getColumn(WalletItemPersonsTableModel.COLUMN_NAME);
		//nameColumn.setCellRenderer(new Renderer_Left());
		
		//TableColumn addrColumn = table.getColumnModel().getColumn(WalletItemPersonsTableModel.COLUMN_ADDRESS);
		//addrColumn.setCellRenderer(new Renderer_Left());
				
		//CREATE SEARCH FIELD
		//	final JTextField txtSearch = new JTextField();
		
		// UPDATE FILTER ON TEXT CHANGE
		my_Person_SplitPanel.searchTextField_SearchToolBar_LeftPanel.getDocument().addDocumentListener(new DocumentListener() {
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
				//String search = txtSearch.getText();
				String search = my_Person_SplitPanel.searchTextField_SearchToolBar_LeftPanel.getText();
				// SET FILTER
				//tableModelPersons.getSortableList().setFilter(search);
				personsModel.fireTableDataChanged();
			
				RowFilter filter = RowFilter.regexFilter(".*" + search + ".*", 1);
				((DefaultRowSorter) sorter1).setRowFilter(filter);
					
				personsModel.fireTableDataChanged();
	
			}
		});
		
		// SET VIDEO			
		my_Person_SplitPanel.jTable_jScrollPanel_LeftPanel.setModel(personsModel);
		my_Person_SplitPanel.jTable_jScrollPanel_LeftPanel = table;
		my_Person_SplitPanel.jScrollPanel_LeftPanel.setViewportView(my_Person_SplitPanel.jTable_jScrollPanel_LeftPanel);		
		my_Person_SplitPanel.setRowHeightFormat(true);
				
		// select row table persons	
		Person_Info info1 = new Person_Info();
		info1.setFocusable(false);
		// JSplitPane PersJSpline = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,true,new JScrollPane(table),new JScrollPane(info1)); 
		 
		// EVENTS on CURSOR
		table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			
			//@SuppressWarnings("deprecation")
			@Override
			public void valueChanged(ListSelectionEvent arg0) {
				
				PersonCls person = null;
				if (table.getSelectedRow() >= 0 )person = personsModel.getItem(table.convertRowIndexToModel(table.getSelectedRow()));
				//info1.show_001(person);
				
				// PersJSpline.setDividerLocation(PersJSpline.getDividerLocation());
				//my_Person_SplitPanel.jSplitPanel.setDividerLocation(my_Person_SplitPanel.jSplitPanel.getDividerLocation());	
				////my_Person_SplitPanel.searchTextField_SearchToolBar_LeftPanel.setEnabled(true);
				
				Person_info_panel_001 info_panel = new Person_info_panel_001(person, false);
				info_panel.setPreferredSize(new Dimension(search_Person_SplitPanel.jScrollPane_jPanel_RightPanel.getSize().width-50,search_Person_SplitPanel.jScrollPane_jPanel_RightPanel.getSize().height-50));
				my_Person_SplitPanel.jScrollPane_jPanel_RightPanel.setViewportView(info_panel);
			}
			
		});
		
		//my_Person_SplitPanel.jScrollPane_jPanel_RightPanel.setViewportView(info1);
	
		
	

		//////////////////////////
		// MENU
		JPopupMenu my_Persons_Table_menu = new JPopupMenu();
		JMenuItem my_favorite = new JMenuItem(Lang.getInstance().translate("Exchange"));
		my_favorite.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			
				favorite_my(table);

							
				
				
				
			}
		});
		
		my_Persons_Table_menu.addPopupMenuListener(new PopupMenuListener(){

			@Override
			public void popupMenuCanceled(PopupMenuEvent arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void popupMenuWillBecomeInvisible(PopupMenuEvent arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void popupMenuWillBecomeVisible(PopupMenuEvent arg0) {
				// TODO Auto-generated method stub
				
				int row = table.getSelectedRow();
				row = table.convertRowIndexToModel(row);
				PersonCls asset = personsModel.getItem(row);
				
				//IF ASSET CONFIRMED AND NOT ERM
				
					favorite.setVisible(true);
					//CHECK IF FAVORITES
					if(Controller.getInstance().isItemFavorite(asset))
					{
						my_favorite.setText(Lang.getInstance().translate("Remove Favorite"));
					}
					else
					{
						my_favorite.setText(Lang.getInstance().translate("Add Favorite"));
					}
					/*	
					//this.favoritesButton.setPreferredSize(new Dimension(200, 25));
					this.favoritesButton.addActionListener(new ActionListener()
					{
						public void actionPerformed(ActionEvent e)
						{
							onFavoriteClick();
						}
					});	
					this.add(this.favoritesButton, labelGBC);
					*/
				
			
			
			
			
			}
			
		}
		
		);
		
		
		my_Persons_Table_menu.add(my_favorite);
		
		
		table.setComponentPopupMenu(my_Persons_Table_menu);
		
		
	
	// DOUBLE CLICK EVENT
		
		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				Point p = e.getPoint();
				int row = table.rowAtPoint(p);
	//			table.setRowSelectionInterval(row, row);
				
				if(e.getClickCount() == 2)
				{
					row = table.convertRowIndexToModel(row);
					PersonCls person = personsModel.getItem(row);
		//			new PersonFrame(person);
					
				}
			
				if(e.getClickCount() == 1 & e.getButton() == e.BUTTON1)
				{
					
					if (table.getSelectedColumn() == WalletItemPersonsTableModel.COLUMN_FAVORITE){
						row = table.convertRowIndexToModel(row);
						PersonCls asset = personsModel.getItem(row);
						favorite_my( table);	
						
						
						
					}
					
					
				}
			
			
			
			
			
			
			}
			
			
			
			
			
			
		});
		
		
		
		
		// issue Assets
		  IssuePersonPanel Issue_Person_Panel = new IssuePersonPanel();
		  Issue_Person_Panel.setName(Lang.getInstance().translate("Issue Person"));
		
		
		
		
		
		
		
		
		
		
		////////////////////////////////////////////////////////////////////////
		////////////////////////////////////////////////////////////////////////
		
		this.jTabbedPane.add(my_Person_SplitPanel);
		
		this.jTabbedPane.add(search_Person_SplitPanel);
		this.jTabbedPane.add(Issue_Person_Panel);
		
		this.pack();
		//	this.setSize(800,600);
		this.setMaximizable(true);
		
		this.setClosable(true);
		this.setResizable(true);
		//	this.setSize(new Dimension( (int)parent.getSize().getWidth()-80,(int)parent.getSize().getHeight()-150));
		this.setLocation(20, 20);
		//	this.setIconImages(icons);
		//CLOSE
		setDefaultCloseOperation(JInternalFrame.DISPOSE_ON_CLOSE);
	    this.setResizable(true);
	    //splitPane_1.setDividerLocation((int)((double)(this.getHeight())*0.7));//.setDividerLocation(.8);
	    //my_person_panel.requestFocusInWindow();
	    this.setVisible(true);
	    //Rectangle k = this.getNormalBounds();
	    //this.setBounds(k);
	    Dimension size = MainFrame.desktopPane.getSize();
	    this.setSize(new Dimension((int)size.getWidth()-100,(int)size.getHeight()-100));
	    // setDividerLocation(700)
	
	 	search_Person_SplitPanel.jSplitPanel.setDividerLocation((int)(size.getWidth()/1.618));
	 	my_Person_SplitPanel.jSplitPanel.setDividerLocation((int)(size.getWidth()/1.618));
	}
	
	void favorite_all(JTable personsTable){
		int row = personsTable.getSelectedRow();
		row = personsTable.convertRowIndexToModel(row);

		PersonCls asset = tableModelPersons.getPerson(row);
		//new AssetPairSelect(asset.getKey());

		
			//CHECK IF FAVORITES
			if(Controller.getInstance().isItemFavorite(asset))
			{
				
				Controller.getInstance().removeItemFavorite(asset);
			}
			else
			{
				
				Controller.getInstance().addItemFavorite(asset);
			}
				

			personsTable.repaint();

	}
	void favorite_my(JTable table){
		int row = table.getSelectedRow();
		row = table.convertRowIndexToModel(row);

		PersonCls asset = personsModel.getItem(row);
		//new AssetPairSelect(asset.getKey());

		
			//CHECK IF FAVORITES
			if(Controller.getInstance().isItemFavorite(asset))
			{
				
				Controller.getInstance().removeItemFavorite(asset);
			}
			else
			{
				
				Controller.getInstance().addItemFavorite(asset);
			}
				

			table.repaint();

	}

}
