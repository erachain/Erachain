package org.erachain.gui.models;

import org.erachain.core.account.Account;
import org.erachain.core.transaction.Transaction;
import org.erachain.datachain.DCSet;
import org.erachain.datachain.TransactionFinalMap;
import org.erachain.utils.DateTimeFormat;
import org.mapdb.Fun;
import org.mapdb.Fun.Tuple3;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Stack;
import java.util.TreeMap;

@SuppressWarnings("serial")
/**
 * Address-Time, Height SeqNo Transaction
 */
public class PersonAccountsModel extends TimerTableModelCls<Fun.Tuple5<String, Integer, Integer, Integer, Transaction>> {
    public static final int COLUMN_ADDRESS = 0;
    public static final int COLUMN_ACCOUNT_NAME = 1;
    public static final int COLUMN_TO_DATE = 2;
    public static final int COLUMN_CREATOR = 3;
    public static final int COLUMN_CREATOR_NAME = 30;
    public static final int COLUMN_CREATOR_KEY = 31;
    public static final int COLUMN_CREATOR_ADDRESS = 32;

    long personKey;

    public PersonAccountsModel(long personKey) {

        super(DCSet.getInstance().getPersonAddressMap(),
                new String[]{"Account", "Name", "Date", "Verifier"},
                new Boolean[]{true, true}, false);

        this.personKey = personKey;

        addObservers();
    }

    public String getTransactionHeightSeqNo(int row) {

        Fun.Tuple5<String, Integer, Integer, Integer, Transaction> value = list.get(row);
        return value.c + "-" + value.d;

    }


    @Override
    public Object getValueAt(int row, int column) {
        if (list == null || row > list.size() - 1) {
            return null;
        }

        Fun.Tuple5<String, Integer, Integer, Integer, Transaction> value = list.get(row);

        Transaction transaction = value.e;
        switch (column) {

            case COLUMN_ADDRESS:

                return value.a;

            case COLUMN_TO_DATE:

                return DateTimeFormat.timestamptoString(value.b * 86400000l);

            case COLUMN_CREATOR:

                return value.e.getCreator().getPersonAsString();

            case COLUMN_CREATOR_KEY:
                if (transaction == null)
                    return null;

                if (transaction.getCreator().getPerson() == null) return null;

                Fun.Tuple4<Long, Integer, Integer, Integer> item = DCSet.getInstance().getAddressPersonMap()
                        .getItem(transaction.getCreator().getShortAddressBytes());
                return item.a;

            case COLUMN_CREATOR_NAME:
                if (transaction == null)
                    return null;

                if (transaction.getCreator().getPerson() == null) return null;
                return transaction.getCreator().getPerson().b.getName();

            case COLUMN_CREATOR_ADDRESS:
                if (transaction == null)
                    return null;

                return transaction.getCreator().getAddress();

            case COLUMN_ACCOUNT_NAME:
                Tuple3<String, String, String> aa = Account.getFromFavorites(value.a);
                if (aa == null) return "";
                return aa.b;

        }


        return null;
    }

    public Account getAccount(int row) {

        Fun.Tuple5<String, Integer, Integer, Integer, Transaction> value = list.get(row);
        int height = value.c;
        int seq = value.d;
        Transaction trans = value.e;
        if (trans == null)
            return null;
        HashSet<Account> accounts = trans.getRecipientAccounts();

        for (Account acc : accounts) {

            String a = acc.getAddress();
            String b = getValueAt(row, COLUMN_ADDRESS).toString();


            if (acc.getAddress().equals(getValueAt(row, COLUMN_ADDRESS).toString())) {

                return acc;

            }

        }

        return null;

    }


    public String getCreator(int row) {
        // TODO Auto-generated method stub

        Fun.Tuple5<String, Integer, Integer, Integer, Transaction> value = list.get(row);
        int height = value.c;
        int seq = value.d;
        Transaction trans = value.e;
        if (trans == null)
            return null;

        return trans.getCreator().getAddress();

    }

    public void getInterval() {

        list = new ArrayList<>();
        TreeMap<String, Stack<Tuple3<Integer, Integer, Integer>>> addresses = DCSet.getInstance().getPersonAddressMap().getItems(personKey);
        TransactionFinalMap transactionsMap = DCSet.getInstance().getTransactionFinalMap();

        for (String address : addresses.keySet()) {
            Stack<Tuple3<Integer, Integer, Integer>> stack = addresses.get(address);
            if (stack == null || stack.isEmpty()) {
                continue;
            }

            Tuple3<Integer, Integer, Integer> item = stack.peek();
            list.add(new Fun.Tuple5<String, Integer, Integer, Integer, Transaction>(address, item.a, item.b, item.c,
                    transactionsMap.get(item.b, item.c)));
        }

    }

}
