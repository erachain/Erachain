package org.erachain.gui.models;

import org.erachain.controller.Controller;
import org.erachain.core.item.ItemCls;
import org.erachain.core.transaction.*;
import org.erachain.datachain.DCSet;
import org.erachain.dbs.IteratorCloseable;
import org.erachain.lang.Lang;
import org.erachain.utils.DateTimeFormat;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;

@SuppressWarnings("serial")
public class WalletTransactionsTableModel extends WalletTableModel<Transaction> {

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
                new Boolean[]{true, true, true, true, true, true, true, false, false, true, true}, true, COLUMN_FAVORITE);

    }

    @Override
    public Object getValueAt(int row, int column) {

        if (this.list == null || row > this.list.size() - 1) {
            return null;
        }

        Transaction transaction = this.list.get(row);

        String itemName = "";
        if (transaction instanceof TransactionAmount && transaction.getAbsKey() > 0) {
            TransactionAmount transAmo = (TransactionAmount) transaction;
            //recipient = transAmo.getRecipient();
            ItemCls item = DCSet.getInstance().getItemAssetMap().get(transAmo.getAbsKey());
            if (item == null)
                return null;

            itemName = item.toString();
        } else if (transaction instanceof GenesisTransferAssetTransaction) {
            GenesisTransferAssetTransaction transGen = (GenesisTransferAssetTransaction) transaction;
            //recipient = transGen.getRecipient();
            ItemCls item = DCSet.getInstance().getItemAssetMap().get(transGen.getAbsKey());
            if (item == null)
                return null;

            itemName = item.toString();
            //creator_address = transGen.getRecipient().getAddress();
        } else if (transaction instanceof IssueItemRecord) {
            IssueItemRecord transIssue = (IssueItemRecord) transaction;
            ItemCls item = transIssue.getItem();
            if (item == null)
                return null;

            itemName = item.getShort();
        } else if (transaction instanceof GenesisIssueItemRecord) {
            GenesisIssueItemRecord transIssue = (GenesisIssueItemRecord) transaction;
            ItemCls item = transIssue.getItem();
            if (item == null)
                return null;

            itemName = item.getShort();
        } else if (transaction instanceof RSertifyPubKeys) {
            RSertifyPubKeys sertifyPK = (RSertifyPubKeys) transaction;
            //recipient = transAmo.getRecipient();
            ItemCls item = DCSet.getInstance().getItemPersonMap().get(sertifyPK.getAbsKey());
            if (item == null)
                return null;

            itemName = item.toString();
        } else {

            try {
                if (transaction.viewItemName() != null) {
                    itemName = transaction.viewItemName();
                }
            } catch (Exception e) {
                // TODO Auto-generated catch block
                //e.printStackTrace();
                itemName = "";
            }


        }
        switch (column) {
            case COLUMN_CONFIRMATIONS:

                return transaction.getConfirmations(DCSet.getInstance());

            case COLUMN_TIMESTAMP:


                return DateTimeFormat.timestamptoString(transaction.getTimestamp());//.viewTimestamp(); // + " " + transaction.getTimestamp() / 1000;

            case COLUMN_TYPE:

                return Lang.getInstance().translate(transaction.viewFullTypeName());

            case COLUMN_CREATOR:

                return transaction.viewCreator();

            case COLUMN_ITEM:
                return itemName;

            case COLUMN_AMOUNT:

                BigDecimal amo = transaction.getAmount();
                if (amo == null)
                    return BigDecimal.ZERO;
                return amo;

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
                Controller.getInstance().isTransactionFavorite(transaction);
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
                    list.add((Transaction) map.get(key));
                }
            } catch (IOException e) {
            }
        } else {
            try (IteratorCloseable iterator = map.getDescendingIterator()) {
                while (iterator.hasNext() && count++ < step) {
                    key = iterator.next();
                    list.add((Transaction) map.get(key));
                }
            } catch (IOException e) {
            }
        }

        DCSet dcSet = DCSet.getInstance();
        for (Transaction item : list) {

            item.setDC(dcSet, false);
            item.calcFee();
        }

    }
}
