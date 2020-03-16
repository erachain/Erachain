package org.erachain.gui.items.imprints;

import org.erachain.core.item.ItemCls;
import org.erachain.core.item.imprints.ImprintCls;
import org.erachain.gui.items.SearchItemSplitPanel;
import org.erachain.settings.Settings;

import java.awt.*;

public class ImprintsSearchSplitPanel extends SearchItemSplitPanel  {

    private static final long serialVersionUID = 2717571093561259483L;
    private static String iconFile = Settings.getInstance().getPatnIcons() + "ImprintsSearchSplitPanel.png";
    private static ImprintsSearchTableModel search_Table_Model = new ImprintsSearchTableModel();

    public ImprintsSearchSplitPanel() {
        super(search_Table_Model, "SearchPersonsSplitPanel", "SearchPersonsSplitPanel");

    }

    // show details
    @Override
    public Component getShow(ItemCls item) {

        return new ImprintsInfoPanel((ImprintCls) item);

    }

    public static  Image getIcon() {
        {
            try {
                return Toolkit.getDefaultToolkit().getImage(iconFile);
            } catch (Exception e) {
                return null;
            }
        }
    }
}
