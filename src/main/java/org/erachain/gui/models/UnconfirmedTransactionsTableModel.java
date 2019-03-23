package org.erachain.gui.models;

import org.erachain.controller.Controller;
import org.erachain.core.transaction.Transaction;
import org.erachain.database.SortableList;
import org.erachain.datachain.DCSet;
import org.erachain.datachain.TransactionMap;
import org.erachain.lang.Lang;
import org.erachain.utils.DateTimeFormat;
import org.erachain.utils.NumberAsString;
import org.erachain.utils.ObserverMessage;
import org.erachain.utils.Pair;
import org.slf4j.LoggerFactory;

import java.util.Observable;
import java.util.Observer;

@SuppressWarnings("serial")
public class UnconfirmedTransactionsTableModel extends SortedListTableModelCls<Long, Transaction> implements Observer {

    public static final int COLUMN_TIMESTAMP = 0;
    public static final int COLUMN_TYPE = 1;
    public static final int COLUMN_FEE = 2;

    SortableList<Long, Transaction> list;

    public UnconfirmedTransactionsTableModel()
    {
        super(DCSet.getInstance().getTransactionMap(),
                new String[]{"Timestamp", "Type", "Fee"},
                new Boolean[]{true, false, false}, true);

        LOGGER = LoggerFactory.getLogger(UnconfirmedTransactionsTableModel.class);
    }

    @Override
    public SortableList<Long, Transaction> getSortableList() {
        return this.list;
    }

    public Transaction getItem(int row) {
        return getTransaction(row);
    }

    public Transaction getTransaction(int row) {
        if (list == null
                || row >= list.size())
            return null;

        return list.get(row).getB();
    }

    @Override
    public int getRowCount() {
        if (this.list == null) {
            return 0;
        }

        return this.list.size();
    }

    @Override
    public Object getValueAt(int row, int column) {
        try {
            if (this.list == null || this.list.size() <= row) {
                return null;
            }

            Transaction transaction = this.list.get(row).getB();
            if (transaction == null)
                return null;

            switch (column) {
                case COLUMN_TIMESTAMP:

                    return DateTimeFormat.timestamptoString(transaction.getTimestamp());

                case COLUMN_TYPE:

                    return Lang.getInstance().translate(transaction.viewTypeName());

                case COLUMN_FEE:

                    return NumberAsString.formatAsString(transaction.getFee());
            }

            return null;

        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return null;
        }
    }

    private int count;

    @SuppressWarnings("unchecked")
    public synchronized void syncUpdate(Observable o, Object arg) {

        ObserverMessage message = (ObserverMessage) arg;
        int type = message.getType();

        if (type == ObserverMessage.LIST_UNC_TRANSACTION_TYPE) {
            needUpdate = true;

        } else if (type == ObserverMessage.RESET_UNC_TRANSACTION_TYPE) {
            needUpdate = false;
            getInterval();
            this.fireTableDataChanged();

        } else if (type == ObserverMessage.ADD_UNC_TRANSACTION_TYPE) {
            needUpdate = true;
        } else if (type == ObserverMessage.REMOVE_UNC_TRANSACTION_TYPE) {
            needUpdate = true;
        } else if (message.getType() == ObserverMessage.GUI_REPAINT
                && Controller.getInstance().isDynamicGUI()
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

        map.addObserver(this);

        Controller.getInstance().guiTimer.addObserver(this); // обработка repaintGUI

        getInterval();
        fireTableDataChanged();

    }

    public void deleteObservers() {
        map.deleteObserver(this);
    }

    @Override
    public long getMapSize() {
        return map.size();
    }

    @Override
    public void getIntervalThis(long startBack, long endBack) {
        list = new SortableList<Long, Transaction>(map, ((TransactionMap)map).getFromToKeys(startBack, endBack));

        DCSet dcSet = DCSet.getInstance();
        for (Pair<Long, Transaction> item: list) {
            if (item.getB() == null)
                continue;

            item.getB().setDC_HeightSeq(dcSet);
            item.getB().calcFee();
        }

    }

}
