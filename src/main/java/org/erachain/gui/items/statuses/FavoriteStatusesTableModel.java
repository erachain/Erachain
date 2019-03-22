package org.erachain.gui.items.statuses;

import org.erachain.controller.Controller;
import org.erachain.core.item.ItemCls;
import org.erachain.core.item.persons.PersonCls;
import org.erachain.core.item.statuses.StatusCls;
import org.erachain.database.SortableList;
import org.erachain.datachain.DCSet;
import org.erachain.gui.items.FavoriteItemModelTable;
import org.erachain.gui.models.TableModelCls;
import org.erachain.utils.ObserverMessage;
import org.mapdb.Fun.Tuple2;

import javax.validation.constraints.Null;
import java.util.*;

@SuppressWarnings("serial")
public class FavoriteStatusesTableModel extends FavoriteItemModelTable implements Observer {
    public static final int COLUMN_KEY = 0;
    public static final int COLUMN_NAME = 1;
    public static final int COLUMN_ADDRESS = 2;
    public static final int COLUMN_CONFIRMED = 3;
    public static final int COLUMN_FAVORITE = 4;

    public FavoriteStatusesTableModel() {
        super(DCSet.getInstance().getItemStatusMap(),
                new String[]{"Key", "Name", "Publisher", "Confirmed", "Favorite"},
                new Boolean[]{false, true, true, false, false},
                ObserverMessage.RESET_STATUS_FAVORITES_TYPE,
                ObserverMessage.ADD_STATUS_FAVORITES_TYPE,
                ObserverMessage.DELETE_STATUS_FAVORITES_TYPE,
                ObserverMessage.LIST_STATUS_FAVORITES_TYPE,
                COLUMN_FAVORITE);
    }

    @Override
    public Object getValueAt(int row, int column) {
        if (this.list == null || row > this.list.size() - 1) {
            return null;
        }

        StatusCls status = (StatusCls) this.list.get(row);
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

    public void addObserversThis() {
        if (Controller.getInstance().doesWalletDatabaseExists())
            Controller.getInstance().wallet.database.getStatusFavoritesSet().addObserver(this);
    }

    public void removeObserversThis() {
        if (Controller.getInstance().doesWalletDatabaseExists())
            Controller.getInstance().wallet.database.getStatusFavoritesSet().deleteObserver(this);
    }

}
