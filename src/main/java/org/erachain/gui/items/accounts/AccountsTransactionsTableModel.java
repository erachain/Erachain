package org.erachain.gui.items.accounts;

import org.erachain.controller.Controller;
import org.erachain.core.account.Account;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.transaction.*;
import org.erachain.database.SortableList;
import org.erachain.database.wallet.TransactionMap;
import org.erachain.datachain.DCSet;
import org.erachain.gui.ObserverWaiter;
import org.erachain.gui.models.TimerTableModelCls;
import org.erachain.lang.Lang;
import org.mapdb.Fun.Tuple2;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

////////

@SuppressWarnings("serial")
public class AccountsTransactionsTableModel extends TimerTableModelCls<AccountsTransactionsTableModel.Trans> implements ObserverWaiter {
    public static final int COLUMN_TIMESTAMP = 0;
    public static final int COLUMN_TRANSACTION = 1;
    public static final int COLUMN_AMOUNT = 2;
    public static final int COLUMN_ASSET = 3;
    public static final int COLUMN_TYPE = 4;
    public static final int COLUMN_SENDER = 5;
    public static final int COLUMN_RECIPIENT = 6;
    public static final int COLUMN_TITLE = 7;
    public static final int COLUMN_CONFIRM = 8;
    public static final int COLUMN_ACTION_TYPE = 19;

    private boolean isEncrypted = true;

    private String[] columnNames = Lang.getInstance().translate(new String[]{"Date", "RecNo", "Amount",
            "Asset", "Type", "Sender", "Recipient", "Title", "Confirmation", "Type Asset"});
    private Boolean[] column_AutuHeight = new Boolean[]{false, true, true, false, false};

    private SortableList<Tuple2<Long, Long>, Transaction> sortableItems;
    private Account sender;

    private AssetCls asset;

    private byte[] privateKey;

    private byte[] publicKey;

    private HashSet actionTypes;
    private DCSet dcSet;

    public AccountsTransactionsTableModel() {
        super(Controller.getInstance().wallet.database.getTransactionMap(),
                new String[]{"Date", "RecNo", "Amount", "Asset", "Type", "Sender", "Recipient", "Title", "Confirmation"},
                new Boolean[]{false, true, true, false, false}, false);

        dcSet = DCSet.getInstance();
        addObservers();

    }

    public void set_Account(Account sender) {
        this.sender = sender;
    }

    public void set_Asset(AssetCls asset) {
        this.asset = asset;
    }

    public void set_Encryption(boolean encr) {
        this.isEncrypted = encr;
    }

    @Override
    public Object getValueAt(int row, int column) {

        if (this.list == null || this.list.size() == 0 || sender == null) {
            return null;
        }

        // fill table

        Trans r_Tran = list.get(row);

        String str;
        switch (column) {
            case COLUMN_TIMESTAMP:
                if (r_Tran.transaction.getTimestamp() == 0)
                    return "---";
                return r_Tran.transaction.viewTimestamp();
           
            case COLUMN_TRANSACTION:

                if (r_Tran.transaction.isConfirmed(DCSet.getInstance()))
                    return r_Tran.transaction.viewHeightSeq();
                return "-";

            case COLUMN_AMOUNT:
                return r_Tran.amount;

            case COLUMN_ASSET:
                return Controller.getInstance().getAsset(r_Tran.key);

            case COLUMN_TYPE:
                return r_Tran.transaction.viewFullTypeName();

            case COLUMN_RECIPIENT:
                return r_Tran.recipient;

            case COLUMN_SENDER:
                if (r_Tran.owner == null)
                    return "GENESIS";
                return r_Tran.transaction.viewCreator();

            case COLUMN_CONFIRM:
                return r_Tran.transaction.isConfirmed(DCSet.getInstance());

            case COLUMN_TITLE:
                return r_Tran.title;

            case COLUMN_ACTION_TYPE:

                return r_Tran.transaction.viewFullTypeName();

        }

        return null;
    }

    @Override
    public void getIntervalThis(long start, int limit) {

        if (this.sender == null || this.asset == null)
            return;

        /// WALLET addesses
        Iterator<Tuple2<Long, Long>> keysIterator = ((TransactionMap) map).getAddressDescendingIterator(this.sender);

        list = new ArrayList<>();

        int counter = 0;
        while (keysIterator.hasNext() && counter < 999) {
            Tuple2<Long, Long> key = keysIterator.next();
            Transaction transaction = ((Tuple2<Long, Transaction>) map.get(key)).b;
            if (trans_Parse(transaction)) {
                counter++;
            }
        }

    }

    private boolean trans_Parse(Transaction transaction) {


        if (this.asset == null)
            return false;

        transaction.setDC_HeightSeq(dcSet);

        if (transaction.getAbsKey() != this.asset.getKey()
                // все для Компушек
                && this.asset.getKey() != Transaction.FEE_KEY)
            return false;

        if (transaction.getType() == Transaction.CALCULATED_TRANSACTION) {
            RCalculated tx = (RCalculated) transaction;
            String mess = tx.getMessage();
            if (mess != null && mess.equals("forging")) {
                return false;
            }
        }

        Trans trr = new Trans();
        if (transaction.getType() == Transaction.SEND_ASSET_TRANSACTION) {
            RSend r_send = (RSend) transaction;
            trr.key = r_send.getKey();
            trr.owner = r_send.getCreator();
            trr.recipient = r_send.viewRecipient();
            trr.transaction = r_send;
            trr.amount = r_send.getAmountAndBackward();
            trr.title = r_send.getTitle();

            //if send for *-1
            // view all types
            if (actionTypes == null || actionTypes.isEmpty()) {

                if (r_send.getCreator().getAddress().equals(this.sender.getAddress()))
                    if (trr.amount != null) {
                        trr.amount = trr.amount.negate();
                    }

            } else {
                // view set types
                if (actionTypes.contains(r_send.viewFullTypeName())) {

                    if (r_send.getCreator().getAddress().equals(this.sender.getAddress())) {
                        if (trr.amount != null) {
                            trr.amount = trr.amount.negate();
                        }
                    }
                }
            }
        } else if (transaction.getType() == Transaction.GENESIS_SEND_ASSET_TRANSACTION) {
            GenesisTransferAssetTransaction gen_send = (GenesisTransferAssetTransaction) transaction;

            String cr = "";
            if (gen_send.getCreator() != null) cr = gen_send.getCreator().getAddress();
            // if is owner
            String own = "";
            if (gen_send.getOwner() != null) own = gen_send.getOwner().getAddress();

            trr.key = gen_send.getKey();
            trr.transaction = gen_send;
            trr.recipient = own;
            trr.amount = gen_send.getAmount();
            trr.title = gen_send.getTitle();

            if (!gen_send.getRecipient().getAddress().equals(this.sender.getAddress()))
                trr.amount = gen_send.getAmount().negate();
            // if is creator
            if (gen_send.getCreator() != null) trr.owner = gen_send.getCreator();
            // if is owner
            if (gen_send.getOwner() != null) trr.owner = gen_send.getOwner();
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
        public Long key;
        public BigDecimal amount;
        public Account owner;
        public String recipient;
        public String title;
        public Transaction transaction;
    }

}
