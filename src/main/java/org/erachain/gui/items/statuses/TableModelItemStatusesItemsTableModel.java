package org.erachain.gui.items.statuses;

import org.erachain.core.item.statuses.StatusCls;
import org.erachain.datachain.DCSet;
import org.erachain.gui.items.SearchItemsTableModel;

@SuppressWarnings("serial")
public class TableModelItemStatusesItemsTableModel extends SearchItemsTableModel {
    public static final int COLUMN_KEY = 0;
    public static final int COLUMN_NAME = 1;
    public static final int COLUMN_ADDRESS = 2;
    public static final int COLUMN_UNIQUE = 3;
    public static final int COLUMN_FAVORITE = 4;

    public TableModelItemStatusesItemsTableModel() {
        super(DCSet.getInstance().getItemStatusMap(), new String[]{"Key", "Name", "Creator", "Unique", "Favorite"},
                new Boolean[]{false, true, true, false});
        super.COLUMN_FAVORITE = COLUMN_FAVORITE;
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

                return status.viewName();

            case COLUMN_ADDRESS:

                return status.getOwner().getPersonAsString();

            case COLUMN_FAVORITE:

                return status.isFavorite();

            case COLUMN_UNIQUE:

                return status.isUnique();

        }

        return null;
    }

}
