package gui.items.statuses;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.DefaultRowSorter;
import javax.swing.JLabel;
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
import core.item.ItemCls;
import core.item.notes.NoteCls;
import core.item.statuses.StatusCls;
import gui.Split_Panel;
import gui.items.notes.Info_Notes;
import gui.library.MTable;
import lang.Lang;
import utils.MenuPopupUtil;

public class Search_Statuses_Tab extends Split_Panel{

private TableModelItemStatuses tableModelItemStatuses;
private MTable statusesTable;
private Search_Statuses_Tab sST;
private JTextField key_Item;

public Search_Statuses_Tab(){
	super("Search_Statuses_Tab");
	sST= this;
	setName(Lang.getInstance().translate("Search Statuses"));
	searthLabel_SearchToolBar_LeftPanel.setText(Lang.getInstance().translate("Search") +":  ");
	searchToolBar_LeftPanel.setVisible(true);
	// not show buttons
		button1_ToolBar_LeftPanel.setVisible(false);
		button2_ToolBar_LeftPanel.setVisible(false);
		jButton1_jToolBar_RightPanel.setVisible(false);
		jButton2_jToolBar_RightPanel.setVisible(false);
		
	// not show My filter
			searth_My_JCheckBox_LeftPanel.setVisible(false);
			searth_Favorite_JCheckBox_LeftPanel.setVisible(false);

			
	

		this.searchToolBar_LeftPanel.setVisible(true);
		this.toolBar_LeftPanel.add(new JLabel(Lang.getInstance().translate("Find Key")+":"));
    	key_Item = new JTextField();
    	key_Item.setToolTipText("");
    	key_Item.setAlignmentX(1.0F);
    	key_Item.setMinimumSize(new java.awt.Dimension(100, 20));
    	key_Item.setName(""); // NOI18N
    	key_Item.setPreferredSize(new java.awt.Dimension(100, 20));
    	key_Item.setMaximumSize(new java.awt.Dimension(2000, 20));
       	
    	MenuPopupUtil.installContextMenu(key_Item);
    	
    	this.toolBar_LeftPanel.add(key_Item);
    	key_Item.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				searchTextField_SearchToolBar_LeftPanel.setText("");
				
				
				Label_search_Info_Panel.setText(Lang.getInstance().translate("Waiting..."));
				jScrollPanel_LeftPanel.setViewportView(search_Info_Panel);
				
				
				
				
			
				
				new Thread() {
					@Override
					public void run() {
						tableModelItemStatuses.Find_item_from_key(key_Item.getText());	
						if (tableModelItemStatuses.getRowCount() < 1) {
							Label_search_Info_Panel.setText(Lang.getInstance().translate("Not Found Persons"));
							jScrollPanel_LeftPanel.setViewportView(search_Info_Panel);
							jScrollPane_jPanel_RightPanel.setViewportView(null);
							return;
						}
						jTable_jScrollPanel_LeftPanel.setRowSelectionInterval(0, 0);
						// ddd.dispose();
						jScrollPanel_LeftPanel.setViewportView(jTable_jScrollPanel_LeftPanel);
					}
				}.start();
				
				
			}
    		
    	});
		
	
	
	// not show buttons
	button1_ToolBar_LeftPanel.setVisible(false);
	button2_ToolBar_LeftPanel.setVisible(false);
	jButton1_jToolBar_RightPanel.setVisible(false);
	jButton2_jToolBar_RightPanel.setVisible(false);
	
	jButton1_jToolBar_RightPanel.addActionListener(new ActionListener()
		{
		    public void actionPerformed(ActionEvent e)
		    {
				//CHECK IF FAVORITES
				if(Controller.getInstance().isItemFavorite(itemAll))
				{
					button1_ToolBar_LeftPanel.setText(Lang.getInstance().translate("Add Favorite"));
					Controller.getInstance().removeItemFavorite(itemAll);
				}
				else
				{
					button1_ToolBar_LeftPanel.setText(Lang.getInstance().translate("Remove Favorite"));
					Controller.getInstance().addItemFavorite(itemAll);
				}
		    }
		});

	
	//CREATE TABLE
	tableModelItemStatuses = new TableModelItemStatuses();
	statusesTable = new MTable(this.tableModelItemStatuses);
	TableColumnModel columnModel = statusesTable.getColumnModel(); // read column model
	columnModel.getColumn(0).setMaxWidth((100));
	//CHECKBOX FOR FAVORITE
	TableColumn favoriteColumn = statusesTable.getColumnModel().getColumn(TableModelItemStatuses.COLUMN_FAVORITE);
		

