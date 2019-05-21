package org.erachain.gui.models;

import org.erachain.core.account.Account;
import org.erachain.core.crypto.Base58;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.transaction.Transaction;
import org.erachain.datachain.DCSet;
import org.erachain.datachain.TransactionFinalMap;
import org.erachain.lang.Lang;
import org.erachain.utils.DateTimeFormat;
import org.erachain.utils.Pair;
import org.mapdb.Fun;
import org.mapdb.Fun.Tuple2;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
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
    public static final int COLUMN_FAVORITE = 6;
    public static final int COLUMN_AMOUNT = 7;

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
        this.fireTableDataChanged();

    }

    public void find(String filter) {

        clear();

        if (filter == null || filter.isEmpty()) return;

        Tuple2<Account, String> accountResult = Account.tryMakeAccount(filter);
        Account account = accountResult.a;

        if (account != null) {
            // ИЩЕМ по СЧЕТУ
            list = ((TransactionFinalMap)map).getTransactionsByAddressLimit(account.getAddress(), 1000);
        } else {

            try {

                // ИЩЕМ по ПОДПИСИ
                byte[] signatute = Base58.decode(filter);
                if (signatute.length > 40) {
                    Long key = DCSet.getInstance().getTransactionFinalMapSigns().get(signatute);
                    list.add(DCSet.getInstance().getTransactionFinalMap().get(key));
                }
            } catch (NumberFormatException e) {
            }

            if (list.isEmpty()) {
                // ИЩЕМ по Заголовку
                DCSet dcSet = DCSet.getInstance();

                Pair<String, Iterable> result = dcSet.getTransactionFinalMap().getKeysByFilterAsArray(filter, start, step);

                if (result.getA() != null) {
                    findMessage = result.getA();
                    return;
                } else {
                    findMessage = "";
                }

                Iterator iterator = result.getB().iterator();

                Transaction item;
                Long key;

                list = new ArrayList<>();

                while (iterator.hasNext()) {
                    key = (Long) iterator.next();
                    item = (Transaction) map.get(key);
                    list.add(item);
                }
            }
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
