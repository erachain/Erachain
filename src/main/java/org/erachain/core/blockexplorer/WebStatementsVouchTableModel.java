package org.erachain.core.blockexplorer;

import org.erachain.core.transaction.R_Vouch;
import org.erachain.core.transaction.Transaction;
import org.erachain.database.SortableList;
import org.erachain.datachain.DCSet;
import org.erachain.datachain.TransactionFinalMap;
import org.erachain.lang.Lang;
import org.erachain.utils.ObserverMessage;
import org.erachain.utils.Pair;
import org.mapdb.Fun.Tuple2;

import javax.swing.table.AbstractTableModel;
import java.math.BigDecimal;
import java.util.*;

//import org.erachain.core.transaction.R_SignStatement_old;

public class WebStatementsVouchTableModel extends AbstractTableModel implements Observer {

    public static final int COLUMN_TIMESTAMP = 0;
    // public static final int COLUMN_TYPE = 1;
    public static final int COLUMN_CREATOR = 1;
    public static final int COLUMN_CREATOR_ADDRESS = 2;
    public static final int COLUMN_TRANSACTION = 3;
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    // public static final int COLUMN_FEE = 3;
    List<Transaction> transactions;

    // private SortableList<byte[], Transaction> transactions;
    TransactionFinalMap table;
    private String[] columnNames = Lang.getInstance().translate(new String[]{"Timestamp", "Creator", "Address_Creator", "Transaction"});// ,
    // AssetCls.FEE_NAME});
    private Boolean[] column_AutuHeight = new Boolean[]{true, true};
    // private Map<byte[], BlockingQueue<Block>> blocks;
    // private Transaction transaction;
    private int blockNo;
    private int recNo;
    private ObserverMessage message;

    private String sss;

    public WebStatementsVouchTableModel(Transaction transaction) {
        table = DCSet.getInstance().getTransactionFinalMap();
        blockNo = transaction.getBlockHeight();
        recNo = transaction.getSeqNo();
        transactions = new ArrayList<Transaction>();
        // transactions = read_Sign_Accoutns();
        //	DCSet.getInstance().getTransactionFinalMap().addObserver(this);
        DCSet.getInstance().getVouchRecordMap().addObserver(this);

    }

    public Class<? extends Object> getColumnClass(int c) { // set column type
        Object o = getValueAt(0, c);
        return o == null ? null : o.getClass();
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

    @Override
    public String getColumnName(int index) {
        return this.columnNames[index];
    }

    @Override
    public int getRowCount() {
        // TODO Auto-generated method stub
        if (transactions == null)
            return 0;
        int c = 0;
        for (Transaction a : this.transactions) {
            if (a != null)
                ++c;
        }
        return c; // transactions.size();
    }

    @Override
    public Object getValueAt(int row, int column) {
        // TODO Auto-generated method stub
        try {
            if (this.transactions == null || this.transactions.size() - 1 < row) {
                return null;
            }

            Transaction transaction = this.transactions.get(row);

            // R_Vouch i;
            switch (column) {
                case COLUMN_TIMESTAMP:

                    // return
                    // DateTimeFormat.timestamptoString(transaction.getTimestamp())
                    // + " " + transaction.getTimestamp();
                    return transaction.viewTimestamp(); // + " " +
                // transaction.getTimestamp()
                // / 1000;

                case COLUMN_CREATOR_ADDRESS:


                    return transaction.getCreator().getAddress().toString();
                case COLUMN_CREATOR:

                    return transaction.getCreator().getPerson().b;

                case COLUMN_TRANSACTION:

                    return transaction;

            }

            return null;

        } catch (Exception e) {
            // LOGGER.error(e.getMessage(),e);
            return null;
        }
    }

    @Override
    public void update(Observable o, Object arg) {
        // try
        // {
        this.syncUpdate(o, arg);
        // }
        // catch(Exception e)
        // {
        // GUI ERROR
        // }
    }

    public synchronized void syncUpdate(Observable o, Object arg) {
        message = (ObserverMessage) arg;
        // System.out.println( message.getType());

        // CHECK IF NEW LIST
        if (message.getType() == ObserverMessage.LIST_VOUCH_TYPE) {
            if (this.transactions.isEmpty()) {
                transactions = read_Sign_Accoutns();
                this.fireTableDataChanged();
            }

        }

        // CHECK IF LIST UPDATED
        if (message.getType() == ObserverMessage.ADD_TRANSACTION_TYPE
            // || message.getType() == ObserverMessage.REMOVE_VOUCH_TYPE
            // || message.getType() == ObserverMessage.LIST_STATEMENT_TYPE
            // || message.getType() == ObserverMessage.REMOVE_STATEMENT_TYPE

                ) {
            Transaction ss = (Transaction) message.getValue();
            if (ss.getType() == Transaction.VOUCH_TRANSACTION) {
                R_Vouch ss1 = (R_Vouch) ss;
                if (ss1.getVouchHeight() == blockNo
                        && ss1.getVouchSeqNo() == recNo
                        )

                    if (!this.transactions.contains(ss)) {
                        this.transactions.add(ss);
                        this.fireTableDataChanged();
                    }
            }


        }
    }

    private List<Transaction> read_Sign_Accoutns() {
        List<Transaction> tran = new ArrayList<Transaction>();
        // ArrayList<Transaction> db_transactions;
        // db_transactions = new ArrayList<Transaction>();
        // tran = new ArrayList<Transaction>();
        // база данных
        // DLSet dcSet = DLSet.getInstance();

        /*
         * Tuple2<BigDecimal, List<Tuple2<Integer, Integer>>> signs =
         * DLSet.getInstance().getVouchRecordMap().get(blockNo, recNo);
         *
         *
         * if (signs == null) return null; for(Tuple2<Integer, Integer> seq:
         * signs.b) {
         *
         * Transaction kk = table.get(seq.a, seq.b); if
         * (!tran.contains(kk)) tran.add(kk); }
         */

        @SuppressWarnings("unchecked")
        SortableList<Tuple2<Integer, Integer>, Tuple2<BigDecimal, List<Tuple2<Integer, Integer>>>> rec = (SortableList<Tuple2<Integer, Integer>, Tuple2<BigDecimal, List<Tuple2<Integer, Integer>>>>) message
                .getValue();

        Iterator<Pair<Tuple2<Integer, Integer>, Tuple2<BigDecimal, List<Tuple2<Integer, Integer>>>>> ss = rec
                .iterator();
        while (ss.hasNext()) {
            Pair<Tuple2<Integer, Integer>, Tuple2<BigDecimal, List<Tuple2<Integer, Integer>>>> a = (Pair<Tuple2<Integer, Integer>, Tuple2<BigDecimal, List<Tuple2<Integer, Integer>>>>) ss
                    .next();
            // block
            if (a.getA().a == blockNo && a.getA().b == recNo) {
                List<Tuple2<Integer, Integer>> ff = a.getB().b;

                for (Tuple2<Integer, Integer> ll : ff) {
                    Integer bl = ll.a;
                    Integer seg = ll.b;

                    Transaction kk = table.get(bl, seg);
                    if (!tran.contains(kk))
                        tran.add(kk);
                }
            }

        }
        return tran;
    }

}
