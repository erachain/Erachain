package org.erachain.gui.items.persons;

import org.erachain.core.item.ItemCls;
import org.erachain.core.item.persons.PersonCls;
import org.erachain.datachain.DCSet;
import org.erachain.datachain.ItemPersonMap;
import org.erachain.gui.models.SortedListTableModelCls;
import org.erachain.gui.models.TimerTableModelCls;
import org.erachain.utils.ObserverMessage;
import org.erachain.utils.Pair;
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

    public TableModelOwnerPersons(Long key) {
        super(DCSet.getInstance().getItemPersonMap(),
                new String[]{"Key", "Name", "Birthday"},
                new Boolean[]{false, true, true, false}, true);

        itemKey = key;

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

        }

        return null;
    }

    @Override
    public void getIntervalThis(long start, long end) {

        list = new ArrayList<>();

        Set<String> publicKeys = DCSet.getInstance().getPersonAddressMap().get(itemKey).keySet();

        for (String publicKey: publicKeys) {
            NavigableMap<Long, ItemCls> addresses = ((ItemPersonMap) map).getOwnerItems(publicKey);
            for (Long key: addresses.keySet()) {
                list.add((PersonCls)addresses.get(key));
            }

        }

    }

}
