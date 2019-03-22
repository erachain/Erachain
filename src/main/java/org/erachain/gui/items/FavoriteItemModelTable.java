package org.erachain.gui.items;

import org.erachain.controller.Controller;
import org.erachain.core.item.ItemCls;
import org.erachain.database.SortableList;
import org.erachain.database.wallet.FavoriteItemMap;
import org.erachain.datachain.DCMap;
import org.erachain.datachain.ItemMap;
import org.erachain.gui.models.TableModelCls;
import org.erachain.utils.ObserverMessage;
import org.erachain.utils.Pair;

import java.util.*;

@SuppressWarnings("serial")
public abstract class FavoriteItemModelTable extends TableModelCls<Long, ItemCls> implements Observer {

    private final int RESET_EVENT;
    private final int ADD_EVENT;
    private final int DELETE_EVENT;
    private final int LIST_EVENT;

    protected FavoriteItemMap favoriteMap;

    public FavoriteItemModelTable(DCMap map, FavoriteItemMap favoriteMap, String[] columnNames, Boolean[] columnAutoHeight,
                                  int resetObserver, int addObserver, int deleteObserver, int listObserver, int favorite) {
        super(columnNames, columnAutoHeight, false);

        // в головной гласс нельзя таблицу передавать - чтобы там лишний раз не запускалась иницализация наблюдения
        // оно еще ен готово так как таблица вторая не присвоена - ниже привяжемся к наблюдениям
        this.map = map;
        this.favoriteMap = favoriteMap;
        this.COLUMN_FAVORITE = favorite;

        this.RESET_EVENT = resetObserver;
        this.ADD_EVENT = addObserver;
        this.DELETE_EVENT = deleteObserver;
        this.LIST_EVENT = listObserver;

        // теперь нужно опять послать событие чтобы загрузить
        getInterval();
        this.fireTableDataChanged();
        needUpdate = false;

        // переиницализация после установуи таблиц
        this.addObservers();

    }

    @SuppressWarnings("unchecked")
    public synchronized void syncUpdate(Observable o, Object arg) {
        ObserverMessage message = (ObserverMessage) arg;

        //CHECK IF NEW LIST
        int type = message.getType();
        if (type == LIST_EVENT) {
            getInterval();
            this.fireTableDataChanged();
            needUpdate = false;

        } else if (type == ADD_EVENT) {
            list.add(Controller.getInstance().getAsset((long) message.getValue()));
            needUpdate = true;

        } else if (type == DELETE_EVENT) {
            list.remove(Controller.getInstance().getAsset((long) message.getValue()));
            needUpdate = true;

        } else if (type == RESET_EVENT) {
            getInterval();
            this.fireTableDataChanged();
            needUpdate = false;
        }
    }

    //public abstract int getMapSize();
    @Override
    public long getMapSize() {
        return favoriteMap.size();
    }

    @Override
    public void getInterval() {

        getIntervalThis( start, step);

    }

    @Override
    public void getIntervalThis(long startBack, long endBack) {
        this.listSorted = new SortableList<Long, ItemCls>((ItemMap)map, favoriteMap.getFromToKeys(0, 999999999));
        this.listSorted.sort();

        this.list = new ArrayList<ItemCls>();
        for (Pair<Long, ItemCls> key: listSorted) {
            if (key.getB() == null)
                continue;

            this.list.add((ItemCls)map.get(key.getA()));
        }

    }

    public void addObservers() {
        if (Controller.getInstance().doesWalletDatabaseExists()
            && favoriteMap != null)
            favoriteMap.addObserver(this);
    }

    public void deleteObservers() {
        if (Controller.getInstance().doesWalletDatabaseExists()
                && favoriteMap != null)
            favoriteMap.deleteObserver(this);
    }

}
