package org.erachain.gui.models;

import org.erachain.core.item.ItemCls;
import org.erachain.database.DBMap;
import org.erachain.database.SortableList;

import java.util.List;

@SuppressWarnings("serial")
public abstract class TableModelCls<T, U> extends TimerTableModelCls<T, U> {

    protected List<T> list;
    protected SortableList<T, U> listSorted;

    public int COLUMN_FAVORITE = 1000;

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

    public TableModelCls(DBMap map, String[] columnNames, Boolean[] column_AutoHeight, int favoriteColumn) {
        super(map, columnNames, column_AutoHeight);
        this.COLUMN_FAVORITE = favoriteColumn;

    }

    public TableModelCls(DBMap map, String name, long timeout, String[] columnNames, Boolean[] column_AutoHeight) {
        super(map, name, timeout, columnNames, column_AutoHeight);
    }

    public SortableList<T, U> getSortableList() {
        return listSorted;
    }

    public U getItem(int row) {
        return this.listSorted.get(row).getB();
    }

    public int getRowCount() {
        return (this.list == null) ? 0 : this.list.size();
    }


}
