package org.erachain.gui.items.persons;

import org.erachain.core.item.ItemCls;
import org.erachain.core.item.persons.PersonCls;
import org.erachain.datachain.DCSet;
import org.erachain.datachain.ItemPersonMap;
import org.erachain.gui.models.SortedListTableModelCls;
import org.erachain.utils.ObserverMessage;
import org.erachain.utils.Pair;
import org.mapdb.Fun.Tuple2;

import java.util.*;

@SuppressWarnings("serial")
public class TableModelOwnerPersons extends SortedListTableModelCls<String, PersonCls> {
    public static final int COLUMN_KEY = 0;
    public static final int COLUMN_NAME = 1;
    public static final int COLUMN_BORN = 2;
    public static final int COLUMN_ADDRESS = 3;
    public static final int COLUMN_FAVORITE = 4;

    private long itemKey;

    public TableModelOwnerPersons(Long key) {
        super(DCSet.getInstance().getItemPersonMap(),
                new String[]{"Key", "Name", "Birthday"},
                new Boolean[]{false, true, true, false}, false);

        itemKey = key;

        addObservers();

    }

    @Override
    public Object getValueAt(int row, int column) {
        if (this.listSorted == null || row > this.listSorted.size() - 1) {
            return null;
        }

        PersonCls person = this.listSorted.get(row).getB();

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

    @SuppressWarnings("unchecked")
    public synchronized void syncUpdate(Observable o, Object arg) {
        ObserverMessage message = (ObserverMessage) arg;

        //CHECK IF NEW LIST
        if (message.getType() == ObserverMessage.LIST_PERSON_TYPE) {

            setRows();
            this.fireTableDataChanged();

        } else
        //CHECK IF LIST UPDATED
        if (message.getType() == ObserverMessage.ADD_PERSON_TYPE || message.getType() == ObserverMessage.REMOVE_PERSON_TYPE) {

            needUpdate = true;

        } else
        if (message.getType() == ObserverMessage.GUI_REPAINT && needUpdate) {
            needUpdate = false;
            setRows();
            this.fireTableDataChanged();
        }
    }

    public void addObservers() {
        super.addObservers();
        map.addObserver(this);
    }

    public void removeObservers() {
        super.deleteObservers();
        map.deleteObserver(this);
    }

    @SuppressWarnings("unchecked")
    private void setRows() {

        listSorted.clear();

        Set<String> publicKeys = DCSet.getInstance().getPersonAddressMap().get(itemKey).keySet();

        for (String publicKey: publicKeys) {
            NavigableMap<Long, ItemCls> addresses = ((ItemPersonMap) map).getOwnerItems(publicKey);
            for (Long key: addresses.keySet()) {
                listSorted.add(new Pair(publicKey, addresses.get(key)));
            }

        }

    }

}
