package org.erachain.gui.items.accounts;

import org.erachain.controller.Controller;
import org.erachain.core.account.Account;
import org.erachain.core.item.ItemCls;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.transaction.*;
import org.erachain.database.wallet.WTransactionMap;
import org.erachain.datachain.DCSet;
import org.erachain.dbs.IteratorCloseable;
import org.erachain.gui.ObserverWaiter;
import org.erachain.gui.models.WalletTableModel;
import org.erachain.lang.Lang;
import org.mapdb.Fun;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;

////////

@SuppressWarnings("serial")
public class AccountsTransactionsTableModel extends WalletTableModel<AccountsTransactionsTableModel.Trans> implements ObserverWaiter {
    public static final int COLUMN_SEQNO = 0;
    public static final int COLUMN_TIMESTAMP = 1;
    public static final int COLUMN_AMOUNT = 2;
    public static final int COLUMN_ITEM_CLS = 3;
    public static final int COLUMN_TYPE = 4;
    public static final int COLUMN_SENDER = 5;
    public static final int COLUMN_RECIPIENT = 6;
    public static final int COLUMN_TITLE = 7;
    public static final int COLUMN_FAVORITE = 8;

    private Account filterAccount;

    private AssetCls asset;

    private HashSet actionTypes;
    private DCSet dcSet = DCSet.getInstance();

    public AccountsTransactionsTableModel() {
        super(Controller.getInstance().getWallet().dwSet.getTransactionMap(),
                new String[]{"№", "Date", "Amount", "Asset", "Type", "Sender", "Recipient", "Title", "Favorite"},
                new Boolean[]{false, true, true, false, false, true, true, true, true, true}, false, COLUMN_FAVORITE);

        step = 200;

    }

    @Override
    protected void updateMap() {
        map = Controller.getInstance().getWallet().dwSet.getTransactionMap();
    }

    public void setAccount(Account sender) {
        this.filterAccount = sender;
    }

    public void setAsset(AssetCls asset) {
        this.asset = asset;
    }

    @Override
    public Object getValueAt(int row, int column) {

        if (this.list == null || this.list.size() == 0 || filterAccount == null) {
            return null;
        }

        // fill table

        Trans itemTran = list.get(row);

        String str;
        switch (column) {
            case COLUMN_IS_OUTCOME:
                return itemTran.isOutCome;

            case COLUMN_UN_VIEWED:
                return itemTran.isUnViewed;

            case COLUMN_CONFIRMATIONS:
                return itemTran.transaction.getConfirmations(dcSet);

            case COLUMN_TIMESTAMP:
                if (itemTran.transaction.getTimestamp() == 0)
                    return "---";

                return itemTran.transaction.viewTimestamp();

            case COLUMN_SEQNO:

                if (itemTran.transaction.getBlockHeight() > 0) {
                    return itemTran.transaction.viewHeightSeq();
                }
                return "-";

            case COLUMN_AMOUNT:
                return itemTran.amount;

            case COLUMN_ITEM_CLS:
                return itemTran.itemCls;

            case COLUMN_TYPE:
                return Lang.T(itemTran.transaction.viewFullTypeName());

            case COLUMN_SENDER:
                if (itemTran.maker != null) {
                    return itemTran.transaction.viewCreator();
                }
                return "GENESIS";

            case COLUMN_RECIPIENT:
                return itemTran.recipient;

            case COLUMN_TITLE:
                return itemTran.title;

            case COLUMN_FAVORITE:
                return Controller.getInstance().isTransactionFavorite(itemTran.transaction);

        }

        return "";
    }

    @Override
    public void getInterval() {

        try {
            try (IteratorCloseable<Fun.Tuple2<Long, Integer>> keysIterator =
                         asset == null || asset.getKey() == AssetCls.FEE_KEY ?
                                 ((WTransactionMap) map).getAddressIterator(this.filterAccount, true)
                                 : ((WTransactionMap) map).getAddressAssetIterator(this.filterAccount, asset.getKey(), true)
            ) {

                list = new ArrayList<>();

                int counter = 0;
                while (keysIterator.hasNext() && counter < step) {

                    Fun.Tuple2<Long, Integer> key = keysIterator.next();
                    Transaction transaction = (Transaction) map.get(key);
                    if (transParse(key, transaction)) {
                        counter++;
                    }
                }
            } finally {
            }
        } catch (IOException e) {
        }

    }

    boolean needCheckUnconfirmed = true;

    @Override
    public void fireTableDataChanged() {
        if (list != null && needCheckUnconfirmed) {
            for (int i = 0; i < list.size(); i++) {
                setValueAt(i, COLUMN_CONFIRMATIONS, list.get(i).transaction.getConfirmations(dcSet));
            }
            super.fireTableDataChanged();
        }
    }

