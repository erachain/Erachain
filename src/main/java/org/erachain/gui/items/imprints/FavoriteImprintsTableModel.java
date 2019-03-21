package org.erachain.gui.items.imprints;

import org.erachain.controller.Controller;
import org.erachain.core.item.ItemCls;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.item.imprints.Imprint;
import org.erachain.core.item.imprints.ImprintCls;
import org.erachain.core.item.persons.PersonCls;
import org.erachain.database.SortableList;
import org.erachain.datachain.DCSet;
import org.erachain.gui.items.FavoriteItemModelTable;
import org.erachain.gui.models.TableModelCls;
import org.erachain.gui.models.TimerTableModelCls;
import org.erachain.utils.ObserverMessage;
import org.mapdb.Fun.Tuple2;

import javax.validation.constraints.Null;
import java.util.*;

@SuppressWarnings("serial")
public class FavoriteImprintsTableModel extends FavoriteItemModelTable<Long, ImprintCls> implements Observer {
    public static final int COLUMN_KEY = 0;
    public static final int COLUMN_NAME = 1;
    public static final int COLUMN_ADDRESS = 2;
    public static final int COLUMN_CONFIRMED = 3;
    public static final int COLUMN_FAVORITE = 4;

    public FavoriteImprintsTableModel() {
        super(ItemCls.IMPRINT_TYPE, new String[]{"Key", "Name", "Publisher", "Confirmed", "Favorite"}, new Boolean[]{false, true, true, false, false});
        super.COLUMN_FAVORITE = COLUMN_FAVORITE;
    }

    @Override
    public Object getValueAt(int row, int column) {
        if (this.list == null || row > this.list.size() - 1) {
            return null;
        }

        ImprintCls item = (ImprintCls) this.list.get(row);
        if (item == null)
            return null;

        switch (column) {
            case COLUMN_KEY:

                return item.getKey(DCSet.getInstance());

            case COLUMN_NAME:

                return item.viewName();

            case COLUMN_ADDRESS:

                return item.getOwner().getPersonAsString();

            case COLUMN_CONFIRMED:

                return item.isConfirmed();

            case COLUMN_FAVORITE:

                return item.isFavorite();

        }

        return null;
    }

    @SuppressWarnings("unchecked")
    public synchronized void syncUpdate(Observable o, Object arg) {
        ObserverMessage message = (ObserverMessage) arg;

        //CHECK IF NEW LIST
        if (message.getType() == ObserverMessage.LIST_IMPRINT_FAVORITES_TYPE && list == null) {
            list = new ArrayList<ItemCls>();
            fill((Set<Long>) message.getValue());
            fireTableDataChanged();
        }
        if (message.getType() == ObserverMessage.ADD_IMPRINT_TYPE_FAVORITES_TYPE) {
            list.add(Controller.getInstance().getImprint((long) message.getValue()));
            fireTableDataChanged();
        }
        if (message.getType() == ObserverMessage.DELETE_IMPRINT_FAVORITES_TYPE) {
            list.remove(Controller.getInstance().getImprint((long) message.getValue()));
            fireTableDataChanged();
        }
    }

    public void addObserversThis() {
        Controller.getInstance().wallet.database.getImprintFavoritesSet().addObserver(this);
    }

    public void removeObserversThis() {
        Controller.getInstance().wallet.database.getImprintFavoritesSet().deleteObserver(this);
    }

}
