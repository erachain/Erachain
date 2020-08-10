package org.erachain.gui.items.imprints;

import org.erachain.core.item.ItemCls;
import org.erachain.core.item.imprints.ImprintCls;
import org.erachain.gui.items.SearchItemSplitPanel;

import java.awt.*;

public class ImprintsSearchSplitPanel extends SearchItemSplitPanel {

    public static String NAME = "ImprintsSearchSplitPanel";
    public static String TITLE = "Search Unique Hashes";

    private static final long serialVersionUID = 2717571093561259483L;

    public ImprintsSearchSplitPanel() {
        super(new ImprintsSearchTableModel(), NAME, TITLE);
    }

    // show details
    @Override
    public Component getShow(ItemCls item) {
        return new ImprintsInfoPanel((ImprintCls) item);
    }

}