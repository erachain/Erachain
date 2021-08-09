package org.erachain.gui.models;

import org.erachain.controller.Controller;
import org.erachain.datachain.DCSet;
import org.erachain.dbs.DBTab;
import org.erachain.dbs.DBTabImpl;
import org.erachain.dbs.IteratorCloseable;
import org.erachain.lang.Lang;
import org.erachain.utils.ObserverMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.table.AbstractTableModel;
import javax.validation.constraints.Null;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

@SuppressWarnings("serial")
public abstract class TimerTableModelCls<U> extends AbstractTableModel implements Observer {

    public static final int COLUMN_IS_OUTCOME = -3;
    public static final int COLUMN_UN_VIEWED = -2;
    public static final int COLUMN_CONFIRMATIONS = -1;
    public int COLUMN_FAVORITE = 1000;

    private String name;
    protected String[] columnNames;
    //private Timer timer;
    protected boolean needUpdate;
    protected boolean descending;

    private int RESET_EVENT;
    private int ADD_EVENT;
    private int DELETE_EVENT;
    private int LIST_EVENT;

    protected List<U> list;

    private Boolean[] columnAutoHeight; // = new Boolean[]{true, true, true, true, true, true, true, false, false};

    protected Object startKey;
    protected int step = 50;
    protected Object lastPageKey;

    protected DCSet dcSet = DCSet.getInstance();

    protected DBTabImpl map;
    protected Logger logger;

    public TimerTableModelCls(String[] columnNames, boolean descending) {
        logger = LoggerFactory.getLogger(this.getClass());
        this.columnNames = columnNames;
        this.descending = descending;
    }

    public TimerTableModelCls(DBTabImpl map, String[] columnNames, boolean descending) {
        logger = LoggerFactory.getLogger(this.getClass());
        this.map = map;
        this.columnNames = columnNames;
        this.descending = descending;
    }

    public TimerTableModelCls(String[] columnNames, Boolean[] columnAutoHeight, boolean descending) {
        logger = LoggerFactory.getLogger(this.getClass());
        this.columnNames = columnNames;
        this.columnAutoHeight = columnAutoHeight;
        this.descending = descending;
    }

    public TimerTableModelCls(DBTabImpl map, String[] columnNames, Boolean[] columnAutoHeight, boolean descending) {
        logger = LoggerFactory.getLogger(this.getClass());
        this.map = map;
        this.columnNames = columnNames;
        this.columnAutoHeight = columnAutoHeight;
        this.descending = descending;
    }

    public TimerTableModelCls(DBTabImpl map, String[] columnNames, Boolean[] columnAutoHeight, int favoriteColumn, boolean descending) {
        logger = LoggerFactory.getLogger(this.getClass());
        this.map = map;
        this.columnNames = columnNames;
        this.columnAutoHeight = columnAutoHeight;
        this.descending = descending;
        this.COLUMN_FAVORITE = favoriteColumn;
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
    //     private String[] columnNames = Lang.T();
    public String getColumnName(int index) {
        return Lang.T(columnNames[index]);
    }

    public String getColumnNameOrigin(int index) {
        return columnNames[index];
    }

    public DBTabImpl getMap() {
        return map;
    }

    public U getItem(int row) {
        if (list == null)
            return null;

        return this.list.get(row);
    }

    public int getRowCount() {
        return (this.list == null) ? 0 : this.list.size();
    }

    public boolean isEmpty() {
        return (this.list == null) ? true : this.list.isEmpty();
    }

    public abstract Object getValueAt(int row, int column);

    public Class<? extends Object> getColumnClass(int c) {
        try {
            Object o = getValueAt(0, c);
            return o == null ? Null.class : o.getClass();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return Null.class;
        }
    }

    public void update(Observable o, Object arg) {
        try {
            this.syncUpdate(o, arg);
        } catch (Exception e) {
            if (logger != null)
                logger.error(e.getMessage(),e);
        }
    }

    @SuppressWarnings("unchecked")
    /**
     * убираем synchronized - так как теперь все размеренно по таймеру вызывается. Иначе блокировка при нажатии
     * на Синхронизировать кошелек очень часто бывает
     */
    public void syncUpdate(Observable o, Object arg) {
        ObserverMessage message = (ObserverMessage) arg;

        if (message.getType() == ADD_EVENT
                || message.getType() == DELETE_EVENT) {
            needUpdate = true;
        } else if (message.getType() == LIST_EVENT
                || message.getType() == RESET_EVENT) {
            needUpdate = true;
        } else if (message.getType() == ObserverMessage.CHAIN_ADD_BLOCK_TYPE && Controller.getInstance().isStatusOK()) {
            repaintConfirms();
        } else if (message.getType() == ObserverMessage.GUI_REPAINT && needUpdate) {
            needUpdate = false;
            getInterval();
            this.fireTableDataChanged();
        }
    }

    /**
     * перерисовка Подтверждений на новый блок
     */
    protected void repaintConfirms() {
    }

    public void getInterval() {
        Object key;
        U item;
        int count = 0;
        list = new ArrayList<>();
        if (map == null)
            return;

        if (startKey == null) {
            try (IteratorCloseable iterator = map.getIndexIterator(0, descending)) {
                while (iterator.hasNext() && count < step) {
                    key = iterator.next();
                    item = (U) map.get(key);
                    if (item == null)
                        continue;
                    list.add(item);
                    count++;
                }
            } catch (IOException e) {
            }
        } else {
            try (IteratorCloseable iterator = map.getIndexIterator(0, descending)) {
                while (iterator.hasNext() && count < step) {
                    key = iterator.next();
                    item = (U) map.get(key);
                    if (item == null)
                        continue;
                    list.add(item);
                    count++;
                }
            } catch (IOException e) {
            }
        }
    }

    public int getMapDefaultIndex() {
        if (map == null)
            return 0;

        return map.getDefaultIndex();
    }

    public void setInterval(Object startKey) {
        this.startKey = startKey;

        getInterval();
    }

    public void addObservers() {
        Controller.getInstance().guiTimer.addObserver(this); // обработка repaintGUI
        Controller.getInstance().getWallet().addObserver(this); // обработка repaintGUI
        if (map != null) {

            RESET_EVENT = (int) map.getObservableData().get(DBTab.NOTIFY_RESET);
            LIST_EVENT = (int) map.getObservableData().get(DBTab.NOTIFY_LIST);
            ADD_EVENT = (int) map.getObservableData().get(DBTab.NOTIFY_ADD);
            DELETE_EVENT = (int) map.getObservableData().get(DBTab.NOTIFY_REMOVE);

            map.addObserver(this);
        }
    }

    public void deleteObservers() {
        Controller.getInstance().guiTimer.deleteObserver(this); // обработка repaintGUI
        if (map != null) {
            map.deleteObserver(this);
        }
    }

}
