package org.erachain.gui.models;

import org.erachain.datachain.SortableList;

import javax.swing.table.AbstractTableModel;

@SuppressWarnings("serial")
public abstract class TableModelCls<T, U> extends AbstractTableModel {

    public int COLUMN_FAVORITE = 1000;

    public abstract SortableList<T, U> getSortableList();

    public abstract Object getItem(int k);
}
