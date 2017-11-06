package gui.items.accounts;

import java.awt.GridBagConstraints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import core.account.Account;
import core.item.assets.AssetCls;
import gui.Split_Panel;
import lang.Lang;

// панель моих адресов
public class My_Accounts_SplitPanel extends Split_Panel {
	
	public Accounts_Panel accountPanel;
	public Accounts_Right_Panel rightPanel;

	private Account selecArg;
	public AssetCls assetSelect;
	
	public My_Accounts_SplitPanel(){
		super("My_Accounts_SplitPanel");
		
	//	LayoutManager favoritesGBC = this.getLayout();
		this.jScrollPanel_LeftPanel.setVisible(false);
		this.searchToolBar_LeftPanel.setVisible(false);
		this.toolBar_LeftPanel.setVisible(false);
		this.setName(Lang.getInstance().translate("My Accounts"));
		this.jToolBar_RightPanel.setVisible(false);
		
		GridBagConstraints PanelGBC = new GridBagConstraints();
		PanelGBC.fill = GridBagConstraints.BOTH; 
		PanelGBC.anchor = GridBagConstraints.NORTHWEST;
		PanelGBC.weightx = 1;
		PanelGBC.weighty = 1;
		PanelGBC.gridx = 0;	
		PanelGBC.gridy= 0;	
		
		accountPanel = new Accounts_Panel();
		rightPanel = new Accounts_Right_Panel();
		
		this.leftPanel.add( accountPanel, PanelGBC);
		//this.rightPanel1.add(rightPanel,PanelGBC);
		jScrollPane_jPanel_RightPanel.setViewportView(rightPanel);
	//	 this.jSplitPanel.setDividerLocation(0.3);
		
		// EVENTS on CURSOR
		accountPanel.table.getSelectionModel().addListSelectionListener(new Account_Tab_Listener());
		
		
		
		this.repaint();
	
	}
	
	@Override
	public void delay_on_close(){
		rightPanel.table_Model.deleteObserver();
		accountPanel.tableModel.deleteObserver();
		
		
	}

	class Account_Tab_Listener implements ListSelectionListener {
		
		

		//@SuppressWarnings("deprecation")
		@Override
		public void valueChanged(ListSelectionEvent arg0) {
			
			AssetCls asset = (AssetCls) accountPanel.cbxFavorites.getSelectedItem();
			Account account = null;
			if (accountPanel.table.getSelectedRow() >= 0 )
				account = accountPanel.tableModel.getAccount(accountPanel.table.convertRowIndexToModel(accountPanel.table.getSelectedRow()));
			//info1.show_001(person);
			if ( account == null) return;
			if (account.equals(selecArg) && asset.equals(assetSelect)) return;
			selecArg = account;
			assetSelect = asset;
			if (account != null) rightPanel.table_Model.set_Account(account);
			rightPanel.table_Model.set_Asset(asset);
			rightPanel.table_Model.set_Encryption(false);
			rightPanel.table_Model.get_R_Send();
//			rightPanel.jTable1.repaint();
//			my_Accounts_SplitPanel.rightPanel.jTable1.revalidate();
			// PersJSpline.setDividerLocation(PersJSpline.getDividerLocation());
			//my_Person_SplitPanel.jSplitPanel.setDividerLocation(my_Person_SplitPanel.jSplitPanel.getDividerLocation());	
			////my_Person_SplitPanel.searchTextField_SearchToolBar_LeftPanel.setEnabled(true);
			
			
		}
		
	}
	

}
