package org.erachain.gui.items.templates;

import org.erachain.core.item.templates.TemplateCls;
import org.erachain.datachain.DCSet;
import org.erachain.gui.items.SearchItemsTableModel;

@SuppressWarnings("serial")
public class TemplatesItemsTableModel extends SearchItemsTableModel {
    public static final int COLUMN_KEY = 0;
    public static final int COLUMN_NAME = 1;
    public static final int COLUMN_ADDRESS = 2;
    public static final int COLUMN_FAVORITE = 3;

    public TemplatesItemsTableModel() {
        super(DCSet.getInstance().getItemTemplateMap(), new String[]{"Key", "Name", "Creator", "Favorite"},
                new Boolean[]{false, true, true, false}, COLUMN_FAVORITE);
    }

    @Override
    public Object getValueAt(int row, int column) {
        if (this.list == null || row > this.list.size() - 1) {
            return null;
        }

        TemplateCls template = (TemplateCls) list.get(row);

        switch (column) {
            case COLUMN_KEY:

                return template.getKey();

            case COLUMN_NAME:

                return template;

            case COLUMN_ADDRESS:

                return template.getMaker().getPersonAsString();

            case COLUMN_FAVORITE:

                return template.isFavorite();

        }

        return null;
    }

}
