package org.erachain.gui.items.imprints;

import org.erachain.core.item.ItemCls;
import org.erachain.core.item.imprints.ImprintCls;
import org.erachain.gui.items.ItemSplitPanel;
import org.erachain.gui.models.WalletItemImprintsTableModel;

import java.awt.*;

public class MyImprintsTab extends ItemSplitPanel {

    public static String NAME = "MyImprintsTab";
    public static String TITLE = "My Unique Hashes";

    private static final long serialVersionUID = 1L;

    public MyImprintsTab() {
        super(new WalletItemImprintsTableModel(), NAME, TITLE);

    }


    // show details
    @Override
    public Component getShow(ItemCls item) {
        return new ImprintDetailsPanel((ImprintCls) item);
    }

}