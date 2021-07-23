package org.erachain.gui.models;

import org.erachain.core.transaction.Transaction;
import org.erachain.datachain.DCSet;
import org.erachain.lang.Lang;
import org.erachain.utils.DateTimeFormat;

@SuppressWarnings("serial")
/**
 * не перерисовыется по событиям - статичная таблица при поиске
 */
public class SearchTransactionsTableModel extends SearchTableModelCls {

    public static final int COLUMN_SEQNO = 0;
    public static final int COLUMN_TIMESTAMP = 1;
    public static final int COLUMN_TYPE = 2;
    public static final int COLUMN_CREATOR = 3;
    public static final int COLUMN_TITLE = 4;
    public static final int COLUMN_KEY = 5;
    public static final int COLUMN_AMOUNT = 6;
    public static final int COLUMN_FAVORITE = 7;

    public SearchTransactionsTableModel() {
        super(DCSet.getInstance().getTransactionFinalMap(), 0,
                new String[]{"№", "Timestamp", "Type", "Creator", "Title", "Key", "Amount", "Favorite"},
                new Boolean[]{false, true, true, true, true, true, true, true},
                COLUMN_FAVORITE, true);

    }


    @Override
    public Object getValueAt(int row, int column) {
        try {
            if (list == null || list.size() - 1 < row) {
                return null;
            }

            Transaction transaction = list.get(row);
            if (transaction == null) {
                return null;
            }

            switch (column) {

                case COLUMN_SEQNO:
                    return transaction.viewHeightSeq();

                case COLUMN_TIMESTAMP:

                    return DateTimeFormat.timestamptoString(transaction.getTimestamp());
                //return transaction.viewTimestamp() + " " + transaction.getTimestamp() / 1000;

                case COLUMN_TYPE:

                    //return Lang.transactionTypes[transaction.getType()];
                    return Lang.T(transaction.viewFullTypeName());

                case COLUMN_AMOUNT:

                    return transaction.getAmount();//.getAmount(transaction.getCreator()));

                case COLUMN_CREATOR:

                    return transaction.viewCreator();

                case COLUMN_KEY:

                    return transaction.getKey();

                case COLUMN_TITLE:

                    return transaction.getTitle();

                case COLUMN_FAVORITE:

                    return cnt.isTransactionFavorite(transaction);

            }

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return null;

    }

}
