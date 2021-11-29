package org.erachain.gui.models;

import org.erachain.controller.Controller;
import org.erachain.core.item.ItemCls;
import org.erachain.database.wallet.FavoriteItemMap;
import org.erachain.dbs.IteratorCloseable;
import org.erachain.gui.ObserverWaiter;
import org.erachain.utils.ObserverMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
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

    protected final int item_type;
    protected List<ItemCls> items = new ArrayList<ItemCls>();

    public FavoriteComboBoxModel(int item_type) {
        this.item_type = item_type;

        logger = LoggerFactory.getLogger(this.getClass());
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

    protected void addElementFiltered(Object o) {
        ItemCls item = getElementByEvent((Long) o);
        if (item == null)
            return;

        items.add(item);
        super.addElement(item);
    }

    public synchronized void syncUpdate(Observable o, Object arg) {
        ObserverMessage message = (ObserverMessage) arg;

        int type = message.getType();
        //CHECK IF LIST UPDATED
        if (type == LIST_EVENT || type == RESET_EVENT) {
            sortAndAdd();
        } else if (type == ADD_EVENT) {
            addElementFiltered(message.getValue());
        } else if (type == DELETE_EVENT) {
            this.removeElement(getElementByEvent((Long) message.getValue()));
        }
    }

    public ItemCls getElementByEvent(Long key) {
        return Controller.getInstance().getItem(item_type, key);
    }

    public abstract void setObservable();

    public boolean filter(ItemCls item) {
        return true;
    }

    // public abstract void sortAndAdd();
    public void sortAndAdd() {
        //GET SELECTED ITEM
        ItemCls selected = (ItemCls) this.getSelectedItem();

        //EMPTY LIST
        this.removeAllElements();

        //INSERT ALL ITEMS
        try (IteratorCloseable<Long> iterator = ((FavoriteItemMap) observable).getIterator()) {
            while (iterator.hasNext()) {

                //GET ASSET
                ItemCls item = Controller.getInstance().getItem(item_type, iterator.next());
                if (item == null || !filter(item))
                    continue;

                items.add(item);

                //ADD

                addElement(item);

                if (selected != null && item.getKey() == selected.getKey()) {
                    selected = item; // need for SELECT as OBJECT
                }

            }
        } catch (IOException e) {
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

    public static class IconListRenderer extends DefaultListCellRenderer{
        private static final long serialVersionUID = 1L;

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus)
        {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            if (value == null)
                return label;

            // Get icon to use for the list item value
            ImageIcon image = ((ItemCls) value).getImageIcon();
            if (image != null) {
                int size = UIManager.getFont("TextField.font").getSize() + 4;
                Image Im = image.getImage().getScaledInstance(size, size, 1);
                label.setIcon(new ImageIcon(Im));
            }

            return label;
        }
    }

    public void addObservers() {
        if (Controller.getInstance().doesWalletDatabaseExists()) {

            setObservable();

            LIST_EVENT = ((FavoriteItemMap) observable).getObserverEvent();
            observable.addObserver(this);

            //RESET_EVENT = (Integer) ((FavoriteItemMap)observable).getObservableData().get(DBMap.NOTIFY_RESET);
            //LIST_EVENT = (Integer) ((FavoriteItemMap)observable).getObservableData().get(DBMap.NOTIFY_LIST);
            //ADD_EVENT = (Integer) ((FavoriteItemMap)observable).getObservableData().get(DBMap.NOTIFY_ADD);
            //DELETE_EVENT = (Integer) ((FavoriteItemMap)observable).getObservableData().get(DBMap.NOTIFY_REMOVE);

            sortAndAdd();

        } else {
            // ожидаем открытия кошелька
            Controller.getInstance().getWallet().addWaitingObserver(this);
        }

    }

    public void removeObservers() {
        if (Controller.getInstance().doesWalletDatabaseExists())
            observable.deleteObserver(this);
    }

}
