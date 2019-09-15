package org.erachain.gui.models;

import org.erachain.controller.Controller;
import org.erachain.dbs.DBMapCommonImpl;
import org.erachain.gui.ObserverWaiter;

@SuppressWarnings("serial")
public abstract class WalletSortedTableModel<T, U> extends SortedListTableModelCls<T, U> implements ObserverWaiter {

    public WalletSortedTableModel(DBMapCommonImpl map, String[] columnNames, Boolean[] column_AutoHeight, boolean descending) {
        super(map, columnNames, column_AutoHeight, descending);

        addObservers();
    }


    public void addObservers() {

        if (Controller.getInstance().doesWalletDatabaseExists()) {
            super.addObservers();
            map.addObserver(this);
        } else {
            // ожидаем открытия кошелька
            Controller.getInstance().wallet.addWaitingObserver(this);
        }
    }

    public void deleteObservers() {
        if (Controller.getInstance().doesWalletDatabaseExists()) {
            super.deleteObservers();
            map.deleteObserver(this);
        }
    }
}
