package org.erachain.gui.items.statement;

import org.erachain.core.transaction.RSignNote;
import org.erachain.core.transaction.Transaction;
import org.erachain.datachain.DCSet;
import org.erachain.lang.Lang;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.mapdb.Fun.Tuple3;

import javax.swing.table.AbstractTableModel;
import javax.validation.constraints.Null;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class StatementsTableModelSearch extends AbstractTableModel {

    public static final int COLUMN_TIMESTAMP = 0;
    public static final int COLUMN_CREATOR = 1;
    // public static final int COLUMN_TEMPLATE = 2;
    public static final int COLUMN_BODY = 2;
    public static final int COLUMN_FAVORITE = 3;
    private static final long serialVersionUID = 1L;
    List<RSignNote> transactions;
    private String[] columnNames = new String[]{"Timestamp",
            "Creator"/* , "Template" */, "Statement", "Favorite"};// ,
    // AssetCls.FEE_NAME});
    private Boolean[] column_AutuHeight = new Boolean[]{true, true, true, false};

    public StatementsTableModelSearch() {

        clear();
    }

    // set class
    public Class<? extends Object> getColumnClass(int c) { // set column type
        Object o = getValueAt(0, c);
        return o == null ? Null.class : o.getClass();
    }

    // читаем колонки которые изменяем высоту
    public Boolean[] get_Column_AutoHeight() {

        return this.column_AutuHeight;
    }

    // устанавливаем колонки которым изменить высоту
    public void set_get_Column_AutoHeight(Boolean[] arg0) {
        this.column_AutuHeight = arg0;
    }

    @Override
    public int getColumnCount() {
        return this.columnNames.length;
    }

    public Transaction get_Statement(int row) {

        if (transactions == null || row < 0 || transactions.size() <= row)
            return null;

        return transactions.get(row);
    }

    @Override
    public String getColumnName(int index) {
        return Lang.getInstance().translate(columnNames[index]);
    }

    public String getColumnNameNO_Translate(int index) {
        return columnNames[index];
    }

    @Override
    public int getRowCount() {
        return transactions.size();
    }

    @Override
    public Object getValueAt(int row, int column) {
        if (this.transactions == null || this.transactions.size() - 1 < row) {
            return null;
        }

        RSignNote record = (RSignNote) this.transactions.get(row);

        switch (column) {
            case COLUMN_TIMESTAMP:

                return record.viewTimestamp();
            /*
             * case COLUMN_TEMPLATE:
             *
             * if (record.getVersion() ==2) {
             *
             * return " "; } //view version 1
             *
             * return ItemCls.getItem(DLSet.getInstance(), ItemCls.TEMPLATE_TYPE,
             * record.getKey()).toString();
             */

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
                return str;// transaction.viewReference();//.viewProperies();
            case COLUMN_CREATOR:

                return record.getCreator().getPersonAsString();
            case COLUMN_FAVORITE:
                return record.isFavorite();
        }

        return null;
    }

    private List<RSignNote> getTransactionByStatement(String str, Long key, boolean isLowerCase) {

        DCSet dcSet = DCSet.getInstance();
        List<RSignNote> tran = new ArrayList<RSignNote>();
        if (key > 0) {
            tran.add((RSignNote)dcSet.getTransactionFinalMap().get(key));
            return tran;
        }

        List<Transaction> lists = dcSet.getTransactionFinalMap().getTransactionsByTitleAndType(str,
                Transaction.SIGN_NOTE_TRANSACTION, 1000, isLowerCase);

        for (Transaction transaction: lists) {

            transaction.setDC_HeightSeq(dcSet);
            tran.add((RSignNote) transaction);
        }

        return tran;
    }

    public void Find_item_from_key(String text) {
        // TODO Auto-generated method stub
        if (text.equals("") || text == null)
            return;
        if (!text.matches("[0-9]*"))
            return;
        if (new Long(text) < 1)
            return;
        transactions = getTransactionByStatement("", new Long(text), false);
        fireTableDataChanged();
    }

    public void clear() {
        transactions = new ArrayList<RSignNote>();
        fireTableDataChanged();
    }

    public void set_Filter_By_Name(String str, boolean isLowerCase) {
        transactions = getTransactionByStatement(str, 0l, isLowerCase);
        fireTableDataChanged();

    }

    private boolean filter_str(String filter, RSignNote record, boolean isLowerCase) {
        if (record.getData() == null)
            return false;

        if (record.getVersion() == 2) {
            Tuple3<String, String, JSONObject> a;
            try {
                a = record.parse_Data_V2_Without_Files();
                String base = a.b;
                if (!isLowerCase) {
                    filter = filter.toLowerCase();
                    base = base.toLowerCase();
                }
                if (base.contains(filter))
                    return true;

                return false;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
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
            return false;
        if (str.contains(filter))
            return true;
        return false;
    }
}
