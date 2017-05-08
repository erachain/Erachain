package gui.items.statuses;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.DefaultRowSorter;
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
import core.item.statuses.StatusCls;
import gui.Split_Panel;
import gui.library.MTable;
import gui.models.WalletItemStatusesTableModel;
import lang.Lang;

public class My_Statuses_Tab extends Split_Panel {
	
	private WalletItemStatusesTableModel statusesModel;
	private MTable table;
	private TableColumn favoriteColumn;
	private My_Statuses_Tab tSP;
	private  TableModelItemStatuses tableModelItemStatuses;
	

	// My statuses		
public My_Statuses_Tab(){	
		
	tSP = this;
		setName(Lang.getInstance().translate("My Statuses"));
		searthLabel_SearchToolBar_LeftPanel.setText(Lang.getInstance().translate("Search") +":  ");
		// not show buttons
		button1_ToolBar_LeftPanel.setVisible(false);
		button2_ToolBar_LeftPanel.setVisible(false);
		jButton1_jToolBar_RightPanel.setVisible(false);
		jButton2_jToolBar_RightPanel.setVisible(false);
		
		//TABLE
				 statusesModel = new WalletItemStatusesTableModel();
				 table = new MTable(statusesModel);
				
				TableColumnModel columnModel = table.getColumnModel(); // read column model
					columnModel.getColumn(0).setMaxWidth((100));
				
					
				TableRowSorter sorter1 = new TableRowSorter(statusesModel);
				table.setRowSorter(sorter1);
				table.getRowSorter();
				if (statusesModel.getRowCount() > 0) statusesModel.fireTableDataChanged();
				
				//CHECKBOX FOR CONFIRMED
				TableColumn confirmedColumn = table.getColumnModel().getColumn(WalletItemStatusesTableModel.COLUMN_CONFIRMED);
				// confirmedColumn.setCellRenderer(table.getDefaultRenderer(Boolean.class));
		//		confirmedColumn.setCellRenderer(new Renderer_Boolean()); //statusesTable.getDefaultRenderer(Boolean.class));
				confirmedColumn.setMinWidth(50);
				confirmedColumn.setMaxWidth(90);
				confirmedColumn.setPreferredWidth(90);//.setWidth(30);
				
				TableColumn isUniqueColumn1 = table.getColumnModel().getColumn(WalletItemStatusesTableModel.COLUMN_UNIQUE);
				// confirmedColumn.setCellRenderer(table.getDefaultRenderer(Boolean.class));
		//		confirmedColumn.setCellRenderer(new Renderer_Boolean()); //statusesTable.getDefaultRenderer(Boolean.class));
				isUniqueColumn1.setMinWidth(50);
				isUniqueColumn1.setMaxWidth(90);
				isUniqueColumn1.setPreferredWidth(90);//.setWidth(30);
				
				//CHECKBOX FOR FAVORITE
				favoriteColumn = table.getColumnModel().getColumn(WalletItemStatusesTableModel.COLUMN_FAVORITE);
				favoriteColumn.setMinWidth(50);
				favoriteColumn.setMaxWidth(90);
				favoriteColumn.setPreferredWidth(90);//.setWidth(30);
				searth_Favorite_JCheckBox_LeftPanel.addActionListener( new ActionListener(){

					@Override
					public void actionPerformed(ActionEvent arg0) {
						// TODO Auto-generated method stub
						onChange( tSP, sorter1);
						
					}
					
				});
					
				
				//CREATE SEARCH FIELD
				final JTextField txtSearch = new JTextField();
				
				// UPDATE FILTER ON TEXT CHANGE
				txtSearch.getDocument().addDocumentListener(new DocumentListener() {
					public void changedUpdate(DocumentEvent e) {
						onChange(tSP, sorter1);
					}
				
					public void removeUpdate(DocumentEvent e) {
						onChange(tSP, sorter1);
					}
				
					public void insertUpdate(DocumentEvent e) {
						onChange(tSP, sorter1);
					}
				
				
				});
				
				

						// set video			
						//jTable_jScrollPanel_Panel2_Tabbed_Panel_Left_Panel.setModel(this.tableModelStatuses);
				jTable_jScrollPanel_LeftPanel.setModel(statusesModel);
						//jTable_jScrollPanel_Panel2_Tabbed_Panel_Left_Panel = statusesTable;
				jTable_jScrollPanel_LeftPanel = table;
						//jScrollPanel_Panel2_Tabbed_Panel_Left_Panel.setViewportView(jTable_jScrollPanel_Panel2_Tabbed_Panel_Left_Panel); // statusesTable; 
				jScrollPanel_LeftPanel.setViewportView(jTable_jScrollPanel_LeftPanel);		
						
						
				
				// select row table statuses
				
				 Status_Info info1 = new Status_Info();
				 info1.setFocusable(false);
			 
				// обработка изменения положения курсора в таблице
				table.getSelectionModel().addListSelectionListener(new ListSelectionListener()  {
					@SuppressWarnings("deprecation")
					@Override
					public void valueChanged(ListSelectionEvent arg0) {
						StatusCls status =null;
						if (table.getSelectedRow() >= 0 )status = statusesModel.getItem(table.convertRowIndexToModel(table.getSelectedRow()));
						if (status == null)return;
						info1.show_001(status);
					//	MainStatusesFrame.itemMy = status;

					//	PersJSpline.setDividerLocation(PersJSpline.getDividerLocation());
						tSP.jSplitPanel.setDividerLocation(tSP.jSplitPanel.getDividerLocation());	
						tSP.searchTextField_SearchToolBar_LeftPanel.setEnabled(true);
					}
				});
				tSP.jScrollPane_jPanel_RightPanel.setViewportView(info1);
		
				// MENU
				
				JPopupMenu my_Statuses_Table_menu = new JPopupMenu();
				
				JMenuItem my_favorite = new JMenuItem(Lang.getInstance().translate(""));
				my_favorite.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						
						favorite_set( table );
						
					}
				});
				
				my_Statuses_Table_menu.add(my_favorite);
				
				my_Statuses_Table_menu.addPopupMenuListener(new PopupMenuListener(){

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
						 StatusCls status = statusesModel.getItem(row);
						
							my_favorite.setVisible(true);
							//CHECK IF FAVORITES
							if(Controller.getInstance().isItemFavorite(status))
							{
								my_favorite.setText(Lang.getInstance().translate("Remove Favorite"));
							}
							else
							{
								my_favorite.setText(Lang.getInstance().translate("Add Favorite"));
							}
							
					}
					
				}
				
				);
				jTable_jScrollPanel_LeftPanel.setComponentPopupMenu(my_Statuses_Table_menu);
								
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
