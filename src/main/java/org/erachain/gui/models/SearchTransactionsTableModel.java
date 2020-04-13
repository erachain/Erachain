package org.erachain.gui.models;

import org.erachain.core.account.Account;
import org.erachain.core.crypto.Base58;
import org.erachain.core.transaction.RCalculated;
import org.erachain.core.transaction.Transaction;
import org.erachain.database.FilteredByStringArray;
import org.erachain.datachain.DCSet;
import org.erachain.datachain.TransactionFinalMap;
import org.erachain.lang.Lang;
import org.erachain.utils.DateTimeFormat;
import org.mapdb.Fun;
import org.mapdb.Fun.Tuple2;

import java.util.List;

@SuppressWarnings("serial")
/**
 * не перерисовыется по событиям - статичная таблица при поиске
 */
public class SearchTransactionsTableModel extends SearchTableModelCls<Transaction> {

    public static final int COLUMN_TIMESTAMP = 0;
    public static final int COLUMN_BLOCK = 1;
    public static final int COLUMN_SEQ_NO = 2;
    public static final int COLUMN_TYPE = 3;
    public static final int COLUMN_TITLE = 4;
    public static final int COLUMN_KEY = 5;
    public static final int COLUMN_AMOUNT = 6;
    public static final int COLUMN_FAVORITE = 7;

    DCSet dcSet = DCSet.getInstance();
    Integer blockNo;

    public SearchTransactionsTableModel() {
        super(DCSet.getInstance().getTransactionFinalMap(),
                new String[]{"Timestamp", "Block", "SeqNo", "Type", "Title", "Key", "Amount", "Favorite"},
                new Boolean[]{true, true, true, true, true, true, true, true},
                false);

    }

    public void setBlockNumber(String string) {

        clear();

        try {
            blockNo = Integer.parseInt(string);
        } catch (NumberFormatException e) {
            Transaction transaction = ((TransactionFinalMap)map).getRecord(string);
            if (transaction != null) {
                transaction.setDC(DCSet.getInstance());
                list.add(transaction);
            }
            this.fireTableDataChanged();
            return;
        }

        list = (List<Transaction>) ((TransactionFinalMap)map).getTransactionsByBlock(blockNo);

        for (Transaction item : list) {
            if (item instanceof RCalculated) {
                list.remove(item);
                continue;
            }
            item.setDC_HeightSeq(dcSet);
        }

        this.fireTableDataChanged();

    }

    public void find(String filter, Long fromID) {

        clear();

        if (filter == null || filter.isEmpty()) return;

        Tuple2<Account, String> accountResult = Account.tryMakeAccount(filter);
        Account account = accountResult.a;

        if (account != null) {
            // ИЩЕМ по СЧЕТУ
            list.addAll(((TransactionFinalMap) map).getTransactionsByAddressLimit(account.getShortAddressBytes(), 1000, true));

        }

        if (!Base58.isExtraSymbols(filter)) {
            byte[] signature = Base58.decode(filter);
            Long key = DCSet.getInstance().getTransactionFinalMapSigns().get(signature);
            list.add(DCSet.getInstance().getTransactionFinalMap().get(key));
        }

        // ИЩЕМ по Заголовку
        DCSet dcSet = DCSet.getInstance();

        String fromWord = null;
        if (false) {
            // TODO сделать поиск по Transaction.searchTransactions
            Fun.Tuple3<Long, Long, List<Transaction>> result = Transaction.searchTransactions(dcSet, filter, false, 10000, fromID, start);
        } else {
            list.addAll(((FilteredByStringArray) dcSet.getTransactionFinalMap())
                    .getKeysByFilterAsArray(filter, fromWord, fromID, start, step, false));
        }

        for (Transaction item : list) {
            if (false && // все берем
                    item instanceof RCalculated) {
                list.remove(item);
                continue;
            }
            item.setDC_HeightSeq(dcSet);
        }

        this.fireTableDataChanged();

    }

    @Override
    public Object getValueAt(int row, int column) {
        try {
            if (this.list == null || this.list.size() - 1 < row) {
                return null;
            }

            Transaction transaction = list.get(row);
            if (transaction == null) {
                return null;
            }


            switch (column) {
                case COLUMN_TIMESTAMP:

                    return DateTimeFormat.timestamptoString(transaction.getTimestamp());
                //return transaction.viewTimestamp() + " " + transaction.getTimestamp() / 1000;

                case COLUMN_TYPE:

                    //return Lang.transactionTypes[transaction.getType()];
                    return Lang.getInstance().translate(transaction.viewTypeName());

                case COLUMN_AMOUNT:

                    return transaction.getAmount();//.getAmount(transaction.getCreator()));

                case COLUMN_BLOCK:

                    return transaction.getBlockHeight();

                case COLUMN_KEY:

                    return transaction.getKey();

                case COLUMN_TITLE:

                    return transaction.getTitle();

                case COLUMN_FAVORITE:

                    return cnt.isTransactionFavorite(transaction);

                case COLUMN_SEQ_NO:
                    return transaction.getSeqNo();

            }


            return null;

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }

}
