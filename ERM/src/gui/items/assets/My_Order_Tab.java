package gui.items.assets;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.DefaultRowSorter;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.RowFilter;
import javax.swing.RowSorter;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;

import controller.Controller;
import core.item.assets.AssetCls;
import core.item.assets.Order;
import gui.CoreRowSorter;
import gui.Split_Panel;
import gui.models.Renderer_Boolean;
import gui.models.Renderer_Right;
import gui.models.WalletItemAssetsTableModel;
import gui.models.WalletItemImprintsTableModel;
import gui.models.WalletOrdersTableModel;
import lang.Lang;

public class My_Order_Tab extends Split_Panel {
	
	/**
	 * 
	 */
	WalletOrdersTableModel ordersModel;
	private static final long serialVersionUID = 1L;

	public My_Order_Tab()
	{

	
	this.setName("My Orders");
	searthLabel_SearchToolBar_LeftPanel.setText(Lang.getInstance().translate("Search") +":  ");
	// not show buttons
		button1_ToolBar_LeftPanel.setVisible(false);
		button2_ToolBar_LeftPanel.setVisible(false);
		jButton1_jToolBar_RightPanel.setVisible(false);
		jButton2_jToolBar_RightPanel.setVisible(false);
		
	//TABLE
		ordersModel = new WalletOrdersTableModel();
	final JTable table = new JTable(ordersModel);
	
	
	
	//assetsModel.getAsset(row)
	//POLLS SORTER
	RowSorter sorter =   new TableRowSorter(ordersModel);
	table.setRowSorter(sorter);	
//	Map<Integer, Integer> indexes = new TreeMap<Integer, Integer>();
//	CoreRowSorter sorter = new CoreRowSorter(assetsModel, indexes);
//	table.setRowSorter(sorter);
			
	//CHECKBOX FOR DIVISIBLE
//	TableColumn divisibleColumn = table.getColumnModel().getColumn(WalletItemAssetsTableModel.COLUMN_DIVISIBLE);
//	divisibleColumn.setCellRenderer(table.getDefaultRenderer(Boolean.class));
	
	//CHECKBOX FOR CONFIRMED
//	TableColumn confirmedColumn = table.getColumnModel().getColumn(WalletItemAssetsTableModel.COLUMN_CONFIRMED);
//	confirmedColumn.setCellRenderer(table.getDefaultRenderer(Boolean.class));
	
	//CHECKBOX FOR FAVORITE
//	TableColumn favoriteColumn = table.getColumnModel().getColumn(WalletItemAssetsTableModel.COLUMN_FAVORITE);
//	favoriteColumn.setCellRenderer(table.getDefaultRenderer(Boolean.class));
	
	//Custom renderer for the String column;
	table.setDefaultRenderer(Long.class, new Renderer_Right()); // set renderer
	table.setDefaultRenderer(String.class, new Renderer_Right()); // set renderer
	table.setDefaultRenderer(Boolean.class, new Renderer_Boolean()); // set renderer

// column #1
	TableColumn column1 = table.getColumnModel().getColumn(WalletItemAssetsTableModel.COLUMN_KEY);//.COLUMN_CONFIRMED);
	column1.setMinWidth(1);
	column1.setMaxWidth(1000);
	column1.setPreferredWidth(50);
// column #1
	TableColumn column2 = table.getColumnModel().getColumn(WalletItemAssetsTableModel.COLUMN_CONFIRMED);//.COLUMN_CONFIRMED);
	column2.setMinWidth(50);
	column2.setMaxWidth(1000);
	column2.setPreferredWidth(50);
		// column #1
		TableColumn column3 = table.getColumnModel().getColumn(WalletItemAssetsTableModel.COLUMN_DIVISIBLE);//.COLUMN_CONFIRMED);
		column3.setMinWidth(50);
		column3.setMaxWidth(1000);
		column3.setPreferredWidth(50);
		// column #1
		TableColumn column4 = table.getColumnModel().getColumn(WalletItemAssetsTableModel.COLUMN_FAVORITE);//.COLUMN_KEY);//.COLUMN_CONFIRMED);
		column4.setMinWidth(50);
		column4.setMaxWidth(1000);
		column4.setPreferredWidth(50);
	
		
		
// add listener
//		jTable_jScrollPanel_LeftPanel.getSelectionModel().addListSelectionListener(table);
// show	
	this.jTable_jScrollPanel_LeftPanel.setModel(ordersModel);
	this.jTable_jScrollPanel_LeftPanel = table;
	jScrollPanel_LeftPanel.setViewportView(jTable_jScrollPanel_LeftPanel);
	
	// UPDATE FILTER ON TEXT CHANGE
			searchTextField_SearchToolBar_LeftPanel.getDocument().addDocumentListener(new DocumentListener() {
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
					String search = searchTextField_SearchToolBar_LeftPanel.getText();

		// SET FILTER
					ordersModel.fireTableDataChanged();
					RowFilter filter = RowFilter.regexFilter(".*" + search + ".*", 1);
					((DefaultRowSorter) sorter).setRowFilter(filter);
					ordersModel.fireTableDataChanged();
									
				}
			});
	
	
	
	
	
	
	
	
	//MENU
	JPopupMenu assetsMenu = new JPopupMenu();
	
