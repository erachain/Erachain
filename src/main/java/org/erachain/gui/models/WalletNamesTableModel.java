package org.erachain.gui.models;

import org.erachain.controller.Controller;
import org.erachain.core.account.Account;
import org.erachain.core.naming.Name;
import org.erachain.database.SortableList;
import org.erachain.database.wallet.NameMap;
import org.erachain.lang.Lang;
import org.erachain.utils.ObserverMessage;
import org.erachain.utils.Pair;
import org.mapdb.Fun.Tuple2;

import java.util.Observable;
import java.util.Observer;

@SuppressWarnings("serial")
public class WalletNamesTableModel extends TableModelCls<Tuple2<String, String>, Name> implements Observer {
    public static final int COLUMN_NAME = 0;
    public static final int COLUMN_ADDRESS = 1;
    private static final int COLUMN_CONFIRMED = 2;

    private SortableList<Tuple2<String, String>, Name> names;

    public WalletNamesTableModel() {
        super("WalletNamesTableModel", 1000,
                new String[]{"Name", "Owner", "Confirmed"});
    }

    @Override
    public SortableList<Tuple2<String, String>, Name> getSortableList() {
        return this.names;
    }

    public Name getName(int row) {
        Pair<Tuple2<String, String>, Name> namepair = this.names.get(row);
        if (!namepair.getA().a.equalsIgnoreCase(namepair.getB().getOwner().getAddress())) {
            //inconsistency, owner was not updated correctly
            Name name = new Name(new Account(namepair.getA().a), namepair.getB().getName(), namepair.getB().getValue());
            return name;
        }
        return this.names.get(row).getB();
    }

    @Override
    public int getRowCount() {
        return this.names.size();
    }

    @Override
    public Object getValueAt(int row, int column) {
        if (this.names == null || row > this.names.size() - 1) {
            return null;
        }

        Name name = getName(row);

        switch (column) {
            case COLUMN_NAME:

                return name.getName();

            case COLUMN_ADDRESS:

                return name.getOwner().getPersonAsString();

            case COLUMN_CONFIRMED:

                return name.isConfirmed();

        }

        return null;
    }

    @Override
    public void update(Observable o, Object arg) {
        try {
            this.syncUpdate(o, arg);
        } catch (Exception e) {
            //GUI ERROR
        }
    }

    @SuppressWarnings("unchecked")
    public synchronized void syncUpdate(Observable o, Object arg) {
        ObserverMessage message = (ObserverMessage) arg;

        //CHECK IF NEW LIST
        if (message.getType() == ObserverMessage.LIST_NAME_TYPE) {
            if (this.names == null) {
                this.names = (SortableList<Tuple2<String, String>, Name>) message.getValue();
                this.names.registerObserver();
                this.names.sort(NameMap.NAME_INDEX);
            }

            this.fireTableDataChanged();
        }

        //CHECK IF LIST UPDATED
        if (message.getType() == ObserverMessage.ADD_NAME_TYPE || message.getType() == ObserverMessage.REMOVE_NAME_TYPE) {
            this.fireTableDataChanged();
        }
    }

    @Override
    public Object getItem(int k) {
        // TODO Auto-generated method stub
        Pair<Tuple2<String, String>, Name> namepair = this.names.get(k);
        if (!namepair.getA().a.equalsIgnoreCase(namepair.getB().getOwner().getAddress())) {
            //inconsistency, owner was not updated correctly
            Name name = new Name(new Account(namepair.getA().a), namepair.getB().getName(), namepair.getB().getValue());
            return name;
        }
        return this.names.get(k).getB();

    }

    public void addObserversThis() {
        Controller.getInstance().addWalletListener(this);
    }

    public void removeObserversThis() {
        Controller.getInstance().deleteObserver(this);

    }
}
