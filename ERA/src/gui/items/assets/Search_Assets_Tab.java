package gui.items.assets;

import java.awt.Component;
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
import java.awt.print.PageFormat;
import java.awt.print.PrinterException;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.print.attribute.HashPrintRequestAttributeSet;
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
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;

import org.json.simple.JSONObject;

import com.activetree.common.report.PageNoPainter;
import com.activetree.common.report.printer.JavaDocumentPrinter;
import com.activetree.common.resource.AtImageList;

import controller.Controller;
import core.item.assets.AssetCls;
import core.item.persons.PersonCls;
import core.transaction.Transaction;
import database.DBSet;
import database.Issue_ItemMap;
import demo.activetree.jreport.SimpleHeaderFooterRenderer;
import gui.CoreRowSorter;
import gui.MainFrame;
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
import utils.MenuPopupUtil;

public class Search_Assets_Tab extends Split_Panel {
	private TableModelItemAssets tableModelItemAssets;
	final MTable assetsTable;
	private Search_Assets_Tab th;
	Asset_Info info_panel;
	protected int row;
	private int selected_Item;
	private JTextField key_Item;
	
	
	public Search_Assets_Tab(boolean search_and_exchange){
		super("Search_Assets_Tab");
		th =this;
		setName(Lang.getInstance().translate("Search Assets"));
		searthLabel_SearchToolBar_LeftPanel.setText(Lang.getInstance().translate("Search") +":  ");
		// not show buttons
			button1_ToolBar_LeftPanel.setVisible(false);
			button2_ToolBar_LeftPanel.setVisible(false);
			toolBar_LeftPanel.setVisible(true);
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
							tableModelItemAssets.Find_item_from_key(key_Item.getText());	
							if (tableModelItemAssets.getRowCount() < 1) {
								Label_search_Info_Panel.setText(Lang.getInstance().translate("Not Found Assets"));
								jScrollPanel_LeftPanel.setViewportView(search_Info_Panel);
								return;
							}
							jTable_jScrollPanel_LeftPanel.setRowSelectionInterval(0, 0);
							// ddd.dispose();
							jScrollPanel_LeftPanel.setViewportView(jTable_jScrollPanel_LeftPanel);
						}
					}.start();
					
					
					
				}
	    		
	    	});
			
			
	    	searth_My_JCheckBox_LeftPanel.setVisible(false);
			searth_Favorite_JCheckBox_LeftPanel.setVisible(false);	
			
			
			jButton1_jToolBar_RightPanel.setVisible(true);
			jButton1_jToolBar_RightPanel.setText(Lang.getInstance().translate("Print"));
			jButton2_jToolBar_RightPanel.setVisible(true);
		jToolBar_RightPanel.setVisible(false);
			
			

				  //button pane
		jButton1_jToolBar_RightPanel.setIcon(AtImageList.IMAGE_LIST.PRINT);
		jButton1_jToolBar_RightPanel.addActionListener(new ActionListener() { 
			      public void actionPerformed(ActionEvent evt) {
			        JavaDocumentPrinter docPrinter = new JavaDocumentPrinter();
			        docPrinter.setPageHeaderFooterListener(new SimpleHeaderFooterRenderer(SimpleHeaderFooterRenderer.DEFAULT_HEADER_TEXT, SimpleHeaderFooterRenderer.DEFAULT_FOOTER_TEXT));
			        boolean showPrinterDialog = true;
			        boolean showPageDialog = false;
			        PageFormat defaultPageFormat = new PageFormat();
			        HashPrintRequestAttributeSet pAttrs = null;
			        docPrinter.print(info_panel, PageNoPainter.PAGE_NONE, showPrinterDialog, showPageDialog, defaultPageFormat, pAttrs);
			      }
			    });
		
		jButton2_jToolBar_RightPanel.setIcon(AtImageList.IMAGE_LIST.PREVIEW);
		 jButton2_jToolBar_RightPanel.addActionListener(new ActionListener() {
		      public void actionPerformed(ActionEvent evt) {
		        JavaDocumentPrinter docPrinter = new JavaDocumentPrinter();
		        boolean showPageDialog = true;
		        docPrinter.setPageHeaderFooterListener(new SimpleHeaderFooterRenderer(SimpleHeaderFooterRenderer.DEFAULT_HEADER_TEXT, SimpleHeaderFooterRenderer.DEFAULT_FOOTER_TEXT));
		        docPrinter.preview(info_panel, PageNoPainter.PAGE_NONE, showPageDialog, MainFrame.getInstance());
		      }
		    });

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
	
					
	// set showvideo			
		jTable_jScrollPanel_LeftPanel.setModel(this.tableModelItemAssets);
		jTable_jScrollPanel_LeftPanel = assetsTable;
		jScrollPanel_LeftPanel.setViewportView(jTable_jScrollPanel_LeftPanel);
		
	
		jTable_jScrollPanel_LeftPanel.getSelectionModel().addListSelectionListener(new search_listener());
		
		
		
		searchTextField_SearchToolBar_LeftPanel.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				// GET VALUE
					String search = searchTextField_SearchToolBar_LeftPanel.getText();
					if (search.equals("")){jScrollPane_jPanel_RightPanel.setViewportView(null);
					tableModelItemAssets.clear();
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
							tableModelItemAssets.set_Filter_By_Name(search);
							if (tableModelItemAssets.getRowCount() < 1) {
								Label_search_Info_Panel.setText(Lang.getInstance().translate("Not Found Assets"));
								jScrollPanel_LeftPanel.setViewportView(search_Info_Panel);
								return;
							}
							jTable_jScrollPanel_LeftPanel.setRowSelectionInterval(0, 0);
							// ddd.dispose();
							jScrollPanel_LeftPanel.setViewportView(jTable_jScrollPanel_LeftPanel);
						}
					}.start();
			
			
					
					
			
			
			}
		});
		
		
		
	
	// MENU
	JPopupMenu nameSalesMenu = new JPopupMenu();
	
	nameSalesMenu.addAncestorListener(new AncestorListener(){

		

		@Override
		public void ancestorAdded(AncestorEvent arg0) {
			// TODO Auto-generated method stub
			row = assetsTable.getSelectedRow();
			if (row < 1 ) {
				nameSalesMenu.disable();
		}
		
		row = assetsTable.convertRowIndexToModel(row);
			
			
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
	
	JMenuItem favorite = new JMenuItem(Lang.getInstance().translate(""));
	favorite.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			
			favorite_set( assetsTable);
			
		}
	});
	
	
	JMenuItem sell = new JMenuItem(Lang.getInstance().translate("To sell"));
	sell.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
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
			
			row = assetsTable.getSelectedRow();
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
	
	
	
	

	
	
	
	
	
	JMenuItem excahge = new JMenuItem(Lang.getInstance().translate("Exchange"));
	excahge.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			AssetCls asset = tableModelItemAssets.getAsset(row);
		//	new AssetPairSelect(asset.getKey(), "","");
			new ExchangeFrame(asset,null,  "", "");	
		}
	});
	
	
	
	JMenuItem buy = new JMenuItem(Lang.getInstance().translate("Buy"));
	buy.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			AssetCls asset = tableModelItemAssets.getAsset(row);
		//	new AssetPairSelect(asset.getKey(), "Buy","");
			new ExchangeFrame(asset,null, "Buy", "");	
		}
	});
	
		
	JMenuItem vouch_menu= new JMenuItem(Lang.getInstance().translate("Vouch"));
	vouch_menu.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			AssetCls asset = tableModelItemAssets.getAsset(row);
			
			DBSet db = DBSet.getInstance();
			Transaction trans = db.getTransactionFinalMap().get(db.getTransactionFinalMapSigns()
								.get(asset.getReference()));
		
			new VouchRecordDialog(trans.getBlockHeight(db), trans.getSeqNo(db));
			
		}
	});
	
	
