package org.erachain.gui.items.statement;

import org.erachain.core.transaction.RSignNote;
import org.erachain.core.transaction.Transaction;
import org.erachain.datachain.DCSet;
import org.erachain.gui.models.SearchTableModelCls;
import org.erachain.gui.models.TimerTableModelCls;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.mapdb.Fun.Tuple3;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

public class StatementsTableModelSearch extends SearchTableModelCls<RSignNote> {

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

        RSignNote record = this.list.get(row);

        switch (column) {
            case COLUMN_TIMESTAMP:

                return record.viewTimestamp();

            case COLUMN_BODY:

                if (record.getData() == null)
                    return "";

                if (record.getVersion() == 2) {
                    Tuple3<String, String, JSONObject> a;
                    try {
                        a = record.parse_Data_V2_Without_Files();

                        return a.b;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                String str;
                try {
                    JSONObject data = (JSONObject) JSONValue
                            .parseWithException(new String(record.getData(), Charset.forName("UTF-8")));
                    str = (String) data.get("!!&_Title");
                    if (str == null)
                        str = (String) data.get("Title");
                } catch (Exception e) {
                    str = new String(record.getData(), Charset.forName("UTF-8"));
                }
                if (str == null)
                    return "";
                if (str.length() > 50)
                    return str.substring(0, 50) + "...";
                return str;
            case COLUMN_CREATOR:

                return record.getCreator().getPersonAsString();

            case COLUMN_FAVORITE:
                return record.isFavorite();
        }

        return null;
    }


    public void findByKey(String text) {
        // TODO Auto-generated method stub
        if (text.equals("") || text == null)
            return;
        if (!text.matches("[0-9]*"))
            return;
        if (new Long(text) < 1)
            return;

        Long key = new Long(text);
        if (key > 0) {
            list.add((RSignNote)DCSet.getInstance().getTransactionFinalMap().get(key));
        }

        fireTableDataChanged();
    }

    public void setFilterByName(String str, boolean isLowerCase) {

        DCSet dcSet = DCSet.getInstance();

        List<Transaction> lists = dcSet.getTransactionFinalMap().getTransactionsByTitleAndType(str,
                Transaction.SIGN_NOTE_TRANSACTION, 1000, isLowerCase);

        for (Transaction transaction: lists) {

            transaction.setDC_HeightSeq(dcSet);
            list.add((RSignNote) transaction);
        }

        fireTableDataChanged();

    }

    public void clear() {
        list = new ArrayList<RSignNote>();
        fireTableDataChanged();
    }

}
