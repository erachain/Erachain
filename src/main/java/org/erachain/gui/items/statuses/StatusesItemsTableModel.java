package org.erachain.gui.items.statuses;

import org.erachain.controller.Controller;
import org.erachain.core.item.statuses.StatusCls;
import org.erachain.datachain.DCSet;
import org.erachain.gui.items.SearchItemsTableModel;

@SuppressWarnings("serial")
public class StatusesItemsTableModel extends SearchItemsTableModel {
    public static final int COLUMN_KEY = 0;
    public static final int COLUMN_NAME = 1;
    public static final int COLUMN_ADDRESS = 2;
    public static final int COLUMN_UNIQUE = 3;
    public static final int COLUMN_FAVORITE = 4;

    public StatusesItemsTableModel() {
        super(DCSet.getInstance().getItemStatusMap(), new String[]{"Key", "Name", "Creator", "Unique", "Favorite"},
                new Boolean[]{false, true, true, false},
                COLUMN_FAVORITE);
    }

    @Override
    protected void updateMap() {
        map = Controller.getInstance().getWallet().dwSet.getTransactionMap();
    }

    @Override
    public Object getValueAt(int row, int column) {
        if (list == null || row > list.size() - 1) {
            return null;
        }

        StatusCls status = (StatusCls) list.get(row);

        switch (column) {
            case COLUMN_KEY:

                return status.getKey();

            case COLUMN_NAME:

                return status;

            case COLUMN_ADDRESS:

                return status.getMaker().getPersonAsString();

            case COLUMN_FAVORITE:

                return status.isFavorite();

            case COLUMN_UNIQUE:

                return status.isUnique();

        }

        return null;
    }

}
