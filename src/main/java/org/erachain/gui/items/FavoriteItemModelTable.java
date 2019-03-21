package org.erachain.gui.items;

import org.erachain.controller.Controller;
import org.erachain.core.item.ItemCls;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.database.DBMap;
import org.erachain.database.SortableList;
import org.erachain.datachain.DCSet;
import org.erachain.gui.models.TableModelCls;
import org.erachain.lang.Lang;
import org.erachain.utils.ObserverMessage;

import java.util.*;

@SuppressWarnings("serial")
public abstract class FavoriteItemModelTable<T, U> extends TableModelCls<Long, ItemCls> implements Observer {

    protected int itemType;
    protected List<ItemCls> list;
    protected SortableList<Long, ItemCls> listSorted;

    public int COLUMN_FAVORITE = 1000;

    public FavoriteItemModelTable(int itemType, String[] columnNames, Boolean[] columnAutoHeight) {
        super(columnNames, columnAutoHeight);
        this.itemType = itemType;
    }

    @Override
    public SortableList<Long, ItemCls> getSortableList() {
        return this.listSorted;
    }

    @Override
    public int getRowCount() {
        if (this.list == null)
            return 0;

        return this.list.size();

    }

    public void fill(Set<Long> set) {
        ItemCls item;
        for (Long s : set) {
            if (s < 1)
                continue;

            item = Controller.getInstance().getItem(itemType, s);
            if (item == null)
                continue;

            list.add(item);
        }
    }

    @Override
    public ItemCls getItem(int k) {
        // TODO Auto-generated method stub
        return this.list.get(k);
    }

}
