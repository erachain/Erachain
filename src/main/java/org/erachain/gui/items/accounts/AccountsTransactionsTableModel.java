package org.erachain.gui.items.accounts;

import org.bouncycastle.crypto.InvalidCipherTextException;
import org.erachain.controller.Controller;
import org.erachain.core.account.Account;
import org.erachain.core.account.PrivateKeyAccount;
import org.erachain.core.crypto.AEScrypto;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.transaction.*;
import org.erachain.database.SortableList;
import org.erachain.database.wallet.TransactionMap;
import org.erachain.datachain.DCSet;
import org.erachain.gui.ObserverWaiter;
import org.erachain.gui.models.SortedListTableModelCls;
import org.erachain.lang.Lang;
import org.erachain.utils.ObserverMessage;
import org.mapdb.Fun.Tuple2;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.*;

////////

@SuppressWarnings("serial")
public class AccountsTransactionsTableModel extends SortedListTableModelCls<Tuple2<Long, Long>, Transaction> implements ObserverWaiter {
    public static final int COLUMN_TIMESTAMP = 0;
    public static final int COLUMN_TRANSACTION = 1;
    public static final int COLUMN_AMOUNT = 2;
    public static final int COLUMN_ASSET = 3;
    public static final int COLUMN_TYPE = 4;
    public static final int COLUMN_SENDER = 5;
    public static final int COLUMN_RECIPIENT = 6;
    public static final int COLUMN_TITLE = 7;
    public static final int COLUMN_MESSAGE = 8;
    public static final int COLUMN_CONFIRM = 9;
    public static final int COLUMN_ACTION_TYPE = 19;

