package org.erachain.gui.items.accounts;

import org.bouncycastle.crypto.InvalidCipherTextException;
import org.erachain.controller.Controller;
import org.erachain.core.account.Account;
import org.erachain.core.account.PrivateKeyAccount;
import org.erachain.core.crypto.AEScrypto;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.transaction.GenesisTransferAssetTransaction;
import org.erachain.core.transaction.R_Send;
import org.erachain.core.transaction.Transaction;
import org.erachain.database.SortableList;
import org.erachain.database.wallet.TransactionMap;
import org.erachain.datachain.DCSet;
import org.erachain.gui.models.SortedListTableModelCls;
import org.erachain.lang.Lang;
import org.erachain.utils.ObserverMessage;
import org.erachain.utils.Pair;
import org.mapdb.Fun.Tuple2;

import javax.validation.constraints.Null;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.*;

////////

@SuppressWarnings("serial")
public class Accounts_Transactions_TableModel extends SortedListTableModelCls<Tuple2<String, String>, Transaction> {
    public static final int COLUMN_TIMESTAMP = 0;
    public static final int COLUMN_TRANSACTION = 1;
    public static final int COLUMN_AMOUNT = 2;
    public static final int COLUMN_ASSET = 3;
    public static final int COLUMN_TYPE = 4;
    public static final int COLUMN_SENDER = 5;
    public static final int COLUMN_RECIPIENT = 6;
    public static final int COLUMN_MESSAGE = 7;
    public static final int COLUMN_CONFIRM = 8;
    public static final int COLUMN_ACTION_TYPE = 19;

 //   private List<Transaction> r_Trans;
    private HashMap<String, Trans> trans_Hash_Map;
    private Object[] trans_List;
    private boolean isEncrypted = true;

    private Account sender;
    private AssetCls asset;
    private byte[] privateKey;
    private byte[] publicKey;

    private HashSet actionTypes;

    public Accounts_Transactions_TableModel() {
        super(Controller.getInstance().wallet.database.getTransactionMap(),
                new String[]{"Date", "RecNo", "Amount", "Asset", "Type", "Sender", "Recipient", "Title", "Confirmation", "Type Asset"},
                new Boolean[]{false, true, true, false, false}, false);

       addObservers();

    }

    public void repaint() {

        this.fireTableDataChanged();
    }

    public void set_Account(Account sender) {

        this.sender = sender;
        if (list != null)
            list.setFilter(this.sender.getAddress());

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
                //	if (r_Tran.transaction.getType() == Transaction.GENESIS_SEND_ASSET_TRANSACTION)
                return r_Tran.amount;
            case COLUMN_ASSET:
                return Controller.getInstance().getAsset(r_Tran.transaction.getAbsKey()).toString();

            case COLUMN_TYPE:
                return r_Tran.transaction.viewFullTypeName();
            case COLUMN_RECIPIENT:
                return r_Tran.transaction.viewRecipient();
            case COLUMN_SENDER:
                if (r_Tran.owner == null)
                    return "GENESIS";
                return r_Tran.transaction.viewCreator();

            case COLUMN_CONFIRM:
                return r_Tran.transaction.isConfirmed(DCSet.getInstance());

            case COLUMN_MESSAGE:

                if (r_Tran.transaction.getType() != Transaction.SEND_ASSET_TRANSACTION)
                    return Lang.getInstance().translate("Genesis Transaction");

                R_Send rs = ((R_Send) r_Tran.transaction);
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
                    return str1; // "{{" + str.substring(0,R_Send.MAX_DATA_VIEW) +
                    // "...}}");
                } catch (UnsupportedEncodingException | InvalidCipherTextException e1) {
                    return ("unknown password");
                }
            case COLUMN_ACTION_TYPE:
			
			/*
			if (r_Tran.transaction.getType() == Transaction.SEND_ASSET_TRANSACTION){
				R_Send rs1 = ((R_Send) r_Tran.transaction);
				return rs1.viewFullTypeName();
				
			}else{
				GenesisTransferAssetTransaction rs2 = (GenesisTransferAssetTransaction)r_Tran.transaction;
				
				return rs2.viewFullTypeName();
			}
			*/

