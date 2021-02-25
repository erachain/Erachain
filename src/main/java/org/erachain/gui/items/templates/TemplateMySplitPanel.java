package org.erachain.gui.items.templates;

import org.erachain.core.item.ItemCls;
import org.erachain.core.item.templates.TemplateCls;
import org.erachain.gui.items.ItemSplitPanel;
import org.erachain.gui.models.WalletItemTemplatesTableModel;

import java.awt.*;


public class TemplateMySplitPanel extends ItemSplitPanel {

    public static String NAME = "TemplateMySplitPanel";
    public static String TITLE = "My Templates";

    private static final long serialVersionUID = 2717571093561259483L;

    public TemplateMySplitPanel() {
        super(new WalletItemTemplatesTableModel(), NAME, TITLE);

        // add items in menu

    }

    // show details
    @Override
    public Component getShow(ItemCls item) {
        return new TemplateInfo((TemplateCls) item);
    }

}