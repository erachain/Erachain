package gui.items.assets;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.DefaultRowSorter;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.RowSorter;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;

import org.json.simple.JSONObject;

import controller.Controller;
import core.item.assets.AssetCls;
import core.item.persons.PersonCls;
import core.transaction.Transaction;
import database.DBSet;
import database.Issue_ItemMap;
import gui.CoreRowSorter;
import gui.Split_Panel;
import gui.items.persons.Person_Info_002;
import gui.items.unions.TableModelUnions;
import gui.library.MTable;
import gui.models.Renderer_Boolean;
import gui.models.Renderer_Left;
import gui.models.Renderer_Right;
import gui.models.WalletItemImprintsTableModel;
import gui.records.VouchRecordDialog;
import lang.Lang;

public class Search_Assets_Tab extends Split_Panel {
	private TableModelItemAssets tableModelItemAssets;
	final MTable assetsTable;
	
	
	public Search_Assets_Tab(){
		super("Search_Assets_Tab");
		
		setName(Lang.getInstance().translate("Search Assets"));
		searthLabel_SearchToolBar_LeftPanel.setText(Lang.getInstance().translate("Search") +":  ");
		// not show buttons
			button1_ToolBar_LeftPanel.setVisible(false);
			button2_ToolBar_LeftPanel.setVisible(false);
			jButton1_jToolBar_RightPanel.setVisible(false);
			jButton2_jToolBar_RightPanel.setVisible(false);
			

	//CREATE TABLE
	tableModelItemAssets = new TableModelItemAssets();
	 assetsTable = new MTable(tableModelItemAssets);
	
	//CHECKBOX FOR DIVISIBLE
//	TableColumn divisibleColumn = assetsTable.getColumnModel().getColumn(TableModelItemAssets.COLUMN_DIVISIBLE);
//	divisibleColumn.setCellRenderer(assetsTable.getDefaultRenderer(Boolean.class));
	
	//CHECKBOX FOR FAVORITE
//	TableColumn favoriteColumn = assetsTable.getColumnModel().getColumn(TableModelItemAssets.COLUMN_FAVORITE);
//	favoriteColumn.setCellRenderer(assetsTable.getDefaultRenderer(Boolean.class));

	//ASSETS SORTER
//	Map<Integer, Integer> indexes = new TreeMap<Integer, Integer>();
//	CoreRowSorter sorter = new CoreRowSorter(tableModelItemAssets, indexes);
//	assetsTable.setRowSorter(sorter);
	
		// column #1
		TableColumn column1 = assetsTable.getColumnModel().getColumn(TableModelItemAssets.COLUMN_KEY);//.COLUMN_CONFIRMED);
		column1.setMinWidth(50);
		column1.setMaxWidth(1000);
		column1.setPreferredWidth(50);
		// column #1
		TableColumn columnM = assetsTable.getColumnModel().getColumn(TableModelItemAssets.COLUMN_MOVABLE);
		columnM.setMinWidth(50);
		columnM.setMaxWidth(1000);
		columnM.setPreferredWidth(50);
		// column #1
		TableColumn column2 = assetsTable.getColumnModel().getColumn(TableModelItemAssets.COLUMN_DIVISIBLE);//.COLUMN_KEY);//.COLUMN_CONFIRMED);
		column2.setMinWidth(50);
		column2.setMaxWidth(1000);
		column2.setPreferredWidth(50);
		// column #1
		TableColumn column3 = assetsTable.getColumnModel().getColumn(TableModelItemAssets.COLUMN_FAVORITE);//.COLUMN_KEY);//.COLUMN_CONFIRMED);
		column3.setMinWidth(50);
		column3.setMaxWidth(1000);
		column3.setPreferredWidth(50);
		TableColumn column4 = assetsTable.getColumnModel().getColumn(TableModelItemAssets.COLUMN_I_OWNER);//.COLUMN_KEY);//.COLUMN_CONFIRMED);
		column4.setMinWidth(60);
		column4.setMaxWidth(1000);
		column4.setPreferredWidth(60);
								
				
				
				
				
//Sorter
		RowSorter sorter =   new TableRowSorter(tableModelItemAssets);
		assetsTable.setRowSorter(sorter);	
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
				tableModelItemAssets.fireTableDataChanged();
				RowFilter filter = RowFilter.regexFilter(".*" + search + ".*", 1);
				((DefaultRowSorter) sorter).setRowFilter(filter);
				tableModelItemAssets.fireTableDataChanged();
								
			}
		});
				
	// set showvideo			
		jTable_jScrollPanel_LeftPanel.setModel(this.tableModelItemAssets);
		jTable_jScrollPanel_LeftPanel = assetsTable;
		jScrollPanel_LeftPanel.setViewportView(jTable_jScrollPanel_LeftPanel);
		
	
		jTable_jScrollPanel_LeftPanel.getSelectionModel().addListSelectionListener(new search_listener());
		
		
		
	
	// MENU
	JPopupMenu nameSalesMenu = new JPopupMenu();
	
	JMenuItem favorite = new JMenuItem(Lang.getInstance().translate(""));
	favorite.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			
			favorite_set( assetsTable);
			
		}
	});
	
	
	JMenuItem sell = new JMenuItem(Lang.getInstance().translate("To sell"));
	sell.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			int row = assetsTable.getSelectedRow();
			row = assetsTable.convertRowIndexToModel(row);

			AssetCls asset = tableModelItemAssets.getAsset(row);
		//	new AssetPairSelect(asset.getKey(), "To sell",  "");
			new ExchangeFrame(asset,null,  "To sell", "");	
		}
	});
	
	
	
	
	
	
	nameSalesMenu.addPopupMenuListener(new PopupMenuListener(){

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
			
			int row = assetsTable.getSelectedRow();
			row = assetsTable.convertRowIndexToModel(row);
			AssetCls asset = tableModelItemAssets.getAsset(row);
			
			//IF ASSET CONFIRMED AND NOT ERM
			if(asset.getKey() >= AssetCls.INITIAL_FAVORITES)
			{
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
	
	
	
	

	
	
	nameSalesMenu.add(favorite);
	
	
	
	
	
	
	JMenuItem details = new JMenuItem(Lang.getInstance().translate("Details"));
	details.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			int row = assetsTable.getSelectedRow();
			row = assetsTable.convertRowIndexToModel(row);

			AssetCls asset = tableModelItemAssets.getAsset(row);
			new AssetFrame(asset);
		}
	});