//	nameSalesMenu.add(favorite);
	if (search_and_exchange){
	 nameSalesMenu.add(excahge);
	nameSalesMenu.addSeparator();
	nameSalesMenu.add(buy);
	
	nameSalesMenu.add(sell);
	nameSalesMenu.addSeparator();
	
	nameSalesMenu.add(favorite);
	
	nameSalesMenu.addSeparator();
	
	nameSalesMenu.add(vouch_menu);
	} else {
		nameSalesMenu.add(favorite);
	}
	
	
	
	assetsTable.setComponentPopupMenu(nameSalesMenu);
	assetsTable.addMouseListener(new MouseAdapter() {
		@Override
		public void mousePressed(MouseEvent e) {
			Point p = e.getPoint();
			row = assetsTable.rowAtPoint(p);
			assetsTable.setRowSelectionInterval(row, row);
			
			if(e.getClickCount() == 2 && search_and_exchange)
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
			int div = th.jSplitPanel.getDividerLocation();
			int or = th.jSplitPanel.getOrientation();
			info_panel = new Asset_Info(asset);
				//info_panel.setPreferredSize(new Dimension(jScrollPane_jPanel_RightPanel.getSize().width-50,jScrollPane_jPanel_RightPanel.getSize().height-50));
				jScrollPane_jPanel_RightPanel.setViewportView(info_panel);
				//jSplitPanel.setRightComponent(info_panel);
				jSplitPanel.setDividerLocation(div);
				jSplitPanel.setOrientation(or);
				jToolBar_RightPanel.setVisible(true);
		}
	}

@Override
public void delay_on_close(){
	// delete observer left panel
	tableModelItemAssets.removeObservers();
	// get component from right panel
	Component c1 = jScrollPane_jPanel_RightPanel.getViewport().getView();
	// if Person_Info 002 delay on close
//	  if (c1 instanceof Asset_Info) ( (Asset_Info)c1).delay_on_Close();
	
}

}
