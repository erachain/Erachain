package org.erachain.gui.items.statement;

import org.erachain.controller.Controller;
import org.erachain.core.block.Block;
import org.erachain.core.transaction.R_SignNote;
import org.erachain.core.transaction.Transaction;
import org.erachain.database.SortableList;
import org.erachain.datachain.DCSet;
import org.erachain.lang.Lang;
import org.erachain.utils.ObserverMessage;
import org.erachain.utils.Pair;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple3;

import javax.swing.table.AbstractTableModel;
import javax.validation.constraints.Null;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

public class Statements_Table_Model_Favorite extends AbstractTableModel implements Observer {

    public static final int COLUMN_TIMESTAMP = 0;
    public static final int COLUMN_CREATOR = 1;
    //	public static final int COLUMN_TEMPLATE = 2;
    public static final int COLUMN_BODY = 2;
    public static final int COLUMN_FAVORITE = 3;
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    SortableList<Tuple2<String, String>, Transaction> transactions;
    private String[] columnNames = new String[]{"Timestamp", "Creator"/*, "Template"*/, "Statement", "Favorite"};// ,
    // AssetCls.FEE_NAME});
    private Boolean[] column_AutuHeight = new Boolean[]{true, true, true, false};

    public Statements_Table_Model_Favorite() {


        addObservers();

    }

    // set class

    public Class<? extends Object> getColumnClass(int c) { // set column type
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

        if (transactions == null || row < 0 || transactions.size() <= row)
            return null;

        return transactions.get(row).getB();
    }

    @Override
    public String getColumnName(int index) {
        return Lang.getInstance().translate(columnNames[index]);
    }

    public String getColumnNameNO_Translate(int index) {
        return columnNames[index];
    }

    @Override
    public int getRowCount() {
        // TODO Auto-generated method stub
        if (transactions == null) return 0;
        return transactions.size();
    }

    @Override
    public Object getValueAt(int row, int column) {
        // TODO Auto-generated method stub
        //	try {
        if (this.transactions == null || this.transactions.size() - 1 < row) {
            return null;
        }

        R_SignNote record = (R_SignNote) this.transactions.get(row).getB();

        switch (column) {
            case COLUMN_TIMESTAMP:

                return record.viewTimestamp();

            //	case COLUMN_TEMPLATE:

            //	return ItemCls.getItem(DLSet.getInstance(), ItemCls.TEMPLATE_TYPE, record.getKey()).toString();

            case COLUMN_BODY:

                if (record.getData() == null)
                    return "";

                if (record.getVersion() == 2) {
                    Tuple3<String, String, JSONObject> a;
                    try {
                        a = record.parse_Data_V2_Without_Files();

                        return a.b;
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }


                String str = "";
                try {
                    JSONObject data = (JSONObject) JSONValue
                            .parseWithException(new String(record.getData(), Charset.forName("UTF-8")));
                    str = (String) data.get("!!&_Title");
                    if (str == null)
                        str = (String) data.get("Title");
                } catch (Exception e) {
                    // TODO Auto-generated catch block

                    str = new String(record.getData(), Charset.forName("UTF-8"));
                }
                if (str == null)
                    return "";
                if (str.length() > 50)
                    return str.substring(0, 50) + "...";
                return str;// transaction.viewReference();//.viewProperies();
            case COLUMN_CREATOR:

                return record.getCreator().getPersonAsString();
            case COLUMN_FAVORITE:
                return record.isFavorite();
        }

        return null;

        //} catch (Exception e) {
        // LOGGER.error(e.getMessage(),e);
        //	return null;
        //}
    }

    @Override
    public void update(Observable o, Object arg) {
        try {
            this.syncUpdate(o, arg);
        } catch (Exception e) {
            // GUI ERROR
        }
    }

    public synchronized void syncUpdate(Observable o, Object arg) {
        ObserverMessage message = (ObserverMessage) arg;
        // System.out.println( message.getType());

        // CHECK IF NEW LIST
        if (message.getType() == ObserverMessage.LIST_STATEMENT_FAVORITES_TYPE) {
            if (this.transactions == null) {
                transactions = (SortableList<Tuple2<String, String>, Transaction>) message.getValue();
                transactions.registerObserver();
                SortableList<Tuple2<String, String>, Transaction> sss = Controller.getInstance().wallet.database.getDocumentFavoritesSet().getList();
                this.fireTableDataChanged();
            }


        }

        // CHECK IF LIST UPDATED
        if (message.getType() == ObserverMessage.ADD_STATEMENT_FAVORITES_TYPE) {
            //		transactions.add( (Pair<Tuple2<String, String>, Transaction>) message.getValue());
            this.fireTableDataChanged();
        }

        if (message.getType() == ObserverMessage.DELETE_STATEMENT_FAVORITES_TYPE) {
            Object mm = message.getValue();
            Transaction ss = (Transaction) message.getValue();
            //	transactions.remove(ss);


            this.fireTableDataChanged();
        }

    }

    private List<Transaction> read_Statement_old() {
        List<Transaction> tran;
        ArrayList<Transaction> db_transactions;
        db_transactions = new ArrayList<Transaction>();
        tran = new ArrayList<Transaction>();
        // база данных
        DCSet dcSet = DCSet.getInstance();
        // читаем все блоки
        SortableList<Integer, Block> lists = dcSet.getBlockMap().getList();
        // проходим по блокам
        for (Pair<Integer, Block> list : lists) {

            // читаем транзакции из блока
            db_transactions = (ArrayList<Transaction>) list.getB().getTransactions();
            // проходим по транзакциям
            for (Transaction transaction : db_transactions) {
                // если ноте то пишем в transactions
                if (transaction.getType() == Transaction.SIGN_NOTE_TRANSACTION)
                    transaction.setDC(dcSet);
                    tran.add(transaction);

            }

        }
        return tran;

    }

    public void removeObservers() {

        if (Controller.getInstance().doesWalletDatabaseExists()) {
            Controller.getInstance().wallet.database.getDocumentFavoritesSet().deleteObserver(this);
            transactions.removeObserver();
        }
    }

    public void addObservers() {
        if (Controller.getInstance().doesWalletDatabaseExists())
            Controller.getInstance().wallet.database.getDocumentFavoritesSet().addObserver(this);
    }

}
