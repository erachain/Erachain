package org.erachain.gui.models;

import org.erachain.database.DBMap;
import org.erachain.database.SortableList;
import org.erachain.utils.Pair;

@SuppressWarnings("serial")
public abstract class SortedListTableModelCls<T, U> extends TimerTableModelCls<U> {

    protected SortableList<T, U> list;

    public int COLUMN_FAVORITE = 1000;

    public SortedListTableModelCls(String[] columnNames, boolean descending) {
        super(columnNames, descending);
    }

    public SortedListTableModelCls(DBMap map, String[] columnNames, boolean descending) {
        super(map, columnNames, descending);
    }

    public SortedListTableModelCls(String[] columnNames, Boolean[] column_AutoHeight, boolean descending) {
        super(columnNames, column_AutoHeight, descending);
    }

    public SortedListTableModelCls(DBMap map, String[] columnNames, Boolean[] column_AutoHeight, boolean descending) {
        super(map, columnNames, column_AutoHeight, descending);
    }

    public SortedListTableModelCls(DBMap map, String[] columnNames, Boolean[] column_AutoHeight, int favoriteColumn, boolean descending) {
        super(map, columnNames, column_AutoHeight, descending);
        this.COLUMN_FAVORITE = favoriteColumn;

    }

    public SortedListTableModelCls(DBMap map, String name, long timeout, String[] columnNames, Boolean[] column_AutoHeight, boolean descending) {
        super(map, name, timeout, columnNames, column_AutoHeight, descending);
    }

    @Override
    public U getItem(int k) {
        return this.list.get(k).getB();
    }

    public Pair<T, U> getPairItem(int k) {
        return this.list.get(k);
    }

    public SortableList<T, U> getSortableList() {
        return list;
    }

    // необходимо переопределить так у супер класса по размеру простого списка
    @Override
    public int getRowCount() {
        if (list == null) {
            return 0;
        }

        return list.size();
    }


}
