package org.erachain.gui.items.accounts;

import org.erachain.controller.Controller;
import org.erachain.core.account.Account;
import org.erachain.gui.ObserverWaiter;
import org.erachain.gui.models.WalletTableModel;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple3;
import org.mapdb.Fun.Tuple5;

import java.math.BigDecimal;

@SuppressWarnings("serial")
public class AccountsNameTableModel extends WalletTableModel<Tuple2<String, Tuple2<String, String>>> implements ObserverWaiter {

    public static final int COLUMN_NO = 0;
    public static final int COLUMN_ADDRESS = 1;
    public static final int COLUMN_NAME = 2;
    public static final int COLUMN_DESCRIPTION = 3;
    public static final int COLUMN_PERSON = 4;

    private Tuple2<String, Tuple2<String, String>> account;

    public AccountsNameTableModel() {
        super(Controller.getInstance().wallet.database.getAccountsPropertisMap(),
                new String[]{"No.", "Account", "Name", "Description", "Person"},
                new Boolean[]{true, false, false, false}, false);

    }

    @Override
    public Object getValueAt(int row, int column) {
        if (this.list == null || row > this.list.size() - 1) {
            return null;
        }

        account = this.list.get(row);
        if (account == null) {
            return null;
        }

        Tuple5<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>
                balance;
        Tuple3<BigDecimal, BigDecimal, BigDecimal> unconfBalance;
        String str;

        JSONObject answer;
        switch (column) {
            case COLUMN_ADDRESS:
                return account.b;
            case COLUMN_NAME:
                return account.a;
            case COLUMN_PERSON:
                return new Account(account.a).viewPerson();
            case COLUMN_NO:
                return row + 1;
            case COLUMN_DESCRIPTION:

                if (true)
                    return account.toString();

                answer = (JSONObject) JSONValue.parse(account.b.b);
                answer = answer == null ? new JSONObject() : answer;
                // set papams
                if (answer.containsKey("description")) {
                    return answer.get("description");
                }
                return "";
        }

        return null;
    }

    /*
    @SuppressWarnings("unchecked")
    public synchronized void syncUpdate(Observable o, Object arg) {
        ObserverMessage message = (ObserverMessage) arg;

        if (message.getType() == ObserverMessage.WALLET_ACCOUNT_PROPERTIES_LIST) {

            needUpdate = false;
            getInterval();
            this.fireTableDataChanged();

        } else if (message.getType() == ObserverMessage.WALLET_ACCOUNT_PROPERTIES_ADD) {

            needUpdate = false;
            getInterval();
            this.fireTableDataChanged();

        } else if (message.getType() == ObserverMessage.WALLET_ACCOUNT_PROPERTIES_DELETE) {

            needUpdate = false;
            getInterval();
            this.fireTableDataChanged();
        }

    }

     */
}
