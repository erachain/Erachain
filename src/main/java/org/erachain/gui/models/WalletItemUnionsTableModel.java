package org.erachain.gui.models;
////////

import org.erachain.controller.Controller;
import org.erachain.core.item.unions.UnionCls;
import org.erachain.database.SortableList;
import org.erachain.datachain.DCSet;
import org.erachain.utils.ObserverMessage;
import org.mapdb.Fun.Tuple2;

import java.util.Observable;
import java.util.Observer;

@SuppressWarnings("serial")
public class WalletItemUnionsTableModel extends SortedListTableModelCls<Tuple2<String, String>, UnionCls> implements Observer {
    public static final int COLUMN_KEY = 0;
    public static final int COLUMN_NAME = 1;
    public static final int COLUMN_ADDRESS = 2;
    public static final int COLUMN_CONFIRMED = 3;
    public static final int COLUMN_FAVORITE = 4;

    public WalletItemUnionsTableModel() {
        super(Controller.getInstance().wallet.database.getUnionMap(),
                new String[]{"Key", "Name", "Creator", "Confirmed", "Favorite"},
                new Boolean[]{false, true, true, false, false}, true);

    }

    @Override
    public Object getValueAt(int row, int column) {
        if (this.unions == null || row > this.unions.size() - 1) {
            return null;
        }

        UnionCls union = this.unions.get(row).getB();

        switch (column) {
            case COLUMN_KEY:

                return union.getKey(DCSet.getInstance());

            case COLUMN_NAME:

                return union.viewName();

            case COLUMN_ADDRESS:

                return union.getOwner().getPersonAsString();

            case COLUMN_CONFIRMED:

                return union.isConfirmed();

            case COLUMN_FAVORITE:

                return union.isFavorite();

        }

        return null;
    }

    @SuppressWarnings("unchecked")
    public synchronized void syncUpdate(Observable o, Object arg) {
        ObserverMessage message = (ObserverMessage) arg;

        //CHECK IF NEW LIST
        if (message.getType() == ObserverMessage.LIST_UNION_TYPE) {
            if (this.listSorted == null) {
                this.listSorted = (SortableList<Tuple2<String, String>, UnionCls>) message.getValue();
                //this.unions.registerObserver();
                //this.unions.sort(PollMap.NAME_INDEX);
            }

            this.fireTableDataChanged();
        }

        //CHECK IF LIST UPDATED
        if (message.getType() == ObserverMessage.ADD_UNION_TYPE || message.getType() == ObserverMessage.REMOVE_UNION_TYPE) {
            this.fireTableDataChanged();
        }
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
