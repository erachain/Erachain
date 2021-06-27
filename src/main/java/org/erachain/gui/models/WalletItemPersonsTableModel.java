package org.erachain.gui.models;
////////

import org.erachain.controller.Controller;
import org.erachain.core.item.persons.PersonCls;
import org.erachain.datachain.DCSet;
import org.erachain.gui.items.WalletItemTableModel;

@SuppressWarnings("serial")
public class WalletItemPersonsTableModel extends WalletItemTableModel<PersonCls> {
    public static final int COLUMN_KEY = 0;
    public static final int COLUMN_NAME = 1;
    public static final int COLUMN_ADDRESS = 2;
    public static final int COLUMN_FAVORITE = 3;

    DCSet dcSet = DCSet.getInstance();

    public WalletItemPersonsTableModel() {
        super(Controller.getInstance().getWallet().dwSet.getPersonMap(),
                new String[]{"Key", "Name", "Publisher", "Favorite"},
                new Boolean[]{true, true, false, false}, COLUMN_FAVORITE, true);

    }

    @Override
    public Object getValueAt(int row, int column) {
        if (this.list == null || row > this.list.size() - 1) {
            return null;
        }

        PersonCls person = this.list.get(row);

        switch (column) {
            case COLUMN_CONFIRMATIONS:
                return person.getConfirmations(dcSet);

            case COLUMN_KEY:
                return person.getKey();
            case COLUMN_NAME:
                return person;
            case COLUMN_ADDRESS:
                return person.getMaker().getPersonAsString();
            case COLUMN_FAVORITE:
                return person.isFavorite();
        }
        return null;
    }

}
