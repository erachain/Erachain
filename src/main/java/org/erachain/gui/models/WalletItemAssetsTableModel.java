package org.erachain.gui.models;

import org.erachain.controller.Controller;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.item.persons.PersonCls;
import org.erachain.database.SortableList;
import org.erachain.datachain.DCSet;
import org.erachain.lang.Lang;
import org.erachain.utils.ObserverMessage;
import org.erachain.utils.Pair;
import org.mapdb.Fun.Tuple2;

import java.util.Observable;
import java.util.Observer;

@SuppressWarnings("serial")
public class WalletItemAssetsTableModel extends WalletAutoKeyTableModel<Tuple2<Long, Long>, Tuple2<Long, AssetCls>> {
    public static final int COLUMN_KEY = 0;
    public static final int COLUMN_NAME = 1;
    public static final int COLUMN_ADDRESS = 2;
    public static final int COLUMN_ASSET_TYPE = 3;
    public static final int COLUMN_AMOUNT = 4;
    public static final int COLUMN_CONFIRMED = 5;
    public static final int COLUMN_FAVORITE = 6;

    public WalletItemAssetsTableModel() {
        super(Controller.getInstance().wallet.database.getAssetMap(),
                new String[]{"Key", "Name", "Owner", "Type", "Quantity", "Confirmed", "Favorite"},
                new Boolean[]{false, true, true, false, false, false, false, false}, true);

    }

    @Override
    public Object getValueAt(int row, int column) {
        if (this.listSorted == null || row > this.listSorted.size() - 1) {
            return null;
        }

        Pair<Tuple2<Long , Long>, Tuple2<Long, AssetCls>> pair = this.listSorted.get(row);
        if (pair == null) {
            return null;
        }
        AssetCls asset = pair.getB().b;

        switch (column) {
            case COLUMN_KEY:

                return asset.getKey(DCSet.getInstance());

            case COLUMN_NAME:

                return asset.viewName();

            case COLUMN_ADDRESS:

                return asset.getOwner().getPersonAsString();

            case COLUMN_ASSET_TYPE:

                return Lang.getInstance().translate(asset.viewAssetType());

            case COLUMN_AMOUNT:

                return asset.getTotalQuantity(DCSet.getInstance());

            case COLUMN_CONFIRMED:

                return asset.isConfirmed();

            case COLUMN_FAVORITE:

                return asset.isFavorite();
        }

        return null;
    }

}
