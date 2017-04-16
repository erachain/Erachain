package gui.items.assets;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.JInternalFrame;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableModel;

import org.junit.Assert;
import org.mapdb.Fun.Tuple2;

import controller.Controller;
import core.account.Account;
import core.account.PublicKeyAccount;
import core.item.ItemCls;
import core.item.assets.AssetCls;
import core.item.unions.UnionCls;
import database.DBSet;
import database.SortableList;
import gui.MainFrame;
import gui.Main_Internal_Frame;
import gui.items.unions.Union_Info;
import gui.models.Balance_from_Adress_TableModel;
import gui.models.WalletItemAssetsTableModel;
import lang.Lang;
import utils.Pair;

public class MainAssetsFrame extends Main_Internal_Frame{

	private static final long serialVersionUID = 1L;
	AssetDetailsPanel001 info2;
	

public MainAssetsFrame(){
// not show buttons main Toolbar
	this.setTitle(Lang.getInstance().translate("Assets"));
	this.jButton2_jToolBar.setVisible(false);
	this.jButton3_jToolBar.setVisible(false);
	this.jButton1_jToolBar.setVisible(false);
	this.jToolBar.setVisible(false);
// buttun1
	this.jButton1_jToolBar.setText(Lang.getInstance().translate("Issue Assets"));
// status panel
	this.jButton1_jToolBar.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				new IssueAssetDialog();
			}
	});	
	
	
	
	 DBSet dbSet =  DBSet.getInstance();  
	
	

// my Assets
	My_Assets_Tab my_Assets_SplitPanel = new My_Assets_Tab();
	this.jTabbedPane.add(my_Assets_SplitPanel);
	
	 Asset_Info info = new Asset_Info();
	
	// обработка изменения положения курсора в таблице
	
		my_Assets_SplitPanel.jTable_jScrollPanel_LeftPanel.getSelectionModel().addListSelectionListener(new ListSelectionListener()  {
			@SuppressWarnings({ "unused" })
			@Override
				public void valueChanged(ListSelectionEvent arg0) {
					String dateAlive;
					String date_birthday;
					String message;
	// устанавливаем формат даты
					SimpleDateFormat formatDate = new SimpleDateFormat("dd.MM.yyyy"); // HH:mm");
	//создаем объект персоны
					UnionCls union;
					if (my_Assets_SplitPanel.jTable_jScrollPanel_LeftPanel.getSelectedRow() >= 0 ){
						WalletItemAssetsTableModel tableModelAssets = (WalletItemAssetsTableModel) my_Assets_SplitPanel.jTable_jScrollPanel_LeftPanel.getModel();//new WalletItemAssetsTableModel();//(WalletItemAssetsTableModel) my_Assets_SplitPanel.jTable_jScrollPanel_LeftPanel.getModel();
						Object asset = tableModelAssets.getAsset(my_Assets_SplitPanel.jTable_jScrollPanel_LeftPanel.convertRowIndexToModel(my_Assets_SplitPanel.jTable_jScrollPanel_LeftPanel.getSelectedRow()));
						//info.show_Asset_001((AssetCls) asset);
						info2=new AssetDetailsPanel001((AssetCls) asset);
						my_Assets_SplitPanel.jSplitPanel.setDividerLocation(my_Assets_SplitPanel.jSplitPanel.getDividerLocation());	
						my_Assets_SplitPanel.searchTextField_SearchToolBar_LeftPanel.setEnabled(true);
						my_Assets_SplitPanel.jScrollPane_jPanel_RightPanel.setViewportView(info2);
					}
				}
			});				
		//	my_Assets_SplitPanel.jScrollPane_jPanel_RightPanel.setViewportView(info);
		
	
	
		
	
	
	
