package org.erachain.gui.items.accounts;

import org.erachain.controller.Controller;
import org.erachain.core.account.Account;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.transaction.*;
import org.erachain.database.wallet.WTransactionMap;
import org.erachain.datachain.DCSet;
import org.erachain.dbs.IteratorCloseable;
import org.erachain.gui.ObserverWaiter;
import org.erachain.gui.models.WalletTableModel;
import org.mapdb.Fun;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;

////////

@SuppressWarnings("serial")
public class AccountsTransactionsTableModel extends WalletTableModel<AccountsTransactionsTableModel.Trans> implements ObserverWaiter {
    public static final int COLUMN_TIMESTAMP = 0;
    public static final int COLUMN_TRANSACTION = 1;
    public static final int COLUMN_AMOUNT = 2;
    public static final int COLUMN_ASSET = 3;
    public static final int COLUMN_TYPE = 4;
    public static final int COLUMN_SENDER = 5;
    public static final int COLUMN_RECIPIENT = 6;
    public static final int COLUMN_TITLE = 7;
    public static final int COLUMN_CONFIRM = 8;
    public static final int COLUMN_FAVORITE = 9;

    private Account filterAccount;

    private AssetCls asset;

    private HashSet actionTypes;
    private DCSet dcSet = DCSet.getInstance();

