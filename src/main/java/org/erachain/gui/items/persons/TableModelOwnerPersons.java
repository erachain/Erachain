package org.erachain.gui.items.persons;

import org.erachain.core.item.ItemCls;
import org.erachain.core.item.persons.PersonCls;
import org.erachain.core.transaction.IssueItemRecord;
import org.erachain.core.transaction.Transaction;
import org.erachain.datachain.DCSet;
import org.erachain.datachain.ItemPersonMap;
import org.erachain.datachain.PersonAddressMap;
import org.erachain.datachain.TransactionFinalMap;
import org.erachain.gui.models.SortedListTableModelCls;
import org.erachain.gui.models.TimerTableModelCls;
import org.erachain.utils.ObserverMessage;
import org.erachain.utils.Pair;
import org.mapdb.Fun;
import org.mapdb.Fun.Tuple2;

import java.util.*;

@SuppressWarnings("serial")
public class TableModelOwnerPersons extends TimerTableModelCls<PersonCls> {
    public static final int COLUMN_KEY = 0;
    public static final int COLUMN_NAME = 1;
    public static final int COLUMN_BORN = 2;
    public static final int COLUMN_ADDRESS = 3;
    public static final int COLUMN_FAVORITE = 4;

    private long itemKey;
    private PersonAddressMap personMap;
    private TransactionFinalMap transactionFinalMap;

    public TableModelOwnerPersons(Long key) {
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

            //	case COLUMN_ADDRESS:

            //		return person.getOwner().getPersonAsString();

            case COLUMN_BORN:

                //	DateFormat f = new DateFormat("DD-MM-YYYY");
                //SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-YYYY");
                //return  dateFormat.format( new Date(person.getBirthday()));
                return person.getBirthdayStr();

            case COLUMN_FAVORITE:

                //	DateFormat f = new DateFormat("DD-MM-YYYY");
                //SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-YYYY");
                //return  dateFormat.format( new Date(person.getBirthday()));
                return person.isFavorite();


        }

        return null;
    }

    @Override
    public void getIntervalThis(long start, long end) {

        list = new ArrayList<>();

        TreeMap<String, Stack<Fun.Tuple3<Integer, Integer, Integer>>> addresses = personMap.getItems(itemKey);
        List<Transaction> myIssuePersons = new ArrayList<Transaction>();

        for (String address : addresses.keySet()) {
            myIssuePersons.addAll(transactionFinalMap.getTransactionsByTypeAndAddress(address,
                    Transaction.ISSUE_PERSON_TRANSACTION, 0));
        }

        for (Transaction myIssuePerson : myIssuePersons) {
            IssueItemRecord record = (IssueItemRecord) myIssuePerson;
            list.add((PersonCls) record.getItem());
        }
    }

}
