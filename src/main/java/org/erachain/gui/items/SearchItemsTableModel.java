package org.erachain.gui.items;

import org.erachain.core.item.ItemCls;
import org.erachain.database.DBMap;
import org.erachain.datachain.ItemMap;
import org.erachain.gui.models.TimerTableModelCls;
import org.erachain.utils.Pair;

import java.util.*;

@SuppressWarnings("serial")
public abstract class SearchItemsTableModel extends TimerTableModelCls<ItemCls> {

    public SearchItemsTableModel(DBMap itemsMap, String[] columnNames, Boolean[] column_AutoHeight, int favorite) {
        super(itemsMap, columnNames, column_AutoHeight, favorite,false);
    }


    public synchronized void syncUpdate(Observable o, Object arg) {
    }

    public void fill(List<Long> keys) {
        ItemCls item;
        list = new ArrayList<ItemCls>();

        for (Long key: keys) {
            list.add((ItemCls)map.get(key));
        }
        this.fireTableDataChanged();
    }

    public void fill(Iterator<Long> iterator) {

        list = new ArrayList<ItemCls>();

        while (iterator.hasNext()) {
            list.add(((ItemMap)map).get(iterator.next()));
        }

        this.fireTableDataChanged();
    }

    public void findByName(String filter) {
        Pair<String, Iterable> result = ((ItemMap) map).getKeysIteratorByFilterAsArray(filter, 0, 1000);
        Iterator iterator = result.getB().iterator();
        fill(iterator);
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
        list = new ArrayList<>();
        fireTableDataChanged();
    }

}
