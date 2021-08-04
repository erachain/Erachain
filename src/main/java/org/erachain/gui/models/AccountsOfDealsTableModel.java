package org.erachain.gui.models;

import org.erachain.controller.Controller;
import org.erachain.core.account.Account;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.transaction.Transaction;
import org.erachain.lang.Lang;
import org.erachain.utils.NumberAsString;
import org.erachain.utils.ObserverMessage;
import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple3;
import org.mapdb.Fun.Tuple5;

import javax.swing.table.AbstractTableModel;
import javax.validation.constraints.Null;
import java.math.BigDecimal;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

@SuppressWarnings("serial")
public class AccountsOfDealsTableModel extends AbstractTableModel implements Observer {
    //	public static final int COLUMN_BALANCE = 1;
    public static final int COLUMN_CONFIRMED_BALANCE = 1;
    public static final int COLUMN_WAINTING_BALANCE = 2;
    //public static final int COLUMN_GENERATING_BALANCE = 3;
    public static final int COLUMN_FEE_BALANCE = 3;
    private static final int COLUMN_ADDRESS = 0;
    int deal;
    private String[] columnNames = Lang.T(new String[]{"Account", "Confirmed Balance", "Waiting"});
    private Boolean[] column_AutuHeight = new Boolean[]{true, false, false, false};
    private List<PublicKeyAccount> publicKeyAccounts;
    private AssetCls asset;
    private Account account;

    public AccountsOfDealsTableModel(int deal) {
        this.publicKeyAccounts = Controller.getInstance().getWalletPublicKeyAccounts();
        Controller.getInstance().addWalletObserver(this);
        Controller.getInstance().addObserver(this);
        this.deal = deal;
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

    // Total
    public String get_Total(int key) {

        BigDecimal ss = new BigDecimal(0);
        for (PublicKeyAccount acc : this.publicKeyAccounts) {
            ss = ss.add(acc.getBalance(this.asset.getKey()).a.b);

        }

        //	balance = account.getBalance(this.asset.getKey(DLSet.getInstance()));

        return NumberAsString.formatAsString(ss);
    }

    // Total waiting

    public String get_Total_Waiting(int key) {
        BigDecimal ss = new BigDecimal(0);
        BigDecimal sW = new BigDecimal(0);
        for (PublicKeyAccount account : this.publicKeyAccounts) {
            ss = ss.add(account.getBalance(this.asset.getKey()).a.b);

            sW = sW.add(account.getUnconfirmedBalance(this.asset.getKey()).a);

        }

        //	balance = account.getBalance(this.asset.getKey(DLSet.getInstance()));

        return NumberAsString.formatAsString(ss.subtract(sW).stripTrailingZeros());
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

        return this.publicKeyAccounts.size() + 1;
    }

    @Override
    public Object getValueAt(int row, int column) {
        if (this.publicKeyAccounts == null) // || row > this.publicKeyAccounts.size() )
        {
            return null;
        }

        if (row == this.publicKeyAccounts.size()) {

            switch (column) {
                case COLUMN_ADDRESS:
                    return "<HTML><b>" + Lang.T("Total") + ":";
                case COLUMN_CONFIRMED_BALANCE:

                    return "<HTML><b>" + get_Total(deal);
                case COLUMN_WAINTING_BALANCE:

                    return "<HTML><b>" + get_Total_Waiting(deal);
                case COLUMN_FEE_BALANCE:

                    return "11";

            }
            return null;
        }


        account = this.publicKeyAccounts.get(row);

        Tuple5<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>> balance;
        Tuple3<BigDecimal, BigDecimal, BigDecimal> unconfBalance;
        String str;

        switch (column) {
            case COLUMN_ADDRESS:
                return account.getPersonAsString();
            case COLUMN_CONFIRMED_BALANCE:
                if (this.asset == null) return "-";
                balance = account.getBalance(this.asset.getKey());
                str = NumberAsString.formatAsString(balance.a.b.setScale(asset.getScale()));
                return str;
            case COLUMN_WAINTING_BALANCE:
                if (this.asset == null) return "-";
                balance = account.getBalance(this.asset.getKey());
                unconfBalance = account.getUnconfirmedBalance(this.asset.getKey());
                str = NumberAsString.formatAsString(unconfBalance.a.subtract(balance.a.b).setScale(asset.getScale()));
                //	+ "/" + unconfBalance.b.subtract(balance.b).toPlainString()
                //	+ "/" + unconfBalance.c.subtract(balance.c).toPlainString();
                return str;
            case COLUMN_FEE_BALANCE:
                if (this.asset == null) return "-";
                return NumberAsString.formatAsString(account.getBalanceUSE(Transaction.FEE_KEY));


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

        if (message.getType() == ObserverMessage.NETWORK_STATUS && (int) message.getValue() == Controller.STATUS_OK) {

            this.fireTableRowsUpdated(0, this.getRowCount() - 1);

        } else if (Controller.getInstance().getStatus() == Controller.STATUS_OK) {

            if (message.getType() == ObserverMessage.WALLET_ADD_BLOCK_TYPE || message.getType() == ObserverMessage.WALLET_REMOVE_BLOCK_TYPE
                    || message.getType() == ObserverMessage.WALLET_ADD_TRANSACTION_TYPE || message.getType() == ObserverMessage.WALLET_REMOVE_TRANSACTION_TYPE) {
                this.publicKeyAccounts = Controller.getInstance().getWalletPublicKeyAccounts();

                this.fireTableRowsUpdated(0, this.getRowCount() - 1);  // WHEN UPDATE DATA - SELECTION DOES NOT DISAPPEAR
            }

            if (message.getType() == ObserverMessage.ADD_ACCOUNT_TYPE || message.getType() == ObserverMessage.REMOVE_ACCOUNT_TYPE) {
                // обновляем данные
                this.publicKeyAccounts = Controller.getInstance().getWalletPublicKeyAccounts();
                this.fireTableDataChanged();
            }
        }

    }

    public BigDecimal getTotalBalance() {
        BigDecimal totalBalance = BigDecimal.ZERO;

        for (Account account : this.publicKeyAccounts) {
            if (this.asset == null) {
                totalBalance = totalBalance.add(account.getBalanceUSE(Transaction.FEE_KEY));
            } else {
                totalBalance = totalBalance.add(account.getBalanceUSE(this.asset.getKey()));
            }
        }

        return totalBalance;
    }
}
