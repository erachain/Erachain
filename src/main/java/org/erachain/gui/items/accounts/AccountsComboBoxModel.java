package org.erachain.gui.items.accounts;

import org.erachain.controller.Controller;
import org.erachain.core.account.Account;
import org.erachain.datachain.DCSet;
import org.erachain.gui.ObserverWaiter;
import org.erachain.utils.ObserverMessage;
import org.mapdb.Fun.Tuple3;

import javax.swing.*;
import java.util.Observable;
import java.util.Observer;
import java.util.Stack;
import java.util.TreeMap;

@SuppressWarnings("serial")
public class AccountsComboBoxModel extends DefaultComboBoxModel implements Observer, ObserverWaiter {

    private Observable observable;
    long personKey;
    TreeMap<String, Stack<Tuple3<Integer, Integer, Integer>>> addresses;
    /**
     * сортирует и выводит балансы в заданной позиции
     * @param personKey
     */
    public AccountsComboBoxModel(long personKey) {
        this.personKey = personKey;
        addresses = DCSet.getInstance().getPersonAddressMap().getItems(personKey);
        observable = Controller.getInstance().getWallet().dwSet.getAccountMap();

        synchronized (addresses) {
            sortAndAdd();
        }

        addObservers();

    }

    @Override
    public void update(Observable o, Object arg) {
        try {
            this.syncUpdate(o, arg);
        } catch (Exception e) {
            //GUI ERROR
        }
    }

    public synchronized void syncUpdate(Observable o, Object arg) {
        ObserverMessage message = (ObserverMessage) arg;

        //CHECK IF LIST UPDATED
        if (message.getType() == ObserverMessage.WALLET_LIST_TRANSACTION_TYPE
                || message.getType() == ObserverMessage.ADD_ACCOUNT_TYPE || message.getType() == ObserverMessage.REMOVE_ACCOUNT_TYPE) {
            addresses = DCSet.getInstance().getPersonAddressMap().getItems(personKey);
            //	this.fireTableDataChanged();

            //GET SELECTED ITEM
            Account selected = (Account) this.getSelectedItem();

            //EMPTY LIST
            this.removeAllElements();

            //INSERT ALL ACCOUNTS
            sortAndAdd();

            //RESET SELECTED ITEM
            if (this.getIndexOf(selected) != -1) {
                this.setSelectedItem(selected);
            }

        }
    }


    //SORTING BY BALANCE (BIGGEST BALANCE FIRST)
    private void sortAndAdd() {
        for (String address : addresses.keySet()) {
            this.addElement(new Account(address));
        }
    }

    public void addObservers() {
        if (Controller.getInstance().doesWalletDatabaseExists()) {
            observable.addObserver(this);
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
