package org.erachain.gui.items.statement;

import org.erachain.core.transaction.Transaction;
import org.erachain.datachain.DCSet;
import org.erachain.gui.models.SearchTableModelCls;
import org.erachain.lang.Lang;
import org.slf4j.LoggerFactory;

public class SearchStatementsTableModel extends SearchTableModelCls {

    public static final int COLUMN_SEQNO = 0;
    public static final int COLUMN_TIMESTAMP = 1;
    public static final int COLUMN_TYPE = 2;
    public static final int COLUMN_CREATOR = 3;
    public static final int COLUMN_TITLE = 4;
    public static final int COLUMN_FAVORITE = 5;
    private static final long serialVersionUID = 1L;

    public SearchStatementsTableModel() {

        super(DCSet.getInstance().getTransactionFinalMap(), Transaction.SIGN_NOTE_TRANSACTION,
                new String[]{"№", "Timestamp", "Type", "Creator", "Statement", "Favorite"},
                new Boolean[]{false, true, true, true, true, false},
                COLUMN_FAVORITE, true);

        logger = LoggerFactory.getLogger(this.getClass());

    }

    @Override
    public Object getValueAt(int row, int column) {

        try {
            if (list == null || list.size() - 1 < row) {
                return null;
            }

            Transaction transaction = list.get(row);
            if (transaction == null) {
                return null;
            }

            switch (column) {

                case COLUMN_SEQNO:
                    return transaction.viewHeightSeq();

                case COLUMN_TIMESTAMP:
                    return transaction.viewTimestamp();
                case COLUMN_TYPE:
                    return Lang.T(transaction.viewFullTypeName());
                case COLUMN_CREATOR:
                    return transaction.viewCreator();
                case COLUMN_TITLE:
                    return transaction.getTitle();
                case COLUMN_FAVORITE:
                    return cnt.isDocumentFavorite(transaction);
            }

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return null;
    }

}
