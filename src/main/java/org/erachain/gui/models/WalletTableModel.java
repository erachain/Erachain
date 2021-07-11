package org.erachain.gui.models;

import org.erachain.controller.Controller;
import org.erachain.dbs.DBTabImpl;
import org.erachain.gui.ObserverWaiter;

@SuppressWarnings("serial")
/**
 * необходима для правильного запуска событий пока не открылся кошелек - обсерверы в очереди на подключение ждут
 */
public abstract class WalletTableModel<T> extends TimerTableModelCls<T> implements ObserverWaiter {

    public WalletTableModel(DBTabImpl map, String[] columnNames, Boolean[] column_AutoHeight, boolean descending, int columnFavorite) {
        super(map, columnNames, column_AutoHeight, columnFavorite, descending);

        addObservers();
    }

    public WalletTableModel(String[] columnNames, Boolean[] columnAutoHeight, boolean descending) {
        super(columnNames, columnAutoHeight, descending);
    }

    public void addObservers() {

        if (Controller.getInstance().doesWalletDatabaseExists()) {
            super.addObservers();
            ///map.addObserver(this);
        } else {
            // ожидаем открытия кошелька
            Controller.getInstance().getWallet().addWaitingObserver(this);
        }
        dcSet.getBlockMap().addObserver(this); // for update CONFIRMS

    }

    public void deleteObservers() {
        if (Controller.getInstance().doesWalletDatabaseExists()) {
            super.deleteObservers();
            map.deleteObserver(this);
        }
        Controller.getInstance().getWallet().removeWaitingObserver(this);
        dcSet.getBlockMap().deleteObserver(this); // for update CONFIRMS
    }
}
