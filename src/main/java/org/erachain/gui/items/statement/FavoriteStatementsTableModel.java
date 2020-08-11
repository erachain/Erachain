package org.erachain.gui.items.statement;

import org.erachain.controller.Controller;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.item.ItemCls;
import org.erachain.core.transaction.RSignNote;
import org.erachain.core.wallet.Wallet;
import org.erachain.datachain.DCSet;
import org.erachain.gui.items.FavoriteItemModelTable;
import org.erachain.utils.ObserverMessage;

public class FavoriteStatementsTableModel extends FavoriteItemModelTable {
    public static final int COLUMN_TIMESTAMP = 0;
    public static final int COLUMN_CREATOR = 1;
    public static final int COLUMN_TITLE = 2;
    public static final int COLUMN_TEMPLATE = 3;
    public static final int COLUMN_FAVORITE = 4;

    Wallet wallet = Controller.getInstance().wallet;

    public FavoriteStatementsTableModel() {
        super(DCSet.getInstance().getTransactionFinalMap(),
                Controller.getInstance().wallet.database.getDocumentFavoritesSet(),
                new String[]{"Timestamp", "Creator", "Title", "Template", "Favorite"},
                new Boolean[]{true, true, true, false, false},
                ObserverMessage.RESET_STATEMENT_FAVORITES_TYPE,
                ObserverMessage.ADD_STATEMENT_FAVORITES_TYPE,
                ObserverMessage.DELETE_STATEMENT_FAVORITES_TYPE,
                ObserverMessage.LIST_STATEMENT_FAVORITES_TYPE,
                COLUMN_FAVORITE);

    }

    @Override
    public Object getValueAt(int row, int column) {
        try {
            if (this.list == null || this.list.isEmpty()) {
                return null;
            }
            RSignNote rNote = (RSignNote) list.get(row);
            if (rNote == null)
                return null;

            rNote.parseData();

            PublicKeyAccount creator;
            switch (column) {
                case COLUMN_IS_OUTCOME:
                    if (rNote.getCreator() != null)
                        return wallet.accountExists(rNote.getCreator());
                    return false;

                case COLUMN_UN_VIEWED:
                    return false;

                case COLUMN_TIMESTAMP:

                    return rNote.viewTimestamp();

                case COLUMN_TEMPLATE:

                    ItemCls item = rNote.getItem();
                    return item == null ? null : item.toString();

                case COLUMN_TITLE:

                    return rNote.getTitle();

                case COLUMN_CREATOR:

                    creator = rNote.getCreator();

                    return creator == null ? null : creator.getPersonAsString();

                case COLUMN_FAVORITE:

                    return wallet.isDocumentFavorite(rNote);
            }

            return null;

        } catch (Exception e) {
            //	logger.error(e.getMessage(),e);
            return null;
        }
    }

}
