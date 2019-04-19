package org.erachain.gui.models;
////////

import org.erachain.controller.Controller;
import org.erachain.core.item.statuses.StatusCls;
import org.erachain.core.item.unions.UnionCls;
import org.erachain.database.SortableList;
import org.erachain.datachain.DCSet;
import org.erachain.utils.ObserverMessage;
import org.erachain.utils.Pair;
import org.mapdb.Fun.Tuple2;

import java.util.Observable;
import java.util.Observer;

@SuppressWarnings("serial")
public class WalletItemUnionsTableModel extends WalletAutoKeyTableModel<Tuple2<Long, Long>, Tuple2<Long, UnionCls>> {
    public static final int COLUMN_KEY = 0;
    public static final int COLUMN_NAME = 1;
    public static final int COLUMN_ADDRESS = 2;
    public static final int COLUMN_CONFIRMED = 3;
    public static final int COLUMN_FAVORITE = 4;

    public WalletItemUnionsTableModel() {
        super(Controller.getInstance().wallet.database.getUnionMap(),
                new String[]{"Key", "Name", "Creator", "Confirmed", "Favorite"},
                new Boolean[]{false, true, true, false, false}, true);

    }

    @Override
    public Object getValueAt(int row, int column) {
        if (this.listSorted == null || row > this.listSorted.size() - 1) {
            return null;
        }

        Pair<Tuple2<Long , Long>, Tuple2<Long, UnionCls>> pair = this.listSorted.get(row);
        if (pair == null) {
            return null;
        }

        UnionCls union = pair.getB().b;

        switch (column) {
            case COLUMN_KEY:

                return union.getKey(DCSet.getInstance());

            case COLUMN_NAME:

                return union.viewName();

            case COLUMN_ADDRESS:

                return union.getOwner().getPersonAsString();

            case COLUMN_CONFIRMED:

                return union.isConfirmed();

            case COLUMN_FAVORITE:

                return union.isFavorite();

        }

        return null;
    }

}
