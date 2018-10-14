package gui.items.link_hashes;

import core.crypto.Base58;
import datachain.DCSet;
import datachain.HashesSignsMap;
import lang.Lang;
import org.mapdb.Fun.Tuple3;

import javax.swing.table.DefaultTableModel;
import javax.validation.constraints.Null;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

@SuppressWarnings("serial")
public class Table_Model_Search_Hash extends DefaultTableModel {

    List<Tuple3<Long, Integer, Integer>> lhh;
    private String[] columnNames = Lang.getInstance().translate(new String[]{"Creator", "Block", "Transaction"});//, "Quantity"});//, "Divisible"});
    private HashesSignsMap map;
    private Stack<Tuple3<Long, Integer, Integer>> hashes;

    public Table_Model_Search_Hash() {
        super();
        //	this.addRow(new Object[]{"", ""});
        lhh = new ArrayList();
        map = DCSet.getInstance().getHashesSignsMap();
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
        if (this.lhh == null) return 0;
        return this.lhh.size();

    }

    public void clear() {
        lhh = null;
        lhh = new ArrayList();
        this.fireTableDataChanged();

    }

    public Tuple3<Long, Integer, Integer> getHashInfo(int row) {
        return lhh.get(row);

    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return false;
    }

    public Class<? extends Object> getColumnClass(int c) {     // set column type
        Object o = getValueAt(0, c);
        return o == null ? Null.class : o.getClass();
    }


    public Object getValueAt(int row, int col) {

        if (this.lhh.size() == 0 || this.getRowCount() == 0) return null;

        Tuple3<Long, Integer, Integer> hh = lhh.get(row); //= hashes.getBySignature(row);
        switch (col) {
            case 0:
                return hh.a;
            case 1:
                return hh.b;
            case 2:
                return hh.c;


        }

        return null;


    }


    public void setHash(String hash) {
        lhh.clear();
        Stack<Tuple3<Long, Integer, Integer>> hh1 = map.get(Base58.decode(hash));
        Iterator<Tuple3<Long, Integer, Integer>> hh1i = hh1.iterator();
        while (hh1i.hasNext()) {
            // hashes = map.getBySignature(Base58.decode(hash));
            Tuple3<Long, Integer, Integer> h = hh1i.next();
            lhh.add(h);
        }
        this.fireTableDataChanged();

    }

}