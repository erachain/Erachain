package org.erachain.core.exdata;

import org.erachain.lang.Lang;

import javax.swing.table.DefaultTableModel;
import javax.validation.constraints.Null;

public class AttacheFilesModel extends DefaultTableModel {

    public static final int NAME_COL = 0;
    public static final int PATH_COL = 1;
    public static final int ZIP_COL = 2;
    public static final int SIZE_COL = 3;
    public static final int BYTES_COL = 4;
    public static final int ZIP_BYTES_COL = 5;

    public AttacheFilesModel() {
        super(new Object[]{Lang.getInstance().translate("Name"), Lang.getInstance().translate("Path"), "Zip?",
                Lang.getInstance().translate("Size/Zip size")}, 0);

    }

    /**
     * Тут в колонке 4 хранится незашифрованный байтМассив, а в 5-й - запакованный
     *
     * @return
     */
    @Override
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
