package org.erachain.gui.models;

import org.erachain.controller.Controller;
import org.erachain.dbs.DBMapCommonImpl;
import org.erachain.lang.Lang;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.table.AbstractTableModel;
import javax.validation.constraints.Null;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

@SuppressWarnings("serial")
public abstract class SearchTableModelCls<U> extends AbstractTableModel {

    private String name;
    private String[] columnNames;
    protected boolean needUpdate;
    protected boolean descending;

    //public int COLUMN_FAVORITE = 1000;

    protected String findMessage;

    protected List<U> list;

    protected Boolean[] columnAutoHeight;

    protected int start = 0;
    protected int step = 500;
    protected long size = 0;

    protected DBMapCommonImpl map;
    protected Logger logger;

    protected Controller cnt;

    public SearchTableModelCls(DBMapCommonImpl map, String[] columnNames, Boolean[] columnAutoHeight,
                               boolean descending) {
        logger = LoggerFactory.getLogger(this.getClass());
        this.map = map;
        this.columnNames = columnNames;
        this.columnAutoHeight = columnAutoHeight;
        this.descending = descending;
        //COLUMN_FAVORITE = columnFavorite;

        cnt = Controller.getInstance();
    }

    public Boolean[] getColumnAutoHeight() {
        return this.columnAutoHeight;
    }

    // устанавливаем колонки которым изменить высоту
    public void setColumnAutoHeight(Boolean[] arg0) {
        this.columnAutoHeight = arg0;
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    //     private String[] columnNames = Lang.getInstance().translate();
    public String getColumnName(int index) {
        return Lang.getInstance().translate(columnNames[index]);
    }

    public String getColumnNameOrigin(int index) {
        return columnNames[index];
    }

    public U getItem(int row) {
        if (list == null)
            return null;

        return this.list.get(row);
    }

    public int getRowCount() {
        return (this.list == null) ? 0 : this.list.size();
    }

    public abstract Object getValueAt(int row, int column);

    public Class<? extends Object> getColumnClass(int c) {
        Object o = getValueAt(0, c);
        return o == null ? Null.class : o.getClass();
    }

    //public abstract void getIntervalThis(int startBack, int endBack);
    public void getIntervalThis(long start, long end) {
    }

    public int getMapDefaultIndex() {
        if (map == null)
            return 0;

        return map.DEFAULT_INDEX;
    }

    public long getMapSize() {
        if (map == null)
            return 0;

        return map.size();
    }

    public synchronized void syncUpdate(Observable o, Object arg) {
    }

    /**
     * если descending установлен, то ключ отрицательный значит и его вычисляем обратно.
     * То есть 10-я запись имеет ключ -9 (отричательный). Тогда отсчет start=0 будет идти от последней записи
     * с отступом step
     */
    public void getInterval() {

        if (descending) {
            long startBack = -getMapSize() + start;
            getIntervalThis(startBack, startBack + step);
        } else {
            getIntervalThis(start, start + step);
        }

    }

    public void setInterval(int start, int step) {
        this.start = start;
        this.step = step;

        getInterval();
    }

    public void clear() {
        findMessage = null;
        list = new ArrayList<>();
        fireTableDataChanged();
    }

}
