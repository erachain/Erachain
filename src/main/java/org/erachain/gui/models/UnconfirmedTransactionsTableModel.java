package org.erachain.gui.models;

import org.erachain.controller.Controller;
import org.erachain.core.transaction.Transaction;
import org.erachain.datachain.TransactionTab;
import org.erachain.dbs.DBTabImpl;
import org.erachain.database.SortableList;
import org.erachain.datachain.DCSet;
import org.erachain.lang.Lang;
import org.erachain.utils.DateTimeFormat;
import org.erachain.utils.NumberAsString;
import org.erachain.utils.ObserverMessage;
import org.erachain.utils.Pair;

import java.util.Observable;
import java.util.Observer;

@SuppressWarnings("serial")
public class UnconfirmedTransactionsTableModel extends SortedListTableModelCls<Long, Transaction> implements Observer {

    public static final int COLUMN_TIMESTAMP = 0;
    public static final int COLUMN_TYPE = 1;
    public static final int COLUMN_NAME = 2;
    public static final int COLUMN_CREATOR = 3;
    public static final int COLUMN_FEE = 4;

    public UnconfirmedTransactionsTableModel()
    {
        super((DBTabImpl) DCSet.getInstance().getTransactionTab(),
                new String[]{"Timestamp", "Type", "Name", "Creator", "Fee"},
                new Boolean[]{true, false, true, true, false}, false);

        addObservers();

    }

    @Override
    public Object getValueAt(int row, int column) {
        try {
            if (this.listSorted == null || this.listSorted.size() <= row) {
                return null;
            }

            Pair<Long, Transaction> pair = this.listSorted.get(row);

            if (pair == null)
                return null;

            Transaction transaction = pair.getB();
            if (transaction == null)
                return null;

            switch (column) {
                case COLUMN_TIMESTAMP:

                    return DateTimeFormat.timestamptoString(transaction.getTimestamp());

                case COLUMN_TYPE:

                    return Lang.getInstance().translate(transaction.viewTypeName());

                case COLUMN_NAME:

                    return Lang.getInstance().translate(transaction.viewFullTypeName());

                case COLUMN_CREATOR:

                    return transaction.viewCreator();

                case COLUMN_FEE:

                    return NumberAsString.formatAsString(transaction.getFee());
            }

            return null;

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }

    private int count;

    @SuppressWarnings("unchecked")
    public synchronized void syncUpdate(Observable o, Object arg) {

        ObserverMessage message = (ObserverMessage) arg;
        int type = message.getType();

        if (type == ObserverMessage.LIST_UNC_TRANSACTION_TYPE) {

            count = 0;
            needUpdate = false;

            getInterval();
            fireTableDataChanged();

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

        super.addObservers();

    }

    public void deleteObservers() {
        map.deleteObserver(this);

        super.deleteObservers();

    }

    @Override
    public long getMapSize() {
        return map.size();
    }

    @Override
    public void getIntervalThis(long startBack, long endBack) {
        listSorted = new SortableList<Long, Transaction>(map, ((TransactionTab)map).getFromToKeys(startBack, endBack));

        DCSet dcSet = DCSet.getInstance();
        for (Pair<Long, Transaction> item: listSorted) {
            if (item.getB() == null)
                continue;

            item.getB().setDC_HeightSeq(dcSet);
            item.getB().calcFee();
        }

    }

}
