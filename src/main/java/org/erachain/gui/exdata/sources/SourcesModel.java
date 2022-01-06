package org.erachain.gui.exdata.sources;

import org.erachain.controller.Controller;
import org.erachain.core.exdata.exLink.ExLinkSource;
import org.erachain.core.transaction.Transaction;
import org.erachain.datachain.DCSet;
import org.erachain.lang.Lang;

import javax.swing.table.DefaultTableModel;
import javax.validation.constraints.Null;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

public class SourcesModel extends DefaultTableModel {
    public static final int KEY_COL = 0;
    public static final int SHARE_COL = 1;
    public static final int NAME_COL = 2;
    public static final int MEMO_COL = 3;


    public SourcesModel(int rows) {
        super(new String[]{Lang.T("Number"),
                        Lang.T("Share"),
                        Lang.T("Source"),
                        Lang.T("Description")
                },
                rows);
        addEmpty();
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        switch (column) {
            case KEY_COL:
                return true;
            case NAME_COL:
                return false;

            default:
                if (getValueAt(row, NAME_COL).equals("")) return false;
        }

        return true;
    }

    public void addEmpty() {
        this.addRow(new Object[]{"1-1", 1, "", ""});
    }

    public Class<? extends Object> getColumnClass(int c) {     // set column type
        Object o = getValueAt(0, c);
        return o == null ? Null.class : o.getClass();
    }


    public Object getValueAt(int row, int col) {

        if (this.getRowCount() < row || this.getRowCount() == 0) return null;

        return super.getValueAt(row, col);


    }

    @Override
    public void setValueAt(Object aValue, int row, int column) {
        //IF STRING
        switch (column) {
            case KEY_COL:
                aValue = aValue.toString().trim();
                Long seqNo = Transaction.parseDBRef((String) aValue);
                Iterator a = this.dataVector.iterator();
                // find TWINS
                while (a.hasNext()) {
                    Vector b = (Vector) a.next();
                    if (b.get(0).equals(seqNo)) return;

                }
                Transaction result = Controller.getInstance().getTransaction(seqNo);
                if (result != null) {
                    super.setValueAt(aValue, row, column);

                    super.setValueAt(result.toStringFullAndCreatorLang(), row, NAME_COL);
                    if (getRowCount() - 1 == row)
                        this.addEmpty();

                } else {
                    super.setValueAt("", row, NAME_COL);
                }
                return;
            case SHARE_COL:
            case MEMO_COL:
                super.setValueAt(aValue, row, column);
                return;
        }
        return;

    }

    public ExLinkSource[] getSources() {
        if (this.getRowCount() == 0)
            return null;

        List<ExLinkSource> temp = new ArrayList<>();
        Iterator iterator = this.dataVector.iterator();
        while (iterator.hasNext()) {
            Vector item = (Vector) iterator.next();
            String value = item.elementAt(KEY_COL).toString().trim();
            if (value.equals("1-1"))
                continue;

            Long seqNo;
            try {
                seqNo = Transaction.parseDBRef(value);
                Transaction parentTx = DCSet.getInstance().getTransactionFinalMap().get(seqNo);
                if (parentTx == null) {
                    // транзакции такой нет
                    continue;
                }
            } catch (Exception e) {
                // персоны такой нет
                continue;
            }

            temp.add(new ExLinkSource((byte) 0, (Integer) item.elementAt(SHARE_COL),
                    seqNo, ((String) item.elementAt(MEMO_COL)).getBytes(StandardCharsets.UTF_8)));
        }

        return temp.toArray(new ExLinkSource[0]);

    }

    @Override
    public void removeRow(int row) {
        super.removeRow(row);
        if (this.getRowCount() == 0) {
            this.addEmpty();
        }
    }
}

