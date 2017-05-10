package gui.items.statuses;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.DefaultRowSorter;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
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
import core.item.statuses.StatusCls;
import gui.Split_Panel;
import gui.library.MTable;
import lang.Lang;

public class Search_Statuses_Tab extends Split_Panel{

private TableModelItemStatuses tableModelItemStatuses;
private MTable statusesTable;
private Search_Statuses_Tab sST;

public Search_Statuses_Tab(){
	super("Search_Statuses_Tab");
	sST= this;
	setName(Lang.getInstance().translate("Search Statuses"));
	searthLabel_SearchToolBar_LeftPanel.setText(Lang.getInstance().translate("Search") +":  ");
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
	
		
	searchTextField_SearchToolBar_LeftPanel.getDocument().addDocumentListener(new DocumentListener()
	{
		
		public void changedUpdate(DocumentEvent e) {
			onChange( sST, sorter);
		}

		public void removeUpdate(DocumentEvent e) {
			onChange(sST, sorter);
		}

		public void insertUpdate(DocumentEvent e) {
			onChange(sST, sorter);
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


	
}
