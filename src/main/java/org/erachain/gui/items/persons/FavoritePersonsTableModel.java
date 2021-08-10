package org.erachain.gui.items.persons;

import org.erachain.controller.Controller;
import org.erachain.core.item.persons.PersonCls;
import org.erachain.datachain.DCSet;
import org.erachain.gui.items.FavoriteItemModelTable;
import org.erachain.utils.ObserverMessage;

@SuppressWarnings("serial")
public class FavoritePersonsTableModel extends FavoriteItemModelTable {
    public static final int COLUMN_KEY = 0;
    public static final int COLUMN_NAME = 1;
    public static final int COLUMN_ADDRESS = 2;
    public static final int COLUMN_FAVORITE = 3;

    public FavoritePersonsTableModel() {
        super(DCSet.getInstance().getItemPersonMap(),
                Controller.getInstance().getWallet().dwSet.getPersonFavoritesSet(),
                new String[]{"Key", "Name", "Publisher", "Favorite"},
                new Boolean[]{false, true, true, false},
                ObserverMessage.RESET_PERSON_FAVORITES_TYPE,
                ObserverMessage.ADD_PERSON_FAVORITES_TYPE,
                ObserverMessage.DELETE_PERSON_FAVORITES_TYPE,
                ObserverMessage.LIST_PERSON_FAVORITES_TYPE,
                COLUMN_FAVORITE);
    }

    @Override
    protected void updateMap() {
        favoriteMap = Controller.getInstance().getWallet().dwSet.getPersonFavoritesSet();
    }

    @Override
    public Object getValueAt(int row, int column) {
        if (this.list == null || row > this.list.size() - 1) {
            return null;
        }

        PersonCls person = (PersonCls) this.list.get(row);
        if (person == null)
            return null;


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
