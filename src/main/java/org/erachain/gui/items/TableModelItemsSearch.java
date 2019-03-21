package org.erachain.gui.items;

import org.erachain.core.item.ItemCls;
import org.erachain.database.SortableList;
import org.erachain.datachain.Item_Map;
import org.erachain.gui.models.TableModelCls;

import java.util.ArrayList;
import java.util.Observable;

@SuppressWarnings("serial")
public abstract class TableModelItemsSearch extends TableModelCls<Long, ItemCls> {

    protected ArrayList<ItemCls> list;
    protected Item_Map db;

    public TableModelItemsSearch(String[] columnNames) {
        super(columnNames);
    }

    public void findByName(String filter) {
        list = (ArrayList<ItemCls>) db.get_By_Name(filter, false);
        this.fireTableDataChanged();
    }

    public void findByKey(String text) {
        if (text.equals("") || text == null)
            return;
        if (!text.matches("[0-9]*"))
            return;
        Long key_filter = new Long(text);
        list = new ArrayList<ItemCls>();

        ItemCls itemCls = db.get(key_filter);

        if (itemCls != null)
            list.add(itemCls);

        this.fireTableDataChanged();
    }

    public void clear() {
        list = new ArrayList<ItemCls>();
        this.fireTableDataChanged();

    }

    @Override
    public void syncUpdate(Observable o, Object arg) { }

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

    @Override
    public void addObserversThis() {
    }

    @Override
    public void removeObserversThis() {
    }

}