                return r_Tran.transaction.viewFullTypeName();

        }

        return null;
    }

    @SuppressWarnings("unchecked")
    public synchronized void syncUpdate(Observable o, Object arg) {
        ObserverMessage message = (ObserverMessage) arg;

        if (message.getType() == ObserverMessage.WALLET_STATUS) {
            // int status = (int) message.getValue();

            // if(status == Wallet.STATUS_LOCKED)
            // {
            // cryptoCloseAll();
            //		get_R_Send();
            // }
        }

        // if( message.getType() == ObserverMessage.NETWORK_STATUS ||
        // (int)message.getValue() == Controller.STATUS_OK ) {
        // this.fireTableDataChanged();
        // }

        // CHECK IF NEW LIST
        if (message.getType() == ObserverMessage.WALLET_LIST_TRANSACTION_TYPE) {
            if (this.trans_List == null) {
                
                list = (SortableList<Tuple2<String, String>, Transaction>) message.getValue();
                //sortableItems.registerObserver();
                //Controller.getInstance().wallet.database.getTransactionMap().addObserver(sortableItems);
                list.sort(TransactionMap.ADDRESS_INDEX, true);
                // this.r_Trans.sort(NameMap.NAME_INDEX);
                get_R_Send();
               
            }

            //	this.fireTableDataChanged();
        }

        // CHECK IF LIST UPDATED
        if (message.getType() == ObserverMessage.WALLET_ADD_TRANSACTION_TYPE) {
            //get_R_Send();
            trans_Parse((Transaction) message.getValue());

        }
        // CHECK IF LIST UPDATED
        if (message.getType() == ObserverMessage.WALLET_REMOVE_TRANSACTION_TYPE) {
            //get_R_Send();
            Object sss = message.getValue();
            if (sss != null && trans_Hash_Map != null)
                trans_Hash_Map.remove((Transaction) sss);

        }

    }

    public void set_ActionTypes(HashSet str) {
        actionTypes = str;

    }

    public void get_R_Send() {

        if (this.sender == null || this.asset == null || list == null)
            return;

        trans_Hash_Map = new HashMap<String, Trans>();
        trans_List = null;
        Iterator<Pair<Tuple2<String, String>, Transaction>> s_it = list.iterator();
        while (s_it.hasNext()) {
            Pair<Tuple2<String, String>, Transaction> tt = s_it.next();
            Transaction ttt = tt.getB();
            trans_Parse(ttt);
        }

        trans_List = trans_Hash_Map.values().toArray();

        this.fireTableDataChanged();
    }

    private void trans_Parse(Transaction ttt) {


        if (this.asset == null)
            return;

        if (ttt.getAbsKey() != this.asset.getKey()) return;

        Trans trr = new Trans();
        if (ttt.getType() == Transaction.SEND_ASSET_TRANSACTION) {
            R_Send tttt = (R_Send) ttt;
            trr.owner = tttt.getCreator();
            trr.recipient = tttt.getRecipient();
            trr.transaction = tttt;
            trr.amount = tttt.getAmount();
            //if send for *-1
            // view all types
            if (actionTypes == null || actionTypes.isEmpty()) {

                if (tttt.getCreator().getAddress().equals(this.sender.getAddress()))
                    trr.amount = tttt.getAmount().multiply(new BigDecimal("-1"));


                trans_Hash_Map.put(ttt.viewSignature(), trr);
                return;
            }
            // view set types
            if (actionTypes.contains(tttt.viewFullTypeName())) {

                if (tttt.getCreator().getAddress().equals(this.sender.getAddress()))
                    trr.amount = tttt.getAmount().multiply(new BigDecimal("-1"));


                trans_Hash_Map.put(ttt.viewSignature(), trr);
            }


        } else if (ttt.getType() == Transaction.GENESIS_SEND_ASSET_TRANSACTION) {
            GenesisTransferAssetTransaction ttt1 = (GenesisTransferAssetTransaction) ttt;

            // String a = ttt1.getCreator().getAddress();

            String cr = "";
            if (ttt1.getCreator() != null) cr = ttt1.getCreator().getAddress();
            // if is owner
            String own = "";
            if (ttt1.getOwner() != null) own = ttt1.getOwner().getAddress();


            trr.transaction = ttt1;
            trr.amount = ttt1.getAmount();
            // if send


            if (!ttt1.getRecipient().getAddress().equals(this.sender.getAddress()))
                trr.amount = ttt1.getAmount().multiply(new BigDecimal("-1"));
            // if is creator
            if (ttt1.getCreator() != null) trr.owner = ttt1.getCreator();
            // if is owner
            if (ttt1.getOwner() != null) trr.owner = ttt1.getOwner();
            trr.recipient = ttt1.getRecipient();

            // view all types
            if (actionTypes == null || actionTypes.isEmpty()) {

                trans_Hash_Map.put(ttt.viewSignature(), trr);
                return;
            }
            // view set types
            if (actionTypes.contains(ttt1.viewTypeName())) {
                trans_Hash_Map.put(ttt.viewSignature(), trr);
            }

        }

    }

    public void addObservers() {
        if (Controller.getInstance().doesWalletDatabaseExists())
            map.addObserver(this);

    }

    public void deleteObservers() {
        if (Controller.getInstance().doesWalletDatabaseExists()) {
            map.deleteObserver(this);
        }

    }

    class Trans {
        public BigDecimal amount;
        public Account owner;
        public Account recipient;
        public Transaction transaction;

    }

}
