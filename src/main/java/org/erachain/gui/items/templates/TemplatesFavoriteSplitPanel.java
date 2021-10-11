package org.erachain.gui.items.templates;

import org.erachain.core.item.ItemCls;
import org.erachain.core.item.templates.TemplateCls;
import org.erachain.gui.items.ItemSplitPanel;

import java.awt.*;

public class TemplatesFavoriteSplitPanel extends ItemSplitPanel {

    public static String NAME = "TemplatesFavoriteSplitPanel";
    public static String TITLE = "Favorite Templates";

    private static final long serialVersionUID = 2717571093561259483L;

    public TemplatesFavoriteSplitPanel() {
        super(new FavoriteTemplatesTableModel(), NAME, TITLE);
        iconName = "favorite.png";

    }

    // show details
    @Override
    public Component getShow(ItemCls item) {
        return new TemplateInfo((TemplateCls) item);
    }


}
