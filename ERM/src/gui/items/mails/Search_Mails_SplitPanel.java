package gui.items.mails;

import gui.Split_Panel;
import lang.Lang;

public class Search_Mails_SplitPanel extends Split_Panel {
	
	public Search_Mails_SplitPanel(){
		
		
//		LayoutManager favoritesGBC = this.getLayout();

			this.toolBar_LeftPanel.setVisible(false);
			this.setName(Lang.getInstance().translate("Search Accounts"));
			this.jToolBar_RightPanel.setVisible(false);	
		
	}

}
