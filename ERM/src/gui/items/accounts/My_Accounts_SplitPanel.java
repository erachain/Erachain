package gui.items.accounts;

import java.awt.GridBagConstraints;

import gui.Split_Panel;
import lang.Lang;

// панель моих адресов
public class My_Accounts_SplitPanel extends Split_Panel {
	
	private Accounts_Panel accountPanel;
	public My_Accounts_SplitPanel(){
		
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
		
		this.leftPanel.add( accountPanel, PanelGBC);
	
	}

}
