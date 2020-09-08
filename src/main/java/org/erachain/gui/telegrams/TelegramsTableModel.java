package org.erachain.gui.telegrams;

import org.erachain.controller.Controller;
import org.erachain.core.transaction.RSend;
import org.erachain.core.transaction.Transaction;
import org.erachain.gui.models.TimerTableModelCls;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Observer;

@SuppressWarnings("serial")
// in list of org.erachain.records in wallet
public class TelegramsTableModel extends TimerTableModelCls<Transaction> implements Observer {

    public static final int COLUMN_DATE = 0;
    public static final int COLUMN_SENDER = 1;
    public static final int COLUMN_RECEIVER = 2;
    public static final int COLUMN_TITLE = 3;
    public static final int COLUMN_MESSAGE = 4;
    public static final int COLUMN_SIGNATURE = 5;

    static Logger LOGGER = LoggerFactory.getLogger(TelegramsTableModel.class);

    public TelegramsTableModel() {
        super(Controller.getInstance().telegramStore.database.getTelegramsMap(),
                new String[]{"Date", "Sender", "Recipient", "Title", "Message"},
                new Boolean[]{false, true, true, true}, false);
        addObservers();
    }

    @Override
    public Object getValueAt(int row, int column) {
        // try
        // {
        if (this.list == null || this.list.size() == 0) {
            return null;
        }

        RSend transaction = (RSend) this.list.get(row);
        if (transaction == null)
            return null;

        switch (column) {
            case COLUMN_DATE:
                return transaction.viewTimestamp();
            case COLUMN_SENDER:
                return transaction.viewCreator();
            case COLUMN_RECEIVER:
                return transaction.viewRecipient();
            case COLUMN_TITLE:
                return transaction.getTitle();
            case COLUMN_MESSAGE:
                return transaction.viewData();
            case COLUMN_SIGNATURE:
                return transaction.viewSignature();

        }

        return null;

    }

}
