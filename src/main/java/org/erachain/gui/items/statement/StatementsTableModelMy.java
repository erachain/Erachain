package org.erachain.gui.items.statement;

import org.erachain.controller.Controller;
import org.erachain.core.account.Account;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.item.ItemCls;
import org.erachain.core.transaction.RSignNote;
import org.erachain.core.transaction.Transaction;
import org.erachain.datachain.DCSet;
import org.erachain.lang.Lang;
import org.erachain.utils.ObserverMessage;
import org.erachain.utils.Pair;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.mapdb.Fun;
import org.mapdb.Fun.Tuple2;

import javax.swing.table.AbstractTableModel;
import javax.validation.constraints.Null;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class StatementsTableModelMy extends AbstractTableModel implements Observer {

    public static final int COLUMN_TIMESTAMP = 0;
    public static final int COLUMN_CREATOR = 1;
    public static final int COLUMN_TEMPLATE = 2;
    public static final int COLUMN_BODY = 3;
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    List<Transaction> transactions;
    Object[] collection;
    private String[] columnNames = Lang.getInstance().translate(new String[]{"Timestamp", "Creator", "Template", "Statement"});//, AssetCls.FEE_NAME});
    private Boolean[] column_AutuHeight = new Boolean[]{true, true, true, false};


    public StatementsTableModelMy() {
        transactions = new ArrayList<Transaction>();
        addObservers();
        transactions = read_Statement();

    }

    // set class

    public Class<? extends Object> getColumnClass(int c) {     // set column type
        Object o = getValueAt(0, c);
        return o == null ? Null.class : o.getClass();
    }

    // читаем колонки которые изменяем высоту
    public Boolean[] get_Column_AutoHeight() {

        return this.column_AutuHeight;
    }

    // устанавливаем колонки которым изменить высоту
    public void set_get_Column_AutoHeight(Boolean[] arg0) {
        this.column_AutuHeight = arg0;
    }

    @Override
    public int getColumnCount() {
        // TODO Auto-generated method stub
        return this.columnNames.length;
    }

    public Transaction get_Statement(int row) {

        if (this.collection == null || this.collection.length <= row) {
            return null;
        }

        Transaction transaction = (Transaction) this.collection[row];
        if (transaction == null)
            return null;

        return transaction;
    }

    @Override
    public String getColumnName(int index) {
        return this.columnNames[index];
    }

    @Override
    public int getRowCount() {
        // TODO Auto-generated method stub
        return collection.length;//transactions.size();
    }

    @Override
    public Object getValueAt(int row, int column) {
        // TODO Auto-generated method stub
        try {
            if (this.collection == null || this.collection.length == 0) {
                return null;
            }
            Transaction trans = (Transaction) collection[row];
            if (trans == null)
                return null;

            RSignNote record = (RSignNote) trans;

            PublicKeyAccount creator;
            switch (column) {
                case COLUMN_TIMESTAMP:

                    //return DateTimeFormat.timestamptoString(transaction.getTimestamp()) + " " + transaction.getTimestamp();
                    return record.viewTimestamp(); // + " " + transaction.getTimestamp() / 1000;

                case COLUMN_TEMPLATE:

                    ItemCls item = ItemCls.getItem(DCSet.getInstance(), ItemCls.TEMPLATE_TYPE, record.getKey());
                    return item == null ? null : item.toString();

                case COLUMN_BODY:

                    if (record.getData() == null)
                        return "";
                    if (record.getVersion() == 2) {
                        Fun.Tuple4<String, String, JSONObject, HashMap<String, Tuple2<Boolean, byte[]>>> a = record.parseDataV2WithoutFiles();

                        return a.b;
                    }

                    String str = "";
                    try {
                        JSONObject data = (JSONObject) JSONValue.parseWithException(new String(record.getData(), StandardCharsets.UTF_8));
                        str = (String) data.get("!!&_Title");
                        if (str == null) str = (String) data.get("Title");
                    } catch (Exception e) {
                        // TODO Auto-generated catch block

                        str = new String(record.getData(), StandardCharsets.UTF_8);
                    }
                    if (str == null) return "";
                    if (str.length() > 50) return str.substring(0, 50) + "...";
                    return str;//transaction.viewReference();//.viewProperies();

                case COLUMN_CREATOR:

                    creator = record.getCreator();

                    return creator == null ? null : creator.getPersonAsString();
            }

            return null;

        } catch (Exception e) {
            //	logger.error(e.getMessage(),e);
            return null;
        }
    }


    @Override
    public void update(Observable o, Object arg) {
        try {
            this.syncUpdate(o, arg);
        } catch (Exception e) {
            //GUI ERROR
        }
    }

    public synchronized void syncUpdate(Observable o, Object arg) {
        ObserverMessage message = (ObserverMessage) arg;

		/*
		//CHECK IF NEW LIST
		if(message.getType() == ObserverMessage.LIST_STATEMENT_TYPE)
		{
			if(this.transactions == null)
			{
				transactions = read_Statement();
				this.fireTableDataChanged();
			}
			
			
		}
		*/


        //CHECK IF LIST UPDATED
        if (message.getType() == ObserverMessage.WALLET_ADD_TRANSACTION_TYPE) {
            Transaction trans = (Transaction) message.getValue();
            if (trans.getType() != Transaction.SIGN_NOTE_TRANSACTION) return;
            transactions.add(trans);
            HashSet<Transaction> col = new HashSet<Transaction>(transactions);
            collection = col.toArray();

            this.fireTableDataChanged();
        }
        if (message.getType() == ObserverMessage.WALLET_REMOVE_TRANSACTION_TYPE) {

            Transaction record = (Transaction) message.getValue();
            byte[] signKey = record.getSignature();
            for (int i = 0; i < this.transactions.size() - 1; i++) {
                Transaction item = this.transactions.get(i);
                if (item == null)
                    return;
                if (Arrays.equals(signKey, item.getSignature())) {
                    this.fireTableRowsDeleted(i, i); //.fireTableDataChanged();
                }
            }
            this.fireTableDataChanged();
            if (false)
                this.transactions.contains(new Pair<Tuple2<String, String>, Transaction>(
                        new Tuple2<String, String>(record.getCreator().getAddress(), new String(record.getSignature())), record));
        }
    }


    private List<Transaction> read_Statement() {
        List<Transaction> tran;
        ArrayList<Transaction> db_transactions;
        db_transactions = new ArrayList<Transaction>();
        tran = new ArrayList<Transaction>();
        transactions.clear();
        // база данных
        for (Transaction transaction : Controller.getInstance().getUnconfirmedTransactions(1000, true)) {
            if (transaction.getType() == Transaction.SIGN_NOTE_TRANSACTION) {
                transactions.add(transaction);
            }
        }

        for (Account account : Controller.getInstance().getAccounts()) {
            transactions.addAll(DCSet.getInstance().getTransactionFinalMap().getTransactionsByAddressAndType(account.getShortAddressBytes(), Transaction.SIGN_NOTE_TRANSACTION, 0, 0));//.SEND_ASSET_TRANSACTION, 0));
        }

        HashSet<Transaction> col = new HashSet<Transaction>(transactions);
        collection = col.toArray();

        return transactions;

    }

    public void removeObservers() {

        Controller.getInstance().deleteObserver(this);

    }

    public void addObservers() {
        Controller.getInstance().addObserver(this);
    }

}
