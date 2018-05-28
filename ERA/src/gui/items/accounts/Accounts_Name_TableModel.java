package gui.items.accounts;

import controller.Controller;
import core.account.Account;
import database.wallet.AccountsPropertisMap;
import datachain.SortableList;
import lang.Lang;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple3;
import org.mapdb.Fun.Tuple5;
import utils.ObserverMessage;
import utils.Pair;

import javax.swing.table.AbstractTableModel;
import javax.validation.constraints.Null;
import java.math.BigDecimal;
import java.util.Observable;
import java.util.Observer;

@SuppressWarnings("serial")
public class Accounts_Name_TableModel extends AbstractTableModel implements Observer {
    public static final int COLUMN_ADDRESS = 1;
    public static final int COLUMN_NAME = 2;
    public static final int COLUMN_DESCRIPTION = 3;
    public static final int COLUMN_PERSON = 4;
    public final int COLUMN_NO = 0;
    //	public static final int COLUMN_WAINTING_BALANCE = 2;
    //public static final int COLUMN_GENERATING_BALANCE = 3;
    //public static final int COLUMN_FEE_BALANCE = 4;
    private String[] columnNames = Lang.getInstance().translate(new String[]{"No.", "Account", "Name", "Description", "Person"}); // "Waiting"
    private Boolean[] column_AutuHeight = new Boolean[]{true, false, false, false};
    private AccountsPropertisMap db;
    private Account accountCLS;
    private SortableList<String, Tuple2<String, String>> accounts;
    private Pair<String, Tuple2<String, String>> account;


    public Accounts_Name_TableModel() {
        db = Controller.getInstance().wallet.database.getAccountsPropertisMap();
        db.addObserver(this);


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


    public Pair<String, Tuple2<String, String>> getAccount(int row) {
        return accounts.get(row);
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

        return this.accounts.size();
    }

    @Override
    public Object getValueAt(int row, int column) {
        if (this.accounts == null || row > this.accounts.size() - 1) {
            return null;
        }

        account = this.accounts.get(row);
        if (account == null) {
            return null;
        }
        accountCLS = new Account(account.getA());


        Tuple5<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>> balance;
        Tuple3<BigDecimal, BigDecimal, BigDecimal> unconfBalance;
        String str;

        JSONObject ansver;
        switch (column) {
            case COLUMN_ADDRESS:
                return account.getA();
            case COLUMN_NAME:
                return account.getB().a;
            case COLUMN_PERSON:
                return accountCLS.viewPerson();
			/*
		case COLUMN_WAINTING_BALANCE:
			if (this.asset == null) return "-";
			balance = account.getBalance(this.asset.getKey(DBSet.getInstance()));
			unconfBalance = account.getUnconfirmedBalance(this.asset.getKey(DBSet.getInstance()));
			str = NumberAsString.getInstance().numberAsString(unconfBalance.a.subtract(balance.a))
					+ "/" + unconfBalance.b.subtract(balance.b).toPlainString()
					+ "/" + unconfBalance.c.subtract(balance.c).toPlainString();
			return str;
			 
		case COLUMN_FEE_BALANCE:
			if (this.asset == null) return "-";
			return account.getBalanceUSE(Transaction.FEE_KEY);
*/

            case COLUMN_NO:
                return row + 1;
            case COLUMN_DESCRIPTION:

                ansver = (JSONObject) JSONValue.parse(account.getB().b);
                ansver = ansver == null ? new JSONObject() : ansver;
                // set papams
                if (ansver.containsKey("description")) {
                    return ansver.get("description");
                }
                return "";
			/*

		case COLUMN_GENERATING_BALANCE:

			if(this.asset == null || this.asset.getKey() == AssetCls.FEE_KEY)
			{
				return  NumberAsString.getInstance().numberAsString(account.getGeneratingBalance());
			}
			else
			{
				return NumberAsString.getInstance().numberAsString(BigDecimal.ZERO);
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

    @SuppressWarnings("unchecked")
    public synchronized void syncUpdate(Observable o, Object arg) {
        ObserverMessage message = (ObserverMessage) arg;


        if (message.getType() == ObserverMessage.WALLET_ACCOUNT_PROPERTIES_LIST) {


            this.accounts = (SortableList<String, Tuple2<String, String>>) message.getValue();
            this.accounts.registerObserver();
            this.fireTableDataChanged();
        }

        if (message.getType() == ObserverMessage.WALLET_ACCOUNT_PROPERTIES_ADD) {
            //this.Accounts.add(((PublicKeyAccount)message.getValue()));
            this.fireTableDataChanged();
        }

        if (message.getType() == ObserverMessage.WALLET_ACCOUNT_PROPERTIES_DELETE) {
            // обновляем данные

            this.fireTableDataChanged();
        }


    }


    public void deleteObserver() {

        db.deleteObserver(this);
        this.accounts.removeObserver();

    }
}
