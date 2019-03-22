package org.erachain.gui.items.imprints;

import org.erachain.core.item.ItemCls;
import org.erachain.core.item.imprints.ImprintCls;
import org.erachain.gui.items.Search_Item_SplitPanel;

import java.awt.*;

public class Imprints_Search_SplitPanel extends Search_Item_SplitPanel {

    private static final long serialVersionUID = 2717571093561259483L;

    private static ImprintsSearchTableModel search_Table_Model = new ImprintsSearchTableModel();

    public Imprints_Search_SplitPanel() {
        super(search_Table_Model, "Search_Persons_SplitPanel", "Search_Persons_SplitPanel");

    }

    // show details
    @Override
    public Component get_show(ItemCls item) {

        return new Imprints_Info_Panel((ImprintCls) item);

    }
}
