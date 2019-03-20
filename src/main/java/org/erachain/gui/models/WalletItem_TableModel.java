package org.erachain.gui.models;

import org.erachain.controller.Controller;
import org.erachain.core.item.ItemCls;
import org.erachain.database.SortableList;
import org.erachain.datachain.DCSet;
import org.erachain.lang.Lang;
import org.mapdb.Fun.Tuple2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Observable;
import java.util.Observer;

@SuppressWarnings("serial")
public abstract class WalletItem_TableModel extends TableModelCls<Tuple2<String, String>, ItemCls> implements Observer {
    public static final int COLUMN_KEY = 0;
    public static final int COLUMN_NAME = 1;
    public static final int COLUMN_ADDRESS = 2;
    public static final int COLUMN_CONFIRMED = 3;
    static Logger LOGGER = LoggerFactory.getLogger(WalletItem_TableModel.class.getName());
    protected SortableList<Tuple2<String, String>, ItemCls> items;

    protected int observer_add;
    protected int observer_remove;
    protected int observer_list;

    public WalletItem_TableModel(String name, int observer_add, int observer_remove, int observer_list) {
        super(name, 1000, new String[]{"Key", "Name", "Owner", "Confirmed"});
        Controller.getInstance().addWalletObserver(this);
        this.observer_add = observer_add;
        this.observer_remove = observer_remove;
        this.observer_list = observer_list;
        this.fireTableDataChanged();
    }

    @Override
    public SortableList<Tuple2<String, String>, ItemCls> getSortableList() {
        return this.items;
    }

    public ItemCls getItem(int row) {
        return this.items.get(row).getB();
    }

    @Override
    public int getRowCount() {
        return this.items.size();
    }

    @Override
    public Object getValueAt(int row, int column) {
        if (this.items == null || row > this.items.size() - 1) {
            return null;
        }

        ItemCls item = this.items.get(row).getB();

        switch (column) {
            case COLUMN_KEY:

                return item.getKey(DCSet.getInstance());

            case COLUMN_NAME:

                return item.viewName();

            case COLUMN_ADDRESS:

                return item.getOwner().getPersonAsString();

            case COLUMN_CONFIRMED:

                return item.isConfirmed();

        }

        return null;
    }

    @Override
    public void update(Observable o, Object arg) {
        try {
            this.syncUpdate(o, arg);
        } catch (Exception e) {
            LOGGER.error(Lang.getInstance().translate("GUI update ERROR"), e);
        }
    }

    //@SuppressWarnings("unchecked")
    public abstract void syncUpdate(Observable o, Object arg);

}
