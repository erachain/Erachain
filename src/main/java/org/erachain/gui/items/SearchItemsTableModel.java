package org.erachain.gui.items;

import org.erachain.core.item.ItemCls;
import org.erachain.database.DBMap;
import org.erachain.database.SortableList;
import org.erachain.datachain.ItemMap;
import org.erachain.gui.models.SortedListTableModelCls;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Set;

@SuppressWarnings("serial")
public abstract class SearchItemsTableModel extends SortedListTableModelCls<Long, ItemCls> {

    public SearchItemsTableModel(DBMap itemsMap, String[] columnNames, Boolean[] column_AutoHeight, int favorite) {
        super(itemsMap, columnNames, column_AutoHeight, favorite, false);
    }

    //protected ItemMap db;
    public void fill(Set<Long> keys) {
        ItemCls item;
        list = new ArrayList<ItemCls>();

        for (Long itemKey : keys) {
            if (itemKey == null || itemKey < 1)
                continue;

            item = (ItemCls) map.get(itemKey);
            if (item == null)
                continue;

            list.add(item);
        }

        this.listSorted = new SortableList<Long, ItemCls>(this.map, keys);
    }

    public void findByName(String filter) {
        list = ((ItemMap) map).get_By_Name(filter, false);
        //this.listSorted = new SortableList<Long, ItemCls>(this.map, keys);
        this.fireTableDataChanged();
    }

    public void findByKey(String text) {
        list = new ArrayList<ItemCls>();

        if (text.equals("") || text == null || !text.matches("[0-9]*")) {
            this.fireTableDataChanged();
            return;
        }

        Long key_filter = new Long(text);

        ItemCls itemCls = (ItemCls) map.get(key_filter);

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
    public ItemCls getItem(int row) {
        return this.list.get(row);
    }

    @Override
    public void addObservers() {
    }

    @Override
    public void deleteObservers() {
    }

}
