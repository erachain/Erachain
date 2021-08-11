package org.erachain.gui.items.templates;

import org.erachain.core.item.ItemCls;
import org.erachain.core.item.templates.TemplateCls;
import org.erachain.gui.items.SearchItemSplitPanel;

import java.awt.*;

@SuppressWarnings("serial")
public class SearchTemplatesSplitPanel extends SearchItemSplitPanel {

    public static String NAME = "SearchTemplatesSplitPanel";
    public static String TITLE = "Search Templates";

    private SearchTemplatesSplitPanel th;

    public SearchTemplatesSplitPanel() {
        super(new TemplatesItemsTableModel(), NAME, TITLE);

        this.th = this;

    }


    //show details
    @Override
    public Component getShow(ItemCls item) {
        return new TemplateInfo((TemplateCls) item);

    }

}