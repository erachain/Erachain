package org.erachain.gui.models;

import org.erachain.controller.Controller;
import org.erachain.database.AutoKeyDBMap;
import org.erachain.database.SortableList;
import org.erachain.utils.ObserverMessage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Observable;

@SuppressWarnings("serial")
public abstract class WalletAutoKeyTableModel<T, U> extends WalletSortedTableModel<T, U> {

    private int reset_type;
    private int list_type;
    private int add_type;
    private int remove_type;
    /**
     * В динамическом режиме перерисовывается автоматически по событию GUI_REPAINT
     * - перерисовка страницы целой, поэтому не так тормозит основные процессы.<br>
     * Без динамического режима перерисовывается только принудительно - по нажатию кнопки тут
     * org.erachain.gui.items.records.MyTransactionsSplitPanel#setIntervalPanel
     */
    public WalletAutoKeyTableModel(AutoKeyDBMap map, String[] columnNames, Boolean[] column_AutoHeight, boolean descending,
               int reset_type, int list_type, int add_type, int remove_type) {

        super(map, columnNames, column_AutoHeight, descending);

        this.reset_type = reset_type;
        this.list_type = list_type;
        this.add_type = add_type;
        this.remove_type = remove_type;

    }

    private int count;

    public synchronized void syncUpdate(Observable o, Object arg) {
        if (Controller.getInstance().wallet.database == null)
            return;

        ObserverMessage message = (ObserverMessage) arg;

        //CHECK IF NEW LIST
        if (message.getType() == reset_type) {

            count = 0;
            needUpdate = false;

            listSorted = new SortableList<T, U>(map, new ArrayList<>());
            fireTableDataChanged();

        } else if (message.getType() == list_type) {

            count = 0;
            needUpdate = false;

            getInterval();
            fireTableDataChanged();

        } else if (message.getType() == add_type) {

            needUpdate = true;

        } else if (message.getType() == remove_type) {

            needUpdate = true;

        } else if (message.getType() == ObserverMessage.GUI_REPAINT
                && needUpdate) {

            if (count++ < 4)
                return;

            count = 0;
            needUpdate = false;

            getInterval();
            fireTableDataChanged();

        }
    }

    @Override
    public void getIntervalThis(long startBack, long endBack) {

        // тут могут быть пустые элементы - пропустим их
        Collection<T> keys = ((AutoKeyDBMap)map).getFromToKeys(startBack, endBack);
        listSorted = new SortableList<T, U>(map, keys);

    }
}
