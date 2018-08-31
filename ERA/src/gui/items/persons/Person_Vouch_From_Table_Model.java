package gui.items.persons;

import core.account.PublicKeyAccount;
import core.item.persons.PersonCls;
import core.transaction.R_SertifyPubKeys;
import core.transaction.Transaction;
import datachain.DCSet;
import datachain.SortableList;
import datachain.TransactionFinalMap;
import lang.Lang;
import org.apache.log4j.Logger;
import org.mapdb.Fun.Tuple2;
import utils.DateTimeFormat;
import utils.ObserverMessage;
import utils.Pair;

import javax.swing.table.AbstractTableModel;
import javax.validation.constraints.Null;
import java.util.*;

public class Person_Vouch_From_Table_Model extends AbstractTableModel implements Observer {

    public static final int COLUMN_TIMESTAMP = 0;
    public static final int COLUMN_CREATOR = 1;
    public static final int COLUMN_HEIGHT = 2;
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(Person_Vouch_From_Table_Model.class);
    List<R_SertifyPubKeys> transactions;
    PersonCls person;
    TransactionFinalMap table;
    private String[] columnNames = Lang.getInstance().translate(new String[]{"Timestamp", "Persons", "Height"});// ,
    private Boolean[] column_AutuHeight = new Boolean[]{true, true};
    private ObserverMessage message;
    private boolean fire;


    public Person_Vouch_From_Table_Model(PersonCls person) {
        fire = false;
        this.person = person;
        transactions = new ArrayList<R_SertifyPubKeys>();
        addObservers();
    }

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

    @Override
    public String getColumnName(int index) {
        return this.columnNames[index];
    }

    @Override
    public int getRowCount() {
        // TODO Auto-generated method stub
        if (transactions == null)
            return 0;

        return transactions.size();
    }

    public PublicKeyAccount get_Public_Account(int row) {
        R_SertifyPubKeys transaction = this.transactions.get(row);
        return transaction.getSertifiedPublicKeys().get(0);

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
            if (this.transactions == null || this.transactions.isEmpty()) return null;

            R_SertifyPubKeys transaction = this.transactions.get(row);
            if (transaction == null)
                return null;

            // R_Vouch i;
            switch (column) {
                case COLUMN_TIMESTAMP:


                    return DateTimeFormat.timestamptoString(transaction.getTimestamp());//.viewTimestamp(); // + " " +

                case COLUMN_CREATOR:

                    return ((R_SertifyPubKeys) transaction).getSertifiedPublicKeys().get(0).getPersonAsString();

                case COLUMN_HEIGHT:

                    return (int) (transaction.getBlockHeight());


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
        message = (ObserverMessage) arg;


        if (message.getType() == ObserverMessage.LIST_TRANSACTION_TYPE) {
            //CHECK IF NEW LIST

            @SuppressWarnings("unchecked")
            SortableList<byte[], Transaction> ss = (SortableList<byte[], Transaction>) message.getValue();
            Iterator<Pair<byte[], Transaction>> s = ss.iterator();
            if (this.transactions.isEmpty()) {

                while (s.hasNext()) {
                    Pair<byte[], Transaction> a = s.next();
                    Transaction t = a.getB();
                    if (t.getType() == Transaction.CERTIFY_PUB_KEYS_TRANSACTION) {
                        R_SertifyPubKeys tt = (R_SertifyPubKeys) t;
                        Tuple2<Integer, PersonCls> personRes = tt.getCreator().getPerson();
                        if (personRes != null && personRes.b.getKey() == person.getKey()) {
                            if (!this.transactions.contains(tt)) {
                                this.transactions.add(tt);
                                fire = true;
                            }
                        }
                    }
                }

                if (!fire)
                    this.fireTableDataChanged();
                fire = true;
            }
        } else if (message.getType() == ObserverMessage.ADD_TRANSACTION_TYPE) {
            Transaction ss = (Transaction) message.getValue();
            if (ss.getType() == Transaction.CERTIFY_PUB_KEYS_TRANSACTION) {
                R_SertifyPubKeys ss1 = (R_SertifyPubKeys) ss;
                Tuple2<Integer, PersonCls> personRes = ss1.getCreator().getPerson();
                if (personRes != null && personRes.b.getKey() == person.getKey()) {
                    if (!this.transactions.contains(ss1)) {
                        this.transactions.add(ss1);
                        this.fireTableDataChanged();
                    }
                }
            }
        } else if (message.getType() == ObserverMessage.REMOVE_TRANSACTION_TYPE) {
            Transaction ss = (Transaction) message.getValue();
            if (ss.getType() == Transaction.CERTIFY_PUB_KEYS_TRANSACTION) {
                R_SertifyPubKeys ss1 = (R_SertifyPubKeys) ss;
                Tuple2<Integer, PersonCls> personRes = ss1.getCreator().getPerson();
                if (personRes != null && personRes.b.getKey() == person.getKey()) {
                    if (this.transactions.contains(ss1)) {
                        this.transactions.remove(ss1);
                        this.fireTableDataChanged();
                    }
                }
            }

        }

    }

    public void addObservers() {
        DCSet.getInstance().getTransactionFinalMap().addObserver(this);
    }

    public void removeObservers() {
        DCSet.getInstance().getTransactionFinalMap().deleteObserver(this);
    }

}
