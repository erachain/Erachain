package gui.items.statuses;

import core.item.ItemCls;
import core.item.statuses.StatusCls;
import datachain.DCSet;
import datachain.ItemStatusMap;
import gui.items.TableModelItems;
import lang.Lang;

import javax.validation.constraints.Null;
import java.util.ArrayList;

@SuppressWarnings("serial")
public class TableModelItemStatuses extends TableModelItems {
    public static final int COLUMN_KEY = 0;
    public static final int COLUMN_NAME = 1;
    public static final int COLUMN_ADDRESS = 2;
    public static final int COLUMN_UNIQUE = 3;
    public static final int COLUMN_FAVORITE = 4;

    //private SortableList<Long, StatusCls> statuses;

    private String[] columnNames = Lang.getInstance().translate(new String[]{"Key", "Name", "Creator", "Unique", "Favorite"});
    private Boolean[] column_AutuHeight = new Boolean[]{false, true, true, false};
    private ItemStatusMap db;
    private ArrayList<ItemCls> list;
    private Long key_filter;
    private String filter_Name;

    public TableModelItemStatuses() {
        //	Controller.getInstance().addObserver(this);
        super.COLUMN_FAVORITE = COLUMN_FAVORITE;
        db = DCSet.getInstance().getItemStatusMap();
    }


    public Class<? extends Object> getColumnClass(int c) {     // set column type
        Object o = getValueAt(0, c);
        return o == null ? Null.class : o.getClass();
    }

    // читаем колонки которые изменяем высоту
    public Boolean[] get_Column_AutoHeight() {

        return this.column_AutuHeight;
    }

    // устанавливаем колонки которым изменить высоту
    public void set_get_Column_AutoHeight(Boolean[] arg0) {
        this.column_AutuHeight = arg0;
    }

    public StatusCls getStatus(int row) {
        return (StatusCls) db.get((long) row);
    }

    @Override
    public ItemCls getItem(int row) {
        return this.list.get(row);
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

        return (list == null) ? 0 : list.size();

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


    public void Find_item_from_key(String text) {
        // TODO Auto-generated method stub
        if (text.equals("") || text == null) return;
        if (!text.matches("[0-9]*")) return;
        key_filter = new Long(text);
        list = new ArrayList<ItemCls>();
        StatusCls status = (StatusCls) db.get(key_filter);
        if (status == null) return;
        list.add(status);

        this.fireTableDataChanged();


    }

    public void clear() {
        list = new ArrayList<ItemCls>();
        this.fireTableDataChanged();

    }

    public void set_Filter_By_Name(String str) {
        filter_Name = str;
        list = (ArrayList<ItemCls>) db.get_By_Name(filter_Name, false);
        this.fireTableDataChanged();

    }
}
