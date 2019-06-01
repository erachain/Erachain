package org.erachain.gui.models;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javax.swing.table.AbstractTableModel;
import javax.validation.constraints.Null;

import org.erachain.gui.ObserverWaiter;
import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple5;

import org.erachain.controller.Controller;
import org.erachain.core.account.Account;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.transaction.Transaction;
import org.erachain.datachain.DCSet;
import org.erachain.lang.Lang;
import org.erachain.utils.NumberAsString;
import org.erachain.utils.ObserverMessage;

@SuppressWarnings("serial")
public class AccountsTableModel extends TimerTableModelCls<PublicKeyAccount> implements ObserverWaiter {
    public static final int COLUMN_ADDRESS = 1;
    public static final int COLUMN_NAME = 2;
    public static final int COLUMN_BALANCE_1 = 3;
    public static final int COLUMN_BALANCE_2 = 4;
    public static final int COLUMN_BALANCE_3 = 5;
    public static final int COLUMN_BALANCE_4 = 6;
    public static final int COLUMN_FEE_BALANCE = 7;
    public final int COLUMN_NO = 0;
    private AssetCls asset;
    private Account account;

    public AccountsTableModel() {
        super(Controller.getInstance().wallet.database.getAccountMap(),
                new String[]{"No.", "Account", "Name",
                        "Balance 1", "Balance 2", "Balance 3", "Balance 4", AssetCls.FEE_NAME},
                new Boolean[]{true, false, false, false, false, false, false, false}, false);

        getInterval();
        fireTableDataChanged();
        needUpdate = false;

        // переиницализация после установуи таблиц
        this.addObservers();

    }

    public void setAsset(AssetCls asset) {
        this.asset = asset;
        this.fireTableDataChanged();
    }

    @Override
    public Object getValueAt(int row, int column) {
        if (this.list == null || row > this.list.size() - 1) {
            return null;
        }

        account = list.get(row);


        Tuple5<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>> balance;
        //Tuple3<BigDecimal, BigDecimal, BigDecimal> unconfBalance;
        String str;

        switch (column) {
            case COLUMN_NO:
                return account.getAccountNo();

            case COLUMN_ADDRESS:
                return account.getPersonAsString();

            case COLUMN_NAME:
                Tuple2<String, String> aa = account.getName();
                if (aa == null) return "";
                return aa.a;

            case COLUMN_BALANCE_1:
                if (this.asset == null) return "-";
                balance = account.getBalance(this.asset.getKey(DCSet.getInstance()));
                str = NumberAsString.formatAsString(balance.a.b.setScale(asset.getScale()));
                return str;
            case COLUMN_BALANCE_2:
                if (this.asset == null) return "-";
                balance = account.getBalance(this.asset.getKey(DCSet.getInstance()));
                str = NumberAsString.formatAsString(balance.b.b.setScale(asset.getScale()));
                return str;
            case COLUMN_BALANCE_3:
                if (this.asset == null) return "-";
                balance = account.getBalance(this.asset.getKey(DCSet.getInstance()));
                str = NumberAsString.formatAsString(balance.c.b.setScale(asset.getScale()));
                return str;
            case COLUMN_BALANCE_4:
                if (this.asset == null) return "-";
                balance = account.getBalance(this.asset.getKey(DCSet.getInstance()));
                str = NumberAsString.formatAsString(balance.d.b.setScale(asset.getScale()));
                return str;
            case COLUMN_FEE_BALANCE:
                return NumberAsString.formatAsString(account.getBalance(Transaction.FEE_KEY).a.b.stripTrailingZeros());
        }

        return null;
    }

    public BigDecimal getTotalBalance() {
        BigDecimal totalBalance = BigDecimal.ZERO;

        for (Account account : this.list) {
            if (this.asset == null) {
                totalBalance = totalBalance.add(account.getBalanceUSE(Transaction.FEE_KEY));
            } else {
                totalBalance = totalBalance.add(account.getBalanceUSE(this.asset.getKey(DCSet.getInstance())));
            }
        }

        return totalBalance;
    }

    @Override
    public void getInterval() {

        list = Controller.getInstance().wallet.getPublicKeyAccounts();

    }

    public void addObservers() {

        super.addObservers();
        if (Controller.getInstance().doesWalletDatabaseExists()) {
            map.addObserver(this);
        } else {
            // ожидаем открытия кошелька
            Controller.getInstance().wallet.addWaitingObserver(this);
        }
    }

    public void deleteObservers() {

        super.deleteObservers();
        if (Controller.getInstance().doesWalletDatabaseExists())
            map.deleteObserver(this);
    }

}
