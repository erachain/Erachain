package org.erachain.gui.items.unions;

import org.erachain.core.item.ItemCls;
import org.erachain.core.item.unions.UnionCls;
import org.erachain.gui.items.ItemSplitPanel;
import org.erachain.gui.models.WalletItemUnionsTableModel;

import java.awt.*;

public class MyUnionsTab extends ItemSplitPanel {

    public static String NAME = "MyUnionsTab";
    public static String TITLE = "My Unions";

    public MyUnionsTab() {
        super(new WalletItemUnionsTableModel(), NAME, TITLE);
    }

    // show details
    @Override
    public Component getShow(ItemCls item) {
        return new UnionDetailsPanel((UnionCls) item);
    }

}