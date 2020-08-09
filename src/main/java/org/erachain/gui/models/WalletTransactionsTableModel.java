package org.erachain.gui.models;

import org.erachain.controller.Controller;
import org.erachain.core.item.ItemCls;
import org.erachain.core.transaction.*;
import org.erachain.database.wallet.WTransactionMap;
import org.erachain.datachain.DCSet;
import org.erachain.dbs.IteratorCloseable;
import org.erachain.lang.Lang;
import org.mapdb.Fun.Tuple2;

import java.io.IOException;
import java.util.ArrayList;

@SuppressWarnings("serial")
public class WalletTransactionsTableModel extends WalletTableModel<Tuple2<Tuple2<Long, Integer>, Transaction>> {

    public static final int COLUMN_IS_OUTCOME = -2;
    public static final int COLUMN_UN_VIEWED = -1;
    public static final int COLUMN_CONFIRMATIONS = 0;
    public static final int COLUMN_TIMESTAMP = 1;
    public static final int COLUMN_TYPE = 2;
    public static final int COLUMN_CREATOR = 3;
    public static final int COLUMN_ITEM = 4;
    public static final int COLUMN_AMOUNT = 5;
    public static final int COLUMN_RECIPIENT = 6;
    public static final int COLUMN_FEE = 7;
    public static final int COLUMN_SIZE = 8;
    public static final int COLUMN_NUMBER = 9;
    public static final int COLUMN_FAVORITE = 10;


    /**
     * В динамическом режиме перерисовывается автоматически по событию GUI_REPAINT
     * - перерисовка страницы целой, поэтому не так тормозит основные процессы.<br>
     * Без динамического режима перерисовывается только принудительно - по нажатию кнопки тут
     * org.erachain.gui.items.records.MyTransactionsSplitPanel#setIntervalPanel
     */
    public WalletTransactionsTableModel() {
        super(Controller.getInstance().getWallet().database.getTransactionMap(),
                new String[]{"Confirmations", "Timestamp", "Type", "Creator", "Item", "Amount", "Recipient", "Fee", "Size", "SeqNo", "Favorite"},
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
                return transaction.getAmount();

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

    @Override
    public void getInterval() {

        Object key;
        int count = 0;
        list = new ArrayList<>();
        if (startKey == null) {
            try (IteratorCloseable iterator = map.getDescendingIterator()) {
                while (iterator.hasNext() && count++ < step) {
                    key = iterator.next();
                    list.add(new Tuple2<>((Tuple2<Long, Integer>) key, (Transaction) map.get(key)));
                }
            } catch (IOException e) {
            }
        } else {
            try (IteratorCloseable iterator = map.getDescendingIterator()) {
                while (iterator.hasNext() && count++ < step) {
                    key = iterator.next();
                    list.add(new Tuple2<>((Tuple2<Long, Integer>) key, (Transaction) map.get(key)));
                }
            } catch (IOException e) {
            }
        }

        DCSet dcSet = DCSet.getInstance();
        for (Tuple2<Tuple2<Long, Integer>, Transaction> item : list) {

            item.b.setDC(dcSet, false);
            item.b.calcFee();
        }

    }
}
