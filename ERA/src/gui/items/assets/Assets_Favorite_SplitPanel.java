	package gui.items.assets;

	import java.awt.Color;
	import java.awt.Component;
	import java.awt.Cursor;
	import java.awt.Dimension;
	import java.awt.GridLayout;
	import java.awt.Point;
	import java.awt.Rectangle;
	import java.awt.event.ActionEvent;
	import java.awt.event.ActionListener;
	import java.awt.event.FocusEvent;
	import java.awt.event.FocusListener;
	import java.awt.event.MouseAdapter;
	import java.awt.event.MouseEvent;
	import java.awt.event.MouseListener;
	import java.awt.event.MouseMotionListener;
	import java.awt.event.WindowEvent;
	import java.awt.event.WindowFocusListener;
	import java.awt.image.ColorModel;
	import javax.swing.Timer;
	import java.awt.*;

import javax.swing.AbstractButton;
import javax.swing.DefaultRowSorter;
	import javax.swing.JButton;
	import javax.swing.JDialog;
	import javax.swing.JFrame;
	import javax.swing.JInternalFrame;
	import javax.swing.JMenuItem;
	import javax.swing.JPanel;
	import javax.swing.JPopupMenu;
	import javax.swing.JScrollPane;
	import javax.swing.JTable;
	import javax.swing.JTextField;
	import javax.swing.RowFilter;
	import javax.swing.RowSorter;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
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
import core.transaction.Transaction;
import database.DBSet;
import gui.MainFrame;
import gui.Main_Internal_Frame;
	import gui.Split_Panel;
import gui.items.accounts.Account_Send_Dialog;
import gui.items.assets.IssueAssetPanel;
	import gui.items.assets.TableModelItemAssets;
import gui.items.mails.Mail_Send_Dialog;
import gui.library.MTable;
import gui.models.Renderer_Boolean;
	import gui.models.Renderer_Left;
	import gui.models.Renderer_Right;
	import gui.models.WalletItemAssetsTableModel;
	import gui.models.WalletItemPersonsTableModel;
