package org.erachain.gui.models;

import org.erachain.controller.Controller;
import org.erachain.core.BlockChain;
import org.erachain.core.account.Account;
import org.erachain.core.crypto.Base58;
import org.erachain.core.transaction.RCalculated;
import org.erachain.core.transaction.Transaction;
import org.erachain.database.FilteredByStringArray;
import org.erachain.datachain.DCSet;
import org.erachain.datachain.TransactionFinalMap;
import org.erachain.dbs.DBTabImpl;
import org.erachain.dbs.IteratorCloseable;
import org.erachain.lang.Lang;
import org.mapdb.Fun;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.table.AbstractTableModel;
import javax.validation.constraints.Null;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial")
public abstract class SearchTableModelCls extends AbstractTableModel {

    protected final Controller cnt = Controller.getInstance();
    protected final DCSet dcSet = DCSet.getInstance();

    Integer blockNo;

    private String[] columnNames;
    protected boolean needUpdate;
    protected boolean descending;

    protected String findMessage;

    protected List<Transaction> list;

    protected Boolean[] columnAutoHeight;

    protected int start = 0;
    protected int step = 500;
    protected long size = 0;

    protected DBTabImpl map;
    protected Logger logger;

    int onlyType;

    public int COLUMN_FAVORITE;

    public SearchTableModelCls(DBTabImpl map, int onlyType, String[] columnNames, Boolean[] columnAutoHeight,
                               int columnFavorite, boolean descending) {
        logger = LoggerFactory.getLogger(this.getClass());
        this.map = map;
        this.onlyType = onlyType;
        this.columnNames = columnNames;
        this.columnAutoHeight = columnAutoHeight;
        this.descending = descending;
        COLUMN_FAVORITE = columnFavorite;

    }

    public void setBlockNumber(String string) {

        clear();

        try {
            blockNo = Integer.parseInt(string);
        } catch (NumberFormatException e) {
            Transaction transaction = ((TransactionFinalMap) map).getRecord(string);
            if (transaction != null && (onlyType == 0 || onlyType == transaction.getType())) {
                transaction.setDC(DCSet.getInstance(), false);
                list.add(transaction);
            }
            this.fireTableDataChanged();
            return;
        }

        List<Transaction> result = (List<Transaction>) ((TransactionFinalMap) map).getTransactionsByBlock(blockNo);

        for (Transaction transaction : result) {
            if (onlyType > 0 && onlyType != transaction.getType()) {
                continue;
            }
            transaction.setDC(dcSet, false);
            list.add(transaction);
        }

        this.fireTableDataChanged();

    }

    public void find(String filter, Long fromID) {

        clear();

        if (filter == null || (filter = filter.trim()).isEmpty()) {
            try (IteratorCloseable<Long> iterator = ((TransactionFinalMap) map).getIndexIterator(0, true)) {
                int limit = 200;
                int countForge = 0;
                while (iterator.hasNext() && limit > 0) {
                    Transaction transaction = ((TransactionFinalMap) map).get(iterator.next());
                    if (onlyType > 0 && onlyType != transaction.getType())
                        continue;

                    if (transaction.getType() == Transaction.CALCULATED_TRANSACTION) {
                        RCalculated tx = (RCalculated) transaction;
                        String mess = tx.getMessage();
                        if (mess != null && mess.equals(BlockChain.MESS_FORGING)) {
                            if (++countForge < 100)
                                continue;
                            else
                                countForge = 0;
                        }
                    }

                    --limit;
                    transaction.setDC(dcSet, false);
                    list.add(transaction);
                }
            } catch (IOException e) {
            }
            return;
        }

        Fun.Tuple2<Account, String> accountResult = Account.tryMakeAccount(filter);
        Account account = accountResult.a;

        if (account != null) {
            // ИЩЕМ по СЧЕТУ
            List<Transaction> result = ((TransactionFinalMap) map).getTransactionsByAddressLimit(account.getShortAddressBytes(), null, null, fromID, 0, 1000, true, descending);
            for (Transaction transaction : result) {
                if (onlyType == 0 || onlyType == transaction.getType()) {
                    transaction.setDC(dcSet, false);
                    list.add(transaction);
                }
            }

        }

        if (!Base58.isExtraSymbols(filter)) {
            byte[] signature = Base58.decode(filter);
            Transaction transaction = dcSet.getTransactionFinalMap().get(signature);
            if (transaction != null && (onlyType == 0 || onlyType == transaction.getType())) {
                transaction.setDC(dcSet, false);
                list.add(transaction);
            }
        }


        String fromWord = null;
        if (false) {
            // TODO сделать поиск по Transaction.searchTransactions
            Fun.Tuple3<Long, Long, List<Transaction>> result = Transaction.searchTransactions(dcSet, filter, false, 1000, fromID, start, true);
        } else {
            List<Transaction> result = ((FilteredByStringArray) dcSet.getTransactionFinalMap())
                    .getByFilterAsArray(filter, fromID, start, step, descending);
            for (Transaction transaction : result) {
                if (onlyType == 0 || onlyType == transaction.getType()) {
                    transaction.setDC(dcSet, false);
                    list.add(transaction);
                }
            }
        }

        this.fireTableDataChanged();

    }

    public Boolean[] getColumnAutoHeight() {
        return this.columnAutoHeight;
    }

    // устанавливаем колонки которым изменить высоту
    public void setColumnAutoHeight(Boolean[] arg0) {
        this.columnAutoHeight = arg0;
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    //     private String[] columnNames = Lang.T();
    public String getColumnName(int index) {
        return Lang.T(columnNames[index]);
    }

    public String getColumnNameOrigin(int index) {
        return columnNames[index];
    }

    public Transaction getItem(int row) {
        if (list == null)
            return null;

        return this.list.get(row);
    }

    public int getRowCount() {
        return (this.list == null) ? 0 : this.list.size();
    }

    public Class<? extends Object> getColumnClass(int c) {
        Object o = getValueAt(0, c);
        return o == null ? Null.class : o.getClass();
    }

    //public abstract void getIntervalThis(int startBack, int endBack);
    public void getIntervalThis(long start, long end) {
    }

    public long getMapSize() {
        if (map == null)
            return 0;

        return map.size();
    }

    /**
     * если descending установлен, то ключ отрицательный значит и его вычисляем обратно.
     * То есть 10-я запись имеет ключ -9 (отричательный). Тогда отсчет start=0 будет идти от последней записи
     * с отступом step
     */
    public void getInterval() {

        if (descending) {
            long startBack = -getMapSize() + start;
            getIntervalThis(startBack, startBack + step);
        } else {
            getIntervalThis(start, start + step);
        }

    }

    public void setInterval(int start, int step) {
        this.start = start;
        this.step = step;

        getInterval();
    }

    public void clear() {
        findMessage = null;
        list = new ArrayList<>();
        fireTableDataChanged();
    }

}
