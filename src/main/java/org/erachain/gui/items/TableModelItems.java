package org.erachain.gui.items;

import org.erachain.core.item.ItemCls;
import org.erachain.database.SortableList;
import org.erachain.datachain.Item_Map;
import org.erachain.gui.models.TableModelCls;

import javax.validation.constraints.Null;
import java.util.ArrayList;

@SuppressWarnings("serial")
public abstract class TableModelItems extends TableModelCls<Long, ItemCls> {
    //public static final int COLUMN_KEY = 0;
    //public static final int COLUMN_NAME = 1;
    //public static final int COLUMN_ADDRESS = 2;
    //public static final int COLUMN_AMOUNT = 3;
    //public static final int COLUMN_ASSET_TYPE = 4;

    protected ArrayList<ItemCls> list;
    protected Item_Map db;


    public TableModelItems(String name, long timeout, String[] columnNames) {
        super(name, timeout, columnNames);
    }

    public void Find_item_from_key(String text) {
        // TODO Auto-generated method stub
        if (text.equals("") || text == null) return;
        if (!text.matches("[0-9]*")) return;
        Long key_filter = new Long(text);
        list = null;
        list = new ArrayList<ItemCls>();
        ItemCls itemCls = (ItemCls) db.get(key_filter);
        if (itemCls == null) return;
        list.add(itemCls);
        this.fireTableDataChanged();
    }

    public void clear() {
        list = null;
        list = new ArrayList<ItemCls>();
        this.fireTableDataChanged();

    }

    public void set_Filter_By_Name(String str) {
        list = null;
        list = (ArrayList<ItemCls>) db.get_By_Name(str, false);
        this.fireTableDataChanged();
    }

    @Override
    public SortableList<Long, ItemCls> getSortableList() {
        return null;
    }

    public ItemCls getItem(int row) {
        return this.list.get(row);
    }

    @Override
    public int getRowCount() {
        return (this.list == null) ? 0 : this.list.size();
    }

}
