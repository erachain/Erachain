package org.erachain.gui.models;

import org.erachain.controller.Controller;
import org.erachain.core.account.Account;
import org.erachain.core.item.ItemCls;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.transaction.*;
import org.erachain.database.SortableList;
import org.erachain.database.wallet.TransactionMap;
import org.erachain.datachain.DCSet;
import org.erachain.gui.library.library;
import org.erachain.lang.Lang;
import org.erachain.ntp.NTP;
import org.erachain.settings.Settings;
import org.erachain.utils.DateTimeFormat;
import org.erachain.utils.ObserverMessage;
import org.erachain.utils.Pair;
import org.erachain.utils.PlaySound;
import org.mapdb.Fun.Tuple2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.Null;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

@SuppressWarnings("serial")
// in list of org.erachain.records in wallet
public class WalletTransactionsTableModel extends TableModelCls<Tuple2<String, String>, Transaction> implements Observer {

    public static final int COLUMN_CONFIRMATIONS = 0;
    public static final int COLUMN_TIMESTAMP = 1;
    public static final int COLUMN_TYPE = 2;
    public static final int COLUMN_CREATOR = 3;
    public static final int COLUMN_ITEM = 4;
    public static final int COLUMN_AMOUNT = 5;
    public static final int COLUMN_RECIPIENT = 6;
    public static final int COLUMN_FEE = 7;
    public static final int COLUMN_SIZE = 8;

    static Logger LOGGER = LoggerFactory.getLogger(WalletTransactionsTableModel.class.getName());

    private SortableList<Tuple2<String, String>, Transaction> transactions;

    private Boolean[] column_AutuHeight = new Boolean[]{true, true, true, true, true, true, true, false, false};

    //private List<Pair<Tuple2<String, String>, Transaction>> pairTransactions;

    public WalletTransactionsTableModel() {
        super("WalletTransactionsTableModel", 1000,
                new String[]{
                        "Confirmations", "Timestamp", "Type", "Creator", "Item", "Amount", "Recipient", "Fee", "Size"});
    }

    @Override
    public SortableList<Tuple2<String, String>, Transaction> getSortableList() {
        return this.transactions;
    }

    public Object getItem(int row) {
        return getTransaction(row);
    }

    // читаем колонки которые изменяем высоту
    public Boolean[] get_Column_AutoHeight() {

        return this.column_AutuHeight;
    }

    // устанавливаем колонки которым изменить высоту
    public void set_get_Column_AutoHeight(Boolean[] arg0) {
        this.column_AutuHeight = arg0;
    }

    public Transaction getTransaction(int row) {
        Pair<Tuple2<String, String>, Transaction> data = this.transactions.get(row);
        if (data == null || data.getB() == null) {
            return null;
        }
        return data.getB();
    }

    @Override
    public int getRowCount() {
        if (this.transactions == null) {
            return 0;
        }

        return this.transactions.size();
    }

    @Override
    public Object getValueAt(int row, int column) {
        //try
        //{
        if (this.transactions == null || this.transactions.size() - 1 < row) {
            return null;
        }

        Pair<Tuple2<String, String>, Transaction> data = this.transactions.get(row);

        if (data == null || data.getB() == null) {
            return null;
        }

        Tuple2<String, String> addr1 = data.getA();
        if (addr1 == null)
            return null;

        String creator_address = data.getA().a;
        //Account creator = new Account(data.getA().a);
        //Account recipient = null; // = new Account(data.getA().b);

        Transaction transaction = data.getB();
        if (transaction == null)
            return null;

        transaction.setDC(DCSet.getInstance());

        //creator = transaction.getCreator();
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
            creator_address = transGen.getRecipient().getAddress();
        } else if (transaction instanceof Issue_ItemRecord) {
            Issue_ItemRecord transIssue = (Issue_ItemRecord) transaction;
            ItemCls item = transIssue.getItem();
            if (item == null)
                return null;

            itemName = item.getShort();
        } else if (transaction instanceof GenesisIssue_ItemRecord) {
            GenesisIssue_ItemRecord transIssue = (GenesisIssue_ItemRecord) transaction;
            ItemCls item = transIssue.getItem();
            if (item == null)
                return null;

            itemName = item.getShort();
        } else if (transaction instanceof R_SertifyPubKeys) {
            R_SertifyPubKeys sertifyPK = (R_SertifyPubKeys) transaction;
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
        }

        return null;

