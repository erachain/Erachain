package org.erachain.gui.models;

import org.erachain.controller.Controller;
import org.erachain.core.item.ItemCls;
import org.erachain.core.transaction.*;
import org.erachain.database.AutoKeyDBMap;
import org.erachain.database.DBMap;
import org.erachain.database.SortableList;
import org.erachain.database.wallet.TransactionMap;
import org.erachain.datachain.DCSet;
import org.erachain.lang.Lang;
import org.erachain.utils.DateTimeFormat;
import org.erachain.utils.ObserverMessage;
import org.erachain.utils.Pair;
import org.mapdb.Fun.Tuple2;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Observable;
import java.util.Observer;

@SuppressWarnings("serial")
public abstract class WalletAutoKeyTableModel<T, U> extends SortedListTableModelCls<T, U> {

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

        addObservers();

    }

    private int count;

    public synchronized void syncUpdate(Observable o, Object arg) {
        if (Controller.getInstance().wallet.database == null)
            return;

        ObserverMessage message = (ObserverMessage) arg;

        //CHECK IF NEW LIST
        if (message.getType() == list_type) {

            count = 0;
            needUpdate = false;

            listSorted = new SortableList<T, U>(map, new ArrayList<>());
            fireTableDataChanged();

        } else if (message.getType() == reset_type) {

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

    public void addObservers() {

        super.addObservers();

        if (!Controller.getInstance().doesWalletDatabaseExists())
            return;

        //REGISTER ON WALLET TRANSACTIONS
        map.addObserver(this);

    }

    public void deleteObservers() {

        super.deleteObservers();

        if (Controller.getInstance().doesWalletDatabaseExists())
            return;

        map.deleteObserver(this);
    }

    @Override
    public void getIntervalThis(long startBack, long endBack) {

        // тут могут быть пустые элементы - пропустим их
        Collection<T> keys = ((AutoKeyDBMap)map).getFromToKeys(startBack, endBack);
        listSorted = new SortableList<T, U>(map, keys);

    }
}
