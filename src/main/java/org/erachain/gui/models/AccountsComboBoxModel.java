package org.erachain.gui.models;

import org.erachain.controller.Controller;
import org.erachain.core.account.Account;
import org.erachain.utils.AccountBalanceComparator;
import org.erachain.utils.ObserverMessage;

import javax.swing.*;
import java.util.*;

@SuppressWarnings("serial")
public class AccountsComboBoxModel extends DefaultComboBoxModel<Account> implements Observer {

    private int viewBalancePosition;

    public AccountsComboBoxModel(int viewBalancePosition) {
        this.viewBalancePosition = viewBalancePosition;

        //INSERT ALL ACCOUNTS
        List<Account> accounts = Controller.getInstance().getAccountsAndSetBalancePosition(viewBalancePosition);
        synchronized (accounts) {
            sortAndAdd();
        }

        if (Controller.getInstance().doesWalletDatabaseExists())
            Controller.getInstance().wallet.database.getAccountMap().addObserver(this);
    }

    /**
     * not show balance on accounts
     */
    public AccountsComboBoxModel() {
        this(0);
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

        if ((message.getType() == ObserverMessage.NETWORK_STATUS && (int) message.getValue() == Controller.STATUS_OK)
                || (Controller.getInstance().getStatus() == Controller.STATUS_OK
                        && (message.getType() == ObserverMessage.ADD_ACCOUNT_TYPE || message.getType() == ObserverMessage.REMOVE_ACCOUNT_TYPE
                            //|| message.getType() == ObserverMessage.ADD_BALANCE_TYPE || message.getType() == ObserverMessage.REMOVE_BALANCE_TYPE
                    ))
        ) {
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
        List<Account> accounts = Controller.getInstance().getAccountsAndSetBalancePosition(viewBalancePosition);
        for (Account account : accounts) {
            this.addElement(account);
        }
    }

    public void removeObservers() {
        if (Controller.getInstance().doesWalletDatabaseExists())
            Controller.getInstance().wallet.database.getAccountMap().deleteObserver(this);
    }
}
