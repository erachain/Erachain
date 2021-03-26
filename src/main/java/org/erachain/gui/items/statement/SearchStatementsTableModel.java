package org.erachain.gui.items.statement;

import org.erachain.controller.Controller;
import org.erachain.core.transaction.Transaction;
import org.erachain.core.wallet.Wallet;
import org.erachain.database.FilteredByStringArray;
import org.erachain.datachain.DCSet;
import org.erachain.gui.models.SearchTableModelCls;
import org.erachain.lang.Lang;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

public class SearchStatementsTableModel extends SearchTableModelCls<Transaction> {

    public static final int COLUMN_SEQNO = 0;
    public static final int COLUMN_TIMESTAMP = 1;
    public static final int COLUMN_TYPE = 2;
    public static final int COLUMN_CREATOR = 3;
    public static final int COLUMN_TITLE = 4;
    public static final int COLUMN_FAVORITE = 5;
    private static final long serialVersionUID = 1L;

    DCSet dcSet = DCSet.getInstance();
    Wallet wallet = Controller.getInstance().wallet;

    public SearchStatementsTableModel() {

        super(DCSet.getInstance().getTransactionFinalMap(),
                new String[]{"№", "Timestamp", "Type", "Creator", "Statement", "Favorite"},
                new Boolean[]{false, true, true, true, true, false},
                true);

        logger = LoggerFactory.getLogger(this.getClass());

    }

    @Override
    public Object getValueAt(int row, int column) {
        if (this.list == null || this.list.size() - 1 < row) {
            return null;
        }

        Transaction transaction = this.list.get(row);

        switch (column) {

            case COLUMN_SEQNO:
                return transaction.viewHeightSeq();

            case COLUMN_TIMESTAMP:
                return transaction.viewTimestamp();
            case COLUMN_TYPE:
                return Lang.T(transaction.viewFullTypeName());
            case COLUMN_CREATOR:
                return transaction.getCreator().getPersonAsString();
            case COLUMN_TITLE:
                return transaction.getTitle();
            case COLUMN_FAVORITE:
                return wallet.isDocumentFavorite(transaction);
        }

        return null;
    }


    public void findByKey(String text) {

        clear();

        // TODO Auto-generated method stub
        if (text.equals("") || text == null)
            return;
        if (!text.matches("[0-9]*"))
            return;
        if (new Long(text) < 1)
            return;

        Long key = new Long(text);
        if (key > 0) {
            list.add(DCSet.getInstance().getTransactionFinalMap().get(key));
        }

        fireTableDataChanged();
    }

    public void setFilterByName(String filter, Long fromID) {

        clear();

        DCSet dcSet = DCSet.getInstance();

        list = ((FilteredByStringArray) dcSet.getTransactionFinalMap())
                .getByFilterAsArray(filter, fromID, start, step, descending);

        fireTableDataChanged();

    }

    public void clear() {
        list = new ArrayList<>();
        fireTableDataChanged();
    }

}
