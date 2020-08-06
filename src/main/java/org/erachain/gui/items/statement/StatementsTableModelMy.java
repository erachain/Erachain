package org.erachain.gui.items.statement;

import org.erachain.controller.Controller;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.item.ItemCls;
import org.erachain.core.transaction.RSignNote;
import org.erachain.core.transaction.Transaction;
import org.erachain.core.wallet.Wallet;
import org.erachain.database.wallet.WTransactionMap;
import org.erachain.datachain.DCSet;
import org.erachain.gui.models.WalletTableModel;
import org.mapdb.Fun;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Observer;

public class StatementsTableModelMy extends WalletTableModel<Transaction> implements Observer {

    public static final int COLUMN_TIMESTAMP = 0;
    public static final int COLUMN_CREATOR = 1;
    public static final int COLUMN_TITLE = 2;
    public static final int COLUMN_TEMPLATE = 3;
    public static final int COLUMN_FAVORITE = 4;
    /**
     *
     */
    DCSet dcSet;
    Wallet wallet = Controller.getInstance().wallet;

    public StatementsTableModelMy() {
        super(Controller.getInstance().getWallet().database.getTransactionMap(),
                new String[]{"Timestamp", "Creator", "Title", "Template", "Favorite"},
                new Boolean[]{true, true, true, false, false}, true, COLUMN_FAVORITE);

        dcSet = DCSet.getInstance();

    }

    @Override
    public Object getValueAt(int row, int column) {
        // TODO Auto-generated method stub
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

    @Override
    public void getInterval() {

        list = new ArrayList<Transaction>();

        Wallet wallet = Controller.getInstance().wallet;
        Iterator<Fun.Tuple2<Long, Integer>> iterator = ((WTransactionMap) map).getTypeIterator(
                (byte) Transaction.SIGN_NOTE_TRANSACTION, true);
        if (iterator == null) {
            return;
        }

        RSignNote rNote;
        Fun.Tuple2<Long, Integer> key;
        while (iterator.hasNext()) {
            key = iterator.next();
            try {
                rNote = (RSignNote) wallet.getTransaction(key);
            } catch (Exception e) {
                continue;
            }

            if (rNote == null)
                continue;

            rNote.setDC(dcSet, false);
            list.add(rNote);
        }
    }

}