import gui.records.VouchRecordDialog;
import lang.Lang;
import utils.TableMenuPopupUtil;


	public class Assets_Favorite_SplitPanel extends Split_Panel{
		private static final long serialVersionUID = 2717571093561259483L;

		
		private TableModelItemAssetsFavorute search_Table_Model;
		private MTable search_Table;
	
		private RowSorter<TableModelItemAssetsFavorute> search_Sorter;
		
	// для прозрачности
	     int alpha =255;
	     int alpha_int;
		
		
	public Assets_Favorite_SplitPanel(){
		super("Assets_Favorite_SplitPanel");
	
		this.setName(Lang.getInstance().translate("Favorite Persons"));
			this.searthLabel_SearchToolBar_LeftPanel.setText(Lang.getInstance().translate("Search") +":  ");
			// not show buttons
		

			
		
			
			
		
			// not show buttons
				jToolBar_RightPanel.setVisible(false);
				toolBar_LeftPanel.setVisible(true);
				button2_ToolBar_LeftPanel.setVisible(false);
				button1_ToolBar_LeftPanel.setText(Lang.getInstance().translate("Search Assets"));
				button1_ToolBar_LeftPanel.addActionListener(new ActionListener(){

					@Override
					public void actionPerformed(ActionEvent arg0) {
						// TODO Auto-generated method stub
						JDialog dd = new JDialog(MainFrame.getInstance());
						dd.setModal(true);
						dd.add(new Search_Assets_Tab(false));
						dd.setPreferredSize(new Dimension(MainFrame.getInstance().getWidth()-100, MainFrame.getInstance().getHeight()-100));
					
						dd.pack();
						//	this.setSize( size.width-(size.width/8), size.height-(size.width/8));
							
							dd.setResizable(true);
							dd.setSize(MainFrame.getInstance().getWidth()-300, MainFrame.getInstance().getHeight()-300);
							dd.setLocationRelativeTo(MainFrame.getInstance());
							dd.setVisible(true);
					
					
					}
					
					
				});
				
				
			
			
		// not show My filter
				searth_My_JCheckBox_LeftPanel.setVisible(false);
				searth_Favorite_JCheckBox_LeftPanel.setVisible(false);
			
		//CREATE TABLE
				search_Table_Model = new TableModelItemAssetsFavorute();
				search_Table = new MTable(this.search_Table_Model);
				TableColumnModel columnModel = search_Table.getColumnModel(); // read column model
				columnModel.getColumn(0).setMaxWidth((100));
		//CHECKBOX FOR FAVORITE	
				TableColumn favorite_Column = search_Table.getColumnModel().getColumn(TableModelItemAssetsFavorute.COLUMN_FAVORITE);//.COLUMN_KEY);//.COLUMN_CONFIRMED);
				favorite_Column.setMinWidth(50);
				favorite_Column.setMaxWidth(1000);
				favorite_Column.setPreferredWidth(50);
				// hand cursor  for Favorite column
				search_Table.addMouseMotionListener(new MouseMotionListener() {
				    public void mouseMoved(MouseEvent e) {
				       
				        if(search_Table.columnAtPoint(e.getPoint())==TableModelItemAssetsFavorute.COLUMN_FAVORITE)
				        {
				     
				        	search_Table.setCursor(new Cursor(Cursor.HAND_CURSOR));
				        } else {
				        	search_Table.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
				        }
				    }

				    public void mouseDragged(MouseEvent e) {
				    }
				});
				
				

//				TableColumn favoriteColumn = search_Table.getColumnModel().getColumn(search_Table_Model.COLUMN_BORN);	
//				favoriteColumn.setCellRenderer(new Renderer_Boolean()); 
			//	 int ss = search_Table_Model.getColumnName(search_Table_Model.COLUMN_BORN).length();
//				int rr = (int) (getFontMetrics( UIManager.getFont("Table.font")).stringWidth(search_Table_Model.getColumnName(search_Table_Model.COLUMN_BORN)));	
//				favoriteColumn.setMinWidth(rr+1);
//				favoriteColumn.setMaxWidth(rr*10);
//				favoriteColumn.setPreferredWidth(rr+5);
				//Sorter
				 search_Sorter = new TableRowSorter<TableModelItemAssetsFavorute>(this.search_Table_Model);
				search_Table.setRowSorter(search_Sorter);	
			
		// UPDATE FILTER ON TEXT CHANGE
				searchTextField_SearchToolBar_LeftPanel.getDocument().addDocumentListener( new search_tab_filter());
		// SET VIDEO			
				jTable_jScrollPanel_LeftPanel.setModel(this.search_Table_Model);
				jTable_jScrollPanel_LeftPanel = search_Table;
				jScrollPanel_LeftPanel.setViewportView(jTable_jScrollPanel_LeftPanel);
//				setRowHeightFormat(true);
		// Event LISTENER		
				jTable_jScrollPanel_LeftPanel.getSelectionModel().addListSelectionListener(new search_listener());
			
				
				jTable_jScrollPanel_LeftPanel.addAncestorListener(new AncestorListener(){

					@Override
					public void ancestorAdded(AncestorEvent arg0) {
						// TODO Auto-generated method stub
						search_Table_Model.addObservers();
					}

					@Override
					public void ancestorMoved(AncestorEvent arg0) {
						// TODO Auto-generated method stub
						
					}

					@Override
					public void ancestorRemoved(AncestorEvent arg0) {
						// TODO Auto-generated method stub
						search_Table_Model.removeObservers();
					}
					
					
					
				});
				
				jTable_jScrollPanel_LeftPanel.addMouseListener(new MouseAdapter() 
					{
						@Override
						public void mousePressed(MouseEvent e) 
						{
							Point p = e.getPoint();
							int row = jTable_jScrollPanel_LeftPanel.rowAtPoint(p);
							jTable_jScrollPanel_LeftPanel.setRowSelectionInterval(row, row);
							
							
							if(e.getClickCount() == 1 & e.getButton() == e.BUTTON1)
							{
								
								if (jTable_jScrollPanel_LeftPanel.getSelectedColumn() == TableModelItemAssetsFavorute.COLUMN_FAVORITE){
									row = jTable_jScrollPanel_LeftPanel.convertRowIndexToModel(row);
									 AssetCls asset = search_Table_Model.getAsset(row);
									favorite_set( jTable_jScrollPanel_LeftPanel);	
									
									
									
								}
								
								
							}
					     }
					});
					
				
				
				JPopupMenu menu = new JPopupMenu();

				
				
				JMenuItem favorite = new JMenuItem(Lang.getInstance().translate(""));
				favorite.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						
						favorite_set( search_Table);
						
					}
				});
				
				
				JMenuItem sell = new JMenuItem(Lang.getInstance().translate("To sell"));
				sell.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						int row = search_Table.getSelectedRow();
						row = search_Table.convertRowIndexToModel(row);

						AssetCls asset = search_Table_Model.getAsset(row);
					//	new AssetPairSelect(asset.getKey(), "To sell",  "");
						new ExchangeFrame(asset,null,  "To sell", "");	
					}
				});
				
				
				
				
				
				
				menu.addPopupMenuListener(new PopupMenuListener(){

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
						
						int row = search_Table.getSelectedRow();
						row = search_Table.convertRowIndexToModel(row);
						AssetCls asset = search_Table_Model.getAsset(row);
						
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
						int row = search_Table.getSelectedRow();
						row = search_Table.convertRowIndexToModel(row);

						AssetCls asset = search_Table_Model.getAsset(row);
					//	new AssetPairSelect(asset.getKey(), "","");
						new ExchangeFrame(asset,null,  "", "");	
					}
				});
				
				
				
				JMenuItem buy = new JMenuItem(Lang.getInstance().translate("Buy"));
				buy.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						int row = search_Table.getSelectedRow();
						row = search_Table.convertRowIndexToModel(row);

						AssetCls asset = search_Table_Model.getAsset(row);
					//	new AssetPairSelect(asset.getKey(), "Buy","");
						new ExchangeFrame(asset,null, "Buy", "");	
					}
				});
				
					
				JMenuItem vouch_menu= new JMenuItem(Lang.getInstance().translate("Vouch"));
				vouch_menu.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						
						int row = jTable_jScrollPanel_LeftPanel.getSelectedRow();
						row = jTable_jScrollPanel_LeftPanel.convertRowIndexToModel(row);
						AssetCls asset = search_Table_Model.getAsset(row);
						
						DBSet db = DBSet.getInstance();
						Transaction trans = db.getTransactionFinalMap().get(db.getTransactionFinalMapSigns()
											.get(asset.getReference()));
					
						new VouchRecordDialog(trans.getBlockHeight(db), trans.getSeqNo(db));
						
					}
				});
				
				

				
				menu.add(excahge);
				menu.addSeparator();
				menu.add(buy);
				
				menu.add(sell);
				menu.addSeparator();
				
				menu.add(favorite);
				
				menu.addSeparator();
				
				menu.add(vouch_menu);
				
				
				
				
			

		    	    	
		    	    	
		    	    	
		    	    	
		    	    	
		    	    	
		    	    	
		    	    	
		    	    	
		   	    	TableMenuPopupUtil.installContextMenu(jTable_jScrollPanel_LeftPanel, menu);
			}
		// set favorite Search	
			void favorite_all(JTable personsTable){
				int row = personsTable.getSelectedRow();
				row = personsTable.convertRowIndexToModel(row);

				AssetCls asset = search_Table_Model.getAsset(row);
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
						

					personsTable.repaint();
				}
			}

		// filter search
			 class search_tab_filter implements DocumentListener {
					
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
			//			search_Table_Model.getSortableList().setFilter(".*" + search + ".*");
			//			search_Table_Model.fireTableDataChanged();
						
			//			search_Table_Model.set_Filter_By_Name(search);
			//			RowFilter filter = RowFilter.regexFilter(".*" + search + ".*", 1);
			//			((DefaultRowSorter) search_Sorter).setRowFilter(filter);
						
						search_Table_Model.fireTableDataChanged();
						
					}
				}
			
		// listener select row	 
			 class search_listener implements ListSelectionListener  {
					@Override
					public void valueChanged(ListSelectionEvent arg0) {
						AssetCls person = null;
						if (search_Table.getSelectedRow() >= 0 ) person = search_Table_Model.getAsset(search_Table.convertRowIndexToModel(search_Table.getSelectedRow()));
						if (person == null) return;
							Asset_Info info_panel = new Asset_Info(person);//, true);
							info_panel.setPreferredSize(new Dimension(jScrollPane_jPanel_RightPanel.getSize().width-50,jScrollPane_jPanel_RightPanel.getSize().height-50));
							jScrollPane_jPanel_RightPanel.setViewportView(info_panel);
						
					}
				}

			 @Override
				public void delay_on_close(){
					// delete observer left panel
				// search_Table_Model.removeObservers();
					// get component from right panel
					Component c1 = jScrollPane_jPanel_RightPanel.getViewport().getView();
					// if Person_Info 002 delay on close
					  if (c1 instanceof Asset_Info) ( (Asset_Info)c1).delay_on_Close();
					
				}
			 
			 public void favorite_set(JTable assetsTable){


				 int row = assetsTable.getSelectedRow();
				 row = assetsTable.convertRowIndexToModel(row);

				  AssetCls asset = search_Table_Model.getAsset(row);
				 //new AssetPairSelect(asset.getKey());
				  if(asset.getKey() >= AssetCls.INITIAL_FAVORITES)
					{
				
				 	//CHECK IF FAVORITES
				 	if(Controller.getInstance().isItemFavorite(asset))
				 	{
				 	//select row in table	
				 		row = assetsTable.getSelectedRow();
				 		Controller.getInstance().removeItemFavorite(asset);
				 		if (search_Table_Model.getRowCount() == 0)  return;
				 		if (row > 0)	assetsTable.addRowSelectionInterval(row-1,row-1);
				 		else assetsTable.addRowSelectionInterval(0,0);
				 		
				 	}
				 	else
				 	{
				 		
				 		Controller.getInstance().addItemFavorite(asset);
				 	}
				 		

				 	assetsTable.repaint();
					}
				 
				 }
			

		}
