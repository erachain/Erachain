package org.erachain.gui.models;
////////

import org.erachain.controller.Controller;
import org.erachain.core.item.persons.PersonCls;
import org.erachain.database.SortableList;
import org.erachain.datachain.DCSet;
import org.erachain.utils.ObserverMessage;
import org.erachain.utils.Pair;
import org.mapdb.Fun.Tuple2;

import java.util.Collections;
import java.util.Observable;
import java.util.Observer;

@SuppressWarnings("serial")
public class WalletItemPersonsTableModel extends SortedListTableModelCls<Tuple2<String, String>, PersonCls> implements Observer {
    public static final int COLUMN_KEY = 0;
    public static final int COLUMN_NAME = 1;
    public static final int COLUMN_ADDRESS = 2;
    public static final int COLUMN_CONFIRMED = 3;
    public static final int COLUMN_FAVORITE = 4;

    public WalletItemPersonsTableModel() {
        super(Controller.getInstance().wallet.database.getPersonMap(),
                new String[]{"Key", "Name", "Publisher", "Confirmed", "Favorite"},
                new Boolean[]{false, true, true, false, false}, true);
    }

    @Override
    public Object getValueAt(int row, int column) {
        if (this.persons == null || row > this.persons.size() - 1) {
            return null;
        }

        Pair<Tuple2<String, String>, PersonCls> personRes = this.persons.get(row);
        if (personRes == null)
            return null;

        PersonCls person = personRes.getB();

        switch (column) {
            case COLUMN_KEY:

                return person.getKey(DCSet.getInstance());

            case COLUMN_NAME:

                return person.viewName();

            case COLUMN_ADDRESS:

                return person.getOwner().getPersonAsString();

            case COLUMN_CONFIRMED:

                return person.isConfirmed();

            case COLUMN_FAVORITE:

                return person.isFavorite();

        }

        return null;
    }

    @SuppressWarnings("unchecked")
    public synchronized void syncUpdate(Observable o, Object arg) {
        ObserverMessage message = (ObserverMessage) arg;

        //CHECK IF NEW LIST
        if (message.getType() == ObserverMessage.LIST_PERSON_TYPE || message.getType() == ObserverMessage.WALLET_LIST_PERSON_TYPE) {
            if (this.persons == null) {
                this.persons = (SortableList<Tuple2<String, String>, PersonCls>) message.getValue();
                //this.persons.registerObserver();
                // sort from comparator
                Collections.sort(this.persons, (a, b) -> a.getB().getName().compareToIgnoreCase(b.getB().getName()));
            }

            this.fireTableDataChanged();
        }

        //CHECK IF LIST UPDATED

        if (message.getType() == ObserverMessage.ADD_PERSON_TYPE || message.getType() == ObserverMessage.REMOVE_PERSON_TYPE
                || message.getType() == ObserverMessage.WALLET_ADD_PERSON_TYPE || message.getType() == ObserverMessage.WALLET_REMOVE_PERSON_TYPE) {
            //		this.persons = (SortableList<Tuple2<String, String>, PersonCls>) message.getValue();
            this.fireTableDataChanged();
        }
    }

    public void addObservers() {

        super.addObservers();

        if (Controller.getInstance().doesWalletDatabaseExists())
            return;

        map.addObserver(this);

    }

    public void deleteObservers() {
        super.deleteObservers();

        if (Controller.getInstance().doesWalletDatabaseExists())
            return;

        map.deleteObserver(this);
    }
}