        //} catch (Exception e) {
        //GUI ERROR
        //	LOGGER.error(e.getMessage(),e);
        //	return null;
        //}

    }

    @SuppressWarnings("unchecked")
    public synchronized void syncUpdate(Observable o, Object arg) {
        if (Controller.getInstance().wallet.database == null)
            return;

        ObserverMessage message = (ObserverMessage) arg;

        //CHECK IF NEW LIST
        if (message.getType() == ObserverMessage.WALLET_LIST_TRANSACTION_TYPE) {
            if (this.transactions == null) {
                this.transactions = (SortableList<Tuple2<String, String>, Transaction>)message.getValue();
                this.transactions.registerObserver();
                this.transactions.sort(TransactionMap.TIMESTAMP_INDEX, true);
            }

            this.fireTableDataChanged();

        } else if (message.getType() == ObserverMessage.CHAIN_ADD_BLOCK_TYPE
                    || message.getType() == ObserverMessage.CHAIN_REMOVE_BLOCK_TYPE) {
            // если прилетел блок или откатился и нужно обновить - то обновляем

            needUpdate = true;
            //this.fireTableDataChanged();

        } else if (message.getType() == ObserverMessage.BLOCKCHAIN_SYNC_STATUS
                            || message.getType() == ObserverMessage.WALLET_SYNC_STATUS) {

            needUpdate = true;
            //this.fireTableDataChanged();

        } else if (message.getType() == ObserverMessage.WALLET_ADD_TRANSACTION_TYPE) {
            // INCOME

            needUpdate = true;
            if (true)
                return;

            Transaction transaction = (Transaction) message.getValue();
            Account creator = transaction.getCreator();
            Pair<Tuple2<String, String>, Transaction> item = new Pair<Tuple2<String, String>, Transaction>(
                    new Tuple2<String, String>(creator == null ? "GENESIS" : creator.getAddress(),
                            new String(transaction.getSignature())), transaction);

            if (false) {
                //*****this.transactions.contains(pair);
                // ОЧЕНЬ сильно тормозит так как внутри перебор обычный

                boolean found = this.transactions.contains(item);

                if (found) {
                    return;
                }
            }

            /*
            Ошибка - это статичный массив - в него нельзя не добавлять ни удалять
            if (this.transactions.size()> 1000)
                this.transactions.remove(this.transactions.size() - 1);

            this.transactions.add(0, pair);
            */

            if (DCSet.getInstance().getTransactionMap().contains(transaction.getSignature())) {
                if (transaction.getType() == Transaction.SEND_ASSET_TRANSACTION) {
                    library.notifySysTrayRecord(transaction);
                } else if (Settings.getInstance().isSoundNewTransactionEnabled()) {
                    PlaySound.getInstance().playSound("newtransaction.wav", transaction.getSignature());
                }
            }

            this.transactions.add(item);
            this.fireTableRowsInserted(0, 0);

            boolean needFire = false;
            while(this.transactions.size() > step) {
                this.transactions.remove(step);
                needFire = true;
            }

            if (needFire) this.fireTableRowsDeleted(step, step);


        } else if (message.getType() == ObserverMessage.ADD_UNC_TRANSACTION_TYPE) {
            // INCOME

            needUpdate = true;
            if (true)
                return;

            Pair<byte[], Transaction> item = (Pair<byte[], Transaction>) message.getValue();
            Transaction transaction = item.getB();

            Account creator = transaction.getCreator();

            if (!Controller.getInstance().wallet.accountExists(creator.getAddress())) {
                return;
            }

            Pair<Tuple2<String, String>, Transaction> itemThis = new Pair<Tuple2<String, String>, Transaction>(
                    new Tuple2<String, String>(creator == null ? "GENESIS" : creator.getAddress(),
                            new String(transaction.getSignature())), transaction);

            if (false) {
                //*****this.transactions.contains(pair);
                // ОЧЕНЬ сильно тормозит так как внутри перебор обычный

                boolean found = this.transactions.contains(item);

                if (found) {
                    return;
                }
            }

            /*
            Ошибка - это статичный массив - в него нельзя не добавлять ни удалять
            if (this.transactions.size()> 1000)
                this.transactions.remove(this.transactions.size() - 1);

            this.transactions.add(0, pair);
            */

            if (DCSet.getInstance().getTransactionMap().contains(transaction.getSignature())) {
                if (transaction.getType() == Transaction.SEND_ASSET_TRANSACTION) {
                    library.notifySysTrayRecord(transaction);
                } else if (Settings.getInstance().isSoundNewTransactionEnabled()) {
                    PlaySound.getInstance().playSound("newtransaction.wav", transaction.getSignature());
                }
            }

            this.transactions.add(0, itemThis);
            this.fireTableRowsInserted(0, 0);

            boolean needFire = false;
            while(this.transactions.size() > step) {
                this.transactions.remove(step);
                needFire = true;
            }

            if (needFire)
                this.fireTableRowsDeleted(step, step);

        } else if (message.getType() == ObserverMessage.WALLET_REMOVE_TRANSACTION_TYPE
                || message.getType() == ObserverMessage.REMOVE_UNC_TRANSACTION_TYPE) {

            needUpdate = true;
            if (true)
                return;

            try {
                this.transactions.remove(0);
            } catch (Exception e) {
                getInterval();
                this.fireTableDataChanged();
                return;
            }
            if (this.transactions.size() > 1) {
                this.fireTableRowsDeleted(0, 0);
            } else {
                getInterval();
                this.fireTableDataChanged();
            }

        }
    }

    public void addObserversThis() {

        if (!Controller.getInstance().doesWalletDatabaseExists())
            return;

        //REGISTER ON WALLET TRANSACTIONS
        Controller.getInstance().getWallet().database.getTransactionMap().addObserver(this);
        // for UNCONFIRMEDs
        DCSet.getInstance().getTransactionMap().addObserver(this);
        // for ??
        ///Controller.getInstance().wallet.database.getPersonMap().addObserver(transactions);

        getInterval();

    }


    public void removeObserversThis() {

        //dbItemAssetMap = DLSet.getInstance().getItemAssetMap();

        if (Controller.getInstance().doesWalletDatabaseExists())
            return;

        Controller.getInstance().getWallet().database.getTransactionMap().deleteObserver(this);
        DCSet.getInstance().getTransactionMap().deleteObserver(this);
        /// ??? Controller.getInstance().wallet.database.getPersonMap().deleteObserver(transactions);
    }

    @Override
    public int getMapSize() {
        return Controller.getInstance().getWallet().database.getTransactionMap().size();
    }

        @Override
    public void getIntervalThis(int startBack, int endBack) {
        transactions = new SortableList<Tuple2<String, String>, Transaction>(
                Controller.getInstance().getWallet().database.getTransactionMap(),
                Controller.getInstance().getWallet().database.getTransactionMap().getFromToKeys(startBack, endBack));

    }
}
