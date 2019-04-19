package org.erachain.gui.models;

import org.erachain.controller.Controller;
import org.erachain.database.AutoKeyDBMap;
import org.erachain.database.DBMap;
import org.erachain.database.SortableList;
import org.erachain.utils.ObserverMessage;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Observable;

@SuppressWarnings("serial")
public abstract class WalletAutoKeyTableModel<T, U> extends WalletSortedTableModel<T, U> {

    protected final int RESET_EVENT;
    private final int LIST_EVENT;
    private final int ADD_EVENT;
    private final int REMOVE_EVENT;
    /**
     * В динамическом режиме перерисовывается автоматически по событию GUI_REPAINT
     * - перерисовка страницы целой, поэтому не так тормозит основные процессы.<br>
     * Без динамического режима перерисовывается только принудительно - по нажатию кнопки тут
     * org.erachain.gui.items.records.MyTransactionsSplitPanel#setIntervalPanel
     */
    public WalletAutoKeyTableModel(AutoKeyDBMap map, String[] columnNames, Boolean[] column_AutoHeight, boolean descending) {

        super(map, columnNames, column_AutoHeight, descending);

        logger = LoggerFactory.getLogger(this.getClass().getName());

        RESET_EVENT = (Integer) map.getObservableData().get(DBMap.NOTIFY_RESET);
        LIST_EVENT = (Integer) map.getObservableData().get(DBMap.NOTIFY_LIST);
        ADD_EVENT = (Integer) map.getObservableData().get(DBMap.NOTIFY_ADD);
        REMOVE_EVENT = (Integer) map.getObservableData().get(DBMap.NOTIFY_REMOVE);

        addObservers();

    }

    private int count;

    public synchronized void syncUpdate(Observable o, Object arg) {
        if (Controller.getInstance().wallet.database == null)
            return;

        ObserverMessage message = (ObserverMessage) arg;

        //CHECK IF NEW LIST
        if (message.getType() == RESET_EVENT) {

            count = 0;
            needUpdate = false;

            listSorted = new SortableList<T, U>(map, new ArrayList<>());
            fireTableDataChanged();

        } else if (message.getType() == LIST_EVENT
                || message.getType() == ObserverMessage.GUI_REPAINT
                    && needUpdate) {

            count = 0;
            needUpdate = false;

            getInterval();
            fireTableDataChanged();

        } else if (message.getType() == ADD_EVENT) {

            needUpdate = true;

        } else if (message.getType() == REMOVE_EVENT) {

            needUpdate = true;

        }
    }

    @Override
    public void getIntervalThis(long startBack, long endBack) {

        // тут могут быть пустые элементы - пропустим их
        Collection<T> keys = ((AutoKeyDBMap)map).getFromToKeys(startBack, endBack);
        listSorted = new SortableList<T, U>(map, keys);

    }
}
