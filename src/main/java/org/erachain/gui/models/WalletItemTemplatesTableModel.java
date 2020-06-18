package org.erachain.gui.models;
////////

import org.erachain.controller.Controller;
import org.erachain.core.item.templates.TemplateCls;
import org.erachain.datachain.DCSet;

@SuppressWarnings("serial")
public class WalletItemTemplatesTableModel extends WalletTableModel<TemplateCls> {
    public static final int COLUMN_KEY = 0;
    public static final int COLUMN_NAME = 1;
    public static final int COLUMN_ADDRESS = 2;
    public static final int COLUMN_CONFIRMED = 3;
    public static final int COLUMN_FAVORITE = 4;

    public WalletItemTemplatesTableModel() {
        super(Controller.getInstance().wallet.database.getTemplateMap(),
                new String[]{"Key", "Name", "Owner", "Confirmed", "Favorite"},
                new Boolean[]{true, true, true, true, true}, true);
    }

    @Override
    public Object getValueAt(int row, int column) {
        if (this.list == null || row > this.list.size() - 1) {
            return null;
        }

        TemplateCls template = this.list.get(row);

        switch (column) {
            case COLUMN_KEY:

                return template.getKey(DCSet.getInstance());

            case COLUMN_NAME:

                return template.viewName();

            case COLUMN_ADDRESS:

                return template.getOwner().getPersonAsString();

            case COLUMN_CONFIRMED:

                return template.isConfirmed();

            case COLUMN_FAVORITE:

                return template.isFavorite();

        }

        return null;
    }

}
