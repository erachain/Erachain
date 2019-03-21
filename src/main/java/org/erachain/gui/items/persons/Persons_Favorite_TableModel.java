package org.erachain.gui.items.persons;

import org.erachain.controller.Controller;
import org.erachain.core.item.persons.PersonCls;
import org.erachain.database.SortableList;
import org.erachain.datachain.DCSet;
import org.erachain.gui.models.TableModelCls;
import org.erachain.utils.ObserverMessage;
import org.mapdb.Fun.Tuple2;

import java.util.*;

@SuppressWarnings("serial")
public class Persons_Favorite_TableModel extends TableModelCls<Tuple2<String, String>, PersonCls> implements Observer {
    public static final int COLUMN_KEY = 0;
    public static final int COLUMN_NAME = 1;
    public static final int COLUMN_ADDRESS = 2;
    public static final int COLUMN_CONFIRMED = 3;
    public static final int COLUMN_FAVORITE = 4;

    private List<PersonCls> persons;

    private Boolean[] column_AutuHeight = new Boolean[]{false, true, true, false, false};

    public Persons_Favorite_TableModel() {
        super(new String[]{"Key", "Name", "Publisher", "Confirmed", "Favorite"});
        super.COLUMN_FAVORITE = COLUMN_FAVORITE;
    }

    @Override
    public SortableList<Tuple2<String, String>, PersonCls> getSortableList() {
        return null;
    }

    // читаем колонки которые изменяем высоту
    public Boolean[] getColumnAutoHeight() {

        return this.column_AutuHeight;
    }

    // устанавливаем колонки которым изменить высоту
    public void setColumnAutoHeight(Boolean[] arg0) {
        this.column_AutuHeight = arg0;
    }

    @Override
    public int getRowCount() {
        if (persons == null) return 0;
        return this.persons.size();
    }

    @Override
    public PersonCls getItem(int row) {
        if (persons == null) return null;
        return this.persons.get(row);
    }

    @Override
    public Object getValueAt(int row, int column) {
        if (this.persons == null || row > this.persons.size() - 1) {
            return null;
        }

        PersonCls person = this.persons.get(row);
        if (person == null)
            return null;


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

        int type = message.getType();
        //CHECK IF NEW LIST
        if (type == ObserverMessage.LIST_PERSON_FAVORITES_TYPE && persons == null) {
            persons = new ArrayList<PersonCls>();
            fill((Set<Long>) message.getValue());
            fireTableDataChanged();
        } else if (type == ObserverMessage.ADD_PERSON_FAVORITES_TYPE) {
            persons.add(Controller.getInstance().getPerson((long) message.getValue()));
            fireTableDataChanged();
        } else if (type == ObserverMessage.DELETE_PERSON_FAVORITES_TYPE) {
            persons.remove(Controller.getInstance().getPerson((long) message.getValue()));
            fireTableDataChanged();
        }

    }

    public void fill(Set<Long> set) {

        //	persons.clear();

        PersonCls person;
        for (Long s : set) {

            if ( s < 1)
                continue;

            person = Controller.getInstance().getPerson(s);
            if (person == null)
                continue;

            persons.add(person);

        }


    }

    public void addObserversThis() {
        //fill((Set<Long>) Controller.getInstance().wallet.database.getPersonFavoritesSet());

        if (Controller.getInstance().doesWalletDatabaseExists())
            Controller.getInstance().wallet.database.getPersonFavoritesSet().addObserver(this);
    }

    public void removeObserversThis() {

        if (Controller.getInstance().doesWalletDatabaseExists())
            Controller.getInstance().wallet.database.getPersonFavoritesSet().deleteObserver(this);

    }

}
