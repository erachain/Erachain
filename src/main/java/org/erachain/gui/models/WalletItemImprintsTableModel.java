package org.erachain.gui.models;
////////

import org.erachain.controller.Controller;
import org.erachain.core.item.imprints.ImprintCls;
import org.erachain.database.SortableList;
import org.erachain.datachain.DCSet;
import org.erachain.utils.ObserverMessage;
import org.mapdb.Fun.Tuple2;

import java.util.Observable;
import java.util.Observer;

@SuppressWarnings("serial")
public class WalletItemImprintsTableModel extends SortedListTableModelCls<Tuple2<String, String>, ImprintCls> implements Observer {
    public static final int COLUMN_KEY = 0;
    public static final int COLUMN_NAME = 1;
    public static final int COLUMN_ADDRESS = 2;
    public static final int COLUMN_CONFIRMED = 3;
    public static final int COLUMN_FAVORITE = 4;

    public WalletItemImprintsTableModel() {
        super(Controller.getInstance().wallet.database.getImprintMap(), "WalletItemImprintsTableModel", 1000,
                new String[]{"Key", "Name", "Owner", "Confirmed", "Favorite"},
                new Boolean[]{false, true, true, false}, false);

        addObservers();
    }

    @Override
    public Object getValueAt(int row, int column) {
        if (this.listSorted == null || row > this.listSorted.size() - 1) {
            return null;
        }

        ImprintCls imprint = this.listSorted.get(row).getB();

        switch (column) {
            case COLUMN_KEY:

                return imprint.getKey(DCSet.getInstance());

            case COLUMN_NAME:

                return imprint.viewName();

            case COLUMN_ADDRESS:

                return imprint.getOwner().getPersonAsString();

            case COLUMN_CONFIRMED:

                return imprint.isConfirmed();
            case COLUMN_FAVORITE:

                return imprint.isFavorite();
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    public synchronized void syncUpdate(Observable o, Object arg) {
        ObserverMessage message = (ObserverMessage) arg;

        //CHECK IF NEW LIST
        if (message.getType() == ObserverMessage.LIST_IMPRINT_TYPE
                || message.getType() == ObserverMessage.WALLET_LIST_IMPRINT_TYPE) {

            needUpdate = false;
            getInterval();
            this.fireTableDataChanged();
        }

        //CHECK IF LIST UPDATED
        if (message.getType() == ObserverMessage.ADD_IMPRINT_TYPE || message.getType() == ObserverMessage.REMOVE_IMPRINT_TYPE
                || message.getType() == ObserverMessage.WALLET_ADD_IMPRINT_TYPE || message.getType() == ObserverMessage.WALLET_REMOVE_IMPRINT_TYPE) {
            needUpdate = true;
        }
    }

    // TODO сделать постранично
    @Override
    public void getInterval() {
        listSorted = new SortableList<Tuple2<String, String>, ImprintCls>(
                map, map.getKeys());
    }

    public void addObservers() {

        super.addObservers();

        if (Controller.getInstance().doesWalletDatabaseExists())
            return;

        map.addObserver(this);

    }

    public void deleteObservers() {
        super.deleteObservers();

        if (Controller.getInstance().doesWalletDatabaseExists())
            return;

        map.deleteObserver(this);
    }
}
