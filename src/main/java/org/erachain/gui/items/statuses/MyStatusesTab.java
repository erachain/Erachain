package org.erachain.gui.items.statuses;

import org.erachain.core.item.ItemCls;
import org.erachain.core.item.statuses.StatusCls;
import org.erachain.gui.items.ItemSplitPanel;
import org.erachain.gui.models.WalletItemStatusesTableModel;

import java.awt.*;

public class MyStatusesTab extends ItemSplitPanel {

    public static String NAME = "MyStatusesTab";
    public static String TITLE = "My Statuses";

    public MyStatusesTab() {
        super(new WalletItemStatusesTableModel(), NAME, TITLE);
    }

    // show details
    @Override
    public Component getShow(ItemCls item) {
        return new StatusInfo((StatusCls) item);
    }
}
