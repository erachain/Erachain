package org.erachain.gui.items;

import org.erachain.core.item.ItemCls;
import org.erachain.database.DBMap;
import org.erachain.database.SortableList;
import org.erachain.datachain.ItemMap;
import org.erachain.gui.models.SortedListTableModelCls;
import org.erachain.utils.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Set;

@SuppressWarnings("serial")
public abstract class SearchItemsTableModel extends SortedListTableModelCls<Long, ItemCls> {

    public SearchItemsTableModel(DBMap itemsMap, String[] columnNames, Boolean[] column_AutoHeight, int favorite) {
        super(itemsMap, columnNames, column_AutoHeight, favorite, false);
    }

    public void fill(List<Long> keys) {
        ItemCls item;
        list = new ArrayList<ItemCls>();

        this.listSorted = new SortableList<Long, ItemCls>(this.map, keys);

        for (Pair<Long, ItemCls> pair : listSorted) {
            if (pair.getA() == null || pair.getA() < 1)
                continue;

            item = pair.getB();
            if (item == null)
                continue;

            list.add(item);
        }
        this.fireTableDataChanged();
    }

    public void findByName(String filter) {
        List<Long> keys = ((ItemMap) map).findKeysByName(filter, false);
        fill(keys);
    }

    public void findByKey(String text) {
        List<Long> keys = new ArrayList<Long>();

        if (text.equals("") || text == null || !text.matches("[0-9]*")) {
            fill(keys);
            return;
        }

        Long key = new Long(text);

        if (map.get(key) != null)
            keys.add(key);

        fill(keys);

    }

    public void clear() {
        List<Long> keys = new ArrayList<Long>();
        fill(keys);

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
