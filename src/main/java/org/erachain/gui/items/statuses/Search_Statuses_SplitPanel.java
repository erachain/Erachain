package org.erachain.gui.items.statuses;

import org.erachain.core.item.ItemCls;
import org.erachain.core.item.statuses.StatusCls;
import org.erachain.gui.items.Search_Item_SplitPanel;

import java.awt.*;

public class Search_Statuses_SplitPanel extends Search_Item_SplitPanel {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private static StatusesItemsTableModel tableModelUnions = new StatusesItemsTableModel();

    public Search_Statuses_SplitPanel() {
        super(tableModelUnions, "Search Statuses", "Search Statuses");

    }

    //show details
    @Override
    protected Component get_show(ItemCls item) {
        Status_Info info = new Status_Info();
        info.show_001((StatusCls) item);
        return info;

    }


}
