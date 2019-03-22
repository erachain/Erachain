package org.erachain.gui.items;

import com.sun.xml.internal.bind.v2.util.CollisionCheckStack;
import org.erachain.controller.Controller;
import org.erachain.core.item.ItemCls;
import org.erachain.database.SortableList;
import org.erachain.database.wallet.FavoriteItemMap;
import org.erachain.datachain.DCMap;
import org.erachain.datachain.Item_Map;
import org.erachain.gui.models.TableModelCls;
import org.erachain.utils.ObserverMessage;
import org.erachain.utils.Pair;

import javax.ws.rs.PathParam;
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
        super(map, columnNames, columnAutoHeight, favorite);

        this.favoriteMap = favoriteMap;

        this.RESET_EVENT = resetObserver;
        this.ADD_EVENT = addObserver;
        this.DELETE_EVENT = deleteObserver;
        this.LIST_EVENT = listObserver;

        // теперь нужно опять послать событие чтобы загрузить
        getInterval();
        this.fireTableDataChanged();
        needUpdate = false;

    }

    @SuppressWarnings("unchecked")
    public synchronized void syncUpdate(Observable o, Object arg) {
        ObserverMessage message = (ObserverMessage) arg;

        //CHECK IF NEW LIST
        int type = message.getType();
        if (type == LIST_EVENT && list == null) {
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
        this.listSorted = new SortableList<Long, ItemCls>((Item_Map)map, favoriteMap.getFromToKeys(startBack, endBack));
        this.list = new ArrayList<Long, ItemCls>();
        this.list.addAll(this.listSorted);

        for (Pair<Long, ItemCls> key: listSorted) {
            ItemCls item = (ItemCls)map.get(key.getA());
        }

    }

    public void addObserversThis() {
        if (Controller.getInstance().doesWalletDatabaseExists()
            && favoriteMap != null)
            favoriteMap.addObserver(this);
    }

    public void removeObserversThis() {
        if (Controller.getInstance().doesWalletDatabaseExists()
                && favoriteMap != null)
            favoriteMap.deleteObserver(this);
    }

}
