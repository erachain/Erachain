package gui.models;

import controller.Controller;
import core.account.Account;
import datachain.SortableList;
import lang.Lang;
import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple5;
import utils.ObserverMessage;
import utils.Pair;

import javax.swing.table.AbstractTableModel;
import javax.validation.constraints.Null;
import java.math.BigDecimal;
import java.util.Observable;
import java.util.Observer;

@SuppressWarnings("serial")
public class BalancesTableModel extends AbstractTableModel implements Observer {
    public static final int COLUMN_BALANCE = 1;
    public static final int COLUMN_OWN = 2;
    private static final int COLUMN_ADDRESS = 0;
    private long key;
    private String[] columnNames = Lang.getInstance().translate(new String[]{"Account", "Balance", "in OWN"});
    private Boolean[] column_AutuHeight = new Boolean[]{true, false};
    private SortableList<Tuple2<String, Long>, Tuple5<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>> balances;

    public BalancesTableModel(long key) {
        this.key = key;
        Controller.getInstance().addObserver(this);
        this.balances = Controller.getInstance().getBalances(key);
        this.balances.registerObserver();
    }


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
        return columnNames.length;
    }

    @Override
    public String getColumnName(int index) {
        return columnNames[index];
    }

    @Override
    public int getRowCount() {
        return this.balances.size();
    }

    @Override
    public Object getValueAt(int row, int column) {
        if (this.balances == null || row > this.balances.size() - 1) {
            return null;
        }

        Pair<Tuple2<String, Long>, Tuple5<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>> aRow = this.balances.get(row);
        Account account = new Account(aRow.getA().a);

        switch (column) {
            case COLUMN_ADDRESS:

                return account.getPersonAsString();

            case COLUMN_BALANCE:

                return account.getBalanceUSE(this.key);  // NumberAsString.getInstance().numberAsString(account.getBalanceUSE(this.key));

            case COLUMN_OWN:

                Tuple5<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>> val = account.getBalance(this.key);
                return val.a; // NumberAsString.getInstance().numberAsString(val.a);
        }

        return null;
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

        //CHECK IF LIST UPDATED
        if ((message.getType() == ObserverMessage.NETWORK_STATUS && (int) message.getValue() == Controller.STATUS_OK)
                || (Controller.getInstance().getStatus() == Controller.STATUS_OK &&
                (message.getType() == ObserverMessage.ADD_BALANCE_TYPE || message.getType() == ObserverMessage.REMOVE_BALANCE_TYPE))) {
            this.fireTableDataChanged();
        }
    }

    public void removeObservers() {
        this.balances.removeObserver();
        Controller.getInstance().deleteObserver(this);
    }
}
