package org.erachain.gui.bank;

import org.erachain.controller.Controller;
import org.erachain.core.item.ItemCls;
import org.erachain.core.transaction.*;
import org.erachain.database.SortableList;
import org.erachain.database.wallet.TransactionMap;
import org.erachain.datachain.DCSet;
import org.erachain.gui.library.library;
import org.erachain.gui.models.TableModelCls;
import org.erachain.lang.Lang;
import org.erachain.utils.*;
import org.mapdb.Fun.Tuple2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.Null;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Observable;
import java.util.Observer;

@SuppressWarnings("serial")
// in list of org.erachain.records in wallet
public class Payment_Orders_TableModel extends TableModelCls<Tuple2<String, String>, Transaction> implements Observer {

    public static final int COLUMN_CONFIRMATIONS = 0;
    public static final int COLUMN_TIMESTAMP = 1;
    public static final int COLUMN_TYPE = 2;
    public static final int COLUMN_CREATOR = 3;
    public static final int COLUMN_ITEM = 4;
    public static final int COLUMN_AMOUNT = 5;
    public static final int COLUMN_RECIPIENT = 6;
    public static final int COLUMN_FEE = 7;
    public static final int COLUMN_SIZE = 8;
    static Logger LOGGER = LoggerFactory.getLogger(Payment_Orders_TableModel.class.getName());
    private SortableList<Tuple2<String, String>, Transaction> transactions;
    //ItemAssetMap dbItemAssetMap;
    private ArrayList<R_Send> trans;
    private Boolean[] column_AutuHeight = new Boolean[]{true, true, true, true, true, true, true, false, false};

    public Payment_Orders_TableModel() {
        super(new String[]{
                        "Confirmation", "Timestamp", "Type", "Creator", "Item", "Amount", "Recipient", "Fee", "Size"});

    }

    @Override
    public SortableList<Tuple2<String, String>, Transaction> getSortableList() {
        return this.transactions;
    }


    public Object getItem(int row) {
        return this.transactions.get(row).getB();
    }

    public Class<? extends Object> getColumnClass(int c) {     // set column type
        Object o = getValueAt(0, c);
        return o == null ? Null.class : o.getClass();
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
        if (this.trans == null) {
            return 0;
        }

        return this.trans.size();
    }

    @Override
    public Object getValueAt(int row, int column) {
        //try
        //{
        if (this.trans == null || this.trans.size() - 1 < row) {
            return null;
        }


        Transaction transaction = trans.get(row);
        if (transaction == null)
            return null;
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
        } else if (transaction instanceof IssueItemRecord) {
            IssueItemRecord transIssue = (IssueItemRecord) transaction;
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
        } else if (transaction.viewItemName() != null) {
            itemName = transaction.viewItemName();
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

                return transaction.viewRecipient();

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

    @Override
    public void update(Observable o, Object arg) {
        try {
            this.syncUpdate(o, arg);
        } catch (Exception e) {
            //GUI ERROR
        }
    }

    @SuppressWarnings("unchecked")
    public synchronized void syncUpdate(Observable o, Object arg) {
        ObserverMessage message = (ObserverMessage) arg;
        int messageType = message.getType();

        //CHECK IF NEW LIST
        if (false && messageType == ObserverMessage.WALLET_LIST_TRANSACTION_TYPE) {
            if (this.transactions == null) {
                transactions = (SortableList<Tuple2<String, String>, Transaction>) message.getValue();
                transactions.registerObserver();
                transactions.sort(TransactionMap.TIMESTAMP_INDEX, true);
                read_trans();
                this.fireTableDataChanged();
            }

        } else if (messageType == ObserverMessage.WALLET_LIST_TRANSACTION_TYPE) {
            //CHECK IF LIST UPDATED
            read_trans();
            this.fireTableDataChanged();

        } else if (message.getType() == ObserverMessage.WALLET_ADD_TRANSACTION_TYPE
                //|| message.getType() == ObserverMessage.WALLET_REMOVE_TRANSACTION_TYPE
                ) {
            Transaction transaction = (Transaction) message.getValue();
            library.notifySysTrayRecord(transaction);
        }

        if (message.getType() == ObserverMessage.WALLET_ADD_BLOCK_TYPE
                || message.getType() == ObserverMessage.WALLET_REMOVE_BLOCK_TYPE
                || message.getType() == ObserverMessage.WALLET_LIST_BLOCK_TYPE
                || message.getType() == ObserverMessage.WALLET_RESET_BLOCK_TYPE
                ) {
            this.fireTableDataChanged();
        }
    }

    private void read_trans() {
        // TODO Auto-generated method stub
        Iterator<Pair<Tuple2<String, String>, Transaction>> it = transactions.iterator();
        trans = new ArrayList<R_Send>();
        while (it.hasNext()) {
            Pair<Tuple2<String, String>, Transaction> tr = it.next();
            Transaction ss = tr.getB();
            if (!(ss instanceof R_Send)) continue;
            String hh = ((R_Send) ss).getHead();
            if (!((R_Send) ss).getHead().equals("SPO")) continue;
            trans.add((R_Send) ss);


        }
    }

    public void addObserversThis() {

        Controller.getInstance().addWalletObserver(this);
    }


    public void removeObserversThis() {

        Controller.getInstance().deleteObserver(this);
    }
}
