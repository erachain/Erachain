package gui.items.assets;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
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
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;

import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple3;

import controller.Controller;
import core.account.Account;
import core.item.assets.AssetCls;
import core.item.assets.Order;
import database.DBSet;
import database.SortableList;
import gui.CoreRowSorter;
import gui.Split_Panel;
import gui.items.assets.My_Assets_Tab.search_listener;
import gui.library.MTable;
import gui.models.Balance_from_Adress_TableModel;
import gui.models.Renderer_Boolean;
import gui.models.Renderer_Left;
import gui.models.Renderer_Right;
import gui.models.WalletItemAssetsTableModel;
import gui.models.WalletItemImprintsTableModel;
import gui.models.WalletOrdersTableModel;
import lang.Lang;
import utils.Pair;

public class My_Balance_Tab extends Split_Panel {
	
	/**
	 * 
	 */
	
	private static final long serialVersionUID = 1L;
	
	Balance_from_Adress_TableModel BalancesModel;
	private SortableList<Tuple2<String, Long>, Tuple3<BigDecimal, BigDecimal, BigDecimal>> balances;

	protected int row;
	final MTable table;
	private My_Balance_Tab th;
	
	@SuppressWarnings({ "null", "unchecked", "rawtypes" })
	public My_Balance_Tab()
	{
		super("My_Balance_Tab");
	th= this;
	this.setName(Lang.getInstance().translate("My Balance"));
	searthLabel_SearchToolBar_LeftPanel.setText(Lang.getInstance().translate("Search") +":  ");
	// not show buttons
		button1_ToolBar_LeftPanel.setVisible(false);
		button2_ToolBar_LeftPanel.setVisible(false);
		jButton1_jToolBar_RightPanel.setVisible(false);
		jButton2_jToolBar_RightPanel.setVisible(false);
		
	//TABLE
		
		  BalancesModel = new Balance_from_Adress_TableModel();
	table = new MTable(BalancesModel);
	
	
	
	//assetsModel.getAsset(row)
	//POLLS SORTER
	RowSorter sorter =   new TableRowSorter(BalancesModel);
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
	
	
// column #1
	TableColumn column1 = table.getColumnModel().getColumn(Balance_from_Adress_TableModel.COLUMN_ASSET_KEY);//.COLUMN_CONFIRMED);
	column1.setMinWidth(1);
	column1.setMaxWidth(1000);
	column1.setPreferredWidth(20);
	column1.setWidth(20);
	
	
	// column #1
	TableColumn column2 = table.getColumnModel().getColumn(Balance_from_Adress_TableModel.COLUMN_A);//.COLUMN_CONFIRMED);
	column2.setMinWidth(50);
	column2.setMaxWidth(1000);
	column2.setPreferredWidth(50);
	
	
	// column #1
		TableColumn column3 = table.getColumnModel().getColumn(Balance_from_Adress_TableModel.COLUMN_B);//.COLUMN_CONFIRMED);
		column3.setMinWidth(50);
		column3.setMaxWidth(1000);
		column3.setPreferredWidth(50);

		// column #1
		TableColumn column4 = table.getColumnModel().getColumn(Balance_from_Adress_TableModel.COLUMN_C);//.COLUMN_CONFIRMED);
		column4.setMinWidth(50);
		column4.setMaxWidth(1000);
		column4.setPreferredWidth(50);
	
		
		// column #1
		TableColumn column5 = table.getColumnModel().getColumn(Balance_from_Adress_TableModel.COLUMN_ASSET_NAME);//.COLUMN_CONFIRMED);
		column5.setMinWidth(50);
		column5.setMaxWidth(1000);
		column5.setPreferredWidth(150);
	
/*		// column #1
		TableColumn column4 = table.getColumnModel().getColumn(Balance_from_Adress_TableModel.COLUMN_ACCOUNT);//.COLUMN_KEY);//.COLUMN_CONFIRMED);
		column4.setMinWidth(50);
		column4.setMaxWidth(1000);
		column4.setPreferredWidth(50);
		column4.setCellRenderer(new Renderer_Right());
*/	
		
		
// add listener
//		jTable_jScrollPanel_LeftPanel.getSelectionModel().addListSelectionListener(table);
// show	
	this.jTable_jScrollPanel_LeftPanel.setModel(BalancesModel);
	this.jTable_jScrollPanel_LeftPanel = table;
	jTable_jScrollPanel_LeftPanel.getSelectionModel().addListSelectionListener(new search_listener());
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
					BalancesModel.fireTableDataChanged();
					RowFilter filter = RowFilter.regexFilter(".*" + search + ".*", 1);
					((DefaultRowSorter) sorter).setRowFilter(filter);
					BalancesModel.fireTableDataChanged();
									
				}
			});
	
	
	
		
	
	
	//MENU
	JPopupMenu assetsMenu = new JPopupMenu();
	
	assetsMenu.addAncestorListener(new AncestorListener(){

		

		@Override
		public void ancestorAdded(AncestorEvent arg0) {
			// TODO Auto-generated method stub
			row = table.getSelectedRow();
			if (row < 1 ) {
				assetsMenu.disable();
		}
		
		row = table.convertRowIndexToModel(row);
			
			
		}

		@Override
		public void ancestorMoved(AncestorEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void ancestorRemoved(AncestorEvent arg0) {
			// TODO Auto-generated method stub
			
		}
		
		
		
	});
	
	
	
	JMenuItem sell = new JMenuItem(Lang.getInstance().translate("To sell"));
	sell.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			AssetCls asset = BalancesModel.getAsset(row);
			String account = BalancesModel.getAccount(row);
			//AssetPairSelect a = new AssetPairSelect(asset.getKey(), "To sell", account);
			new ExchangeFrame(asset,null, "To sell", account);
			
			
		}
	});
	
	
	JMenuItem excahge = new JMenuItem(Lang.getInstance().translate("Exchange"));
	excahge.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			AssetCls asset = BalancesModel.getAsset(row);
		//	new AssetPairSelect(asset.getKey(), "","");
			new ExchangeFrame(asset,null, "", "");			
		}
	});
	assetsMenu.add(excahge);
	
	
	JMenuItem buy = new JMenuItem(Lang.getInstance().translate("Buy"));
	buy.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			AssetCls asset = BalancesModel.getAsset(row);
	//		new AssetPairSelect(asset.getKey(), "Buy","");
			new ExchangeFrame(asset,null,  "Buy", "");	
		}
	});
	
	assetsMenu.addSeparator();
	assetsMenu.add(buy);
	
	assetsMenu.add(sell);
	assetsMenu.addSeparator();
	
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
			
			row = table.getSelectedRow();
			row = table.convertRowIndexToModel(row);
			 Class<? extends Object> order = BalancesModel.getColumnClass(row);
			
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
	
	

	
	
	
	
	
	JMenuItem details = new JMenuItem(Lang.getInstance().translate("Details"));
	details.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			
	//		AssetCls asset = assetsModel.getAsset(row);
