package org.erachain.gui.items.accounts;

import org.erachain.controller.Controller;
import org.erachain.core.account.Account;
import org.erachain.datachain.DCSet;
import org.erachain.gui.ObserverWaiter;
import org.mapdb.Fun.Tuple3;
import org.erachain.utils.AccountBalanceComparator;
import org.erachain.utils.ObserverMessage;

import javax.swing.*;
import java.util.*;

@SuppressWarnings("serial")
public class AccountsComboBoxModel extends DefaultComboBoxModel implements Observer, ObserverWaiter {

    private Observable observable;
    long key_person_table;
    TreeMap<String, Stack<Tuple3<Integer, Integer, Integer>>> addresses;

    public AccountsComboBoxModel(long person_Key) {
        //INSERT ALL ACCOUNTS
		List<Account> accounts = Controller.getInstance().getAccounts();

        key_person_table = person_Key;
        addresses = DCSet.getInstance().getPersonAddressMap().getItems(key_person_table);
        observable = Controller.getInstance().wallet.database.getAccountMap();

        addObservers();

        synchronized (addresses) {
            sortAndAdd();
        }
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
            addresses = DCSet.getInstance().getPersonAddressMap().getItems(key_person_table);
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
        //TO AVOID PROBLEMS WE DON'T WANT TO SORT THE ORIGINAL LIST!
        ArrayList<Account> accoountsToSort = new ArrayList<Account>(Controller.getInstance().getAccounts());
        Collections.sort(accoountsToSort, new AccountBalanceComparator());
        Collections.reverse(accoountsToSort);
//		for(Account account: accoountsToSort)
//		{
//			this.addElement(account);
//		}

        for (String addrses_key : addresses.keySet()) {

            this.addElement(addrses_key);

        }

    }

    public void addObservers() {
        if (Controller.getInstance().doesWalletDatabaseExists()) {
            observable.addObserver(this);
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
