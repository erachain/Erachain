package org.erachain.gui.items.accounts;

import org.erachain.gui.SplitPanel;
import org.erachain.lang.Lang;

public class SearchAccountsSplitPanel extends SplitPanel {

    public SearchAccountsSplitPanel() {
        super("SearchAccountsSplitPanel");

//		LayoutManager favoritesGBC = this.getLayout();

        this.toolBar_LeftPanel.setVisible(false);
        this.setName(Lang.getInstance().translate("Search Accounts"));
        this.jToolBar_RightPanel.setVisible(false);

    }

}
