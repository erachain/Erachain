package org.erachain.gui.items.imprints;

import org.erachain.core.item.ItemCls;
import org.erachain.core.item.imprints.ImprintCls;
import org.erachain.gui.items.Item_Search_SplitPanel;

import java.awt.*;

public class Imprints_Search_SplitPanel extends Item_Search_SplitPanel {

    private static final long serialVersionUID = 2717571093561259483L;

    private static ImprintsSearchTableModel search_Table_Model = new ImprintsSearchTableModel();

    public Imprints_Search_SplitPanel() {
        super(search_Table_Model, "Persons_Search_SplitPanel", "Persons_Search_SplitPanel");

    }

    // show details
    @Override
    public Component get_show(ItemCls item) {

        return new Imprints_Info_Panel((ImprintCls) item);

    }
}