    public AccountsTransactionsTableModel() {
        super(Controller.getInstance().wallet.database.getTransactionMap(),
                new String[]{"Date", "RecNo", "Amount", "Asset", "Type", "Sender", "Recipient", "Title", "Confirmation"},
                new Boolean[]{false, true, true, false, false}, false, COLUMN_FAVORITE);

        step = 200;

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

        Trans r_Tran = list.get(row);

        String str;
        switch (column) {
            case COLUMN_TIMESTAMP:
                if (r_Tran.transaction.getTimestamp() == 0)
                    return "---";

                if (((WTransactionMap) map).isUnViewed(r_Tran.transaction)) {
                    return "<html><span style='color:red;font-weight:bold'>" + r_Tran.transaction.viewTimestamp() + "</span></html>";
                }
                return r_Tran.transaction.viewTimestamp();

            case COLUMN_TRANSACTION:

                if (r_Tran.transaction.getBlockHeight() > 0) {
                    if (((WTransactionMap) map).isUnViewed(r_Tran.transaction)) {
                        return "<html><span style='color:red;font-weight:bold'>" + r_Tran.transaction.viewHeightSeq() + "</span></html>";
                    }
                    return r_Tran.transaction.viewHeightSeq();
                }
                return "-";

            case COLUMN_AMOUNT:
                if (r_Tran.amount != null) {
                    return r_Tran.amount;
                }

            case COLUMN_ASSET:

                if (r_Tran.key != 0) {
                    return Controller.getInstance().getAsset(r_Tran.key);
                }

            case COLUMN_TYPE:
                if (((WTransactionMap) map).isUnViewed(r_Tran.transaction)) {
                    return "<html><span style='color:red;font-weight:bold'>" + r_Tran.transaction.viewFullTypeName() + "</span></html>";
                }
                return r_Tran.transaction.viewFullTypeName();

            case COLUMN_SENDER:
                if (r_Tran.owner != null) {
                    if (((WTransactionMap) map).isUnViewed(r_Tran.transaction)) {
                        return "<html><span style='color:red;font-weight:bold'>" + r_Tran.transaction.viewCreator() + "</span></html>";
                    }
                    return r_Tran.transaction.viewCreator();
                }
                return "GENESIS";

            case COLUMN_RECIPIENT:
                if (r_Tran.recipient != null) {
                    if (((WTransactionMap) map).isUnViewed(r_Tran.transaction)) {
                        return "<html><span style='color:red;font-weight:bold'>" + r_Tran.recipient + "</span></html>";
                    }
                    return r_Tran.recipient;
                }

            case COLUMN_CONFIRM:
                return r_Tran.transaction.isConfirmed(DCSet.getInstance());

            case COLUMN_TITLE:
                if (r_Tran.title != null) {
                    if (((WTransactionMap) map).isUnViewed(r_Tran.transaction)) {
                        return "<html><span style='color:red;font-weight:bold'>" + r_Tran.title + "</span></html>";
                    }
                    return r_Tran.title;
                }

            case COLUMN_FAVORITE:
                return Controller.getInstance().isTransactionFavorite(r_Tran.transaction);

        }

        return null;
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

    private boolean transParse(Fun.Tuple2<Long, Integer> walletKey, Transaction transaction) {


        //transaction.setDC_HeightSeq(dcSet, true);
        transaction.setDC(dcSet, false);

        if (transaction.getType() == Transaction.CALCULATED_TRANSACTION) {
            RCalculated tx = (RCalculated) transaction;
            String mess = tx.getMessage();
            if (mess != null && mess.equals("forging")) {
                return false;
            }
        }

        Trans trr = new Trans(walletKey);
        if (transaction.getType() == Transaction.SEND_ASSET_TRANSACTION) {
            RSend r_send = (RSend) transaction;
            trr.key = r_send.getKey();
            trr.owner = r_send.getCreator();
            trr.recipient = r_send.viewRecipient();
            trr.transaction = r_send;
            trr.amount = r_send.getAmountAndBackward();
            trr.title = r_send.getTitle();

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

            // if is owner
            String own = "";
            if (gen_send.getCreator() != null) own = gen_send.getCreator().getAddress();

            trr.key = gen_send.getKey();
            trr.transaction = gen_send;
            trr.recipient = own;
            trr.amount = gen_send.getAmount();
            trr.title = gen_send.getTitle();

            if (filterAccount != null && !gen_send.getRecipient().getAddress().equals(this.filterAccount.getAddress()))
                trr.amount = gen_send.getAmount().negate();
            // if is creator
            if (gen_send.getCreator() != null) trr.owner = gen_send.getCreator();
            // if is owner
            if (gen_send.getCreator() != null) trr.owner = gen_send.getCreator();
            trr.recipient = gen_send.viewRecipient();

        } else if (transaction.getType() == Transaction.CALCULATED_TRANSACTION) {
            RCalculated calculated = (RCalculated) transaction;

            trr.transaction = calculated;
            trr.key = calculated.getKey();
            trr.amount = calculated.getAmount();
            trr.recipient = calculated.viewRecipient();
            trr.title = calculated.getMessage();

        } else if (transaction.getType() == Transaction.CREATE_ORDER_TRANSACTION) {
            CreateOrderTransaction createOrder = (CreateOrderTransaction) transaction;

            trr.key = createOrder.getKey();
            trr.owner = createOrder.getCreator();
            trr.transaction = createOrder;
            trr.amount = createOrder.getAmount().negate();
            trr.recipient = "" + createOrder.getWantKey();
            trr.title = ""+ createOrder.getAmountWant().toPlainString();

        } else if (transaction.getType() == Transaction.CANCEL_ORDER_TRANSACTION) {
            CancelOrderTransaction cancelOrder = (CancelOrderTransaction) transaction;

            trr.key = cancelOrder.getKey();
            trr.owner = cancelOrder.getCreator();
            trr.transaction = cancelOrder;
            trr.amount = cancelOrder.getAmount();
            trr.recipient = "" + cancelOrder.getOrderID();
            trr.title = "";

        } else {
            trr.key = transaction.getKey();
            trr.owner = transaction.getCreator();
            trr.transaction = transaction;
            trr.amount = transaction.getAmount();
            trr.recipient = "";
            trr.title = transaction.getTitle();

        }

        list.add(trr);

        return true;

    }

    class Trans {
        public Fun.Tuple2<Long, Integer> walletKey;
        public Long key;
        public BigDecimal amount;
        public Account owner;
        public String recipient;
        public String title;
        public Transaction transaction;

        Trans(Fun.Tuple2<Long, Integer> walletKey) {
            this.walletKey = walletKey;
        }
    }

}