    @Override
    protected void repaintConfirms() {
        fireTableDataChanged();
    }

    private boolean transParse(Fun.Tuple2<Long, Integer> walletKey, Transaction transaction) {

        if (transaction == null)
            return false;

        transaction.setDC(dcSet, false);

        if (transaction.getType() == Transaction.CALCULATED_TRANSACTION) {
            RCalculated tx = (RCalculated) transaction;
            String mess = tx.getMessage();
            if (mess != null && mess.equals("forging")) {
                return false;
            }
        }

        Trans trr = new Trans(walletKey, transaction);

        if (transaction.getType() == Transaction.SEND_ASSET_TRANSACTION) {
            RSend r_send = (RSend) transaction;
            trr.recipient = r_send.viewRecipient();

            if (filterAccount != null) {
                //if send for *-1
                // view all types
                if (actionTypes == null || actionTypes.isEmpty()) {

                    if (this.filterAccount.equals(r_send.getCreator()))
                        if (trr.amount != null) {
                            trr.amount = trr.amount.negate();
                        }

                } else {
                    // view set types
                    if (actionTypes.contains(r_send.viewFullTypeName())) {

                        if (this.filterAccount.equals(r_send.getCreator())) {
                            if (trr.amount != null) {
                                trr.amount = trr.amount.negate();
                            }
                        }
                    }
                }
            }
        } else if (transaction.getType() == Transaction.GENESIS_SEND_ASSET_TRANSACTION) {
            GenesisTransferAssetTransaction gen_send = (GenesisTransferAssetTransaction) transaction;

            // if is maker
            String own = "";
            if (gen_send.getCreator() != null) own = gen_send.getCreator().getAddress();

            trr.recipient = own;

            if (filterAccount != null && !gen_send.getRecipient().getAddress().equals(this.filterAccount.getAddress()))
                trr.amount = gen_send.getAmount().negate();
            // if is creator
            if (gen_send.getCreator() != null) trr.maker = gen_send.getCreator();
            // if is maker
            if (gen_send.getCreator() != null) trr.maker = gen_send.getCreator();
            trr.recipient = gen_send.viewRecipient();

        } else if (transaction.getType() == Transaction.CALCULATED_TRANSACTION) {
            RCalculated calculated = (RCalculated) transaction;

            trr.recipient = calculated.viewRecipient();
            trr.title = calculated.getMessage();

        } else if (transaction.getType() == Transaction.CREATE_ORDER_TRANSACTION) {
            CreateOrderTransaction createOrder = (CreateOrderTransaction) transaction;

            trr.amount = createOrder.getAmount().negate();
            trr.recipient = "" + createOrder.getWantKey();
            trr.title = ""+ createOrder.getAmountWant().toPlainString();

        } else if (transaction.getType() == Transaction.CANCEL_ORDER_TRANSACTION) {
            CancelOrderTransaction cancelOrder = (CancelOrderTransaction) transaction;

            trr.recipient = "" + cancelOrder.getOrderID();
            trr.title = "";

        } else if (transaction.getType() == Transaction.CHANGE_ORDER_TRANSACTION) {
            ChangeOrderTransaction updateOrder = (ChangeOrderTransaction) transaction;

            trr.amount = updateOrder.getNewAmount();
            trr.recipient = ""; // + updateOrder.getAmountWant();
            trr.title = "" + updateOrder.getNewAmount().toPlainString();

        } else {
            trr.recipient = "";
            trr.title = transaction.getTitle();

        }

        list.add(trr);

        return true;

    }

    class Trans {
        public boolean isUnViewed;
        public boolean isOutCome;
        public Fun.Tuple2<Long, Integer> walletKey;
        public Transaction transaction;
        public ItemCls itemCls;
        public BigDecimal amount;
        public Account maker;
        public String recipient;
        public String title;

        Trans(Fun.Tuple2<Long, Integer> walletKey, Transaction transaction) {
            transaction.setDC(dcSet, true);
            this.transaction = transaction;
            this.walletKey = walletKey;
            isUnViewed = ((WTransactionMap) map).isUnViewed(transaction);
            if (transaction instanceof Itemable) {
                itemCls = ((Itemable) transaction).getItem();
            } else {
                Long itemKey = transaction.getAbsKey();
                if (itemKey != null && itemKey > 0) {
                    itemCls = dcSet.getItemAssetMap().get(itemKey);
                }
            }
            maker = transaction.getCreator();
            if (maker != null)
                isOutCome = maker.hashCode() == walletKey.b;
            amount = transaction.getAmount();
            title = transaction.getTitle();
        }
    }

}
