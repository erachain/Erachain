package org.erachain.gui.items.unions;

import org.erachain.controller.Controller;
import org.erachain.core.item.unions.UnionCls;
import org.erachain.datachain.DCSet;
import org.erachain.gui.items.SearchItemsTableModel;

@SuppressWarnings("serial")
public class TableModelUnionsItemsTableModel extends SearchItemsTableModel {
    public static final int COLUMN_KEY = 0;
    public static final int COLUMN_NAME = 1;
    public static final int COLUMN_ADDRESS = 2;
    public static final int COLUMN_FAVORITE = 3;

    public TableModelUnionsItemsTableModel() {
        super(DCSet.getInstance().getItemUnionMap(), new String[]{"Key", "Name", "Creator", "Favorite"},
                null,
                COLUMN_FAVORITE);
    }

    @Override
    protected void updateMap() {
        map = Controller.getInstance().getWallet().dwSet.getTransactionMap();
    }

    @Override
    public Object getValueAt(int row, int column) {
        if (this.list == null || row > this.list.size() - 1) {
            return null;
        }

        UnionCls union = (UnionCls) this.list.get(row);

        switch (column) {
            case COLUMN_KEY:

                return union.getKey();

            case COLUMN_NAME:

                return union;

            case COLUMN_ADDRESS:

                return union.getMaker().getPersonAsString();

            case COLUMN_FAVORITE:

                return union.isFavorite();

        }

        return null;
    }

}