// search Assets
	Search_Assets_Tab search_Assets_SplitPanel = new Search_Assets_Tab();
	search_Assets_SplitPanel.jScrollPane_jPanel_RightPanel.setBorder(null);
	
	// Asset_Info info1 = new Asset_Info();
	
		
		// обработка изменения положения курсора в таблице
	 search_Assets_SplitPanel.jTable_jScrollPanel_LeftPanel.getSelectionModel().addListSelectionListener(new ListSelectionListener()  {
				@SuppressWarnings({ "unused" })
				@Override
					public void valueChanged(ListSelectionEvent arg0) {
						String dateAlive;
						String date_birthday;
						String message; 
		// устанавливаем формат даты
						SimpleDateFormat formatDate = new SimpleDateFormat("dd.MM.yyyy"); // HH:mm");
		//создаем объект персоны
						UnionCls union;
						if (search_Assets_SplitPanel.jTable_jScrollPanel_LeftPanel.getSelectedRow() >= 0 ){
							TableModelItemAssets tableModelAssets1 =  (TableModelItemAssets) search_Assets_SplitPanel.jTable_jScrollPanel_LeftPanel.getModel();//new WalletItemAssetsTableModel();//(WalletItemAssetsTableModel) my_Assets_SplitPanel.jTable_jScrollPanel_LeftPanel.getModel();
							Object asset = tableModelAssets1.getAsset(search_Assets_SplitPanel.jTable_jScrollPanel_LeftPanel.convertRowIndexToModel(search_Assets_SplitPanel.jTable_jScrollPanel_LeftPanel.getSelectedRow()));
						//	info1.show_Asset_002((AssetCls) asset);
							
							
							
							 //info2 = new AssetPanel((AssetCls) asset);
						//	 info2 = new AssetDetailsPanel001((AssetCls) asset);
						//	search_Assets_SplitPanel.jSplitPanel.setDividerLocation(search_Assets_SplitPanel.jSplitPanel.getDividerLocation());	
						//	search_Assets_SplitPanel.searchTextField_SearchToolBar_LeftPanel.setEnabled(true);
						/*	
							 search_Assets_SplitPanel.jScrollPane_jPanel_RightPanel.setVisible(false);
							 
							 GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
						        gridBagConstraints.gridx = 0;
						        gridBagConstraints.gridy = 1;
						        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
						        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
						        gridBagConstraints.weightx = 1.0;
						        gridBagConstraints.weighty = 1.0;
						        gridBagConstraints.insets = new java.awt.Insets(0, 8, 8, 8);
						       
						       
						// delete info component
							 Component[] comp = search_Assets_SplitPanel.rightPanel1.getComponents();
							for (int i=0; i<comp.length; i=i+1){
								if (comp[i].getClass().getName()==info2.getClass().getName()){
									search_Assets_SplitPanel.rightPanel1.remove(comp[i]);
								}
							}
						
							search_Assets_SplitPanel.rightPanel1.add(info2, gridBagConstraints);
						*/		
							
							search_Assets_SplitPanel.jScrollPane_jPanel_RightPanel.setViewportView(new AssetDetailsPanel001((AssetCls) asset));
							
							 
						}
					}
				});				
	
		
