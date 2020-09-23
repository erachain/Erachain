package org.erachain.gui.library;


import org.erachain.controller.Controller;
import org.erachain.database.wallet.FavoriteAccountsMap;
import org.erachain.dbs.DBTab;
import org.erachain.dbs.IteratorCloseable;
import org.erachain.gui.ObserverWaiter;
import org.erachain.utils.MenuPopupUtil;
import org.erachain.utils.ObserverMessage;
import org.erachain.utils.Pair;
import org.mapdb.Fun;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.io.IOException;
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
    private String selectedItem = "";
    private JTextField comboTextField;
    private RecipientAddressInterface worker = null;


    public RecipientAddress(RecipientAddressInterface item) {
        RecipientModel model = new RecipientModel();
        this.setModel(model);
        this.setEditable(true);
        worker = item;

// select & edit text account
        comboTextField = (JTextField) this.getEditor().getEditorComponent();
        MenuPopupUtil.installContextMenu(comboTextField);
        comboTextField.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                lifework(e);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                lifework(e);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                lifework(e);
            }
        });
    }

    private void lifework(DocumentEvent e){
        selectedItem = comboTextField.getText();
        if (worker != null) {
            try {
                worker.recipientAddressWorker(selectedItem);
            } catch (Exception ex) {
                // ex.printStackTrace();
            }
        }

    }

    public String getSelectedAddress() {
        return this.selectedItem;
    }

    public void setSelectedAddress(String address) {
        this.selectedItem = address;
    }

    // model class
    class RecipientModel extends DefaultComboBoxModel<String> implements Observer, ObserverWaiter {
        protected FavoriteAccountsMap favoriteMap;

        public RecipientModel() {
            favoriteMap = Controller.getInstance().wallet.database.getFavoriteAccountsMap();
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
            //CHECK IF LIST UPDATE
            if (type == LIST_EVENT || type == RESET_EVENT) {
                sortAndAdd();
            } else if (type == ADD_EVENT) {
                Pair<String, Fun.Tuple3<String, String, String>> pair = (Pair<String, Fun.Tuple3<String, String, String>>) message.getValue();
                this.addElement(pair.getB().b + " " + pair.getA());

            } else if (type == DELETE_EVENT) {
                Pair<String, Fun.Tuple3<String, String, String>> pair = (Pair<String, Fun.Tuple3<String, String, String>>) message.getValue();
                this.removeElement(pair.getB().b + " " + pair.getA());
            }
        }

        @Override
        public String getElementAt(int row) {
            return super.getElementAt(row);
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
            try (IteratorCloseable<String> iterator = favoriteMap.getIterator()) {
                while (iterator.hasNext()) {
                    String key = iterator.next();
                    Fun.Tuple3<String, String, String> item = favoriteMap.get(key);
                    if (item == null) {
                        this.addElement(key);
                    } else {
                        this.addElement(item.b + " - " + key);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void removeObservers() {
            if (Controller.getInstance().doesWalletDatabaseExists())
                favoriteMap.deleteObserver(this);
        }

    }

    public interface RecipientAddressInterface {
        public void recipientAddressWorker(String e);
    }

}