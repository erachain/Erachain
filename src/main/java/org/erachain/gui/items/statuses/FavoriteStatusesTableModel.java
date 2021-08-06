package org.erachain.gui.items.statuses;

import org.erachain.controller.Controller;
import org.erachain.core.item.statuses.StatusCls;
import org.erachain.datachain.DCSet;
import org.erachain.gui.items.FavoriteItemModelTable;
import org.erachain.utils.ObserverMessage;

@SuppressWarnings("serial")
public class FavoriteStatusesTableModel extends FavoriteItemModelTable {
    public static final int COLUMN_KEY = 0;
    public static final int COLUMN_NAME = 1;
    public static final int COLUMN_ADDRESS = 2;
    public static final int COLUMN_FAVORITE = 3;

    public FavoriteStatusesTableModel() {
        super(DCSet.getInstance().getItemStatusMap(),
                Controller.getInstance().getWallet().dwSet.getStatusFavoritesSet(),
                new String[]{"Key", "Name", "Publisher", "Favorite"},
                new Boolean[]{false, true, true, false},
                ObserverMessage.RESET_STATUS_FAVORITES_TYPE,
                ObserverMessage.ADD_STATUS_FAVORITES_TYPE,
                ObserverMessage.DELETE_STATUS_FAVORITES_TYPE,
                ObserverMessage.LIST_STATUS_FAVORITES_TYPE,
                COLUMN_FAVORITE);
    }

    @Override
    public Object getValueAt(int row, int column) {
        if (this.list == null || row > this.list.size() - 1) {
            return null;
        }

        StatusCls status = (StatusCls) this.list.get(row);
        if (status == null)
            return null;


        switch (column) {
            case COLUMN_CONFIRMATIONS:
                return status.getConfirmations(dcSet);

            case COLUMN_KEY:
                return status.getKey();

            case COLUMN_NAME:
                return status;

            case COLUMN_ADDRESS:
                return status.getMaker().getPersonAsString();

            case COLUMN_FAVORITE:
                return status.isFavorite();

        }

        return null;
    }

}