// my orders
	 
	// search Assets
		 My_Order_Tab my_Orders_SplitPanel = new My_Order_Tab();
		my_Orders_SplitPanel.jScrollPane_jPanel_RightPanel.setBorder(null);
		
		// Asset_Info info1 = new Asset_Info();
		
			
			// обработка изменения положения курсора в таблице
		my_Orders_SplitPanel.jTable_jScrollPanel_LeftPanel.getSelectionModel().addListSelectionListener(new ListSelectionListener()  {
					@SuppressWarnings({ "unused" })
					@Override
						public void valueChanged(ListSelectionEvent arg0) {
							String dateAlive;
							String date_birthday;
							String message; 
			// устанавливаем формат даты
							SimpleDateFormat formatDate = new SimpleDateFormat("dd.MM.yyyy"); // HH:mm");
			//создаем объект персоны
							UnionCls union;
							if (my_Orders_SplitPanel.jTable_jScrollPanel_LeftPanel.getSelectedRow() >= 0 ){
								 TableModel tableModelOrder = my_Orders_SplitPanel.jTable_jScrollPanel_LeftPanel.getModel();//new WalletItemAssetsTableModel();//(WalletItemAssetsTableModel) my_Assets_SplitPanel.jTable_jScrollPanel_LeftPanel.getModel();
					//			Object asset = tableModelOrder..get.getAsset(my_Orders_SplitPanel.jTable_jScrollPanel_LeftPanel.convertRowIndexToModel(my_Orders_SplitPanel.jTable_jScrollPanel_LeftPanel.getSelectedRow()));
							//	info1.show_Asset_002((AssetCls) asset);
								
								
								
								 //info2 = new AssetPanel((AssetCls) asset);
							//	 info2 = new AssetDetailsPanel001((AssetCls) asset);
							//	search_Assets_SplitPanel.jSplitPanel.setDividerLocation(search_Assets_SplitPanel.jSplitPanel.getDividerLocation());	
							//	search_Assets_SplitPanel.searchTextField_SearchToolBar_LeftPanel.setEnabled(true);
							/*	
								 search_Assets_SplitPanel.jScrollPane_jPanel_RightPanel.setVisible(false);
								 
								 GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
							        gridBagConstraints.gridx = 0;
							        gridBagConstraints.gridy = 1;
							        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
							        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
							        gridBagConstraints.weightx = 1.0;
							        gridBagConstraints.weighty = 1.0;
							        gridBagConstraints.insets = new java.awt.Insets(0, 8, 8, 8);
							       
							       
							// delete info component
								 Component[] comp = search_Assets_SplitPanel.rightPanel1.getComponents();
								for (int i=0; i<comp.length; i=i+1){
									if (comp[i].getClass().getName()==info2.getClass().getName()){
										search_Assets_SplitPanel.rightPanel1.remove(comp[i]);
									}
								}
							
								search_Assets_SplitPanel.rightPanel1.add(info2, gridBagConstraints);
							*/		
								
						//		my_Orders_SplitPanel.jScrollPane_jPanel_RightPanel.setViewportView(new AssetDetailsPanel001((AssetCls) asset));
								
								 
							}
						}
					});				
		
			
			
			
		
		// my balanses
		 
		
			 My_Balance_Tab my_Balanses_SplitPanel = new My_Balance_Tab();
			 my_Balanses_SplitPanel.jScrollPane_jPanel_RightPanel.setBorder(null);
			
			// Asset_Info info1 = new Asset_Info();
			
				
				// обработка изменения положения курсора в таблице
			 my_Balanses_SplitPanel.jTable_jScrollPanel_LeftPanel.getSelectionModel().addListSelectionListener(new ListSelectionListener()  {
						@SuppressWarnings({ "unused" })
						@Override
							public void valueChanged(ListSelectionEvent arg0) {
								String dateAlive;
								String date_birthday;
								String message; 
				// устанавливаем формат даты
								SimpleDateFormat formatDate = new SimpleDateFormat("dd.MM.yyyy"); // HH:mm");
				//создаем объект персоны
								UnionCls union;
								if (my_Balanses_SplitPanel.jTable_jScrollPanel_LeftPanel.getSelectedRow() >= 0 ){
									Balance_from_Adress_TableModel tableModelAssets1 = (Balance_from_Adress_TableModel) my_Balanses_SplitPanel.jTable_jScrollPanel_LeftPanel.getModel();//new WalletItemAssetsTableModel();//(WalletItemAssetsTableModel) my_Assets_SplitPanel.jTable_jScrollPanel_LeftPanel.getModel();
									 AssetCls asset = tableModelAssets1.getAsset(my_Balanses_SplitPanel.jTable_jScrollPanel_LeftPanel.convertRowIndexToModel(my_Balanses_SplitPanel.jTable_jScrollPanel_LeftPanel.getSelectedRow()));
			 						// AssetCls asset = tableModelAssets1.getAsset(item);
									
									
									 //info2 = new AssetPanel((AssetCls) asset);
								//	 info2 = new AssetDetailsPanel001((AssetCls) asset);
								//	search_Assets_SplitPanel.jSplitPanel.setDividerLocation(search_Assets_SplitPanel.jSplitPanel.getDividerLocation());	
								//	search_Assets_SplitPanel.searchTextField_SearchToolBar_LeftPanel.setEnabled(true);
								/*	
									 search_Assets_SplitPanel.jScrollPane_jPanel_RightPanel.setVisible(false);
									 
									 GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
								        gridBagConstraints.gridx = 0;
								        gridBagConstraints.gridy = 1;
								        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
								        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
								        gridBagConstraints.weightx = 1.0;
								        gridBagConstraints.weighty = 1.0;
								        gridBagConstraints.insets = new java.awt.Insets(0, 8, 8, 8);
								       
								       
								// delete info component
									 Component[] comp = search_Assets_SplitPanel.rightPanel1.getComponents();
									for (int i=0; i<comp.length; i=i+1){
										if (comp[i].getClass().getName()==info2.getClass().getName()){
											search_Assets_SplitPanel.rightPanel1.remove(comp[i]);
										}
									}
								
									search_Assets_SplitPanel.rightPanel1.add(info2, gridBagConstraints);
								*/		
									
			 						my_Balanses_SplitPanel.jScrollPane_jPanel_RightPanel.setViewportView(new Asset_to_Accounts_DetailsPanel((AssetCls) asset));
									
									 
								}
							}
						});				
			
				
			// issue Assets
				 IssueAssetPanel Issue_Assets_SplitPanel = new IssueAssetPanel();
				 Issue_Assets_SplitPanel.setName(Lang.getInstance().translate("Issue Asset"));
						
				
			
	
	 
	 
	 
	
	
	
	
	
	
	this.jTabbedPane.add(my_Balanses_SplitPanel);
	this.jTabbedPane.add(search_Assets_SplitPanel);
	this.jTabbedPane.add(my_Orders_SplitPanel);
	this.jTabbedPane.add(Issue_Assets_SplitPanel);	
	this.pack();
	this.setSize(800,600);
	this.setMaximizable(true);
	this.setClosable(true);
	this.setResizable(true);
	this.setLocation(20, 20);
	setDefaultCloseOperation(JInternalFrame.DISPOSE_ON_CLOSE);
	this.setResizable(true);
    this.setVisible(true);
	Dimension size = MainFrame.getInstance().desktopPane.getSize();
	this.setSize(new Dimension((int)size.getWidth()-100,(int)size.getHeight()-100));
	search_Assets_SplitPanel.jSplitPanel.setDividerLocation((int)(size.getWidth()/1.618));
	my_Assets_SplitPanel.jSplitPanel.setDividerLocation((int)(size.getWidth()/1.618));
	my_Orders_SplitPanel.jSplitPanel.setDividerLocation((int)(size.getWidth()/1.618));
	my_Balanses_SplitPanel.jSplitPanel.setDividerLocation((int)(size.getWidth()/2));
	
	}


}
