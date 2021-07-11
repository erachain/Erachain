package org.erachain.gui.models;
////////

import org.erachain.controller.Controller;
import org.erachain.core.item.templates.TemplateCls;

@SuppressWarnings("serial")
public class WalletItemTemplatesTableModel extends WalletTableModel<TemplateCls> {
    public static final int COLUMN_KEY = 0;
    public static final int COLUMN_NAME = 1;
    public static final int COLUMN_ADDRESS = 2;
    public static final int COLUMN_CONFIRMED = 3;
    public static final int COLUMN_FAVORITE = 4;

    public WalletItemTemplatesTableModel() {
        super(Controller.getInstance().getWallet().dwSet.getTemplateMap(),
                new String[]{"Key", "Name", "Maker", "Confirmed", "Favorite"},
                new Boolean[]{true, true, true, true, true}, true, COLUMN_FAVORITE);
    }

    @Override
    public Object getValueAt(int row, int column) {
        if (this.list == null || row > this.list.size() - 1) {
            return null;
        }

        TemplateCls template = this.list.get(row);

        switch (column) {
            case COLUMN_CONFIRMATIONS:
                return template.getConfirmations(dcSet);

            case COLUMN_KEY:
                return template.getKey();

            case COLUMN_NAME:
                return template;

            case COLUMN_ADDRESS:
                return template.getMaker().getPersonAsString();

            case COLUMN_CONFIRMED:
                return template.isConfirmed();

            case COLUMN_FAVORITE:
                return template.isFavorite();

        }

        return null;
    }

}
