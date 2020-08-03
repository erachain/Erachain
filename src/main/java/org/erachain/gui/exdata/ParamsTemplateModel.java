package org.erachain.gui.exdata;

import org.erachain.lang.Lang;

import javax.swing.table.DefaultTableModel;
import javax.validation.constraints.Null;

class ParamsTemplateModel extends DefaultTableModel {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public ParamsTemplateModel() {
        super(new Object[]{Lang.getInstance().translate("Name"), Lang.getInstance().translate("=")}, 0);

    }

    public int getColumnCount() {
        return 2;
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        if (column == 1)
            return true;
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
