package org.erachain.gui.items.unions;

import org.erachain.controller.Controller;
import org.erachain.core.item.unions.UnionCls;
import org.erachain.datachain.DCSet;
import org.erachain.gui.items.FavoriteItemModelTable;
import org.erachain.utils.ObserverMessage;

@SuppressWarnings("serial")
public class FavoriteUnionsTableModel extends FavoriteItemModelTable {
    public static final int COLUMN_KEY = 0;
    public static final int COLUMN_NAME = 1;
    public static final int COLUMN_ADDRESS = 2;
    public static final int COLUMN_FAVORITE = 3;

    public FavoriteUnionsTableModel() {
        super(DCSet.getInstance().getItemUnionMap(),
                Controller.getInstance().wallet.database.getUnionFavoritesSet(),
                new String[]{"Key", "Name", "Publisher", "Favorite"},
                new Boolean[]{false, true, true, false},
                ObserverMessage.RESET_UNION_FAVORITES_TYPE,
                ObserverMessage.ADD_UNION_FAVORITES_TYPE,
                ObserverMessage.DELETE_UNION_FAVORITES_TYPE,
                ObserverMessage.LIST_UNION_FAVORITES_TYPE,
                COLUMN_FAVORITE);
    }

    @Override
    public Object getValueAt(int row, int column) {
        if (this.list == null || row > this.list.size() - 1) {
            return null;
        }

        UnionCls union = (UnionCls) this.list.get(row);
        if (union == null)
            return null;

        switch (column) {
            case COLUMN_CONFIRMATIONS:
                return union.getConfirmations(dcSet);

            case COLUMN_KEY:
                return union.getKey();

            case COLUMN_NAME:
                return union;

            case COLUMN_ADDRESS:
                return union.getMaker().getPersonAsString();

            case COLUMN_FAVORITE:
                return union.isFavorite();

        }

        return null;
    }

}
