package org.erachain.gui.items;

import com.google.common.collect.Iterators;
import org.erachain.core.item.ItemCls;
import org.erachain.datachain.ItemMap;
import org.erachain.dbs.DBTabImpl;
import org.erachain.dbs.IteratorCloseable;
import org.erachain.utils.Pair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Observable;

@SuppressWarnings("serial")
public abstract class SearchItemsTableModel extends WalletItemTableModel<ItemCls> {

    public SearchItemsTableModel(DBTabImpl itemsMap, String[] columnNames, Boolean[] column_AutoHeight, int favorite) {
        super(itemsMap, columnNames, column_AutoHeight, favorite, true);
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

        if (iterator != null) {
            while (iterator.hasNext()) {
                list.add(((ItemMap) map).get(iterator.next()));
            }
        }

        this.fireTableDataChanged();
    }

    public void findByName(String filter) {

        IteratorCloseable iterator = null;
        try {
            Pair<String, IteratorCloseable<Long>> result = ((ItemMap) map).getKeysIteratorByFilterAsArray(filter, null, 0, descending);
            iterator = result.getB();
            fill(iterator);
        } finally {
            if (iterator != null) {
                try {
                    iterator.close();
                } catch (IOException e) {
                }
            }
        }
    }

    public void getLast() {
        try (IteratorCloseable iterator = ((ItemMap) map).getIndexIterator(0, true)) {
            fill(Iterators.limit(iterator, 200));
        } catch (IOException e) {
        }
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
