package org.erachain.gui.items;

import org.erachain.controller.Controller;
import org.erachain.database.wallet.FavoriteItemMap;
import org.erachain.dbs.DBTabImpl;
import org.erachain.dbs.IteratorCloseable;
import org.erachain.gui.ObserverWaiter;
import org.erachain.utils.ObserverMessage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

@SuppressWarnings("serial")
public abstract class FavoriteItemModelTable extends WalletItemTableModel implements Observer, ObserverWaiter {

    private final int RESET_EVENT;
    private final int ADD_EVENT;
    private final int DELETE_EVENT;
    private final int LIST_EVENT;

    protected FavoriteItemMap favoriteMap;

    public FavoriteItemModelTable(DBTabImpl map, FavoriteItemMap favoriteMap, String[] columnNames, Boolean[] columnAutoHeight,
                                  int resetObserver, int addObserver, int deleteObserver, int listObserver, int favorite) {
        super(columnNames, columnAutoHeight, false);

        // в головной класс нельзя таблицу передавать - чтобы там лишний раз не запускалась иницализация наблюдения
        // оно еще ен готово так как таблица вторая не присвоена - ниже привяжемся к наблюдениям
        this.map = map;
        this.favoriteMap = favoriteMap;
        COLUMN_FAVORITE = favorite;

        RESET_EVENT = resetObserver;
        ADD_EVENT = addObserver;
        DELETE_EVENT = deleteObserver;
        LIST_EVENT = listObserver;

        step = 500;

        // теперь нужно опять послать событие чтобы загрузить
        getInterval();
        fireTableDataChanged();
        needUpdate = false;

        // переиницализация после установуи таблиц
        this.addObservers();

    }

    @Override
    protected void clearMap() {
        favoriteMap = null;
    }

    @Override
    public void syncUpdate(Observable o, Object arg) {
        ObserverMessage message = (ObserverMessage) arg;

        //CHECK IF NEW LIST
        int type = message.getType();
        if (type == LIST_EVENT) {
            getInterval();
            fireTableDataChanged();
            needUpdate = false;

        } else if (type == ADD_EVENT) {
            list.add(map.get(message.getValue()));
            needUpdate = true;

        } else if (type == DELETE_EVENT) {
            //list.remove(Controller.getInstance().getAsset((long) message.getValue()));
            list.remove(map.get(message.getValue()));
            needUpdate = true;

        } else if (type == RESET_EVENT) {
            getInterval();
            fireTableDataChanged();
            needUpdate = false;
        }
    }

    @Override
    public void getInterval() {
        Object key;
        int count = 0;
        list = new ArrayList<>();
        if (startKey == null) {
            try (IteratorCloseable iterator = favoriteMap.getDescIterator()) {
                while (iterator.hasNext() && count < step) {
                    key = iterator.next();
                    Object item = map.get(key);
                    if (item == null)
                        // это может быть так как пока еще не вся цепочка засосалась но Избранные уже заданы
                        continue;
                    list.add(item);
                    count++; // только теперь счетчик увеличим - иначе пустые сбивают счет
                }
            } catch (IOException e) {
            }
        } else {
            try (IteratorCloseable iterator = favoriteMap.getDescIterator()) {
                while (iterator.hasNext() && count < step) {
                    key = iterator.next();
                    Object item = map.get(key);
                    if (item == null)
                        // это может быть так как пока еще не вся цепочка засосалась но Избранные уже заданы
                        continue;
                    list.add(item);
                    count++; // только теперь счетчик увеличим - иначе пустые сбивают счет
                }
            } catch (IOException e) {
            }
        }
    }

    public void addObservers() {

        if (Controller.getInstance().doesWalletDatabaseExists()) {
            favoriteMap.addObserver(this);
        } else {
            // ожидаем открытия кошелька
            Controller.getInstance().getWallet().addWaitingObserver(this);
        }
    }

    public void deleteObservers() {
        if (Controller.getInstance().doesWalletDatabaseExists())
            favoriteMap.deleteObserver(this);
    }

}
