package org.erachain.gui.models;

import org.erachain.controller.Controller;
import org.erachain.core.item.ItemCls;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.database.DBMap;
import org.erachain.database.wallet.FavoriteItemMap;
import org.erachain.gui.ObserverWaiter;
import org.erachain.utils.ObserverMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@SuppressWarnings("serial")
public abstract class FavoriteComboBoxModel extends DefaultComboBoxModel<ItemCls> implements Observer, ObserverWaiter {

    Lock lock = new ReentrantLock();
    protected Observable observable;

    protected Logger logger;

    private int RESET_EVENT;
    private int ADD_EVENT;
    private int DELETE_EVENT;
    private int LIST_EVENT;

    private final int item_type;

    public FavoriteComboBoxModel(int item_type) {
        this.item_type = item_type;

        logger = LoggerFactory.getLogger(this.getClass().getName());
        addObservers();
    }

    @Override
    public void update(Observable o, Object arg) {
        try {
            if (lock.tryLock()) {
                try {
                    this.syncUpdate(o, arg);
                } finally {
                    lock.unlock();
                }
            }

        } catch (Exception e) {
            //GUI ERROR
        }
    }

    public synchronized void syncUpdate(Observable o, Object arg) {
        ObserverMessage message = (ObserverMessage) arg;

        int type = message.getType();
        //CHECK IF LIST UPDATED
        if (type == LIST_EVENT) {
            sortAndAdd();
        } else if (type == ADD_EVENT) {
            this.addElement(getElementByEvent((Long) message.getValue()));

        } else if (type == DELETE_EVENT) {
            this.removeElement(getElementByEvent((Long) message.getValue()));

        }
    }

    public ItemCls getElementByEvent(Long key) {
        return Controller.getInstance().getItem(item_type, key);
    }

    public abstract void setObservable();

    // public abstract void sortAndAdd();
    public void sortAndAdd() {
        //GET SELECTED ITEM
        ItemCls selected = (ItemCls) this.getSelectedItem();
        int selectedIndex = -1;

        //EMPTY LIST
        this.removeAllElements();

        //INSERT ALL ITEMS
        Collection<Long> keys = ((FavoriteItemMap)observable).getFromToKeys(0, 9999999);
        List<ItemCls> items = new ArrayList<ItemCls>();
        int i = 0;
        for (Long key : keys) {
            if (key == 0
                    || key > 2 && key < 10
            )
                continue;

            //GET ASSET
            ItemCls item = Controller.getInstance().getItem(item_type, key);
            if (item == null)
                continue;

            items.add(item);

            //ADD

            this.addElement(item);

            if (selected != null && item.getKey() == selected.getKey()) {
                selectedIndex = i;
                selected = item; // need for SELECT as OBJECT
            }

            i++;
        }

        //RESET SELECTED ITEM
        if (this.getIndexOf(selected) != -1) {
            for (ItemCls item : items) {
                if (item.getKey() == selected.getKey()) {
                    this.setSelectedItem(item);
                    return;
                }
            }
        }

    }


    public void addObservers() {
        if (Controller.getInstance().doesWalletDatabaseExists()) {

            setObservable();

            LIST_EVENT = (Integer) ((FavoriteItemMap)observable).getObserverEvent();

            //RESET_EVENT = (Integer) ((DBMap)observable).getObservableData().get(DBMap.NOTIFY_RESET);
            //LIST_EVENT = (Integer) ((DBMap)observable).getObservableData().get(DBMap.NOTIFY_LIST);
            //ADD_EVENT = (Integer) ((DBMap)observable).getObservableData().get(DBMap.NOTIFY_ADD);
            //DELETE_EVENT = (Integer) ((DBMap)observable).getObservableData().get(DBMap.NOTIFY_REMOVE);

            sortAndAdd();

        } else {
            // ожидаем открытия кошелька
            Controller.getInstance().wallet.addWaitingObserver(this);
        }

    }


    public void removeObservers() {
        if (Controller.getInstance().doesWalletDatabaseExists())
            observable.deleteObserver(this);
    }

}
