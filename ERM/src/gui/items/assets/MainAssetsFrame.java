package gui.items.assets;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;

import javax.swing.JInternalFrame;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableModel;

import core.item.assets.AssetCls;
import core.item.unions.UnionCls;
import gui.MainFrame;
import gui.Main_Internal_Frame;
import gui.items.unions.Union_Info;
import gui.models.WalletItemAssetsTableModel;
import lang.Lang;

public class MainAssetsFrame extends Main_Internal_Frame{

	private static final long serialVersionUID = 1L;
	AssetDetailsPanel001 info2;
	

public MainAssetsFrame(){
// not show buttons main Toolbar
	this.setTitle(Lang.getInstance().translate("Assets"));
	this.jButton2_jToolBar.setVisible(false);
	this.jButton3_jToolBar.setVisible(false);
// buttun1
	this.jButton1_jToolBar.setText(Lang.getInstance().translate("Issue Assets"));
// status panel
	this.jLabel_status_jPanel.setText(Lang.getInstance().translate("Work with assets"));
	this.jButton1_jToolBar.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				new IssueAssetDialog();
			}
	});		
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
						info.show_Asset_001((AssetCls) asset);
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
	
		
		
		
	
	
	
	
	
	
	
	
	this.jTabbedPane.add(search_Assets_SplitPanel);
	this.pack();
	this.setSize(800,600);
	this.setMaximizable(true);
	this.setClosable(true);
	this.setResizable(true);
	this.setLocation(20, 20);
	setDefaultCloseOperation(JInternalFrame.DISPOSE_ON_CLOSE);
	this.setResizable(true);
    this.setVisible(true);
	Dimension size = MainFrame.desktopPane.getSize();
	this.setSize(new Dimension((int)size.getWidth()-100,(int)size.getHeight()-100));
	search_Assets_SplitPanel.jSplitPanel.setDividerLocation((int)(size.getWidth()/1.618));
	my_Assets_SplitPanel.jSplitPanel.setDividerLocation((int)(size.getWidth()/1.618));	
	
	}


}