	JMenuItem favorite = new JMenuItem(Lang.getInstance().translate("Exchange"));
	favorite.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			
			favorite_set( table);
			
		}
	});
	
	assetsMenu.addPopupMenuListener(new PopupMenuListener(){

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
			 Order order = ordersModel.getOrder(row);
			
			//IF ASSET CONFIRMED AND NOT ERM
			/*
				favorite.setVisible(true);
				//CHECK IF FAVORITES
				if(Controller.getInstance().isItemFavorite(order))
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
	
	
	assetsMenu.add(favorite);
	
	
	
	
	
	JMenuItem details = new JMenuItem(Lang.getInstance().translate("Details"));
	details.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			int row = table.getSelectedRow();
			row = table.convertRowIndexToModel(row);

	//		AssetCls asset = assetsModel.getAsset(row);
//			new AssetFrame(asset);
		}
	});
//	assetsMenu.add(details);
	JMenuItem dividend = new JMenuItem(Lang.getInstance().translate("Pay dividend"));
	dividend.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			int row = table.getSelectedRow();
			row = table.convertRowIndexToModel(row);

	//		AssetCls asset = assetsModel.getAsset(row);	
	//		new PayDividendFrame(asset);
		}
	});
	assetsMenu.add(dividend);
	table.setComponentPopupMenu(assetsMenu);
	
	//MOUSE ADAPTER
	table.addMouseListener(new MouseAdapter() 
	{
		@Override
		public void mousePressed(MouseEvent e) 
		{
			Point p = e.getPoint();
			int row = table.rowAtPoint(p);
			table.setRowSelectionInterval(row, row);
	     }
	});
	
	table.addMouseListener(new MouseAdapter() 
	{
		@Override
		public void mousePressed(MouseEvent e) 
		{
			Point p = e.getPoint();
			int row = table.rowAtPoint(p);
			table.setRowSelectionInterval(row, row);
	/*		
			if(e.getClickCount() == 2)
			{
				row = table.convertRowIndexToModel(row);
				AssetCls asset = assetsModel.getAsset(row);
				new AssetFrame(asset);
			}
			if(e.getClickCount() == 1 & e.getButton() == e.BUTTON1)
			{
				
				if (table.getSelectedColumn() == WalletItemAssetsTableModel.COLUMN_FAVORITE){
					row = table.convertRowIndexToModel(row);
					AssetCls asset = orderModel.getAsset(row);
					favorite_set( table);	
					
					
					
				}
				
				
			}
			*/
	     }
	});
	
	
	
	
}

public void onIssueClick()
{
	new IssueAssetFrame();
}

public void onAllClick()
{
	new AllAssetsFrame();
}

public void onMyOrdersClick()
{
	new MyOrdersFrame();
}

public void favorite_set(JTable assetsTable){


int row = assetsTable.getSelectedRow();
row = assetsTable.convertRowIndexToModel(row);

Order order = ordersModel.getOrder(row);
//new AssetPairSelect(asset.getKey());
/*
if(order.getKey() >= AssetCls.INITIAL_FAVORITES)
{
	//CHECK IF FAVORITES
	if(Controller.getInstance().isItemFavorite(asset))
	{
		
		Controller.getInstance().removeItemFavorite(asset);
	}
	else
	{
		
		Controller.getInstance().addItemFavorite(asset);
	}
		

	assetsTable.repaint();

}
*/
}


}
