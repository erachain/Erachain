package org.erachain.gui.items.records;

import org.erachain.controller.Controller;
import org.erachain.core.transaction.Transaction;
import org.erachain.core.wallet.Wallet;
import org.erachain.datachain.DCSet;
import org.erachain.gui.items.FavoriteItemModelTable;
import org.erachain.lang.Lang;
import org.erachain.utils.ObserverMessage;
import org.slf4j.LoggerFactory;

@SuppressWarnings("serial")
public class FavoriteTransactionTableModel extends FavoriteItemModelTable {
    public static final int COLUMN_SEQNO = 0;
    public static final int COLUMN_TIMESTAMP = 1;
    public static final int COLUMN_TYPE = 2;
    public static final int COLUMN_CREATOR = 3;
    public static final int COLUMN_TITLE = 4;
    public static final int COLUMN_FAVORITE = 5;
    private static final long serialVersionUID = 1L;

    DCSet dcSet = DCSet.getInstance();
    Wallet wallet = Controller.getInstance().getWallet();

    public FavoriteTransactionTableModel() {
        super(DCSet.getInstance().getTransactionFinalMap(),
                Controller.getInstance().getWallet().dwSet.getTransactionFavoritesSet(),
                new String[]{"№", "Timestamp", "Type", "Creator", "Statement", "Favorite"},
                new Boolean[]{true, true, true, true, true, false},
                ObserverMessage.RESET_TRANSACTION_FAVORITES_TYPE,
                ObserverMessage.ADD_TRANSACTION_FAVORITES_TYPE,
                ObserverMessage.DELETE_TRANSACTION_FAVORITES_TYPE,
                ObserverMessage.LIST_TRANSACTION_FAVORITES_TYPE,
                COLUMN_FAVORITE);

        logger = LoggerFactory.getLogger(this.getClass());

    }

    @Override
    protected void updateMap() {
        favoriteMap = Controller.getInstance().getWallet().dwSet.getTransactionFavoritesSet();
    }

    @Override
    public Object getValueAt(int row, int column) {
        if (this.list == null || this.list.size() - 1 < row) {
            return null;
        }

        Transaction transaction = (Transaction) this.list.get(row);

        switch (column) {
            case COLUMN_IS_OUTCOME:
                if (transaction.getCreator() != null)
                    return wallet.accountExists(transaction.getCreator());
                return false;

            case COLUMN_UN_VIEWED:
                return false;

            case COLUMN_CONFIRMATIONS:
                return transaction.getConfirmations(dcSet);
            case COLUMN_SEQNO:
                return transaction.viewHeightSeq();

            case COLUMN_TIMESTAMP:
                return transaction.viewTimestamp();
            case COLUMN_TYPE:
                return Lang.T(transaction.viewFullTypeName());
            case COLUMN_CREATOR:
                if (transaction.getCreator() != null) {
                    return transaction.getCreator().getPersonAsString();
                } else return "GENESIS";
            case COLUMN_TITLE:
                return transaction.getTitle();
            case COLUMN_FAVORITE:
                return Controller.getInstance().isTransactionFavorite(transaction);
        }

        return null;
    }

}
