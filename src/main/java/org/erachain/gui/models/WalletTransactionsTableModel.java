package org.erachain.gui.models;

import org.erachain.controller.Controller;
import org.erachain.core.transaction.Itemable;
import org.erachain.core.transaction.Transaction;
import org.erachain.database.wallet.WTransactionMap;
import org.erachain.dbs.IteratorCloseable;
import org.erachain.dbs.IteratorCloseableImpl;
import org.erachain.lang.Lang;
import org.mapdb.Fun.Tuple2;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;

@SuppressWarnings("serial")
public class WalletTransactionsTableModel extends WalletTableModel<Tuple2<Tuple2<Long, Integer>, Transaction>> {

    public static final int COLUMN_NUMBER = 0;
    public static final int COLUMN_TIMESTAMP = 1;
    public static final int COLUMN_TYPE = 2;
    public static final int COLUMN_CREATOR = 3;
    public static final int COLUMN_ITEM = 4;
    public static final int COLUMN_AMOUNT = 5;
    public static final int COLUMN_RECIPIENT = 6;
    public static final int COLUMN_FEE = 7;
    public static final int COLUMN_SIZE = 8;
    public static final int COLUMN_FAVORITE = 9;

    private boolean onlyUnread = false;

    /**
     * В динамическом режиме перерисовывается автоматически по событию GUI_REPAINT
     * - перерисовка страницы целой, поэтому не так тормозит основные процессы.<br>
     * Без динамического режима перерисовывается только принудительно - по нажатию кнопки тут
     * org.erachain.gui.items.records.MyTransactionsSplitPanel#setIntervalPanel
     */
    public WalletTransactionsTableModel() {
        super(Controller.getInstance().getWallet().dwSet.getTransactionMap(),
                new String[]{"№", "Timestamp", "Type", "Creator", "Item", "Amount", "Recipient", "Fee", "Size", "Favorite"},
                new Boolean[]{true, true, true, true, true, true, true, false, false, true, true},
                true, COLUMN_FAVORITE);

    }

    @Override
    protected void updateMap() {
        map = Controller.getInstance().getWallet().dwSet.getTransactionMap();
        getInterval();

    }

    @Override
    public Object getValueAt(int row, int column) {

        if (this.list == null || row > this.list.size() - 1) {
            return null;
        }

        Tuple2<Tuple2<Long, Integer>, Transaction> rowItem = this.list.get(row);
        Transaction transaction = rowItem.b;

        switch (column) {
            case COLUMN_IS_OUTCOME:
                if (transaction.getCreator() != null)
                    return transaction.getCreator().hashCode() == rowItem.a.b;
                return false;

            case COLUMN_UN_VIEWED:
                return ((WTransactionMap) map).isUnViewed(transaction);

            case COLUMN_CONFIRMATIONS:
                return transaction.getConfirmations(dcSet);

            case COLUMN_NUMBER:
                return transaction.viewHeightSeq();

            case COLUMN_TIMESTAMP:
                return transaction.viewTimestamp();//.viewTimestamp(); // + " " + transaction.getTimestamp() / 1000;

            case COLUMN_TYPE:
                return Lang.T(transaction.viewFullTypeName());

            case COLUMN_CREATOR:
                return transaction.viewCreator();

            case COLUMN_ITEM:

                Long itemKey;
                if (transaction instanceof Itemable) {
                    return ((Itemable) transaction).getItem();
                } else {
                    itemKey = transaction.getAbsKey();
                }

                if (itemKey == null)
                    return null;

                return dcSet.getItemAssetMap().get(itemKey);

            case COLUMN_AMOUNT:
                BigDecimal amount;
                amount = transaction.getAmount();

                return amount;

            case COLUMN_RECIPIENT:

                try {
                    return transaction.viewRecipient();
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    //e.printStackTrace();
                    return "";
                }

            case COLUMN_FEE:
                return transaction.getFee();

            case COLUMN_SIZE:
                return transaction.viewSize(Transaction.FOR_NETWORK);

            case COLUMN_FAVORITE:
                return Controller.getInstance().isTransactionFavorite(transaction);
        }

        return null;

    }

    @Override
    protected void repaintConfirms() {
        for (int i = 0; i < list.size(); i++) {
            setValueAt(i, COLUMN_CONFIRMATIONS, list.get(i).b.getConfirmations(dcSet));
        }
        fireTableDataChanged();
    }

    public void setOnlyUndead() {
        onlyUnread = !onlyUnread;
        getInterval();
        fireTableDataChanged();
    }

    public void clearAllOnlyUndead() {
        ((WTransactionMap) map).clearUnViewed();
        fireTableDataChanged();
    }

    @Override
    public void getInterval() {

        Object key;
        int count = 0;
        list = new ArrayList<>();
        if (onlyUnread) {
            try (IteratorCloseable iterator = IteratorCloseableImpl.make(((WTransactionMap) map).getUndeadIterator(false))) {
                while (iterator.hasNext() && count++ < step) {
                    key = iterator.next();
                    Transaction item = (Transaction) map.get(key);
                    if (item == null)
                        continue;
                    list.add(new Tuple2<>((Tuple2<Long, Integer>) key, item));
                }
            } catch (IOException e) {
            }

        } else {
            if (startKey == null) {
                try (IteratorCloseable iterator = map.getDescendingIterator()) {
                    while (iterator.hasNext() && count++ < step) {
                        key = iterator.next();
                        Transaction item = (Transaction) map.get(key);
                        if (item == null)
                            continue;
                        list.add(new Tuple2<>((Tuple2<Long, Integer>) key, item));
                    }
                } catch (IOException e) {
                }
            } else {
                try (IteratorCloseable iterator = map.getDescendingIterator()) {
                    while (iterator.hasNext() && count++ < step) {
                        key = iterator.next();
                        Transaction item = (Transaction) map.get(key);
                        if (item == null)
                            continue;
                        list.add(new Tuple2<>((Tuple2<Long, Integer>) key, item));
                    }
                } catch (IOException e) {
                }
            }
        }

        for (Tuple2<Tuple2<Long, Integer>, Transaction> item : list) {
            try {
                item.b.setDC(dcSet, false);
                // тут может выскочить ошибка если кошелек не с той цепочки и тут нет активов
                item.b.calcFee(true);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }

    }
}
