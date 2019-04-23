package org.erachain.gui.models;
////////

import org.erachain.controller.Controller;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.item.imprints.Imprint;
import org.erachain.core.item.imprints.ImprintCls;
import org.erachain.database.SortableList;
import org.erachain.datachain.DCSet;
import org.erachain.utils.ObserverMessage;
import org.erachain.utils.Pair;
import org.mapdb.Fun.Tuple2;

import java.util.Observable;
import java.util.Observer;

@SuppressWarnings("serial")
public class WalletItemImprintsTableModel extends WalletAutoKeyTableModel<Tuple2<Long, Long>, Tuple2<Long, ImprintCls>> {
    public static final int COLUMN_KEY = 0;
    public static final int COLUMN_NAME = 1;
    public static final int COLUMN_ADDRESS = 2;
    public static final int COLUMN_CONFIRMED = 3;
    public static final int COLUMN_FAVORITE = 4;

    public WalletItemImprintsTableModel() {
        super(Controller.getInstance().wallet.database.getImprintMap(),
                new String[]{"Key", "Name", "Owner", "Confirmed", "Favorite"},
                new Boolean[]{false, true, true, false}, true);
    }

    @Override
    public Object getValueAt(int row, int column) {
        if (this.listSorted == null || row > this.listSorted.size() - 1) {
            return null;
        }

        Pair<Tuple2<Long , Long>, Tuple2<Long, ImprintCls>> pair = this.listSorted.get(row);
        if (pair == null) {
            return null;
        }

        ImprintCls imprint = pair.getB().b;

        switch (column) {
            case COLUMN_KEY:

                return imprint.getKey(DCSet.getInstance());

            case COLUMN_NAME:

                return imprint.viewName();

            case COLUMN_ADDRESS:

                return imprint.getOwner().getPersonAsString();

            case COLUMN_CONFIRMED:

                return imprint.isConfirmed();
            case COLUMN_FAVORITE:

                return imprint.isFavorite();
        }

        return null;
    }

}
