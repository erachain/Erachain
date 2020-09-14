package org.erachain.gui.library;


import org.erachain.controller.Controller;
import org.erachain.database.wallet.FavoriteAccountsMap;
import org.erachain.dbs.DBTab;
import org.erachain.dbs.IteratorCloseable;
import org.erachain.gui.ObserverWaiter;
import org.erachain.lang.Lang;
import org.erachain.utils.ObserverMessage;
import org.erachain.utils.Pair;
import org.mapdb.Fun;

import javax.swing.*;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.text.DefaultEditorKit;
import java.awt.event.*;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class RecipientAddress extends JComboBox {

    Lock lock = new ReentrantLock();
    private int RESET_EVENT;
    private int ADD_EVENT;
    private int DELETE_EVENT;
    private int LIST_EVENT;
    protected Observable observable;
    private String selectedItem= "";

    public RecipientAddress() {
        RecipientModel model = new RecipientModel();
        this.setModel(model);
        this.setEditable(true);

// result
        this.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange()==1){
                    selectedItem =(String)e.getItem();
                }
            }
        });

        // menu


    }

    public String getSelectedAddress() {
        return (String) this.selectedItem;
    }

    public void showMenu(final MouseEvent e) {
        if (e.isPopupTrigger()) {
            this.requestFocus();
            final JPopupMenu menu = new JPopupMenu();
            JMenuItem item;
            item = new JMenuItem(new DefaultEditorKit.CopyAction());
            item.setText(Lang.getInstance().translate("Copy"));
            item.setEnabled(true);
            menu.add(item);
            item = new JMenuItem(new DefaultEditorKit.CutAction());
            item.setText(Lang.getInstance().translate("Cut"));
            item.setEnabled(true);
            menu.add(item);
            item = new JMenuItem(new DefaultEditorKit.PasteAction());
            item.setText(Lang.getInstance().translate("Paste"));
            item.setEnabled(this.isEditable());
            menu.add(item);
            menu.show(e.getComponent(), e.getX(), e.getY());
        }
    }

    // model
    class RecipientModel extends DefaultComboBoxModel<String> implements Observer, ObserverWaiter {
        protected FavoriteAccountsMap favoriteMap;

        public RecipientModel() {
            favoriteMap = Controller.getInstance().wallet.database.getFavoriteAccountsMap();
            addObservers();
            //          sortAndAdd();
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
            //CHECK IF LIST UPDATE
            if (type == LIST_EVENT || type == RESET_EVENT) {
                sortAndAdd();
            } else if (type == ADD_EVENT) {
                this.addElement(((Pair<String, Fun.Tuple3<String, String, String>>) message.getValue()).getA());

            } else if (type == DELETE_EVENT) {
                this.removeElement(((Pair<String, Fun.Tuple3<String, String, String>>) message.getValue()).getA());
            }
        }

        @Override
        public void addObservers() {
            if (Controller.getInstance().doesWalletDatabaseExists()) {
                Map<Integer, Integer> observersDBMap = favoriteMap.getObservableData();

                RESET_EVENT = observersDBMap.get(DBTab.NOTIFY_RESET);
                LIST_EVENT = observersDBMap.get(DBTab.NOTIFY_LIST);
                ADD_EVENT = observersDBMap.get(DBTab.NOTIFY_ADD);
                DELETE_EVENT = observersDBMap.get(DBTab.NOTIFY_REMOVE);

                favoriteMap.addObserver(this);
            } else {
                // ожидаем открытия кошелька
                Controller.getInstance().wallet.addWaitingObserver(this);
            }

        }

        private void sortAndAdd() {
            // add empty item
            this.addElement("");
            // add favorite address
            IteratorCloseable<String> iterator = favoriteMap.getIterator();
            while (iterator.hasNext()) {
                this.addElement(iterator.next());
            }
        }

        public void removeObservers() {
            if (Controller.getInstance().doesWalletDatabaseExists())
                favoriteMap.deleteObserver(this);
        }

    }

}