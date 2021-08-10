package org.erachain.gui.models;

import org.erachain.controller.Controller;
import org.erachain.dbs.DBTabImpl;
import org.erachain.gui.ObserverWaiter;
import org.erachain.utils.ObserverMessage;

import java.util.ArrayList;
import java.util.Observable;

@SuppressWarnings("serial")
/**
 * необходима для правильного запуска событий пока не открылся кошелек - обсерверы в очереди на подключение ждут
 */
public abstract class WalletTableModel<T> extends TimerTableModelCls<T> implements ObserverWaiter {

    public WalletTableModel(DBTabImpl map, String[] columnNames, Boolean[] column_AutoHeight, boolean descending, int columnFavorite) {
        super(map, columnNames, column_AutoHeight, columnFavorite, descending);
        updateMap();
        addObservers();
    }

    public WalletTableModel(String[] columnNames, Boolean[] columnAutoHeight, boolean descending) {
        super(columnNames, columnAutoHeight, descending);
    }

    protected void clearMap() {
        map = null;
    }

    protected abstract void updateMap();

    @Override
    public void update(Observable o, Object arg) {

        ObserverMessage message = (ObserverMessage) arg;
        if (message.getType() == ObserverMessage.WALLET_DB_CLOSED) {
            needUpdate = false;
            list = new ArrayList<>();
            clearMap();
            this.fireTableDataChanged();
            return;
        } else if (message.getType() == ObserverMessage.WALLET_DB_OPEN) {
            needUpdate = false;
            updateMap();
            getInterval();
            this.fireTableDataChanged();
            return;
        }
        super.update(o, arg);
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
