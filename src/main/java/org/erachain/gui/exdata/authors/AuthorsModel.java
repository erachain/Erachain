package org.erachain.gui.exdata.authors;

import org.erachain.controller.Controller;
import org.erachain.core.exdata.ExAuthor;
import org.erachain.core.item.persons.PersonHuman;
import org.erachain.lang.Lang;

import javax.swing.table.DefaultTableModel;
import javax.validation.constraints.Null;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

public class AuthorsModel extends DefaultTableModel {
    private static final int KEY_COL = 0;
    private static final int SHARE_COL = 1;
    private static final int NAME_COL = 2;
    private static final int MEMO_COL = 3;


    public AuthorsModel(int rows) {
        super(new String[]{Lang.getInstance().translate("Number"),
                        Lang.getInstance().translate("Share"),
                        Lang.getInstance().translate("Name"),
                        Lang.getInstance().translate("Mark")
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

    private void addEmpty() {
        this.addRow(new Object[]{0L, 1, "", ""});
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
                PersonHuman result = null;
                Long personKey = (Long) aValue;
                Iterator a = this.dataVector.iterator();
                // find TWINS
                while (a.hasNext()) {
                    Vector b = (Vector) a.next();
                    if (b.get(0).equals(personKey)) return;

                }
                result = (PersonHuman) Controller.getInstance().getPerson(personKey);
                if (result != null) {
                    super.setValueAt(aValue, row, column);
                    super.setValueAt(result.getName(), row, NAME_COL);
                    this.addEmpty();
                }
                return;
            case SHARE_COL:
            case MEMO_COL:
                super.setValueAt(aValue, row, column);
                return;
        }
        return;

    }

    public void setAuthors(PersonHuman[] Authors) {
        clearAuthors();

        for (int i = 0; i < Authors.length; ++i) {
            addRow(new Object[]{Authors[i].getKey(), 1, Authors[i].getName(), Authors[i].getDescription()});
        }
    }

    public void clearAuthors() {
        while (getRowCount() > 0) {
            this.removeRow(getRowCount() - 1);
        }
    }

    public ExAuthor[] getAuthors() {
        if (this.getRowCount() == 0)
            return null;

        List<ExAuthor> temp = new ArrayList<>();
        Iterator iterator = this.dataVector.iterator();
        while (iterator.hasNext()) {
            Vector item = (Vector) iterator.next();
            if (item.elementAt(NAME_COL).equals("")) {
                // персоны такой нет
                continue;
            }

            temp.add(new ExAuthor((byte) 0, (Integer) item.elementAt(SHARE_COL),
                    (Long) item.elementAt(KEY_COL), (String) item.elementAt(MEMO_COL)));
        }

        return temp.toArray(new ExAuthor[0]);

    }

    @Override
    public void removeRow(int row) {
        super.removeRow(row);
        if (this.getRowCount() == 0) {
            this.addEmpty();
        }
    }
}

