package org.erachain.gui.models;

import org.erachain.database.DBMap;
import org.erachain.database.SortableList;

@SuppressWarnings("serial")
public abstract class TableModelCls<T, U> extends TimerTableModelCls<U> {

    protected SortableList<T, U> listSorted;

    public int COLUMN_FAVORITE = 1000;

    public TableModelCls(String[] columnNames, boolean descending) {
        super(columnNames, descending);
    }

    public TableModelCls(DBMap map, String[] columnNames, boolean descending) {
        super(map, columnNames, descending);
    }

    public TableModelCls(String[] columnNames, Boolean[] column_AutoHeight, boolean descending) {
        super(columnNames, column_AutoHeight, descending);
    }

    public TableModelCls(DBMap map, String[] columnNames, Boolean[] column_AutoHeight, boolean descending) {
        super(map, columnNames, column_AutoHeight, descending);
    }

    public TableModelCls(DBMap map, String[] columnNames, Boolean[] column_AutoHeight, int favoriteColumn, boolean descending) {
        super(map, columnNames, column_AutoHeight, descending);
        this.COLUMN_FAVORITE = favoriteColumn;

    }

    public TableModelCls(DBMap map, String name, long timeout, String[] columnNames, Boolean[] column_AutoHeight, boolean descending) {
        super(map, name, timeout, columnNames, column_AutoHeight, descending);
    }

    public SortableList<T, U> getSortableList() {
        return listSorted;
    }

}
