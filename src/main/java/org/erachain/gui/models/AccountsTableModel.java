package org.erachain.gui.models;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javax.swing.table.AbstractTableModel;
import javax.validation.constraints.Null;

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
public class AccountsTableModel extends AbstractTableModel implements Observer {
    public static final int COLUMN_ADDRESS = 1;
    public static final int COLUMN_NAME = 2;
    public static final int COLUMN_CONFIRMED_BALANCE = 3;
    //	public static final int COLUMN_WAINTING_BALANCE = 2;
    //public static final int COLUMN_GENERATING_BALANCE = 3;
    public static final int COLUMN_FEE_BALANCE = 4;
    public final int COLUMN_NO = 0;
    private String[] columnNames = Lang.getInstance().translate(new String[]{"No.", "Account", "Name", "Confirmed Balance", AssetCls.FEE_NAME}); // "Waiting"
    private Boolean[] column_AutuHeight = new Boolean[]{true, false, false, false};
    private List<PublicKeyAccount> publicKeyAccounts;
    private AssetCls asset;
    private Account account;

    public AccountsTableModel() {
        if (Controller.getInstance().doesWalletDatabaseExists()) {
            this.publicKeyAccounts = Controller.getInstance().getPublicKeyAccounts();
            Controller.getInstance().wallet.database.getAccountMap().addObserver(this);
        } else {
            this.publicKeyAccounts = new ArrayList<>();
        }

    }


    @Override
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


    public Account getAccount(int row) {
        return publicKeyAccounts.get(row);
    }

    public PublicKeyAccount getPublicKeyAccount(int row) {
        return publicKeyAccounts.get(row);
    }

    public void setAsset(AssetCls asset) {
        this.asset = asset;
        this.fireTableDataChanged();
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

        return this.publicKeyAccounts.size();
    }

    @Override
    public Object getValueAt(int row, int column) {
        if (this.publicKeyAccounts == null || row > this.publicKeyAccounts.size() - 1) {
            return null;
        }

        account = this.publicKeyAccounts.get(row);


        Tuple5<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>> balance;
        //Tuple3<BigDecimal, BigDecimal, BigDecimal> unconfBalance;
        String str;

        switch (column) {
            case COLUMN_ADDRESS:
                return account.getPersonAsString();
            case COLUMN_NAME:
                Tuple2<String, String> aa = account.getName();
                if (aa == null) return "";
                return aa.a;
            case COLUMN_CONFIRMED_BALANCE:
                if (this.asset == null) return "-";
                balance = account.getBalance(this.asset.getKey(DCSet.getInstance()));
                str = NumberAsString.formatAsString(balance.a.b.setScale(asset.getScale()))
                        + " / " + NumberAsString.formatAsString(balance.b.b.setScale(asset.getScale()))
                        + " / " + NumberAsString.formatAsString(balance.c.b.setScale(asset.getScale()));
                return str;
			/*
		case COLUMN_WAINTING_BALANCE:
			if (this.asset == null) return "-";
			balance = account.getBalance(this.asset.getKey(DLSet.getInstance()));
			unconfBalance = account.getUnconfirmedBalance(this.asset.getKey(DLSet.getInstance()));
			str = NumberAsString.formatAsString(unconfBalance.a.subtract(balance.a))
					+ "/" + unconfBalance.b.subtract(balance.b).toPlainString()
					+ "/" + unconfBalance.c.subtract(balance.c).toPlainString();
			return str;
			 */
            case COLUMN_FEE_BALANCE:
                if (this.asset == null)
                    return "-";
                return NumberAsString.formatAsString(account.getBalanceUSE(Transaction.FEE_KEY).stripTrailingZeros());

            case COLUMN_NO:
                return account.getAccountNo();


			/*

		case COLUMN_GENERATING_BALANCE:

			if(this.asset == null || this.asset.getKey() == AssetCls.FEE_KEY)
			{
				return  NumberAsString.formatAsString(account.getGeneratingBalance());
			}
			else
			{
				return NumberAsString.formatAsString(BigDecimal.ZERO);
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
            //GUI ERROR
        }
    }

    public synchronized void syncUpdate(Observable o, Object arg) {
        ObserverMessage message = (ObserverMessage) arg;

        if (message.getType() == ObserverMessage.LIST_ALL_ACCOUNT_TYPE) {
            ;
        } else if (message.getType() == ObserverMessage.RESET_ALL_ACCOUNT_TYPE) {
            this.publicKeyAccounts = new ArrayList<>();
            this.fireTableDataChanged();

        } else if (message.getType() == ObserverMessage.ADD_ACCOUNT_TYPE) {
            this.publicKeyAccounts.add(((PublicKeyAccount) message.getValue()));
            this.fireTableDataChanged();
        } else if (message.getType() == ObserverMessage.REMOVE_ACCOUNT_TYPE) {
            this.publicKeyAccounts.remove((message.getValue()));
            this.fireTableDataChanged();
        }

    }

    public BigDecimal getTotalBalance() {
        BigDecimal totalBalance = BigDecimal.ZERO;

        for (Account account : this.publicKeyAccounts) {
            if (this.asset == null) {
                totalBalance = totalBalance.add(account.getBalanceUSE(Transaction.FEE_KEY));
            } else {
                totalBalance = totalBalance.add(account.getBalanceUSE(this.asset.getKey(DCSet.getInstance())));
            }
        }

        return totalBalance;
    }

    public void deleteObserver() {

        Controller.getInstance().wallet.database.getAccountMap().deleteObserver(this);

    }
}
