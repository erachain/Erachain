package org.erachain.gui.items.persons;

import org.erachain.core.account.Account;
import org.erachain.core.item.persons.PersonCls;
import org.erachain.core.transaction.IssueItemRecord;
import org.erachain.core.transaction.Transaction;
import org.erachain.datachain.DCSet;
import org.erachain.datachain.PersonAddressMap;
import org.erachain.datachain.TransactionFinalMap;
import org.erachain.gui.models.TimerTableModelCls;
import org.mapdb.Fun;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.TreeMap;

@SuppressWarnings("serial")
public class TableModelMakerPersons extends TimerTableModelCls<PersonCls> {
    public static final int COLUMN_KEY = 0;
    public static final int COLUMN_NAME = 1;
    public static final int COLUMN_BORN = 2;
    public static final int COLUMN_ADDRESS = 3;
    public static final int COLUMN_FAVORITE = 4;

    private long itemKey;
    private PersonAddressMap personMap;
    private TransactionFinalMap transactionFinalMap;

    public TableModelMakerPersons(Long key) {
        super(DCSet.getInstance().getItemPersonMap(),
                new String[]{"Key", "Name", "Birthday", "Favorite"},
                null, COLUMN_FAVORITE, true);

        itemKey = key;
        personMap = DCSet.getInstance().getPersonAddressMap();
        transactionFinalMap = DCSet.getInstance().getTransactionFinalMap();

        addObservers();

    }

    @Override
    public Object getValueAt(int row, int column) {
        if (this.list == null || row > this.list.size() - 1) {
            return null;
        }

        PersonCls person = this.list.get(row);

        switch (column) {
            case COLUMN_KEY:

                return person.getKey();

            case COLUMN_NAME:

                return person.viewName();

            case COLUMN_BORN:

                return person.getBirthdayStr();

            case COLUMN_FAVORITE:

                return person.isFavorite();


        }

        return null;
    }

    @Override
    public void getInterval() {

        list = new ArrayList<>();

        TreeMap<String, Stack<Fun.Tuple3<Integer, Integer, Integer>>> addresses = personMap.getItems(itemKey);
        List<Transaction> myIssuePersons = new ArrayList<Transaction>();

        for (String address : addresses.keySet()) {
            List<Transaction> recs = transactionFinalMap.getTransactionsByAddressAndType(Account.makeShortBytes(address),
                    Transaction.ISSUE_PERSON_TRANSACTION, 0, 0);
            if (recs != null)
                myIssuePersons.addAll(recs);
        }

        for (Transaction myIssuePerson : myIssuePersons) {
            IssueItemRecord record = (IssueItemRecord) myIssuePerson;
            list.add((PersonCls) record.getItem());
        }
    }

}
