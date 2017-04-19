package gui.items.accounts;

import gui.Split_Panel;
import lang.Lang;

public class Search_Accounts_SplitPanel extends Split_Panel {
	
	public Search_Accounts_SplitPanel(){
		
		
//		LayoutManager favoritesGBC = this.getLayout();

			this.toolBar_LeftPanel.setVisible(false);
			this.setName(Lang.getInstance().translate("Search Accounts"));
			this.jToolBar_RightPanel.setVisible(false);	
		
	}

}
