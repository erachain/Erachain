package org.erachain.gui.models;

import org.erachain.controller.Controller;
import org.erachain.core.account.Account;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.datachain.ItemAssetBalanceMap;
import org.erachain.lang.Lang;
import org.erachain.utils.ObserverMessage;
import org.erachain.utils.Pair;
import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple3;
import org.mapdb.Fun.Tuple5;

import javax.swing.table.AbstractTableModel;
import javax.validation.constraints.Null;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

@SuppressWarnings("serial")
public class BalanceFromAssets extends AbstractTableModel implements Observer {
    //private static final int COLUMN_ADDRESS = 0;
    public static final int COLUMN_BALANCE = 3;
    public static final int COLUMN_KEY = 2;
    public static final int COLUMN_ASSET_NAME = 1;
    public static final int COLUMN_ACCOUNT = 0;
    List<Account> accounts;
    Account account;
    //Pair<byte[], Tuple5<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>> balance;
    Object tab_Balances;
    Tuple2<Long, String> asset;
    private long key;
    private String[] columnNames = Lang.T(new String[]{"Account", "Asset", "key Asset", "Balance"});
    // balances;
    private List<Tuple2<byte[], Tuple5<
            Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>,
            Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>>> balances;
    private ArrayList<Pair<Account, Pair<Long, Tuple3<BigDecimal, BigDecimal, BigDecimal>>>> table_balance;

    @SuppressWarnings({"unchecked", "rawtypes"})
    public BalanceFromAssets() {


        //this.key = key;
        Controller.getInstance().addObserver(this);
        List<Account> accounts = Controller.getInstance().getWalletAccounts();
        //	 table_balance = new List();
        table_balance = new ArrayList<>();//Pair();

        //ItemAssetBalanceMap map = DCSet.getInstance().getAssetBalanceMap();
        for (int ia = 0; accounts.size() > ia; ia++) {
            account = accounts.get(ia);
            balances = Controller.getInstance().getBalances(account); //.getBalances(key);
            for (int ib = 0; this.balances.size() > ib; ib++) {
                Tuple2<byte[], Tuple5<
                        Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>,
                        Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>> item = this.balances.get(ib);
                long assetKey = ItemAssetBalanceMap.getAssetKeyFromKey(item.a);
                Tuple5 balance = item.b;
                table_balance.add(new Pair(account, new Pair(assetKey, balance)));
            }
        }
    }


    public Class<? extends Object> getColumnClass(int c) {     // set column type
        Object o = getValueAt(0, c);
        return o == null ? Null.class : o.getClass();
    }


    public AssetCls getAsset(int row) {
        return Controller.getInstance().getAsset(table_balance.get(row).getB().getA());
    }

    public String getAccount(int row) {
        // TODO Auto-generated method stub
        return table_balance.get(row).getA().getAddress();
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

        return table_balance.size();
    }

    @Override
    public Object getValueAt(int row, int column) {


        if (table_balance == null || row > table_balance.size() - 1) {
            return null;
        }

        AssetCls asset = Controller.getInstance().getAsset(table_balance.get(row).getB().getA());


        Pair<Tuple2<String, Long>, BigDecimal> sa;
        switch (column) {
            case COLUMN_KEY:

                return asset.getKey();

            case COLUMN_BALANCE:

                return table_balance.get(row).getB().getB().a;

            case COLUMN_ASSET_NAME:

                return asset.viewName();

            case COLUMN_ACCOUNT:
                return table_balance.get(row).getA().getAddress();
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
                || (Controller.getInstance().getStatus() == Controller.STATUS_OK
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
