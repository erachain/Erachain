package org.erachain.gui.models;

import org.erachain.database.DBMap;
import org.erachain.database.SortableList;

@SuppressWarnings("serial")
public abstract class TableModelCls<T, U> extends TimerTableModelCls<T, U> {

    public int COLUMN_FAVORITE = 1000;

    public abstract SortableList<T, U> getSortableList();

    public TableModelCls(String[] columnNames) {
        super(columnNames);
    }

    public TableModelCls(DBMap map, String[] columnNames) {
        super(map, columnNames);
    }

    public TableModelCls(String[] columnNames, Boolean[] column_AutoHeight) {
        super(columnNames, column_AutoHeight);
    }

    public TableModelCls(DBMap map, String[] columnNames, Boolean[] column_AutoHeight) {
        super(map, columnNames, column_AutoHeight);
    }

    public TableModelCls(DBMap map, String name, long timeout, String[] columnNames, Boolean[] column_AutoHeight) {
        super(map, name, timeout, columnNames, column_AutoHeight);
    }

}
