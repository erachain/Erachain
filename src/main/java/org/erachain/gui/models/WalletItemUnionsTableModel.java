package org.erachain.gui.models;
////////

import org.erachain.controller.Controller;
import org.erachain.core.item.unions.UnionCls;
import org.erachain.datachain.DCSet;

@SuppressWarnings("serial")
public class WalletItemUnionsTableModel extends WalletTableModel<UnionCls> {
    public static final int COLUMN_KEY = 0;
    public static final int COLUMN_NAME = 1;
    public static final int COLUMN_ADDRESS = 2;
    public static final int COLUMN_CONFIRMED = 3;
    public static final int COLUMN_FAVORITE = 4;

    public WalletItemUnionsTableModel() {
        super(Controller.getInstance().wallet.database.getUnionMap(),
                new String[]{"Key", "Name", "Creator", "Confirmed", "Favorite"},
                new Boolean[]{false, true, true, false, false}, true, COLUMN_FAVORITE);

    }

    @Override
    public Object getValueAt(int row, int column) {
        if (this.list == null || row > this.list.size() - 1) {
            return null;
        }

        UnionCls union = this.list.get(row);

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

}
