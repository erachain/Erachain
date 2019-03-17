package org.erachain.gui.models;

import org.erachain.controller.Controller;
import org.erachain.core.transaction.Transaction;
import org.erachain.database.SortableList;
import org.mapdb.Fun;

import javax.swing.table.AbstractTableModel;
import java.util.Timer;
import java.util.TimerTask;

@SuppressWarnings("serial")
public abstract class TableModelCls<T, U> extends TimerTableModelCls {

    public abstract SortableList<T, U> getSortableList();

    public TableModelCls(String[] columnNames) {
        super(columnNames);
    }

    public TableModelCls(String name, long timeout, String[] columnNames) {
        super(name, timeout, columnNames);
    }

}
