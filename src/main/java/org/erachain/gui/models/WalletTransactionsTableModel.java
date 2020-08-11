package org.erachain.gui.models;

import org.erachain.controller.Controller;
import org.erachain.core.item.ItemCls;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.transaction.*;
import org.erachain.database.wallet.WTransactionMap;
import org.erachain.datachain.DCSet;
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
        super(Controller.getInstance().getWallet().database.getTransactionMap(),
                new String[]{"SeqNo", "Timestamp", "Type", "Creator", "Item", "Amount", "Recipient", "Fee", "Size", "Favorite"},
                new Boolean[]{true, true, true, true, true, true, true, false, false, true, true},
                true, COLUMN_FAVORITE);

    }

    @Override
    public Object getValueAt(int row, int column) {

        if (this.list == null || row > this.list.size() - 1) {
            return null;
        }

        Tuple2<Tuple2<Long, Integer>, Transaction> rowItem = this.list.get(row);
        Transaction transaction = rowItem.b;

        ItemCls item = null;
        if (transaction instanceof TransactionAmount && transaction.getAbsKey() > 0) {
            TransactionAmount transAmo = (TransactionAmount) transaction;
            item = DCSet.getInstance().getItemAssetMap().get(transAmo.getAbsKey());
        } else if (transaction instanceof GenesisTransferAssetTransaction) {
            GenesisTransferAssetTransaction transGen = (GenesisTransferAssetTransaction) transaction;
            item = DCSet.getInstance().getItemAssetMap().get(transGen.getAbsKey());
        } else if (transaction instanceof IssueItemRecord) {
            IssueItemRecord transIssue = (IssueItemRecord) transaction;
            item = transIssue.getItem();
        } else if (transaction instanceof GenesisIssueItemRecord) {
            GenesisIssueItemRecord transIssue = (GenesisIssueItemRecord) transaction;
            item = transIssue.getItem();
        } else if (transaction instanceof RSertifyPubKeys) {
            RSertifyPubKeys sertifyPK = (RSertifyPubKeys) transaction;
            item = DCSet.getInstance().getItemPersonMap().get(sertifyPK.getAbsKey());
        } else {

        }
        switch (column) {
            case COLUMN_IS_OUTCOME:
                if (transaction.getCreator() != null)
                    return transaction.getCreator().hashCode() == rowItem.a.b;
                return false;

            case COLUMN_UN_VIEWED:
                return ((WTransactionMap) map).isUnViewed(transaction);

            case COLUMN_CONFIRMATIONS:
                return transaction.getConfirmations(DCSet.getInstance());

            case COLUMN_TIMESTAMP:
                return transaction.viewTimestamp();//.viewTimestamp(); // + " " + transaction.getTimestamp() / 1000;

            case COLUMN_TYPE:
                return Lang.getInstance().translate(transaction.viewFullTypeName());

            case COLUMN_CREATOR:
                return transaction.viewCreator();

            case COLUMN_ITEM:
                return item;

            case COLUMN_AMOUNT:
                BigDecimal amount = transaction.getAmount();
                if (amount != null && item != null && item instanceof AssetCls) {
                    amount = amount.setScale(((AssetCls) item).getScale());
                }

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

            case COLUMN_NUMBER:
                return transaction.viewHeightSeq();

            case COLUMN_FAVORITE:
                return Controller.getInstance().isTransactionFavorite(transaction);
        }

        return null;

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

        DCSet dcSet = DCSet.getInstance();
        for (Tuple2<Tuple2<Long, Integer>, Transaction> item : list) {

            item.b.setDC(dcSet, false);
            item.b.calcFee();
        }

    }
}
