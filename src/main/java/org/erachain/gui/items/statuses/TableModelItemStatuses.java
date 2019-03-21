package org.erachain.gui.items.statuses;

import org.erachain.core.item.ItemCls;
import org.erachain.core.item.statuses.StatusCls;
import org.erachain.datachain.DCSet;
import org.erachain.datachain.ItemStatusMap;
import org.erachain.gui.items.TableModelItemsSearch;

import java.util.ArrayList;

@SuppressWarnings("serial")
public class TableModelItemStatuses extends TableModelItemsSearch {
    public static final int COLUMN_KEY = 0;
    public static final int COLUMN_NAME = 1;
    public static final int COLUMN_ADDRESS = 2;
    public static final int COLUMN_UNIQUE = 3;
    public static final int COLUMN_FAVORITE = 4;

    //private SortableList<Long, StatusCls> statuses;

    private Boolean[] column_AutuHeight = new Boolean[]{false, true, true, false};
    private ItemStatusMap db;
    private ArrayList<ItemCls> list;
    private Long key_filter;
    private String filter_Name;

    public TableModelItemStatuses() {
        super(new String[]{"Key", "Name", "Creator", "Unique", "Favorite"});

        super.COLUMN_FAVORITE = COLUMN_FAVORITE;
        db = DCSet.getInstance().getItemStatusMap();
    }

    // читаем колонки которые изменяем высоту
    public Boolean[] getColumnAutoHeight() {

        return this.column_AutuHeight;
    }

    // устанавливаем колонки которым изменить высоту
    public void setColumnAutoHeight(Boolean[] arg0) {
        this.column_AutuHeight = arg0;
    }

    @Override
    public Object getValueAt(int row, int column) {
        if (list == null || row > list.size() - 1) {
            return null;
        }

        StatusCls status = (StatusCls) list.get(row);

        switch (column) {
            case COLUMN_KEY:

                return status.getKey();

            case COLUMN_NAME:

                return status.viewName();

            case COLUMN_ADDRESS:

                return status.getOwner().getPersonAsString();

            case COLUMN_FAVORITE:

                return status.isFavorite();

            case COLUMN_UNIQUE:

                return status.isUnique();

        }

        return null;
    }

}
