package org.erachain.gui.exdata.authors;

import org.erachain.controller.Controller;
import org.erachain.core.exdata.exLink.ExLinkAuthor;
import org.erachain.core.item.ItemCls;
import org.erachain.core.item.persons.PersonHuman;
import org.erachain.datachain.DCSet;
import org.erachain.lang.Lang;

import javax.swing.table.DefaultTableModel;
import javax.validation.constraints.Null;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

public class AuthorsModel extends DefaultTableModel {
    public static final int KEY_COL = 0;
    public static final int SHARE_COL = 1;
    public static final int NAME_COL = 2;
    public static final int MEMO_COL = 3;


    public AuthorsModel(int rows) {
        super(new String[]{Lang.T("Number"),
                        Lang.T("Share"),
                        Lang.T("Name"),
                        Lang.T("Mark")
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
                Long personKey = (Long) aValue;
                Iterator a = this.dataVector.iterator();
                // find TWINS
                while (a.hasNext()) {
                    Vector b = (Vector) a.next();
                    if (b.get(0).equals(personKey)) return;

                }
                PersonHuman result = (PersonHuman) Controller.getInstance().getPerson(personKey);
                if (result != null) {
                    super.setValueAt(aValue, row, column);
                    super.setValueAt(result.getName(), row, NAME_COL);
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

    public ExLinkAuthor[] getAuthors() {
        if (this.getRowCount() == 0)
            return null;

        List<ExLinkAuthor> temp = new ArrayList<>();
        Iterator iterator = this.dataVector.iterator();
        while (iterator.hasNext()) {
            Vector item = (Vector) iterator.next();
            Long personKey;
            try {
                personKey = (Long) item.elementAt(KEY_COL);
                ItemCls person = DCSet.getInstance().getItemPersonMap().get(personKey);
                if (person == null) {
                    // персоны такой нет
                    continue;
                }
            } catch (Exception e) {
                // персоны такой нет
                continue;
            }

            temp.add(new ExLinkAuthor((byte) 0, (Integer) item.elementAt(SHARE_COL),
                    personKey, ((String) item.elementAt(MEMO_COL)).getBytes(StandardCharsets.UTF_8)));
        }

        return temp.toArray(new ExLinkAuthor[0]);

    }

    @Override
    public void removeRow(int row) {
        super.removeRow(row);
        if (this.getRowCount() == 0) {
            this.addEmpty();
        }
    }
}

