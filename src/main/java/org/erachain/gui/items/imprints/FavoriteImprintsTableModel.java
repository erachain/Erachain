package org.erachain.gui.items.imprints;

import org.erachain.controller.Controller;
import org.erachain.core.item.imprints.ImprintCls;
import org.erachain.datachain.DCSet;
import org.erachain.gui.items.FavoriteItemModelTable;
import org.erachain.utils.ObserverMessage;

import java.util.Observer;

@SuppressWarnings("serial")
public class FavoriteImprintsTableModel extends FavoriteItemModelTable implements Observer {
    public static final int COLUMN_KEY = 0;
    public static final int COLUMN_NAME = 1;
    public static final int COLUMN_ADDRESS = 2;
    public static final int COLUMN_FAVORITE = 3;
    public FavoriteImprintsTableModel() {
        super(DCSet.getInstance().getItemImprintMap(),
                Controller.getInstance().wallet.database.getImprintFavoritesSet(),
                new String[]{"Key", "Name", "Publisher", "Favorite"},
                new Boolean[]{false, true, true, false},
                ObserverMessage.RESET_IMPRINT_FAVORITES_TYPE,
                ObserverMessage.ADD_IMPRINT_FAVORITES_TYPE,
                ObserverMessage.REMOVE_IMPRINT_FAVORITES_TYPE,
                ObserverMessage.LIST_IMPRINT_FAVORITES_TYPE,
                COLUMN_FAVORITE);
    }

    @Override
    public Object getValueAt(int row, int column) {
        if (this.list == null || row > this.list.size() - 1) {
            return null;
        }

        ImprintCls item = (ImprintCls) this.list.get(row);
        if (item == null)
            return null;

        switch (column) {
            case COLUMN_CONFIRMATIONS:
                return item.getConfirmations(dcSet);

            case COLUMN_KEY:
                return item.getKey(DCSet.getInstance());

            case COLUMN_NAME:
                return item;

            case COLUMN_ADDRESS:
                return item.getMaker().getPersonAsString();

            case COLUMN_FAVORITE:
                return item.isFavorite();

        }

        return null;
    }

}
