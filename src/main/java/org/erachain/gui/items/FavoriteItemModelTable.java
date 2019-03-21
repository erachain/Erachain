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
public class FavoriteItemModelTable<T, U> extends TableModelCls<T, U> implements Observer {

    protected int itemType;
    protected List<U> list;
    protected SortableList<T, U> listSorted;

    public int COLUMN_FAVORITE = 1000;

    public FavoriteItemModelTable(int itemType, String[] columnNames, Boolean[] columnAutoHeight) {
        super(columnNames, columnAutoHeight);
        this.itemType = itemType;
    }

    @Override
    public SortableList<T, U> getSortableList() {
        return this.listSorted;
    }

    @Override
    public int getRowCount() {
        if (this.list == null)
            return 0;

        return this.list.size();

    }

    public void fill(Set<Long> set) {
        AssetCls asset;
        for (Long s : set) {
            if (s < 1)
                continue;

            asset = Controller.getInstance().getItem(itemType, s);
            if (asset == null)
                continue;

            assets.add(asset);
        }
    }

}
