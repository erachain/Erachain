package org.erachain.gui.items;

import org.erachain.controller.Controller;
import org.erachain.core.item.ItemCls;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.database.DBMap;
import org.erachain.database.SortableList;
import org.erachain.datachain.DCSet;
import org.erachain.gui.models.TableModelCls;
import org.erachain.lang.Lang;
import org.erachain.utils.ObserverMessage;

import java.util.*;

@SuppressWarnings("serial")
public abstract class FavoriteItemModelTable<T, U> extends SearchItemsTableModel<Long, ItemCls> implements Observer {

    public final int RESET_EVENT;
    public final int ADD_EVENT;
    public final int DELETE_EVENT;
    public final int LIST_EVENT;

    public FavoriteItemModelTable(DBMap map, String[] columnNames, Boolean[] columnAutoHeight,
              int resetObserver, int addObserver, int deleteObserver, int listObserver, int favorite) {
        super(map, columnNames, columnAutoHeight, favorite);

        this.RESET_EVENT = resetObserver;
        this.ADD_EVENT = addObserver;
        this.DELETE_EVENT = deleteObserver;
        this.LIST_EVENT = listObserver;

    }

    public void fill(Set<Long> keys) {
        ItemCls item;
        list = new ArrayList<ItemCls>();

        for (Long itemKey : keys) {
            if (itemKey == null || itemKey < 1)
                continue;

            item = Controller.getInstance().getItem(itemType, itemKey);
            if (item == null)
                continue;

            list.add(item);
        }

        this.listSorted = new SortableList<Long, ItemCls>(this.map, keys);
    }

    @SuppressWarnings("unchecked")
    public synchronized void syncUpdate(Observable o, Object arg) {
        ObserverMessage message = (ObserverMessage) arg;

        //CHECK IF NEW LIST
        int type = message.getType();
        if (type == LIST_EVENT && list == null) {
            this.fireTableDataChanged();

        } else if (type == ADD_EVENT) {
            list.add(Controller.getInstance().getAsset((long) message.getValue()));
            this.fireTableDataChanged();

        } else if (type == DELETE_EVENT) {
            list.remove(Controller.getInstance().getAsset((long) message.getValue()));
            this.fireTableDataChanged();

        } else if (type == RESET_EVENT) {
            list = new ArrayList<ItemCls>();
            this.fireTableDataChanged();
        }
    }

}