//	favoriteColumn.setCellRenderer(new Renderer_Boolean()); //statusesTable.getDefaultRenderer(Boolean.class));
	favoriteColumn.setMinWidth(50);
	favoriteColumn.setMaxWidth(90);
	favoriteColumn.setPreferredWidth(90);//.setWidth(30);
	
	TableColumn isUniqueColumn = statusesTable.getColumnModel().getColumn(TableModelItemStatuses.COLUMN_UNIQUE);
	isUniqueColumn.setMinWidth(50);
	isUniqueColumn.setMaxWidth(90);
	isUniqueColumn.setPreferredWidth(90);//.setWidth(30);
	
	//	statusesTable.setAutoResizeMode(5);//.setAutoResizeMode(mode);.setAutoResizeMode(0);
	//Sorter
	RowSorter sorter =   new TableRowSorter(this.tableModelItemStatuses);
	statusesTable.setRowSorter(sorter);	
	// UPDATE FILTER ON TEXT CHANGE
	
	
	searth_Favorite_JCheckBox_LeftPanel.addActionListener( new ActionListener(){

		@Override
		public void actionPerformed(ActionEvent arg0) {
			// TODO Auto-generated method stub
			onChange( sST, sorter);
			
		}
		
	});
	
		
	searchTextField_SearchToolBar_LeftPanel.addActionListener(new ActionListener(){

		@Override
		public void actionPerformed(ActionEvent arg0) {
			// TODO Auto-generated method stub
			// GET VALUE
				String search = searchTextField_SearchToolBar_LeftPanel.getText();
				if (search.equals("")){jScrollPane_jPanel_RightPanel.setViewportView(null);
				tableModelItemStatuses.clear();
				 Label_search_Info_Panel.setText(Lang.getInstance().translate("Enter more  2 characters"));
				 jScrollPanel_LeftPanel.setViewportView(search_Info_Panel);
				return;
			}
				if (search.length()<3) {
					Label_search_Info_Panel.setText(Lang.getInstance().translate("Enter more  2 characters"));
					 jScrollPanel_LeftPanel.setViewportView(search_Info_Panel);
					
					
					
					return;
				}
				key_Item.setText("");
				
				Label_search_Info_Panel.setText(Lang.getInstance().translate("Waiting..."));
				jScrollPanel_LeftPanel.setViewportView(search_Info_Panel);
				
				
				
				
			//	search_Table_Model.set_Filter_By_Name(search);
				
				new Thread() {
					@Override
					public void run() {
						tableModelItemStatuses.set_Filter_By_Name(search);
						if (tableModelItemStatuses.getRowCount() < 1) {
							Label_search_Info_Panel.setText(Lang.getInstance().translate("Not Found Persons"));
							jScrollPanel_LeftPanel.setViewportView(search_Info_Panel);
							jScrollPane_jPanel_RightPanel.setViewportView(null);
							return;
						}
						jTable_jScrollPanel_LeftPanel.setRowSelectionInterval(0, 0);
						// ddd.dispose();
						jScrollPanel_LeftPanel.setViewportView(jTable_jScrollPanel_LeftPanel);
					}
				}.start();
		
		
				
				
				
			
				
				
				
			//	jScrollPanel_LeftPanel.setViewportView(null);
			//	jScrollPane_jPanel_RightPanel.setViewportView(null);
				
				
				// if (search.length()<3) return;
			
				// show message
				// jTable_jScrollPanel_LeftPanel.setVisible(false);//
				

				
		
		
		}
		
	});
	
	
	// set video			
	//jTable_jScrollPanel_Panel2_Tabbed_Panel_Left_Panel.setModel(this.tableModelStatuses);
	jTable_jScrollPanel_LeftPanel.setModel(this.tableModelItemStatuses);
	//jTable_jScrollPanel_Panel2_Tabbed_Panel_Left_Panel = statusesTable;
	jTable_jScrollPanel_LeftPanel = statusesTable;
	//jScrollPanel_Panel2_Tabbed_Panel_Left_Panel.setViewportView(jTable_jScrollPanel_Panel2_Tabbed_Panel_Left_Panel); // statusesTable; 
	jScrollPanel_LeftPanel.setViewportView(sST.jTable_jScrollPanel_LeftPanel);
	// select row table statuses
	jTable_jScrollPanel_LeftPanel.getSelectionModel().addListSelectionListener(new search_listener());
		
	Status_Info info = new Status_Info(); 
	info.setFocusable(false);
	//		
	// обработка изменения положения курсора в таблице
		
	 //jTable_jScrollPanel_Panel2_Tabbed_Panel_Left_Panel.getSelectionModel().addListSelectionListener(new ListSelectionListener()  {
	 jTable_jScrollPanel_LeftPanel.getSelectionModel().addListSelectionListener(new ListSelectionListener()  {
		@Override
		public void valueChanged(ListSelectionEvent arg0) {
			StatusCls status = null;
			if (statusesTable.getSelectedRow() >= 0 ) status = tableModelItemStatuses.getStatus(statusesTable.convertRowIndexToModel(statusesTable.getSelectedRow()));
			if (status == null) return;
			info.show_001(status);
			itemAll = status;
			
			jButton1_jToolBar_RightPanel.setText(status.isFavorite()?Lang.getInstance().translate("Remove Favorite"):Lang.getInstance().translate("Add Favorite"));
			jButton1_jToolBar_RightPanel.setVisible(false);
					
			jSplitPanel.setDividerLocation(sST.jSplitPanel.getDividerLocation());	
			searchTextField_SearchToolBar_LeftPanel.setEnabled(true);
		}
	});
			 
		
		
		
	jScrollPane_jPanel_RightPanel.setViewportView(info);
	
	// MENU
					
	JPopupMenu search_Statuses_Table_menu = new JPopupMenu();
	
	JMenuItem favorite = new JMenuItem(Lang.getInstance().translate(""));
	favorite.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			
			favorite_set( statusesTable );
			
		}
	});
	
	search_Statuses_Table_menu.add(favorite);
	
	search_Statuses_Table_menu.addPopupMenuListener(new PopupMenuListener(){

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
				
		}
		
	}
	
	);
	
	
	 jTable_jScrollPanel_LeftPanel.setComponentPopupMenu(search_Statuses_Table_menu);
			

}
public ItemCls itemAll;
public ItemCls itemMy;


	


