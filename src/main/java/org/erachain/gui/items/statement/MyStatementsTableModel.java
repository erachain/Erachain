package org.erachain.gui.items.statement;

import org.erachain.controller.Controller;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.transaction.RSignNote;
import org.erachain.core.transaction.Transaction;
import org.erachain.core.wallet.Wallet;
import org.erachain.database.wallet.WTransactionMap;
import org.erachain.datachain.DCSet;
import org.erachain.gui.models.WalletTableModel;
import org.mapdb.Fun.Tuple2;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Observer;

public class MyStatementsTableModel extends WalletTableModel<Tuple2<Tuple2<Long, Integer>, Transaction>> implements Observer {

    public static final int COLUMN_SEQNO = 0;
    public static final int COLUMN_TIMESTAMP = 1;
    public static final int COLUMN_CREATOR = 2;
    public static final int COLUMN_TITLE = 3;
    public static final int COLUMN_TEMPLATE = 4;
    public static final int COLUMN_FAVORITE = 5;
    /**
     *
     */
    DCSet dcSet;
    Wallet wallet = Controller.getInstance().getWallet();

    public MyStatementsTableModel() {
        super(Controller.getInstance().getWallet().dwSet.getTransactionMap(),
                new String[]{"№", "Timestamp", "Creator", "Title", "Template", "Favorite"},
                new Boolean[]{false, true, true, true, false, false}, true, COLUMN_FAVORITE);

        dcSet = DCSet.getInstance();

    }

    @Override
    protected void updateMap() {
        map = Controller.getInstance().getWallet().dwSet.getTransactionMap();
    }

    @Override
    public Object getValueAt(int row, int column) {
        // TODO Auto-generated method stub
        try {
            if (this.list == null || this.list.isEmpty()) {
                return null;
            }

            Tuple2<Tuple2<Long, Integer>, Transaction> rowItem = list.get(row);
            RSignNote rNote = (RSignNote) rowItem.b;
            if (rNote == null)
                return null;

            rNote.parseDataFull();

            PublicKeyAccount creator;
            switch (column) {
                case COLUMN_IS_OUTCOME:
                    if (rNote.getCreator() != null)
                        return rNote.getCreator().hashCode() == rowItem.a.b;
                    return false;

                case COLUMN_UN_VIEWED:
                    return ((WTransactionMap) map).isUnViewed(rNote);

                case COLUMN_CONFIRMATIONS:
                    return rNote.getConfirmations(dcSet);

                case COLUMN_SEQNO:
                    return rNote.viewHeightSeq();

                case COLUMN_TIMESTAMP:

                    return rNote.viewTimestamp();

                case COLUMN_TEMPLATE:

                    return rNote.getItem();

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

        list = new ArrayList<>();

        Iterator<Tuple2<Long, Integer>> iterator = ((WTransactionMap) map).getTypeIterator(
                (byte) Transaction.SIGN_NOTE_TRANSACTION, true);
        if (iterator == null) {
            return;
        }

        RSignNote rNote;
        Tuple2<Long, Integer> key;
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
            list.add(new Tuple2<>(key, rNote));
        }
    }

}
