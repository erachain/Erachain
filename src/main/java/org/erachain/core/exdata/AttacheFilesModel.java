package org.erachain.core.exdata;

import org.erachain.lang.Lang;

import javax.swing.table.DefaultTableModel;
import javax.validation.constraints.Null;

class AttacheFilesModel extends DefaultTableModel {

    public AttacheFilesModel() {
        super(new Object[]{Lang.getInstance().translate("Name"), Lang.getInstance().translate("Path"), "Zip?",
                Lang.getInstance().translate("Size/Zip size")}, 0);

    }

    public int getColumnCount() {
        return 6;
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return new Boolean(null);
    }

    public Class<? extends Object> getColumnClass(int c) { // set column type
        Object o = getValueAt(0, c);
        return o == null ? Null.class : o.getClass();
    }

    public Object getValueAt(int row, int col) {

        if (this.getRowCount() < row || this.getRowCount() == 0 || col < 0 || row < 0)
            return null;
        return super.getValueAt(row, col);

    }
}
