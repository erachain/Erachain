package gui.items.statuses;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.List;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.util.ArrayList;

import javax.swing.DefaultRowSorter;
import javax.swing.JInternalFrame;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
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
import core.item.ItemCls;
import core.item.assets.AssetCls;
import core.item.persons.PersonCls;
import core.item.statuses.StatusCls;
import gui.MainFrame;
import gui.Main_Internal_Frame;
import gui.RunMenu;
import gui.Split_Panel;
import gui.items.assets.AssetFrame;
import gui.items.assets.TableModelItemAssets;
import gui.items.persons.MainPersonsFrame;
import gui.items.persons.PersonConfirmDialog;
import gui.items.persons.PersonSetStatusDialog;
import gui.models.Renderer_Boolean;
import gui.models.Renderer_Left;
import gui.models.Renderer_Right;
import gui.models.WalletItemStatusesTableModel;
import lang.Lang;

public class MainStatusesFrame extends Main_Internal_Frame{
	private static final long serialVersionUID = 1L;
	private TableModelItemStatuses tableModelItemStatuses;
	final JTable statusesTable ;
	
	RunMenu Search_run_menu;

	
	public MainStatusesFrame(){

		this.setTitle(Lang.getInstance().translate("Statuses"));
		this.jButton2_jToolBar.setVisible(false);
		this.jButton3_jToolBar.setVisible(false);
		// buttun1
		this.jButton1_jToolBar.setText(Lang.getInstance().translate("Issue Status"));
		this.jButton1_jToolBar.setVisible(false);
		// status panel
		this.jLabel_status_jPanel.setText(Lang.getInstance().translate("Work with statuses"));
	 
		this.jButton1_jToolBar.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
		    	 new IssueStatusDialog();
			}
		});	
		this.jToolBar.setVisible(false);
		 
		// all statuses 
		Split_Panel search_Status_SplitPanel = new Split_Panel();
		search_Status_SplitPanel.setName(Lang.getInstance().translate("Search Statuses"));
		search_Status_SplitPanel.searthLabel_SearchToolBar_LeftPanel.setText(Lang.getInstance().translate("Search") +":  ");
		// not show buttons
		search_Status_SplitPanel.button1_ToolBar_LeftPanel.setVisible(false);
		search_Status_SplitPanel.button2_ToolBar_LeftPanel.setVisible(false);
		search_Status_SplitPanel.jButton1_jToolBar_RightPanel.setVisible(false);
		search_Status_SplitPanel.jButton2_jToolBar_RightPanel.setVisible(false);
		
		search_Status_SplitPanel.jButton1_jToolBar_RightPanel.addActionListener(new ActionListener()
			{
			    public void actionPerformed(ActionEvent e)
			    {
					//CHECK IF FAVORITES
					if(Controller.getInstance().isItemFavorite(MainStatusesFrame.itemAll))
					{
						search_Status_SplitPanel.button1_ToolBar_LeftPanel.setText(Lang.getInstance().translate("Add Favorite"));
						Controller.getInstance().removeItemFavorite(MainStatusesFrame.itemAll);
					}
					else
					{
						search_Status_SplitPanel.button1_ToolBar_LeftPanel.setText(Lang.getInstance().translate("Remove Favorite"));
						Controller.getInstance().addItemFavorite(MainStatusesFrame.itemAll);
					}
			    }
			});

		
		//CREATE TABLE
		this.tableModelItemStatuses = new TableModelItemStatuses();
		statusesTable = new JTable(this.tableModelItemStatuses);
		TableColumnModel columnModel = statusesTable.getColumnModel(); // read column model
		columnModel.getColumn(0).setMaxWidth((100));
		//Custom renderer for the String column;
		statusesTable.setDefaultRenderer(Long.class, new Renderer_Right()); // set renderer
		statusesTable.setDefaultRenderer(String.class, new Renderer_Left()); // set renderer
		//CHECKBOX FOR FAVORITE
		TableColumn favoriteColumn = statusesTable.getColumnModel().getColumn(TableModelItemStatuses.COLUMN_FAVORITE);
			
	
		favoriteColumn.setCellRenderer(new Renderer_Boolean()); //statusesTable.getDefaultRenderer(Boolean.class));
		favoriteColumn.setMinWidth(50);
		favoriteColumn.setMaxWidth(90);
		favoriteColumn.setPreferredWidth(90);//.setWidth(30);
		//	statusesTable.setAutoResizeMode(5);//.setAutoResizeMode(mode);.setAutoResizeMode(0);
		//Sorter
		RowSorter sorter =   new TableRowSorter(this.tableModelItemStatuses);
		statusesTable.setRowSorter(sorter);	
		// UPDATE FILTER ON TEXT CHANGE
		
		
		search_Status_SplitPanel.searth_Favorite_JCheckBox_LeftPanel.addActionListener( new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				onChange( search_Status_SplitPanel, sorter);
				
			}
			
		});
		
			
		search_Status_SplitPanel.searchTextField_SearchToolBar_LeftPanel.getDocument().addDocumentListener(new DocumentListener()
		{
			
			public void changedUpdate(DocumentEvent e) {
				onChange( search_Status_SplitPanel, sorter);
			}

			public void removeUpdate(DocumentEvent e) {
				onChange(search_Status_SplitPanel, sorter);
			}

			public void insertUpdate(DocumentEvent e) {
				onChange(search_Status_SplitPanel, sorter);
			}

		
		});
		
		// set video			
		//jTable_jScrollPanel_Panel2_Tabbed_Panel_Left_Panel.setModel(this.tableModelStatuses);
		search_Status_SplitPanel.jTable_jScrollPanel_LeftPanel.setModel(this.tableModelItemStatuses);
		//jTable_jScrollPanel_Panel2_Tabbed_Panel_Left_Panel = statusesTable;
		search_Status_SplitPanel.jTable_jScrollPanel_LeftPanel = statusesTable;
		//jScrollPanel_Panel2_Tabbed_Panel_Left_Panel.setViewportView(jTable_jScrollPanel_Panel2_Tabbed_Panel_Left_Panel); // statusesTable; 
		search_Status_SplitPanel.jScrollPanel_LeftPanel.setViewportView(search_Status_SplitPanel.jTable_jScrollPanel_LeftPanel);
		// select row table statuses
	
			
		Status_Info info = new Status_Info(); 
		info.setFocusable(false);
		//		
		// обработка изменения положения курсора в таблице
			
		 //jTable_jScrollPanel_Panel2_Tabbed_Panel_Left_Panel.getSelectionModel().addListSelectionListener(new ListSelectionListener()  {
		 search_Status_SplitPanel.jTable_jScrollPanel_LeftPanel.getSelectionModel().addListSelectionListener(new ListSelectionListener()  {
			@Override
			public void valueChanged(ListSelectionEvent arg0) {
				StatusCls status = null;
				if (statusesTable.getSelectedRow() >= 0 ) status = tableModelItemStatuses.getStatus(statusesTable.convertRowIndexToModel(statusesTable.getSelectedRow()));
				info.show_001(status);
				MainStatusesFrame.itemAll = status;
				
				search_Status_SplitPanel.jButton1_jToolBar_RightPanel.setText(status.isFavorite()?Lang.getInstance().translate("Remove Favorite"):Lang.getInstance().translate("Add Favorite"));
				search_Status_SplitPanel.jButton1_jToolBar_RightPanel.setVisible(false);
						
				search_Status_SplitPanel.jSplitPanel.setDividerLocation(search_Status_SplitPanel.jSplitPanel.getDividerLocation());	
				search_Status_SplitPanel.searchTextField_SearchToolBar_LeftPanel.setEnabled(true);
			}
		});
				 
			
			
			
		search_Status_SplitPanel.jScrollPane_jPanel_RightPanel.setViewportView(info);
		// MENU
	/*					
		JPopupMenu all_Statuses_Table_menu = new JPopupMenu();
		
		JMenuItem favorite = new JMenuItem(Lang.getInstance().translate(""));
		favorite.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				favorite_set( statusesTable );
				
			}
		});
		
		
		
		
		/*
		JMenuItem details = new JMenuItem(Lang.getInstance().translate("Details"));
		details.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				StatusCls status = tableModelItemStatuses.getStatus(statusesTable.getSelectedRow());
				//new StatusDetailsFrame(status);
			}
		});
		all_Statuses_Table_menu.add(details);
		*

		JMenuItem confirm_Menu = new JMenuItem(Lang.getInstance().translate("Confirm"));
		confirm_Menu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				/*
				int row = statusesTable.getSelectedRow();
				row = statusesTable.convertRowIndexToModel(row);

				StatusCls status = tableModelStatuses.getStatus(row);
				new StatusFrame(status);
				*
				// открываем диалоговое окно ввода данных для подтверждения персоны 
				StatusCls status = tableModelItemStatuses.getStatus(statusesTable.convertRowIndexToModel(statusesTable.getSelectedRow()));

		    	//StatusConfirmDialog fm = new StatusConfirmDialog(MainStatusesFrame.this, status);	
		    	// обрабатываем полученные данные от диалогового окна
		    	//if(fm.isOK()){
                //    JOptionPane.showMessageDialog(Form1.this, "OK");
                //}
			
			}
		});

		all_Statuses_Table_menu.add(confirm_Menu);

		JMenuItem setStatus_Menu = new JMenuItem(Lang.getInstance().translate("Set Status"));
		setStatus_Menu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				StatusCls status = tableModelItemStatuses.getStatus(statusesTable.convertRowIndexToModel(statusesTable.getSelectedRow()));

				SetStatusToItemDialog fm = new SetStatusToItemDialog(MainStatusesFrame.this, status);	
				
				
		    	// обрабатываем полученные данные от диалогового окна
		    	//if(fm.isOK()){
                //    JOptionPane.showMessageDialog(Form1.this, "OK");
                //}
			
			}
		});
		
		

		
		all_Statuses_Table_menu.add(setStatus_Menu);

		statusesTable.setComponentPopupMenu(all_Statuses_Table_menu);
	*/	
		statusesTable.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				Point p = e.getPoint();
				int row = statusesTable.rowAtPoint(p);
				statusesTable.setRowSelectionInterval(row, row);
				row = statusesTable.convertRowIndexToModel(row);
				StatusCls status = tableModelItemStatuses.getStatus(row);
				
				if(e.getClickCount() == 2)
				{
		//			row = statusesTable.convertRowIndexToModel(row);
		//			StatusCls status = tableModelItemStatuses.getStatus(row);
		//			new StatusFrame(status);
					
				}
				if(e.getClickCount() == 1 & e.getButton() == e.BUTTON1)
				{
					
					if (statusesTable.getSelectedColumn() == tableModelItemStatuses.COLUMN_FAVORITE){
						
		//				favorite_set( statusesTable);	
						
						
						
					}
// выводим меню всплывающее
					
					
					
					if(Controller.getInstance().isItemFavorite(status))
					{
						Search_run_menu.jButton3.setText(Lang.getInstance().translate("Remove Favorite"));
					}
					else
					{
						Search_run_menu.jButton3.setText(Lang.getInstance().translate("Add Favorite"));
					}
					Search_run_menu.setLocation(e.getXOnScreen(), e.getYOnScreen());
				    Search_run_menu.setVisible(true);		
				}
				
				
				
				
				
			}
		});
	/*	
		// hand cursor  for Favorite column
		statusesTable.addMouseMotionListener(new MouseMotionListener() {
		    public void mouseMoved(MouseEvent e) {
		    	
		     if(statusesTable.columnAtPoint(e.getPoint())==tableModelItemStatuses.COLUMN_FAVORITE)
		        {
		     
		        	statusesTable.setCursor(new Cursor(Cursor.HAND_CURSOR));
		        } else {
		        	statusesTable.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		        }
		        
		    }
		    

		    public void mouseDragged(MouseEvent e) {
		    }
		});
		
	*/	
	/*	
		all_Statuses_Table_menu.addPopupMenuListener(new PopupMenuListener(){

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
				
				int row = statusesTable.getSelectedRow();
				row = statusesTable.convertRowIndexToModel(row);
				StatusCls status = tableModelItemStatuses.getStatus(row);
				
				//IF ASSET CONFIRMED AND NOT ERM
				if(status.getKey() >= StatusCls.INITIAL_FAVORITES)
				{
					favorite.setVisible(true);
					//CHECK IF FAVORITES
					if(Controller.getInstance().isItemFavorite(status))
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
		/*
				} else {
					
					favorite.setVisible(false);
				}
			//	sell.setVisible(false);
			//	boolean a = Controller.getInstance().isAddressIsMine(asset.getCreator().getAddress());
			//	if (Controller.getInstance().isAddressIsMine(asset.getCreator().getAddress())) 
			//	{
			//		sell.setVisible(true);
			//	}
				
		
			
			
			
			
			}
			
		}
		
		);
		
		
		
		

		all_Statuses_Table_menu.addSeparator();
		
		all_Statuses_Table_menu.add(favorite);
	*/	
		
		

		Search_run_menu = new RunMenu();
		Dimension dim = new Dimension(180,50);
    	Search_run_menu.setSize(dim);
    	Search_run_menu.setPreferredSize(dim);
    	Search_run_menu.setVisible(false);
    	Search_run_menu.jButton1.setText(Lang.getInstance().translate("Set Status"));
   // 	aaa.jButton1.setBorderPainted(false);
    	Search_run_menu.jButton1.setFocusPainted(true);
   	Search_run_menu.jButton1.setFocusCycleRoot(true);
   
   	
    	Search_run_menu.jButton1.addActionListener(new ActionListener(){
  		@Override
    	public void actionPerformed(ActionEvent e) {
   // TODO Auto-generated method stub
  			StatusCls status = tableModelItemStatuses.getStatus(statusesTable.convertRowIndexToModel(statusesTable.getSelectedRow()));

			SetStatusToItemDialog fm = new SetStatusToItemDialog(MainStatusesFrame.this, status);	
			}
    		    	
    	});
    	   	
   /* 	
    	Search_run_menu.jButton2.setText(Lang.getInstance().translate("Confirm"));
  //  	aaa.jButton2.setBorderPainted(false);
    	Search_run_menu.getContentPane().add(Search_run_menu.jButton2);
    	Search_run_menu.jButton2.addActionListener(new ActionListener(){
  		@Override
    	public void actionPerformed(ActionEvent e) {
   // TODO Auto-generated method stub
    //		PersonCls person = tableModelPersons.getPerson(personsTable.convertRowIndexToModel(personsTable.getSelectedRow()));
   // 		PersonConfirmDialog fm = new PersonConfirmDialog(MainPersonsFrame.this,  tableModelPersons.getPerson(personsTable.convertRowIndexToModel(personsTable.getSelectedRow())));		
    		}
    		    	
    	});
    	
    	*/
  //  	aaa.jButton3.setBorderPainted(false);
    	Search_run_menu.getContentPane().add(Search_run_menu.jButton3);
    	Search_run_menu.jButton3.addActionListener(new ActionListener(){
// вычисляем устанавливаем\ сбрасываем флажек выбранные
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				
				int row = statusesTable.getSelectedRow();
				
				row = statusesTable.convertRowIndexToModel(row);
				StatusCls status = tableModelItemStatuses.getStatus(row);
				favorite_set( statusesTable);	
				
				if(Controller.getInstance().isItemFavorite(status))
				{
					Search_run_menu.jButton3.setText(Lang.getInstance().translate("Remove Favorite"));
				}
				else
				{
					Search_run_menu.jButton3.setText(Lang.getInstance().translate("Add Favorite"));
				}
			
			
			}
    	
    	});
   
    	Search_run_menu.pack();
		
		
    	Search_run_menu.addWindowFocusListener(new WindowFocusListener(){
			@Override
			public void windowGainedFocus(WindowEvent arg0) {
				// TODO Auto-generated method stub
			}
			@Override
			public void windowLostFocus(WindowEvent arg0) {
				// TODO Auto-generated method stub
				Search_run_menu.setVisible(false);
			}
    	});
    	
		
		
		
		
		
		
		
	 
			
	// My statuses		
			
	Split_Panel my_Status_SplitPanel = new Split_Panel();
	my_Status_SplitPanel.setName(Lang.getInstance().translate("My Statuses"));
	my_Status_SplitPanel.searthLabel_SearchToolBar_LeftPanel.setText(Lang.getInstance().translate("Search") +":  ");
	// not show buttons
	my_Status_SplitPanel.button1_ToolBar_LeftPanel.setVisible(false);
	my_Status_SplitPanel.button2_ToolBar_LeftPanel.setVisible(false);
	my_Status_SplitPanel.jButton1_jToolBar_RightPanel.setVisible(false);
	my_Status_SplitPanel.jButton2_jToolBar_RightPanel.setVisible(false);
	
	//TABLE
			final WalletItemStatusesTableModel statusesModel = new WalletItemStatusesTableModel();
			final JTable table = new JTable(statusesModel);
			
			columnModel = table.getColumnModel(); // read column model
				columnModel.getColumn(0).setMaxWidth((100));
			
			//Custom renderer for the String column;
			table.setDefaultRenderer(Long.class, new Renderer_Right()); // set renderer
			table.setDefaultRenderer(String.class, new Renderer_Left()); // set renderer
			
			
			TableRowSorter sorter1 = new TableRowSorter(statusesModel);
			table.setRowSorter(sorter1);
			table.getRowSorter();
			if (statusesModel.getRowCount() > 0) statusesModel.fireTableDataChanged();
			
			//CHECKBOX FOR CONFIRMED
			TableColumn confirmedColumn = table.getColumnModel().getColumn(WalletItemStatusesTableModel.COLUMN_CONFIRMED);
			// confirmedColumn.setCellRenderer(table.getDefaultRenderer(Boolean.class));
			confirmedColumn.setCellRenderer(new Renderer_Boolean()); //statusesTable.getDefaultRenderer(Boolean.class));
			confirmedColumn.setMinWidth(50);
			confirmedColumn.setMaxWidth(90);
			confirmedColumn.setPreferredWidth(90);//.setWidth(30);
			
			
			//CHECKBOX FOR FAVORITE
			favoriteColumn = table.getColumnModel().getColumn(WalletItemStatusesTableModel.COLUMN_FAVORITE);
			//favoriteColumn.setCellRenderer(table.getDefaultRenderer(Boolean.class));
			favoriteColumn.setCellRenderer(new Renderer_Boolean()); //statusesTable.getDefaultRenderer(Boolean.class));
			favoriteColumn.setMinWidth(50);
			favoriteColumn.setMaxWidth(90);
			favoriteColumn.setPreferredWidth(90);//.setWidth(30);
			
		//	TableColumn keyColumn = table.getColumnModel().getColumn(WalletItemStatusesTableModel.COLUMN_KEY);
		//	keyColumn.setCellRenderer(new Renderer_Right());
			
		//	TableColumn nameColumn = table.getColumnModel().getColumn(WalletItemStatusesTableModel.COLUMN_NAME);
		//	nameColumn.setCellRenderer(new Renderer_Left());
			
		//	TableColumn addrColumn = table.getColumnModel().getColumn(WalletItemStatusesTableModel.COLUMN_ADDRESS);
		//	addrColumn.setCellRenderer(new Renderer_Left());
			
			

			my_Status_SplitPanel.searth_Favorite_JCheckBox_LeftPanel.addActionListener( new ActionListener(){

				@Override
				public void actionPerformed(ActionEvent arg0) {
					// TODO Auto-generated method stub
					onChange( my_Status_SplitPanel, sorter1);
					
				}
				
			});
				
			
			//CREATE SEARCH FIELD
			final JTextField txtSearch = new JTextField();
			
			// UPDATE FILTER ON TEXT CHANGE
			txtSearch.getDocument().addDocumentListener(new DocumentListener() {
				public void changedUpdate(DocumentEvent e) {
					onChange(my_Status_SplitPanel, sorter1);
				}
			
				public void removeUpdate(DocumentEvent e) {
					onChange(my_Status_SplitPanel, sorter1);
				}
			
				public void insertUpdate(DocumentEvent e) {
					onChange(my_Status_SplitPanel, sorter1);
				}
			
			
			});
			
			

					// set video			
					//jTable_jScrollPanel_Panel2_Tabbed_Panel_Left_Panel.setModel(this.tableModelStatuses);
			my_Status_SplitPanel.jTable_jScrollPanel_LeftPanel.setModel(statusesModel);
					//jTable_jScrollPanel_Panel2_Tabbed_Panel_Left_Panel = statusesTable;
			my_Status_SplitPanel.jTable_jScrollPanel_LeftPanel = table;
					//jScrollPanel_Panel2_Tabbed_Panel_Left_Panel.setViewportView(jTable_jScrollPanel_Panel2_Tabbed_Panel_Left_Panel); // statusesTable; 
			my_Status_SplitPanel.jScrollPanel_LeftPanel.setViewportView(my_Status_SplitPanel.jTable_jScrollPanel_LeftPanel);		
					
					
			
			// select row table statuses
			
			 Status_Info info1 = new Status_Info();
			 info1.setFocusable(false);
			// JSplitPane PersJSpline = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,true,new JScrollPane(table),new JScrollPane(info1)); 
			 
		//	 my_Status_SplitPanel.jTable_jScrollPanel_LeftPanel = table;
			
			 
			 
			// обработка изменения положения курсора в таблице
			table.getSelectionModel().addListSelectionListener(new ListSelectionListener()  {
				@SuppressWarnings("deprecation")
				@Override
				public void valueChanged(ListSelectionEvent arg0) {
					StatusCls status =null;
					if (table.getSelectedRow() >= 0 )status = statusesModel.getItem(table.convertRowIndexToModel(table.getSelectedRow()));
					info1.show_001(status);
					MainStatusesFrame.itemMy = status;

				//	PersJSpline.setDividerLocation(PersJSpline.getDividerLocation());
					my_Status_SplitPanel.jSplitPanel.setDividerLocation(my_Status_SplitPanel.jSplitPanel.getDividerLocation());	
					my_Status_SplitPanel.searchTextField_SearchToolBar_LeftPanel.setEnabled(true);
				}
			});



			my_Status_SplitPanel.jScrollPane_jPanel_RightPanel.setViewportView(info1);
	
		
			
	// issue status
			
		 JPanel issuePanel = new IssueStatusPanel();
		 issuePanel.setName(Lang.getInstance().translate("Create Status"));	
			
	
		this.jTabbedPane.add(my_Status_SplitPanel);
		
		this.jTabbedPane.add(search_Status_SplitPanel);
		
		this.jTabbedPane.add(issuePanel);
		
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
	//    splitPane_1.setDividerLocation((int)((double)(this.getHeight())*0.7));//.setDividerLocation(.8);
	    //my_status_panel.requestFocusInWindow();
	    this.setVisible(true);
	    Rectangle k = this.getNormalBounds();
	 //   this.setBounds(k);
	    Dimension size = MainFrame.desktopPane.getSize();
	    this.setSize(new Dimension((int)size.getWidth()-100,(int)size.getHeight()-100));
	 // setDividerLocation(700)
	
	 	search_Status_SplitPanel.jSplitPanel.setDividerLocation((int)(size.getWidth()/1.618));
	 	my_Status_SplitPanel.jSplitPanel.setDividerLocation((int)(size.getWidth()/1.618));

	}
	
	static private ItemCls itemAll;
	static private ItemCls itemMy;

	public void favorite_set(JTable assetsTable){


		int row = assetsTable.getSelectedRow();
		row = assetsTable.convertRowIndexToModel(row);

		 StatusCls status = tableModelItemStatuses.getStatus(row);
		//new AssetPairSelect(asset.getKey());

		if(status.getKey() >= StatusCls.INITIAL_FAVORITES)
		{
			//CHECK IF FAVORITES
			if(Controller.getInstance().isItemFavorite(status))
			{
				
				Controller.getInstance().removeItemFavorite(status);
			}
			else
			{
				
				Controller.getInstance().addItemFavorite(status);
			}
				

			assetsTable.repaint();

		}
		}
	
	public void onChange(Split_Panel search_Status_SplitPanel, RowSorter sorter) {
		// filter
						// GET VALUE
						String search = search_Status_SplitPanel.searchTextField_SearchToolBar_LeftPanel.getText();

					 	String isFavorite;
						// SET FILTER
						//		tableModelStatuses.getSortableList().setFilter(search);
						
					 	RowFilter<Object,Object> fooBarFilter;
						tableModelItemStatuses.fireTableDataChanged();
						
						if (search_Status_SplitPanel.searth_Favorite_JCheckBox_LeftPanel.isSelected()) {
							
							ArrayList<RowFilter<Object, Object>> filters = new ArrayList<RowFilter<Object,Object>>(2);
							   filters.add(RowFilter.regexFilter(".*" + search + ".*", tableModelItemStatuses.COLUMN_NAME));
							   filters.add(RowFilter.regexFilter(".*true*",tableModelItemStatuses.COLUMN_FAVORITE));
							    fooBarFilter = RowFilter.andFilter(filters);	
													
						} else {
							
							fooBarFilter  = RowFilter.regexFilter(".*" + search + ".*", tableModelItemStatuses.COLUMN_NAME);	
						}
						
						
						   
						   
						   
						   
						   
						((DefaultRowSorter) sorter).setRowFilter(fooBarFilter);
						
						tableModelItemStatuses.fireTableDataChanged();
					//	String a = search_Status_SplitPanel.searth_Favorite_JCheckBox_LeftPanel.isSelected().get.getText();
					//	a = a+ " ";
					}
	
	
}