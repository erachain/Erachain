package org.erachain.gui.items.persons;

import org.erachain.controller.Controller;
import org.erachain.core.item.ItemCls;
import org.erachain.core.item.imprints.ImprintCls;
import org.erachain.core.item.persons.PersonCls;
import org.erachain.database.SortableList;
import org.erachain.datachain.DCSet;
import org.erachain.gui.items.FavoriteItemModelTable;
import org.erachain.gui.models.TableModelCls;
import org.erachain.utils.ObserverMessage;
import org.mapdb.Fun.Tuple2;

import java.util.*;

@SuppressWarnings("serial")
public class FavoritePersonsTableModel extends FavoriteItemModelTable<Long, PersonCls> implements Observer {
    public static final int COLUMN_KEY = 0;
    public static final int COLUMN_NAME = 1;
    public static final int COLUMN_ADDRESS = 2;
    public static final int COLUMN_CONFIRMED = 3;
    public static final int COLUMN_FAVORITE = 4;

    public FavoritePersonsTableModel() {
        super(ItemCls.PERSON_TYPE, new String[]{"Key", "Name", "Publisher", "Confirmed", "Favorite"},
                new Boolean[]{false, true, true, false, false});
        super.COLUMN_FAVORITE = COLUMN_FAVORITE;
    }

    @Override
    public Object getValueAt(int row, int column) {
        if (this.list == null || row > this.list.size() - 1) {
            return null;
        }

        PersonCls person = (PersonCls) this.list.get(row);
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
        if (type == ObserverMessage.LIST_PERSON_FAVORITES_TYPE && list == null) {
            list = new ArrayList<ItemCls>();
            fill((Set<Long>) message.getValue());
            fireTableDataChanged();
        } else if (type == ObserverMessage.ADD_PERSON_FAVORITES_TYPE) {
            list.add(Controller.getInstance().getPerson((long) message.getValue()));
            fireTableDataChanged();
        } else if (type == ObserverMessage.DELETE_PERSON_FAVORITES_TYPE) {
            list.remove(Controller.getInstance().getPerson((long) message.getValue()));
            fireTableDataChanged();
        }

    }

    public void addObserversThis() {
        if (Controller.getInstance().doesWalletDatabaseExists())
            Controller.getInstance().wallet.database.getPersonFavoritesSet().addObserver(this);
    }

    public void removeObserversThis() {
        if (Controller.getInstance().doesWalletDatabaseExists())
            Controller.getInstance().wallet.database.getPersonFavoritesSet().deleteObserver(this);

    }

}
