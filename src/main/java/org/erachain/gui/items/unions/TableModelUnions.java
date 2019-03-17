package org.erachain.gui.items.unions;

import org.erachain.core.item.unions.UnionCls;
import org.erachain.datachain.DCSet;
import org.erachain.gui.items.TableModelItems;
import org.erachain.lang.Lang;

@SuppressWarnings("serial")
public class TableModelUnions extends TableModelItems {
    public static final int COLUMN_KEY = 0;
    public static final int COLUMN_NAME = 1;
    public static final int COLUMN_ADDRESS = 2;
    public static final int COLUMN_FAVORITE = 3;

    public TableModelUnions() {
        super("TableModelUnions", 0, new String[]{"Key", "Name", "Creator", "Favorite"});
        super.COLUMN_FAVORITE = COLUMN_FAVORITE;
        db = DCSet.getInstance().getItemUnionMap();
    }

    @Override
    public Object getValueAt(int row, int column) {
        if (this.list == null || row > this.list.size() - 1) {
            return null;
        }

        UnionCls union = (UnionCls) this.list.get(row);

        switch (column) {
            case COLUMN_KEY:

                return union.getKey();

            case COLUMN_NAME:

                return union.viewName();

            case COLUMN_ADDRESS:

                return union.getOwner().getPersonAsString();

            case COLUMN_FAVORITE:

                return union.isFavorite();

        }

        return null;
    }

}
