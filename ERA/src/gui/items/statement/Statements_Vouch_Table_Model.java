package gui.items.statement;

import core.account.Account;
import core.account.PublicKeyAccount;
import core.transaction.R_Vouch;
import core.transaction.Transaction;
import datachain.DCSet;
import lang.Lang;
import org.apache.log4j.Logger;
import org.mapdb.Fun.Tuple2;
import utils.DateTimeFormat;
import utils.ObserverMessage;

import javax.swing.table.AbstractTableModel;
import javax.validation.constraints.Null;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

public class Statements_Vouch_Table_Model extends AbstractTableModel implements Observer {

    public static final int COLUMN_TIMESTAMP = 0;
    public static final int COLUMN_CREATOR = 1;
    public static final int COLUMN_HEIGHT = 2;
    public static final int COLUMN_CREATOR_NAME = 30;
    private static final long serialVersionUID = 1L;
    /**
     *
     */
    static Logger LOGGER = Logger.getLogger(Statements_Vouch_Table_Model.class.getName());
    List<R_Vouch> transactions;
    private String[] columnNames = Lang.getInstance().translate(new String[]{"Timestamp", "Creator", "Height"});
    private int blockNo;
    private int recNo;

    public Statements_Vouch_Table_Model(Transaction transaction) {
        if (transaction != null) {
            blockNo = transaction.getBlockHeight(DCSet.getInstance());
            recNo = transaction.getSeqNo(DCSet.getInstance());
        }
        transactions = new ArrayList<R_Vouch>();
        addObservers();


    }

    public Class<? extends Object> getColumnClass(int c) { // set column type
        Object o = getValueAt(0, c);
        return o == null ? Null.class : o.getClass();
    }

    @Override
    public int getColumnCount() {
        // TODO Auto-generated method stub
        return this.columnNames.length;
    }

    @Override
    public String getColumnName(int index) {
        return this.columnNames[index];
    }

    public PublicKeyAccount get_Public_Account(int row) {
        R_Vouch transaction = this.transactions.get(row);
        return transaction.getCreator();

    }

    @Override
    public int getRowCount() {
        // TODO Auto-generated method stub
        if (transactions == null)
            return 0;

        return transactions.size();
    }

    public String get_No_Trancaction(int row) {

        if (this.transactions == null || this.transactions.size() <= row) {
            return null;
        }

        Transaction transaction = this.transactions.get(row);
        if (transaction == null)
            return null;

        return transaction.viewHeightSeq(DCSet.getInstance());

    }

    public Transaction getTrancaction(int row) {

        if (this.transactions == null || this.transactions.size() <= row) {
            return null;
        }

        return this.transactions.get(row);

    }

    @Override
    public Object getValueAt(int row, int column) {
        // TODO Auto-generated method stub
        try {
            if (this.transactions == null || this.transactions.size() <= row) {
                return null;
            }

            R_Vouch transaction = this.transactions.get(row);
            if (transaction == null)
                return null;

            switch (column) {
                case COLUMN_TIMESTAMP:

                    return DateTimeFormat.timestamptoString(transaction.getTimestamp());

                case COLUMN_CREATOR:

                    return transaction.getCreator().getPersonAsString();

                case COLUMN_HEIGHT:

                    return (int) (transaction.getBlockHeight(DCSet.getInstance()));

                case COLUMN_CREATOR_NAME:
                    return ((Account) transaction.getCreator()).getPerson().b.getName();
            }

            return null;

        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return null;
        }
    }

    @Override
    public void update(Observable o, Object arg) {
        try {
            this.syncUpdate(o, arg);
        } catch (Exception e) {
            // GUI ERROR
            LOGGER.error(e.getMessage(), e);
        }
    }

    public synchronized void syncUpdate(Observable o, Object arg) {
        ObserverMessage message = (ObserverMessage) arg;

        if (message.getType() == ObserverMessage.LIST_VOUCH_TYPE || message.getType() == ObserverMessage.ADD_VOUCH_TYPE || message.getType() == ObserverMessage.REMOVE_VOUCH_TYPE) {
            // read indexes to DB
            Tuple2<BigDecimal, List<Tuple2<Integer, Integer>>> vouches = DCSet.getInstance().getVouchRecordMap().get(new Tuple2<Integer, Integer>(this.blockNo, this.recNo));
            if (vouches == null) {
                fireTableDataChanged();
                return;

            }

            List<Tuple2<Integer, Integer>> ttxs = DCSet.getInstance().getVouchRecordMap().get(new Tuple2<Integer, Integer>(this.blockNo, this.recNo)).b;
            transactions.clear();
            for (Tuple2<Integer, Integer> ttx : ttxs) {
                // write R-Vouch transaction
                transactions.add((R_Vouch) DCSet.getInstance().getTransactionFinalMap().getTransaction(ttx.a, ttx.b));

            }
            fireTableDataChanged();
        }
    }

    public void addObservers() {
        DCSet.getInstance().getVouchRecordMap().addObserver(this);
    }

    public void removeObservers() {
        DCSet.getInstance().getVouchRecordMap().deleteObserver(this);
    }


}