//	nameSalesMenu.add(details);
	
	JMenuItem excahge = new JMenuItem(Lang.getInstance().translate("Exchange"));
	excahge.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			int row = assetsTable.getSelectedRow();
			row = assetsTable.convertRowIndexToModel(row);

			AssetCls asset = tableModelItemAssets.getAsset(row);
		//	new AssetPairSelect(asset.getKey(), "","");
			new ExchangeFrame(asset,null,  "", "");	
		}
	});
	 nameSalesMenu.add(excahge);
	
	
	JMenuItem buy = new JMenuItem(Lang.getInstance().translate("Buy"));
	buy.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			int row = assetsTable.getSelectedRow();
			row = assetsTable.convertRowIndexToModel(row);

			AssetCls asset = tableModelItemAssets.getAsset(row);
		//	new AssetPairSelect(asset.getKey(), "Buy","");
			new ExchangeFrame(asset,null, "Buy", "");	
		}
	});
	
		
	JMenuItem vouch_menu= new JMenuItem(Lang.getInstance().translate("Vouch"));
	vouch_menu.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			
			int row = jTable_jScrollPanel_LeftPanel.getSelectedRow();
			row = jTable_jScrollPanel_LeftPanel.convertRowIndexToModel(row);
			AssetCls asset = tableModelItemAssets.getAsset(row);
			
			DBSet db = DBSet.getInstance();
			Transaction trans = db.getTransactionFinalMap().get(db.getTransactionFinalMapSigns()
								.get(asset.getReference()));
		
			new VouchRecordDialog(trans.getBlockHeight(db), trans.getSeqNo(db));
			
		}
	});
	
	
	
	
	nameSalesMenu.addSeparator();
	nameSalesMenu.add(buy);
	
	nameSalesMenu.add(sell);
	nameSalesMenu.addSeparator();
	
	nameSalesMenu.add(favorite);
	
	nameSalesMenu.addSeparator();
	
	nameSalesMenu.add(vouch_menu);
	
	assetsTable.setComponentPopupMenu(nameSalesMenu);
	assetsTable.addMouseListener(new MouseAdapter() {
		@Override
		public void mousePressed(MouseEvent e) {
			Point p = e.getPoint();
			int row = assetsTable.rowAtPoint(p);
			assetsTable.setRowSelectionInterval(row, row);
			
			if(e.getClickCount() == 2)
			{
				row = assetsTable.convertRowIndexToModel(row);
				AssetCls asset = tableModelItemAssets.getAsset(row);
			//	new AssetPairSelect(asset.getKey(), "","");
				new ExchangeFrame(asset,null, "", "");
		//		new AssetFrame(asset);
			}
			if(e.getClickCount() == 1 & e.getButton() == e.BUTTON1)
			{
				
				if (assetsTable.getSelectedColumn() == TableModelItemAssets.COLUMN_FAVORITE){
					row = assetsTable.convertRowIndexToModel(row);
					AssetCls asset = tableModelItemAssets.getAsset(row);
					favorite_set( assetsTable);	
					
					
					
				}
				
				
			}
		}
	});

	// hand cursor  for Favorite column
	assetsTable.addMouseMotionListener(new MouseMotionListener() {
	    public void mouseMoved(MouseEvent e) {
	       
	        if(assetsTable.columnAtPoint(e.getPoint())==TableModelItemAssets.COLUMN_FAVORITE)
	        {
	     
	        	assetsTable.setCursor(new Cursor(Cursor.HAND_CURSOR));
	        } else {
	        	assetsTable.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
	        }
	    }

	    public void mouseDragged(MouseEvent e) {
	    }
	});
	
	
	
	
}


public void favorite_set(JTable assetsTable){


int row = assetsTable.getSelectedRow();
row = assetsTable.convertRowIndexToModel(row);

AssetCls asset = tableModelItemAssets.getAsset(row);
//new AssetPairSelect(asset.getKey());

if(asset.getKey() >= AssetCls.INITIAL_FAVORITES)
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
}
// listener select row	 
class search_listener implements ListSelectionListener  {
		@Override
		public void valueChanged(ListSelectionEvent arg0) {
			AssetCls asset = null;
			if (assetsTable.getSelectedRow() >= 0 ) asset = tableModelItemAssets.getAsset(assetsTable.convertRowIndexToModel(assetsTable.getSelectedRow()));
			if (asset == null) return;
			AssetDetailsPanel001 info_panel = new AssetDetailsPanel001(asset);
				//info_panel.setPreferredSize(new Dimension(jScrollPane_jPanel_RightPanel.getSize().width-50,jScrollPane_jPanel_RightPanel.getSize().height-50));
				jScrollPane_jPanel_RightPanel.setViewportView(info_panel);
			
		}
	}

}
