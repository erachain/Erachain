package org.erachain.gui.items;

import org.erachain.controller.Controller;
import org.erachain.database.wallet.FavoriteItemMap;
import org.erachain.dbs.DBTabImpl;
import org.erachain.gui.ObserverWaiter;
import org.erachain.gui.models.TimerTableModelCls;
import org.erachain.utils.ObserverMessage;

import java.util.Observable;
import java.util.Observer;

@SuppressWarnings("serial")
public abstract class FavoriteItemModelTable extends TimerTableModelCls implements Observer, ObserverWaiter {

    private final int RESET_EVENT;
    private final int ADD_EVENT;
    private final int DELETE_EVENT;
    private final int LIST_EVENT;

    protected FavoriteItemMap favoriteMap;

    public FavoriteItemModelTable(DBTabImpl map, FavoriteItemMap favoriteMap, String[] columnNames, Boolean[] columnAutoHeight,
                                  int resetObserver, int addObserver, int deleteObserver, int listObserver, int favorite) {
        super(columnNames, columnAutoHeight, false);

        // в головной гласс нельзя таблицу передавать - чтобы там лишний раз не запускалась иницализация наблюдения
        // оно еще ен готово так как таблица вторая не присвоена - ниже привяжемся к наблюдениям
        this.map = map;
        this.favoriteMap = favoriteMap;
        COLUMN_FAVORITE = favorite;

        RESET_EVENT = resetObserver;
        ADD_EVENT = addObserver;
        DELETE_EVENT = deleteObserver;
        LIST_EVENT = listObserver;

        // теперь нужно опять послать событие чтобы загрузить
        getInterval();
        fireTableDataChanged();
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
            fireTableDataChanged();
            needUpdate = false;

        } else if (type == ADD_EVENT) {
            list.add(Controller.getInstance().getAsset((long) message.getValue()));
            needUpdate = true;

        } else if (type == DELETE_EVENT) {
            list.remove(Controller.getInstance().getAsset((long) message.getValue()));
            needUpdate = true;

        } else if (type == RESET_EVENT) {
            getInterval();
            fireTableDataChanged();
            needUpdate = false;
        }
    }

    public void addObservers() {

        if (Controller.getInstance().doesWalletDatabaseExists()) {
            favoriteMap.addObserver(this);
        } else {
            // ожидаем открытия кошелька
            Controller.getInstance().wallet.addWaitingObserver(this);
        }
    }

    public void deleteObservers() {
        if (Controller.getInstance().doesWalletDatabaseExists())
            favoriteMap.deleteObserver(this);
    }

}