 //   private List<Transaction> r_Trans;
    private HashMap<String, Trans> trans_Hash_Map;
    private Object[] trans_List;
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
                new String[]{"Date", "RecNo", "Amount", "Asset", "Type", "Sender", "Recipient", "Title", "Confirmation", "Type Asset"},
                new Boolean[]{false, true, true, false, false}, false);

        dcSet = DCSet.getInstance();
       addObservers();

    }

    public void repaint() {

        this.fireTableDataChanged();
    }

    public void set_Account(Account sender) {

        this.sender = sender;
        if (listSorted != null)
            listSorted.setFilter(this.sender.getAddress());

    }

    public void set_Asset(AssetCls asset) {

        this.asset = asset;

    }

    public void set_Encryption(boolean encr) {
        this.isEncrypted = encr;

    }

    public Transaction getItem(int row) {
        return ((Trans) this.trans_List[row]).transaction;
    }


    @Override
    public int getRowCount() {
        // return this.r_Trans.size();
        if (trans_List == null || trans_List.length == 0)
            return 0;
        return trans_List.length;
    }

    @Override
    public Object getValueAt(int row, int column) {

        if (this.trans_List == null || this.trans_List.length == 0 || sender == null) {
            return null;
        }

        // fill table

        Trans r_Tran = (Trans) trans_List[row];

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

            case COLUMN_MESSAGE:

                if (r_Tran.transaction.getType() != Transaction.SEND_ASSET_TRANSACTION)
                    return "";

                RSend rs = ((RSend) r_Tran.transaction);
                if (rs == rs)
                    return rs.getHead();
                if (!rs.isEncrypted())
                    return rs.viewData();
                if (this.isEncrypted)
                    return rs.viewData();
                if (!Controller.getInstance().isWalletUnlocked())
                    return rs.viewData();

                // IF SENDER ANOTHER
                // if(account == null)
                if (!r_Tran.transaction.getCreator().getAddress().equals(this.sender.getAddress()))

                {
                    PrivateKeyAccount accountRecipient = Controller.getInstance()
                            .getPrivateKeyAccountByAddress(rs.getRecipient().getAddress());
                    privateKey = accountRecipient.getPrivateKey();

                    publicKey = rs.getCreator().getPublicKey();
                }
                // IF SENDER ME
                else {
                    PrivateKeyAccount accountRecipient = Controller.getInstance()
                            .getPrivateKeyAccountByAddress(this.sender.getAddress());
                    privateKey = accountRecipient.getPrivateKey();

                    publicKey = Controller.getInstance().getPublicKeyByAddress(rs.getRecipient().getAddress());
                }

                try {
                    byte[] ddd = AEScrypto.dataDecrypt(rs.getData(), privateKey, publicKey);
                    String sss = new String(ddd, "UTF-8");
                    String str1 = (new String(AEScrypto.dataDecrypt(rs.getData(), privateKey, publicKey), "UTF-8"));
                    return str1; // "{{" + str.substring(0,RSend.MAX_DATA_VIEW) +
                    // "...}}");
                } catch (UnsupportedEncodingException | InvalidCipherTextException e1) {
                    return ("unknown password");
                }
            case COLUMN_ACTION_TYPE:

                return r_Tran.transaction.viewFullTypeName();

        }

        return null;
    }

    @SuppressWarnings("unchecked")
    public synchronized void syncUpdate(Observable o, Object arg) {
        ObserverMessage message = (ObserverMessage) arg;

        // CHECK IF NEW LIST
        if (message.getType() == ObserverMessage.WALLET_LIST_TRANSACTION_TYPE) {
            if (this.trans_List == null) {
                get_R_Send();

            }

        } else if (message.getType() == ObserverMessage.WALLET_ADD_TRANSACTION_TYPE) {
            Transaction transaction = (Transaction) ((Tuple2) message.getValue()).b;
            if (transaction != null && trans_Hash_Map != null) {
                trans_Parse(transaction);
                needUpdate = true;
            }

        } else if (message.getType() == ObserverMessage.WALLET_REMOVE_TRANSACTION_TYPE) {
            Object transaction = message.getValue();
            if (transaction != null && trans_Hash_Map != null && trans_Hash_Map.remove(((Tuple2)transaction).b) != null) {
                needUpdate = true;
            }

        } else if (message.getType() == ObserverMessage.GUI_REPAINT && needUpdate) {

            needUpdate = false;
            get_R_Send();
            this.fireTableDataChanged();

        }

    }

    public void set_ActionTypes(HashSet str) {
        actionTypes = str;

    }

    public void get_R_Send() {

        if (this.sender == null || this.asset == null)
            return;

        Iterator<Tuple2<Long, Long>> keysIterator = ((TransactionMap) map).getAddressIterator(this.sender);

        trans_Hash_Map = new HashMap<String, Trans>();
        trans_List = null;
        int counter = 0;
        while (keysIterator.hasNext() && counter < 333) {
            Tuple2<Long, Long> key = keysIterator.next();
            Transaction transaction = ((Tuple2<Long, Transaction>) map.get(key)).b;
            if (trans_Parse(transaction)) {
                counter++;
            }
        }

        trans_List = trans_Hash_Map.values().toArray();

        this.fireTableDataChanged();
    }

    private boolean trans_Parse(Transaction transaction) {


        if (this.asset == null)
            return false;

        if (transaction.getAbsKey() != this.asset.getKey())
            return false;

        Trans trr = new Trans();
        if (transaction.getType() == Transaction.SEND_ASSET_TRANSACTION) {
            RSend r_send = (RSend) transaction;
            trr.key = r_send.getKey();
            trr.owner = r_send.getCreator();
            trr.recipient = r_send.viewRecipient();
            trr.transaction = r_send;
            trr.amount = r_send.getAmount();
            trr.title = r_send.getHead();

            //if send for *-1
            // view all types
            if (actionTypes == null || actionTypes.isEmpty()) {

                if (r_send.getCreator().getAddress().equals(this.sender.getAddress()))
                    trr.amount = r_send.getAmount().multiply(new BigDecimal("-1"));


                trans_Hash_Map.put(transaction.viewSignature(), trr);
            } else
            // view set types
            if (actionTypes.contains(r_send.viewFullTypeName())) {

                if (r_send.getCreator().getAddress().equals(this.sender.getAddress()))
                    trr.amount = r_send.getAmount().multiply(new BigDecimal("-1"));


                trans_Hash_Map.put(transaction.viewSignature(), trr);
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
            trr.title = "";

            if (!gen_send.getRecipient().getAddress().equals(this.sender.getAddress()))
                trr.amount = gen_send.getAmount().multiply(new BigDecimal("-1"));
            // if is creator
            if (gen_send.getCreator() != null) trr.owner = gen_send.getCreator();
            // if is owner
            if (gen_send.getOwner() != null) trr.owner = gen_send.getOwner();
            trr.recipient = gen_send.viewRecipient();
            trans_Hash_Map.put(transaction.viewSignature(), trr);

        } else if (transaction.getType() == Transaction.CALCULATED_TRANSACTION) {
            RCalculated calculated = (RCalculated) transaction;

            trr.transaction = calculated;
            trr.key = calculated.getKey();
            trr.amount = calculated.getAmount();
            trr.recipient = calculated.viewRecipient();
            trr.title = calculated.getMessage();
            trans_Hash_Map.put(calculated.viewSignature(), trr);

        } else if (transaction.getType() == Transaction.CREATE_ORDER_TRANSACTION) {
            CreateOrderTransaction createOrder = (CreateOrderTransaction) transaction;

            trr.key = createOrder.getKey();
            trr.owner = createOrder.getCreator();
            trr.transaction = createOrder;
            trr.amount = createOrder.getAmount();
            trr.recipient = "" + createOrder.getWantKey();
            trr.title = ""+ createOrder.getAmountWant().toPlainString();
            trans_Hash_Map.put(transaction.viewSignature(), trr);

        } else if (transaction.getType() == Transaction.CANCEL_ORDER_TRANSACTION) {
            CancelOrderTransaction cancelOrder = (CancelOrderTransaction) transaction;

            trr.key = cancelOrder.getKey();
            trr.owner = cancelOrder.getCreator();
            trr.transaction = cancelOrder;
            trr.amount = cancelOrder.getAmount();
            trr.recipient = "" + cancelOrder.getOrderID();
            trr.title = "";
            trans_Hash_Map.put(transaction.viewSignature(), trr);

        } else {
            trr.key = transaction.getKey();
            trr.owner = transaction.getCreator();
            trr.transaction = transaction;
            trr.amount = transaction.getAmount();
            trr.recipient = "";
            trr.title = "";
            trans_Hash_Map.put(transaction.viewSignature(), trr);

        }

        return true;

    }

    public void addObservers() {
        if (Controller.getInstance().doesWalletDatabaseExists()) {
            map.addObserver(this);
        }

        super.addObservers();

    }

    public void deleteObservers() {

        super.deleteObservers();

        if (Controller.getInstance().doesWalletDatabaseExists()) {
            map.deleteObserver(this);
        }

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
