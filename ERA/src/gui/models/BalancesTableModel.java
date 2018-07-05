package gui.models;

import java.math.BigDecimal;
import java.util.Observable;
import java.util.Observer;

import javax.swing.table.AbstractTableModel;
import javax.validation.constraints.Null;

import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple5;

import controller.Controller;
import core.account.Account;
import core.item.assets.AssetCls;
import datachain.SortableList;
import lang.Lang;
import utils.NumberAsString;
import utils.ObserverMessage;
import utils.Pair;

@SuppressWarnings("serial")
public class BalancesTableModel extends AbstractTableModel implements Observer {
    private static final int COLUMN_ADDRESS = 0;
    public static final int COLUMN_OWN = 1;
    public static final int COLUMN_DEBT = 2;
    public static final int COLUMN_USE = 3;
    public static final int COLUMN_HAND = 4;
    private AssetCls asset;
    private int balanceIndex;
    private long key;
    private int scale;
    private String[] columnNames = Lang.getInstance().translate(new String[] { "Account", "in OWN", "in DEBT", "in USE", "on HAND" });
    private Boolean[] column_AutuHeight = new Boolean[] { true, false };
    private SortableList<Tuple2<String, Long>, Tuple5<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>> balances;
    
    public BalancesTableModel(AssetCls asset, int balanceIndex) {
        this.asset = asset;
        this.balanceIndex = balanceIndex;
        this.key = asset.getKey();
        this.scale = asset.getScale();
        Controller.getInstance().addObserver(this);
        this.balances = Controller.getInstance().getBalances(key);
        this.balances.registerObserver();
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
        
        Pair<Tuple2<String, Long>, Tuple5<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>> aRow = this.balances
                .get(row);
        Account account = new Account(aRow.getA().a);
        
        switch (column) {
            case COLUMN_ADDRESS:
                
                return account.getPersonAsString();

            case COLUMN_OWN:
                
                Tuple5<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>> balance = account
                        .getBalance(this.key);
                return (balance.a.b.setScale(scale));

            case COLUMN_DEBT:
                
                balance = account.getBalance(this.key);
                return (balance.b.b.setScale(scale));

            case COLUMN_USE:
                
                return (account.getBalanceUSE(this.key));

            case COLUMN_HAND:
                
                balance = account.getBalance(this.key);
                return (balance.c.b.setScale(scale));

                /*
            case COLUMN_OWN:
                
                Tuple5<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>> balance = account
                        .getBalance(this.key);
                switch (this.balanceIndex) {
                    case 1:
                        return NumberAsString.formatAsString(balance.a.b.setScale(scale));
                    case 2:
                        return NumberAsString.formatAsString(balance.b.b.setScale(scale));
                    case 3:
                        return NumberAsString.formatAsString(balance.c.b.setScale(scale));
                    case 4:
                        return NumberAsString.formatAsString(balance.d.b.setScale(scale));
                    case 5:
                        return NumberAsString.formatAsString(balance.e.b.setScale(scale));
                    default:
                        return NumberAsString.formatAsString(balance.a.b.setScale(scale)) + " / "
                                + NumberAsString.formatAsString(balance.b.b.setScale(scale)) + " / "
                                + NumberAsString.formatAsString(balance.c.b.setScale(scale));
                }
                */
        }
        
        return null;
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
        
        // CHECK IF LIST UPDATED
        if ((message.getType() == ObserverMessage.NETWORK_STATUS && (int) message.getValue() == Controller.STATUS_OK)
                || (Controller.getInstance().getStatus() == Controller.STATUS_OK
                        && (message.getType() == ObserverMessage.ADD_BALANCE_TYPE
                                || message.getType() == ObserverMessage.REMOVE_BALANCE_TYPE))) {
            this.fireTableDataChanged();
        }
    }
    
    public void removeObservers() {
        this.balances.removeObserver();
        Controller.getInstance().deleteObserver(this);
    }
}
