package gui.items.accounts;

import java.awt.GridBagConstraints;

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import core.account.Account;
import core.item.assets.AssetCls;
import gui.Split_Panel;
import lang.Lang;

// панель моих адресов
public class My_Credits_SplitPanel extends Split_Panel {
	
	public Credits_Panel accountPanel;
	public Accounts_Right_Panel rightPanel;
	public My_Credits_SplitPanel(){
		
	//	LayoutManager favoritesGBC = this.getLayout();
		this.jScrollPanel_LeftPanel.setVisible(false);
		this.searchToolBar_LeftPanel.setVisible(false);
		this.toolBar_LeftPanel.setVisible(false);
		this.setName(Lang.getInstance().translate("My Credits"));
		this.jToolBar_RightPanel.setVisible(false);
		
		GridBagConstraints PanelGBC = new GridBagConstraints();
		PanelGBC.fill = GridBagConstraints.BOTH; 
		PanelGBC.anchor = GridBagConstraints.NORTHWEST;
		PanelGBC.weightx = 1;
		PanelGBC.weighty = 1;
		PanelGBC.gridx = 0;	
		PanelGBC.gridy= 0;	
		
		accountPanel = new Credits_Panel();
		rightPanel = new Accounts_Right_Panel();
		
		this.leftPanel.add( accountPanel, PanelGBC);
		//this.rightPanel1.add(rightPanel,PanelGBC);
		jScrollPane_jPanel_RightPanel.setViewportView(rightPanel);
		
		// EVENTS on CURSOR
		accountPanel.table.getSelectionModel().addListSelectionListener(new Account_Tab_Listener());
		
		
	
	}
	

	class Account_Tab_Listener implements ListSelectionListener {
		
		//@SuppressWarnings("deprecation")
		@Override
		public void valueChanged(ListSelectionEvent arg0) {
			
			AssetCls asset = (AssetCls) accountPanel.cbxFavorites.getSelectedItem();
			Account account = null;
	//		if (accountPanel.table.getSelectedRow() >= 0 )
	//			account = accountPanel.tableModel.getAccount(accountPanel.table.convertRowIndexToModel(accountPanel.table.getSelectedRow()));
			//info1.show_001(person);
//			rightPanel.jTable1.Search_Accoutnt_Transaction_From_Asset(account, asset);
//			my_Accounts_SplitPanel.rightPanel.jTable1.revalidate();
			// PersJSpline.setDividerLocation(PersJSpline.getDividerLocation());
			//my_Person_SplitPanel.jSplitPanel.setDividerLocation(my_Person_SplitPanel.jSplitPanel.getDividerLocation());	
			////my_Person_SplitPanel.searchTextField_SearchToolBar_LeftPanel.setEnabled(true);
			
			
		}
		
	}
	

}
