package org.erachain.gui.models;

import org.erachain.controller.Controller;
import org.erachain.core.account.Account;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.database.SortableList;
import org.erachain.lang.Lang;
import org.erachain.utils.NumberAsString;
import org.erachain.utils.ObserverMessage;
import org.erachain.utils.Pair;
import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple5;

import javax.swing.table.AbstractTableModel;
import javax.validation.constraints.Null;
import java.math.BigDecimal;
import java.util.*;

@SuppressWarnings("serial")
public class BalanceFromAddressTableModel extends AbstractTableModel implements Observer {
    public static final int COLUMN_C = 4;
    public static final int COLUMN_B = 3;
    public static final int COLUMN_A = 2;
    public static final int COLUMN_ASSET_NAME = 1;
    public static final int COLUMN_ASSET_KEY = 0;
    Account account;
    Pair<Tuple2<String, Long>, Tuple5<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>> balance;
    Tuple2<Long, String> asset;
    private String[] columnNames = Lang.getInstance()
            .translate(new String[]{"key Asset", "Asset", "Balance A", "Balance B", "Balance C"});
    // balances;
    private SortableList<Tuple2<String, Long>, Tuple5<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>> balances;
    private ArrayList<Pair<Account, Pair<Long, Tuple5<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>>>> tableBalance;
    private ArrayList<Pair<Account, Pair<Tuple2<String, Long>, Tuple5<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>>>> tableBalance1;

    @SuppressWarnings({"unchecked", "rawtypes"})
    public BalanceFromAddressTableModel() {
        Controller.getInstance().addObserver(this);
        List<Account> accounts = Controller.getInstance().getAccounts();
        tableBalance = new ArrayList<>();// Pair();
        tableBalance1 = new ArrayList<>();
        HashSet<Long> item;
        item = new HashSet();

        for (Account account1 : accounts) {
            account = account1;
            balances = Controller.getInstance().getBalances(account);
            for (Pair<Tuple2<String, Long>,
                    Tuple5<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>,
                            Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>> balance1 : this.balances) {
                balance = balance1;
                tableBalance1.add(new Pair(account, new Pair(balance.getA(), balance.getB())));
                item.add(balance.getA().b);
            }
        }

        for (Long i : item) {
            BigDecimal sumAA = new BigDecimal(0);
            BigDecimal sumBA = new BigDecimal(0);
            BigDecimal sumCA = new BigDecimal(0);
            BigDecimal sumDA = new BigDecimal(0);
            BigDecimal sumEA = new BigDecimal(0);
            BigDecimal sumAB = new BigDecimal(0);
            BigDecimal sumBB = new BigDecimal(0);
            BigDecimal sumCB = new BigDecimal(0);
            BigDecimal sumDB = new BigDecimal(0);
            BigDecimal sumEB = new BigDecimal(0);
            for (Pair<Account, Pair<Tuple2<String, Long>, Tuple5<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>>> k : this.tableBalance1) {
                if (k.getB().getA().b.equals(i)) {

                    sumAA = sumAA.add(k.getB().getB().a.a);
                    sumBA = sumBA.add(k.getB().getB().b.a);
                    sumCA = sumCA.add(k.getB().getB().c.a);
                    sumDA = sumDA.add(k.getB().getB().d.a);
                    sumEA = sumEA.add(k.getB().getB().e.a);

                    sumAB = sumAB.add(k.getB().getB().a.b);
                    sumBB = sumBB.add(k.getB().getB().b.b);
                    sumCB = sumCB.add(k.getB().getB().c.b);
                    sumDB = sumDB.add(k.getB().getB().d.b);
                    sumEB = sumEB.add(k.getB().getB().e.b);

                }

            }
            tableBalance.add(new Pair(account, new Pair(i, new Tuple5(
                    new Tuple2(sumAA, sumAB), new Tuple2(sumBA, sumBB), new Tuple2(sumCA, sumCB),
                    new Tuple2(sumDA, sumDB), new Tuple2(sumEA, sumEB)))));

        }

        balances.registerObserver();
    }

    public Class<?> getColumnClass(int c) { // set column type
        Object o = getValueAt(0, c);
        return o == null ? Null.class : o.getClass();
    }

    public AssetCls getAsset(int row) {
        return Controller.getInstance().getAsset(tableBalance.get(row).getB().getA());
    }

    public String getAccount(int row) {
        return tableBalance.get(row).getA().getAddress();
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

        return tableBalance.size();
    }

    @Override
    public Object getValueAt(int row, int column) {

        if (tableBalance == null || row > tableBalance.size() - 1) {
            return null;
        }

        AssetCls asset = Controller.getInstance().getAsset(tableBalance.get(row).getB().getA());

        switch (column) {
            case COLUMN_ASSET_KEY:
                return asset.getKey();
            case COLUMN_A:
                return NumberAsString.formatAsString(tableBalance.get(row).getB().getB().a.b);
            case COLUMN_B:
                return NumberAsString.formatAsString(tableBalance.get(row).getB().getB().b.b);
            case COLUMN_C:
                return NumberAsString.formatAsString(tableBalance.get(row).getB().getB().c.b);
            case COLUMN_ASSET_NAME:
                return asset.viewName();
        }
        return null;
    }

    @Override
    public void update(Observable o, Object arg) {
        try {
            this.syncUpdate(o, arg);
        } catch (Exception ignored) {
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
