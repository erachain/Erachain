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

    private int start = 0;
    private int step = 50;
    private int size = 0;

    public abstract SortableList<T, U> getSortableList();

    public TableModelCls(String[] columnNames) {
        super(columnNames);
    }

    public TableModelCls(String name, long timeout, String[] columnNames) {
        super(name, timeout, columnNames);
    }

    //public abstract void getIntervalThis(int startBack, int endBack);
    public void getIntervalThis(int startBack, int endBack) {
    }

    //public abstract int getMapSize();
    public int getMapSize() {
        return 0;
    }

    public void getInterval() {

        int startBack = -getMapSize() + start;
        getIntervalThis( startBack, startBack + step);

    }

    public void setInterval(int start, int step) {
        this.start = start;
        this.step = step;

        getInterval();
    }

}