//			new AssetFrame(asset);
		}
	});
//	assetsMenu.add(details);
	JMenuItem dividend = new JMenuItem(Lang.getInstance().translate("Pay dividend"));
	dividend.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			
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

//Order order = ordersModel.getOrder(row);
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

//listener select row	 
class search_listener implements ListSelectionListener  {
		@Override
		public void valueChanged(ListSelectionEvent arg0) {
			AssetCls asset = null;
			if (table.getSelectedRow() >= 0 ) asset = BalancesModel.getAsset(table.convertRowIndexToModel(table.getSelectedRow()));
			if (asset == null) return;
			//AssetDetailsPanel001 info_panel = new AssetDetailsPanel001(asset);
				//info_panel.setPreferredSize(new Dimension(jScrollPane_jPanel_RightPanel.getSize().width-50,jScrollPane_jPanel_RightPanel.getSize().height-50));
				int div = th.jSplitPanel.getDividerLocation();
				int or = th.jSplitPanel.getOrientation();
				Asset_Info info_panel = new Asset_Info(asset);
					//info_panel.setPreferredSize(new Dimension(jScrollPane_jPanel_RightPanel.getSize().width-50,jScrollPane_jPanel_RightPanel.getSize().height-50));
					jScrollPane_jPanel_RightPanel.setViewportView(info_panel);
					//jSplitPanel.setRightComponent(info_panel);
					jSplitPanel.setDividerLocation(div);
					jSplitPanel.setOrientation(or);
			
		}
	}

}
