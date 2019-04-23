package org.erachain.gui.models;
////////

import org.erachain.controller.Controller;
import org.erachain.core.item.statuses.StatusCls;
import org.erachain.database.SortableList;
import org.erachain.datachain.DCSet;
import org.erachain.utils.ObserverMessage;
import org.erachain.utils.Pair;
import org.mapdb.Fun.Tuple2;

import java.util.Observable;
import java.util.Observer;

@SuppressWarnings("serial")
public class WalletItemStatusesTableModel extends WalletAutoKeyTableModel<Tuple2<Long, Long>, Tuple2<Long, StatusCls>> {
    public static final int COLUMN_KEY = 0;
    public static final int COLUMN_NAME = 1;
    public static final int COLUMN_ADDRESS = 2;
    public static final int COLUMN_UNIQUE = 3;
    public static final int COLUMN_CONFIRMED = 4;
    public static final int COLUMN_FAVORITE = 5;

    public WalletItemStatusesTableModel() {
        super(Controller.getInstance().wallet.database.getStatusMap(),
                new String[]{"Key", "Name", "Creator", "Unique", "Confirmed", "Favorite"},
                new Boolean[]{false, true, true, false, false}, true);
    }

    @Override
    public Object getValueAt(int row, int column) {
        if (this.listSorted == null || row > this.listSorted.size() - 1) {
            return null;
        }

        Pair<Tuple2<Long , Long>, Tuple2<Long, StatusCls>> pair = this.listSorted.get(row);
        if (pair == null) {
            return null;
        }

        StatusCls status = pair.getB().b;

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
