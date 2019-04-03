package org.erachain.gui.items.statuses;

import org.erachain.core.item.ItemCls;
import org.erachain.core.item.statuses.StatusCls;
import org.erachain.gui.items.SearchItemSplitPanel;

import java.awt.*;

public class SearchStatusesSplitPanel extends SearchItemSplitPanel {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private static StatusesItemsTableModel tableModelUnions = new StatusesItemsTableModel();

    public SearchStatusesSplitPanel() {
        super(tableModelUnions, "Search Statuses", "Search Statuses");

    }

    //show details
    @Override
    protected Component get_show(ItemCls item) {
        StatusInfo info = new StatusInfo();
        info.show_001((StatusCls) item);
        return info;

    }


}