public  void favorite_set(JTable assetsTable){


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

public  void onChange(Split_Panel search_Status_SplitPanel, RowSorter sorter) {
	// filter
					// GET VALUE
					String search = search_Status_SplitPanel.searchTextField_SearchToolBar_LeftPanel.getText();

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

@Override
public void delay_on_close(){
	// delete observer left panel
	tableModelItemStatuses.removeObservers();
	// get component from right panel
	Component c1 = jScrollPane_jPanel_RightPanel.getViewport().getView();
	// if Person_Info 002 delay on close
	  if (c1 instanceof Status_Info) ( (Status_Info)c1).delay_on_Close();
	
}
//listener select row	 
class search_listener implements ListSelectionListener  {
		@Override
		public void valueChanged(ListSelectionEvent arg0) {
			if (statusesTable.getSelectedRow() < 0 )
				return;

			 StatusCls note = tableModelItemStatuses.getStatus(statusesTable.convertRowIndexToModel(statusesTable.getSelectedRow()));
			Status_Info info_Note = new Status_Info();
			info_Note.show_001(note);
			info_Note.setPreferredSize(new Dimension(jScrollPane_jPanel_RightPanel.getSize().width-50,jScrollPane_jPanel_RightPanel.getSize().height-50));
			jScrollPane_jPanel_RightPanel.setViewportView(info_Note);
		}
	}


	
}
