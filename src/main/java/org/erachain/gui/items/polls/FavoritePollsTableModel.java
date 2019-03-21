package org.erachain.gui.items.polls;

import org.erachain.controller.Controller;
import org.erachain.core.item.ItemCls;
import org.erachain.core.item.persons.PersonCls;
import org.erachain.core.item.polls.PollCls;
import org.erachain.database.SortableList;
import org.erachain.datachain.DCSet;
import org.erachain.gui.items.FavoriteItemModelTable;
import org.erachain.gui.models.TableModelCls;
import org.erachain.utils.ObserverMessage;
import org.mapdb.Fun.Tuple2;

import javax.validation.constraints.Null;
import java.util.*;

@SuppressWarnings("serial")
public class FavoritePollsTableModel extends FavoriteItemModelTable<Long, PollCls> implements Observer {
    public static final int COLUMN_KEY = 0;
    public static final int COLUMN_NAME = 1;
    public static final int COLUMN_ADDRESS = 2;
    public static final int COLUMN_CONFIRMED = 3;
    public static final int COLUMN_FAVORITE = 4;

    public FavoritePollsTableModel() {
        super(ItemCls.POLL_TYPE, new String[]{"Key", "Name", "Publisher", "Confirmed", "Favorite"},
                new Boolean[]{false, true, true, false, false});
        super.COLUMN_FAVORITE = COLUMN_FAVORITE;
    }

    @Override
    public Object getValueAt(int row, int column) {
        if (this.list == null || row > this.list.size() - 1) {
            return null;
        }

        PollCls status = (PollCls) this.list.get(row);
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
        if (message.getType() == ObserverMessage.WALLET_LIST_POLL_FAVORITES_TYPE && list == null) {
            list = new ArrayList<ItemCls>();
            fill((Set<Long>) message.getValue());
            fireTableDataChanged();
        }
        if (message.getType() == ObserverMessage.WALLET_ADD_POLL_FAVORITES_TYPE) {
            list.add(Controller.getInstance().getPoll((long) message.getValue()));
            fireTableDataChanged();
        }
        if (message.getType() == ObserverMessage.WALLET_DELETE_POLL_FAVORITE_TYPE) {
            list.remove(Controller.getInstance().getPoll((long) message.getValue()));
            fireTableDataChanged();
        }
    }

    public void addObserversThis() {
        Controller.getInstance().wallet.database.getPollFavoritesSet().addObserver(this);
    }

    public void removeObserversThis() {
        Controller.getInstance().wallet.database.getPollFavoritesSet().deleteObserver(this);
    }

}
