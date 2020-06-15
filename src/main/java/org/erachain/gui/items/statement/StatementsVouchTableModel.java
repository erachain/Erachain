package org.erachain.gui.items.statement;

import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.transaction.RVouch;
import org.erachain.core.transaction.Transaction;
import org.erachain.datachain.DCSet;
import org.erachain.datachain.TransactionFinalMapImpl;
import org.erachain.datachain.VouchRecordMap;
import org.erachain.gui.models.TimerTableModelCls;
import org.erachain.utils.DateTimeFormat;
import org.mapdb.Fun.Tuple2;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class StatementsVouchTableModel extends TimerTableModelCls<RVouch> {

    public static final int COLUMN_TIMESTAMP = 0;
    public static final int COLUMN_CREATOR = 1;
    public static final int COLUMN_HEIGHT = 2;
    public static final int COLUMN_CREATOR_NAME = 30;
    private static final long serialVersionUID = 1L;

    private int blockNo;
    private int recNo;

    TransactionFinalMapImpl transactionMap;

    public StatementsVouchTableModel(Transaction transaction) {

        super(DCSet.getInstance().getVouchRecordMap(),
                new String[]{"Timestamp", "Voucher / Signatory", "Height"}, null, false);

        if (transaction != null) {
            blockNo = transaction.getBlockHeight();
            recNo = transaction.getSeqNo();
        }

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

    @Override
    public void getInterval() {

        if (list == null) {
            list = new ArrayList<>();
        } else {
            list.clear();
        }

        // read indexes to DB
        Tuple2<BigDecimal, List<Long>> vouches = ((VouchRecordMap) map).get(Transaction.makeDBRef(this.blockNo, this.recNo));
        if (vouches == null) {
            fireTableDataChanged();
            return;

        }

        for (Long key : vouches.b) {
            // write R-Vouch transaction
            list.add((RVouch) transactionMap.get(key));

        }
    }

}
