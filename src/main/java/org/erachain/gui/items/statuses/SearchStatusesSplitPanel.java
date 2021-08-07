package org.erachain.gui.items.statuses;

import org.erachain.core.item.ItemCls;
import org.erachain.core.item.statuses.StatusCls;
import org.erachain.gui.items.SearchItemSplitPanel;

import java.awt.*;

public class SearchStatusesSplitPanel extends SearchItemSplitPanel {

    public static String NAME = "SearchStatusesSplitPanel";
    public static String TITLE = "Search Statuses";

    private static final long serialVersionUID = 1L;

    public SearchStatusesSplitPanel() {
        super(new StatusesItemsTableModel(), NAME, TITLE);

    }

    //show details
    @Override
    public Component getShow(ItemCls item) {
        return new StatusInfo((StatusCls) item);
    }

}