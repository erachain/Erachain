package org.erachain.gui.items;

import org.erachain.dbs.DBTabImpl;
import org.erachain.gui.models.WalletTableModel;

abstract public class WalletItemTableModel<T> extends WalletTableModel<T> {

    public WalletItemTableModel(DBTabImpl map, String[] columnNames, Boolean[] column_AutoHeight, int columnFavorite, boolean descending) {
        super(map, columnNames, column_AutoHeight, descending, columnFavorite);

    }

    public WalletItemTableModel(String[] columnNames, Boolean[] columnAutoHeight, boolean descending) {
        super(columnNames, columnAutoHeight, descending);
    }
}
