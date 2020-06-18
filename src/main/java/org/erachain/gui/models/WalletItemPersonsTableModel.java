package org.erachain.gui.models;
////////

import org.erachain.controller.Controller;
import org.erachain.core.item.persons.PersonCls;

@SuppressWarnings("serial")
public class WalletItemPersonsTableModel extends WalletTableModel<PersonCls> {
    public static final int COLUMN_KEY = 0;
    public static final int COLUMN_NAME = 1;
    public static final int COLUMN_ADDRESS = 2;
    public static final int COLUMN_CONFIRMED = 3;
    public static final int COLUMN_FAVORITE = 4;

    public WalletItemPersonsTableModel() {
        super(Controller.getInstance().getWallet().database.getPersonMap(),
                new String[]{"Key", "Name", "Publisher", "Confirmed", "Favorite"},
                new Boolean[]{false, true, true, false, false}, true);

    }

    @Override
    public Object getValueAt(int row, int column) {
        if (this.list == null || row > this.list.size() - 1) {
            return null;
        }

        PersonCls person = this.list.get(row);

        switch (column) {
            case COLUMN_KEY:
                return person.getKey();
            case COLUMN_NAME:
                return person;
            case COLUMN_ADDRESS:
                return person.getOwner().getPersonAsString();
            case COLUMN_CONFIRMED:
                return person.isConfirmed();
            case COLUMN_FAVORITE:
                return person.isFavorite();
        }
        return null;
    }

}
