package org.erachain.gui.items.records;

import org.erachain.controller.Controller;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.transaction.Transaction;
import org.erachain.datachain.DCSet;
import org.erachain.gui.items.FavoriteItemModelTable;
import org.erachain.lang.Lang;
import org.erachain.utils.ObserverMessage;
import org.slf4j.LoggerFactory;

@SuppressWarnings("serial")
public class FavoriteTransactionTableModel extends FavoriteItemModelTable {
    public static final int COLUMN_TIMESTAMP = 0;
    public static final int COLUMN_TYPE = 1;
    public static final int COLUMN_CREATOR = 2;
    public static final int COLUMN_TITLE = 3;
    public static final int COLUMN_FAVORITE = 4;
    private static final long serialVersionUID = 1L;

    public FavoriteTransactionTableModel() {
        super(DCSet.getInstance().getTransactionFinalMap(),
                Controller.getInstance().wallet.database.getTransactionFavoritesSet(),
                new String[]{"Timestamp", "Type", "Creator", "Statement", "Favorite"},
                new Boolean[]{true, true, true, true, false},
                ObserverMessage.RESET_ASSET_FAVORITES_TYPE,
                ObserverMessage.ADD_ASSET_FAVORITES_TYPE,
                ObserverMessage.DELETE_ASSET_FAVORITES_TYPE,
                ObserverMessage.LIST_ASSET_FAVORITES_TYPE,
                COLUMN_FAVORITE);

        logger = LoggerFactory.getLogger(this.getClass());

    }

    @Override
    public Object getValueAt(int row, int column) {
        if (this.list == null || this.list.size() - 1 < row) {
            return null;
        }

        Transaction transaction = (Transaction)this.list.get(row);

        switch (column) {
            case COLUMN_TIMESTAMP:
                return transaction.viewTimestamp();
            case COLUMN_TYPE:
                return transaction.viewFullTypeName();
            case COLUMN_CREATOR:
                return transaction.getCreator().getPersonAsString();
            case COLUMN_TITLE:
                return transaction.getTitle();
            case COLUMN_FAVORITE:
                return Controller.getInstance().isTransactionFavorite(transaction);
        }

        return null;
    }

}
