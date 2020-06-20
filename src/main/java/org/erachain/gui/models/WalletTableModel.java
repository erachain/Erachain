package org.erachain.gui.models;

import org.erachain.controller.Controller;
import org.erachain.dbs.DBTabImpl;
import org.erachain.gui.ObserverWaiter;

@SuppressWarnings("serial")
/**
 * необходима для правильного запуска событий пока не открылся кошелек - обсерверы в очереди на подключение ждут
 */
public abstract class WalletTableModel<T> extends TimerTableModelCls<T> implements ObserverWaiter {

    public WalletTableModel(DBTabImpl map, String[] columnNames, Boolean[] column_AutoHeight, boolean descending) {
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
        Controller.getInstance().wallet.removeWaitingObserver(this);
    }
}
