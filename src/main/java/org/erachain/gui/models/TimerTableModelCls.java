package org.erachain.gui.models;

import org.erachain.database.DBMap;
import org.erachain.lang.Lang;
import org.slf4j.Logger;

import javax.swing.table.AbstractTableModel;
import javax.validation.constraints.Null;
import java.util.List;
import java.util.Observable;
import java.util.Timer;
import java.util.TimerTask;

@SuppressWarnings("serial")
public abstract class TimerTableModelCls<U> extends AbstractTableModel {

    private String name;
    private long timeout;
    private String[] columnNames;
    private Timer timer;
    protected boolean needUpdate;
    protected boolean descending;

    protected List<U> list;

    protected Boolean[] columnAutoHeight; // = new Boolean[]{true, true, true, true, true, true, true, false, false};

    protected long start = 0;
    protected int step = 50;
    protected long size = 0;

    protected DBMap map;
    protected Logger LOGGER;

    public TimerTableModelCls(String[] columnNames, boolean descending) {
        this.columnNames = columnNames;
        this.descending = descending;
    }

    public TimerTableModelCls(DBMap map, String[] columnNames, boolean descending) {
        this.map = map;
        this.columnNames = columnNames;
        this.descending = descending;
    }

    public TimerTableModelCls(String[] columnNames, Boolean[] columnAutoHeight, boolean descending) {
        this.columnNames = columnNames;
        this.columnAutoHeight = columnAutoHeight;
        this.descending = descending;
    }

    public TimerTableModelCls(DBMap map, String[] columnNames, Boolean[] columnAutoHeight, boolean descending) {
        this.map = map;
        this.columnNames = columnNames;
        this.columnAutoHeight = columnAutoHeight;
        this.descending = descending;
    }

    public TimerTableModelCls(DBMap map, String name, long timeout, String[] columnNames, Boolean[] columnAutoHeight, boolean descending) {
        this.map = map;
        this.columnNames = columnNames;
        this.name = name;
        this.timeout = timeout;
        this.columnAutoHeight = columnAutoHeight;
        this.descending = descending;
    }

    public void initTimer() {
        if (this.timer == null && name != null) {
            this.timer = new Timer(name);

            TimerTask action = new TimerTask() {
                public void run() {
                    try {
                        if (needUpdate) {
                            getInterval();
                            fireTableDataChanged();
                            needUpdate = false;
                        }
                    } catch (Exception e) {
                        //LOGGER.error(e.getMessage(),e);
                        String err = e.getMessage();
                    }
                }
            };

            this.timer.schedule(action, 100, timeout);
        }

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

    public abstract void syncUpdate(Observable o, Object arg);

    public void update(Observable o, Object arg) {
        try {
            this.syncUpdate(o, arg);
        } catch (Exception e) {
            if (LOGGER != null)
                LOGGER.error(e.getMessage(),e);
        }
    }

    //public abstract void getIntervalThis(int startBack, int endBack);
    public void getIntervalThis(long start, long end) {
    }

    //public abstract int getMapSize();
    public long getMapSize() {
        return 0;
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

    public void addObservers() {
        if (timeout > 0)
            initTimer();
    }

    public void deleteObservers() {
        stopTimer();
    }

    public void stopTimer() {
        if (this.timer != null){
            this.timer.cancel();
            this.timer = null;
        }

    }

}
