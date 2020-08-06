package org.erachain.gui.models;
////////

import org.erachain.controller.Controller;
import org.erachain.core.item.statuses.StatusCls;
import org.erachain.datachain.DCSet;

@SuppressWarnings("serial")
public class WalletItemStatusesTableModel extends WalletTableModel<StatusCls> {
    public static final int COLUMN_KEY = 0;
    public static final int COLUMN_NAME = 1;
    public static final int COLUMN_ADDRESS = 2;
    public static final int COLUMN_UNIQUE = 3;
    public static final int COLUMN_CONFIRMED = 4;
    public static final int COLUMN_FAVORITE = 5;

    public WalletItemStatusesTableModel() {
        super(Controller.getInstance().wallet.database.getStatusMap(),
                new String[]{"Key", "Name", "Creator", "Unique", "Confirmed", "Favorite"},
                new Boolean[]{false, true, true, false, false}, true, COLUMN_FAVORITE);
    }

    @Override
    public Object getValueAt(int row, int column) {
        if (this.list == null || row > this.list.size() - 1) {
            return null;
        }

        StatusCls status = this.list.get(row);

        switch (column) {
            case COLUMN_KEY:

                return status.getKey(DCSet.getInstance());

            case COLUMN_NAME:

                return status.viewName();

            case COLUMN_ADDRESS:

                return status.getOwner().getPersonAsString();

            case COLUMN_CONFIRMED:

                return status.isConfirmed();

            case COLUMN_FAVORITE:

                return status.isFavorite();

            case COLUMN_UNIQUE:

                return status.isUnique();

        }

        return null;
    }

}
