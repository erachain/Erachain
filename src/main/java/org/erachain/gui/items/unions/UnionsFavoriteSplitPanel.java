package org.erachain.gui.items.unions;

import org.erachain.core.item.ItemCls;
import org.erachain.core.item.unions.UnionCls;
import org.erachain.gui.items.ItemSplitPanel;

import java.awt.*;

public class UnionsFavoriteSplitPanel extends ItemSplitPanel {

    public static String NAME = "UnionsFavoriteSplitPanel";
    public static String TITLE = "Favorite Unions";

    private static final long serialVersionUID = 2717571093561259483L;

    public UnionsFavoriteSplitPanel() {
        super(new FavoriteUnionsTableModel(), NAME, TITLE);
        iconName = "favorite.png";

    }

    // show details
    @Override
    public Component getShow(ItemCls item) {
        UnionInfo unionInfo = new UnionInfo();
        unionInfo.show_Union_001((UnionCls) item);
        return unionInfo;
    }
}
