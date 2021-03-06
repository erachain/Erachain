package org.erachain.gui.models;

import org.erachain.controller.Controller;
import org.erachain.core.account.Account;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.datachain.ItemAssetBalanceMap;
import org.erachain.lang.Lang;
import org.erachain.utils.ObserverMessage;
import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple5;

import javax.swing.table.AbstractTableModel;
import javax.validation.constraints.Null;
import java.math.BigDecimal;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

@SuppressWarnings("serial")
public class BalancesTableModel extends AbstractTableModel implements Observer {
    private static final int COLUMN_ADDRESS = 0;
    public static final int COLUMN_OWN = 1;
    public static final int COLUMN_DEBT = 2;
    public static final int COLUMN_HOLD = 3;
    public static final int COLUMN_SPEND = 4;
    private AssetCls asset;
    private int balanceIndex;
    private long key;
    private int scale;
    private String[] columnNames = Lang.T(new String[]{"Account", "OWN (1)", "DEBT (2)", "HOLD (3)", "SPEND (4)"});
    private Boolean[] column_AutuHeight = new Boolean[]{true, false};
    private List<Tuple2<byte[], Tuple5<
            Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>,
            Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>>> balances;

    public BalancesTableModel(AssetCls asset, int balanceIndex) {
        this.asset = asset;
        this.balanceIndex = balanceIndex;
        this.key = asset.getKey();
        this.scale = asset.getScale();
        Controller.getInstance().addObserver(this);
        this.balances = Controller.getInstance().getBalances(key);
        //this.balances.registerObserver();
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

        Tuple2<byte[], Tuple5<
                Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>,
                Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>>
                aRow = this.balances.get(row);
        Account account = new Account(ItemAssetBalanceMap.getShortAccountFromKey(aRow.a));

        switch (column) {
            case COLUMN_ADDRESS:

                return account.getPersonAsString();

            case COLUMN_OWN:

                Tuple5<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>
                        balance = account.getBalance(this.key);
                return (balance.a.b.setScale(scale));

            case COLUMN_DEBT:

                balance = account.getBalance(this.key);
                return (balance.b.b.setScale(scale));

            case COLUMN_HOLD:

                balance = account.getBalance(this.key);
                return (balance.c.b.setScale(scale));

            case COLUMN_SPEND:

                balance = account.getBalance(this.key);
                return (balance.d.b.setScale(scale));

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
                || (false && Controller.getInstance().getStatus() == Controller.STATUS_OK
                        && (message.getType() == ObserverMessage.ADD_BALANCE_TYPE || message.getType() == ObserverMessage.REMOVE_BALANCE_TYPE))
        ) {
            this.fireTableDataChanged();
        }
    }
    
    public void removeObservers() {
        //this.balances.removeObserver();
        Controller.getInstance().deleteObserver(this);
    }
}
