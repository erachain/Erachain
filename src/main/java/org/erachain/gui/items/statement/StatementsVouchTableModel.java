package org.erachain.gui.items.statement;

import org.erachain.core.account.Account;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.transaction.RVouch;
import org.erachain.core.transaction.Transaction;
import org.erachain.datachain.DCSet;
import org.erachain.datachain.TransactionFinalMap;
import org.erachain.datachain.VouchRecordMap;
import org.erachain.gui.models.TimerTableModelCls;
import org.erachain.lang.Lang;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.mapdb.Fun.Tuple2;
import org.erachain.utils.DateTimeFormat;
import org.erachain.utils.ObserverMessage;

import javax.swing.table.AbstractTableModel;
import javax.validation.constraints.Null;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

public class StatementsVouchTableModel extends TimerTableModelCls<RVouch> {

    public static final int COLUMN_TIMESTAMP = 0;
    public static final int COLUMN_CREATOR = 1;
    public static final int COLUMN_HEIGHT = 2;
    public static final int COLUMN_CREATOR_NAME = 30;
    private static final long serialVersionUID = 1L;

    private int blockNo;
    private int recNo;

    TransactionFinalMap transactionMap;

    public StatementsVouchTableModel(Transaction transaction) {

        super(DCSet.getInstance().getVouchRecordMap(),
                new String[]{"Timestamp", "Creator", "Height"}, null, false);

        if (transaction != null) {
            blockNo = transaction.getBlockHeight();
            recNo = transaction.getSeqNo();
        }

        logger = LoggerFactory.getLogger(StatementsVouchTableModel.class.getName());

        transactionMap = DCSet.getInstance().getTransactionFinalMap();

        addObservers();

    }


    public PublicKeyAccount getCreator(int row) {
        RVouch transaction = this.list.get(row);
        return transaction.getCreator();

    }


    public String getTransactionHeightSeqNo(int row) {

        if (this.list == null || this.list.size() <= row) {
            return null;
        }

        Transaction transaction = this.list.get(row);
        if (transaction == null)
            return null;

        return transaction.viewHeightSeq();

    }


    @Override
    public Object getValueAt(int row, int column) {

        if (this.list == null || this.list.size() <= row) {
            return null;
        }

        RVouch transaction = this.list.get(row);
        if (transaction == null)
            return null;

        switch (column) {
            case COLUMN_TIMESTAMP:

                return DateTimeFormat.timestamptoString(transaction.getTimestamp());

            case COLUMN_CREATOR:

                return transaction.getCreator().getPersonAsString();

            case COLUMN_HEIGHT:

                return transaction.getBlockHeight();

            case COLUMN_CREATOR_NAME:
                return transaction.getCreator().getPerson().b.getName();
        }

        return null;

    }

    public synchronized void syncUpdate(Observable o, Object arg) {
        ObserverMessage message = (ObserverMessage) arg;

        if (message.getType() == ObserverMessage.LIST_VOUCH_TYPE) {

            setRows();
            fireTableDataChanged();

        } else if (message.getType() == ObserverMessage.LIST_VOUCH_TYPE
                || message.getType() == ObserverMessage.ADD_VOUCH_TYPE || message.getType() == ObserverMessage.REMOVE_VOUCH_TYPE) {
            needUpdate = true;

        } else if (message.getType() == ObserverMessage.GUI_REPAINT && needUpdate) {

            needUpdate = false;
            setRows();
            fireTableDataChanged();
        }
    }

    public void setRows() {

        if (list == null) {
            list = new ArrayList<>();
        } else {
            list.clear();
        }

        // read indexes to DB
        Tuple2<BigDecimal, List<Long>> vouches = ((VouchRecordMap)map).get(Transaction.makeDBRef(this.blockNo, this.recNo));
        if (vouches == null) {
            fireTableDataChanged();
            return;

        }

        for (Long key : vouches.b) {
            // write R-Vouch transaction
            list.add((RVouch) transactionMap.get(key));

        }
    }

    public void addObservers() {
        super.addObservers();
        map.addObserver(this);
    }

    public void removeObservers() {
        super.deleteObservers();
        map.deleteObserver(this);
    }


}
