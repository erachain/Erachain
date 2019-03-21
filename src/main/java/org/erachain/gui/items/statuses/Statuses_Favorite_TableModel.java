package org.erachain.gui.items.statuses;

import org.erachain.controller.Controller;
import org.erachain.core.item.persons.PersonCls;
import org.erachain.core.item.statuses.StatusCls;
import org.erachain.database.SortableList;
import org.erachain.datachain.DCSet;
import org.erachain.gui.models.TableModelCls;
import org.erachain.utils.ObserverMessage;
import org.mapdb.Fun.Tuple2;

import javax.validation.constraints.Null;
import java.util.*;

@SuppressWarnings("serial")
public class Statuses_Favorite_TableModel extends TableModelCls<Tuple2<String, String>, StatusCls> implements Observer {
    public static final int COLUMN_KEY = 0;
    public static final int COLUMN_NAME = 1;
    public static final int COLUMN_ADDRESS = 2;
    public static final int COLUMN_CONFIRMED = 3;
    public static final int COLUMN_FAVORITE = 4;

    private List<StatusCls> statuses;

    public Statuses_Favorite_TableModel() {
        super(new String[]{"Key", "Name", "Publisher", "Confirmed", "Favorite"},
                new Boolean[]{false, true, true, false, false});
        super.COLUMN_FAVORITE = COLUMN_FAVORITE;
    }

    @Override
    public SortableList<Tuple2<String, String>, StatusCls> getSortableList() {
        return null;
    }

    public StatusCls getItem(int row) {
        return this.statuses.get(row);

    }

    @Override
    public int getRowCount() {
        if (statuses == null) return 0;
        return this.statuses.size();
    }

    @Override
    public Object getValueAt(int row, int column) {
        if (this.statuses == null || row > this.statuses.size() - 1) {
            return null;
        }

        StatusCls status = this.statuses.get(row);
        if (status == null)
            return null;


        switch (column) {
            case COLUMN_KEY:

                return status.getKey(DCSet.getInstance());

            case COLUMN_NAME:

                return status.viewName();

            case COLUMN_ADDRESS:

                return status.getOwner().getPersonAsString();

            case COLUMN_CONFIRMED:

                return status.isConfirmed();

            case COLUMN_FAVORITE:

                return status.isFavorite();

        }

        return null;
    }

    @SuppressWarnings("unchecked")
    public synchronized void syncUpdate(Observable o, Object arg) {
        ObserverMessage message = (ObserverMessage) arg;

        //CHECK IF NEW LIST
        if (message.getType() == ObserverMessage.LIST_STATUS_FAVORITES_TYPE && statuses == null) {
            statuses = new ArrayList<StatusCls>();
            fill((Set<Long>) message.getValue());
            fireTableDataChanged();
        }
        if (message.getType() == ObserverMessage.ADD_STATUS_TYPE_FAVORITES_TYPE) {
            statuses.add(Controller.getInstance().getStatus((long) message.getValue()));
            fireTableDataChanged();
        }
        if (message.getType() == ObserverMessage.DELETE_STATUS_FAVORITES_TYPE) {
            statuses.remove(Controller.getInstance().getStatus((long) message.getValue()));
            fireTableDataChanged();
        }


    }


    public void fill(Set<Long> set) {

        //	persons.clear();

        for (Long s : set) {

            statuses.add(Controller.getInstance().getStatus(s));


        }


    }

    public void addObserversThis() {
        //fill((Set<Long>) Controller.getInstance().wallet.database.getPersonFavoritesSet());

        if (Controller.getInstance().doesWalletDatabaseExists())
            Controller.getInstance().wallet.database.getStatusFavoritesSet().addObserver(this);
    }

    public void removeObserversThis() {
        if (Controller.getInstance().doesWalletDatabaseExists())
            Controller.getInstance().wallet.database.getStatusFavoritesSet().deleteObserver(this);
    }

}
