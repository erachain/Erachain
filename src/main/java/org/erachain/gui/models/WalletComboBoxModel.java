package org.erachain.gui.models;

import org.erachain.controller.Controller;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.database.DBMap;
import org.erachain.gui.ObserverWaiter;
import org.erachain.utils.ObserverMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@SuppressWarnings("serial")
public abstract class WalletComboBoxModel<U> extends DefaultComboBoxModel<U> implements Observer, ObserverWaiter {

    Lock lock = new ReentrantLock();
    protected Observable observable;

    protected Logger logger;

    private int RESET_EVENT;
    private int ADD_EVENT;
    private int DELETE_EVENT;
    private int LIST_EVENT;

    public WalletComboBoxModel() {
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
            this.addElement(getElementByEvent((long) message.getValue()));

        } else if (type == DELETE_EVENT) {
            this.removeElement(getElementByEvent((long) message.getValue()));

        }
    }

    public abstract U getElementByEvent(Object key);

    public abstract void setObservable();

    public abstract void sortAndAdd();

    public void addObservers() {
        if (Controller.getInstance().doesWalletDatabaseExists()) {
            setObservable();

            RESET_EVENT = (Integer) ((DBMap)observable).getObservableData().get(DBMap.NOTIFY_RESET);
            LIST_EVENT = (Integer) ((DBMap)observable).getObservableData().get(DBMap.NOTIFY_LIST);
            ADD_EVENT = (Integer) ((DBMap)observable).getObservableData().get(DBMap.NOTIFY_ADD);
            DELETE_EVENT = (Integer) ((DBMap)observable).getObservableData().get(DBMap.NOTIFY_REMOVE);

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
