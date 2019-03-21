package org.erachain.gui.models;

import org.erachain.database.SortableList;
import org.erachain.lang.Lang;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.table.AbstractTableModel;
import javax.validation.constraints.Null;
import java.util.Observable;
import java.util.Timer;
import java.util.TimerTask;

@SuppressWarnings("serial")
public abstract class TimerTableModelCls<T, U> extends AbstractTableModel {

    private String name;
    private long timeout;
    private String[] columnNames;
    private Timer timer;
    boolean needUpdate;

    protected int start = 0;
    protected int step = 50;
    protected int size = 0;

    public int COLUMN_FAVORITE = 1000;

    protected Logger LOGGER;


    public TimerTableModelCls(String[] columnNames) {
        this.columnNames = columnNames;
        //this.initComponents();
        addObservers();
    }

    public TimerTableModelCls(String name, long timeout, String[] columnNames) {
        this.columnNames = columnNames;
        this.name = name;
        this.timeout = timeout;
        //this.initComponents();
        addObservers();
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

            this.timer.schedule(action, 500, timeout);
        }

    }

    //public abstract void initComponents();

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

    public abstract U getItem(int row);

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

    protected abstract void addObserversThis();

    public void addObservers() {
        addObserversThis();
        if (timeout > 0) initTimer();
    }

    protected abstract void removeObserversThis();

    public void removeObservers() {
        stopTimer();
        removeObserversThis();
    }

    public void stopTimer() {
        if (this.timer != null){
            this.timer.cancel();
            this.timer = null;
        }

    }

}
