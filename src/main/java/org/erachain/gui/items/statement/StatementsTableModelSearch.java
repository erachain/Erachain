package org.erachain.gui.items.statement;

import org.erachain.core.transaction.Transaction;
import org.erachain.datachain.DCSet;
import org.erachain.gui.models.SearchTableModelCls;
import org.mapdb.Fun;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class StatementsTableModelSearch extends SearchTableModelCls<Transaction> {

    public static final int COLUMN_TIMESTAMP = 0;
    public static final int COLUMN_CREATOR = 1;
    // public static final int COLUMN_TEMPLATE = 2;
    public static final int COLUMN_BODY = 2;
    public static final int COLUMN_FAVORITE = 3;
    private static final long serialVersionUID = 1L;

    public StatementsTableModelSearch() {

        super(DCSet.getInstance().getTransactionFinalMap(),
                new String[]{"Timestamp",
                "Creator", "Statement", "Favorite"}, new Boolean[]{true, true, true, false}, false);

        logger = LoggerFactory.getLogger(this.getClass());

    }

    @Override
    public Object getValueAt(int row, int column) {
        if (this.list == null || this.list.size() - 1 < row) {
            return null;
        }

        Transaction record = this.list.get(row);

        switch (column) {
            case COLUMN_TIMESTAMP:

                return record.viewTimestamp();

            case COLUMN_BODY:
                return record.getTitle();
            case COLUMN_CREATOR:

                return record.getCreator().getPersonAsString();

            case COLUMN_FAVORITE:
                return false; //record.isFavorite();
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

    public void setFilterByName(String str) {

        clear();

        DCSet dcSet = DCSet.getInstance();

        Iterable keys = dcSet.getTransactionFinalMap().getKeysByTitleAndType(str,
                Transaction.SIGN_NOTE_TRANSACTION, start, step);

        Iterator iterator = keys.iterator();

        Transaction item;
        Long key;

        while (iterator.hasNext()) {
            key = (Long) iterator.next();
            Fun.Tuple2<Integer, Integer> pair = Transaction.parseDBRef(key);
            item = (Transaction) map.get(key);
            list.add(item);
        }


        fireTableDataChanged();

    }

    public void clear() {
        list = new ArrayList<Transaction>();
        fireTableDataChanged();
    }

}
