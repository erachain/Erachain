package org.erachain.gui.items.accounts;

import org.erachain.gui.SplitPanel;
import org.erachain.lang.Lang;

public class SearchAccountsSplitPanel extends SplitPanel {

    public SearchAccountsSplitPanel() {
        super("SearchAccountsSplitPanel");

//		LayoutManager favoritesGBC = this.getLayout();

        this.toolBarLeftPanel.setVisible(false);
        this.setName(Lang.getInstance().translate("Search Accounts"));
        this.jToolBarRightPanel.setVisible(false);

    }

}
