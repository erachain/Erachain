package gui.items.documents;

import core.crypto.Base58;
import core.item.persons.PersonCls;
import core.transaction.Transaction;
import datachain.DCSet;
import datachain.HashesSignsMap;
import lang.Lang;
import org.mapdb.Fun.Tuple3;
import utils.DateTimeFormat;

import javax.swing.table.AbstractTableModel;
import java.util.Stack;

public class Model_Hashes_info extends AbstractTableModel {

    public static final int COLUMN_KEY = 0;
    public static final int COLUMN_NAME = 1;
    public static final int COLUMN_ADDRESS = 2;
    // public static final int COLUMN_AMOUNT = 3;
    // public static final int COLUMN_ASSET_TYPE = 4;
    Stack<Tuple3<Long, Integer, Integer>> hashs = null;
    DCSet db;
    HashesSignsMap map;
    byte[] a;                                                                                                // "Quantity"});//,
    private String[] columnNames = Lang.getInstance().translate(new String[]{"Date", "Block", "Owner"});// ,
    // "Divisible"});

    public Model_Hashes_info() {

        db = DCSet.getInstance();
        map = db.getHashesSignsMap();
        hashs = map.get(Base58.decode("a"));

    }


    public void Set_Data(String aa) {


        a = Base58.decode(aa);


        hashs = map.get(a);
        this.fireTableDataChanged();

    }

    @Override
    public int getColumnCount() {
        return this.columnNames.length;
    }

    @Override
    public String getColumnName(int index) {
        return this.columnNames[index];
    }

    @Override
    public int getRowCount() {
        return hashs.size();

    }


    public PersonCls getCreatorAdress(int row) {

        Tuple3<Long, Integer, Integer> ss = hashs.get(row);
        Transaction tt = db.getTransactionFinalMap().get(ss.b, 1);


        try {
            return tt.getCreator().getPerson().b;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            return null;
        }


    }


    @Override
    public Object getValueAt(int row, int column) {
        if (hashs == null || row > hashs.size() - 1) {
            return null;
        }

        Tuple3<Long, Integer, Integer> ss = hashs.get(row);
        Transaction tt = db.getTransactionFinalMap().get(ss.b, 1);

        switch (column) {
            case COLUMN_KEY:

                return DateTimeFormat.timestamptoString(tt.getTimestamp());

            case COLUMN_NAME:

                return ss.b;

            case COLUMN_ADDRESS:

                return tt.getCreator();

        }

        return null;
    }

}
