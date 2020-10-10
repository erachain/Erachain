package org.erachain.gui.exdata.sources;

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

public class SourcesModel extends DefaultTableModel {
    public static final int KEY_COL = 0;
    public static final int SHARE_COL = 1;
    public static final int NAME_COL = 2;
    public static final int MEMO_COL = 3;


    public SourcesModel(int rows) {
        super(new String[]{Lang.getInstance().translate("Number"),
                        Lang.getInstance().translate("Share"),
                        Lang.getInstance().translate("Source"),
                        Lang.getInstance().translate("Description")
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
                return true;
            case SHARE_COL:
                return true;
            case MEMO_COL:
                return true;

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
     super.setValueAt(aValue, row, column);
    }

    public void setAuthors(PersonHuman[] Authors) {
        clearSources();

        for (int i = 0; i < Authors.length; ++i) {
            addRow(new Object[]{Authors[i].getKey(), 1, Authors[i].getName(), Authors[i].getDescription()});
        }
    }

    public void clearSources() {
        while (getRowCount() > 0) {
            this.removeRow(getRowCount() - 1);
        }
    }

    public String[] getSources() {
       return null;

    }

    @Override
    public void removeRow(int row) {
        super.removeRow(row);
        if (this.getRowCount() == 0) {
            this.addEmpty();
        }
    }
}

