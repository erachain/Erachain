package org.erachain.gui.items.imprints;

import org.erachain.core.item.ItemCls;
import org.erachain.core.item.imprints.ImprintCls;
import org.erachain.datachain.DCSet;
import org.erachain.datachain.ItemImprintMap;
import org.erachain.datachain.Item_Map;
import org.erachain.gui.items.TableModelItemsSearch;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import javax.validation.constraints.Null;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial")
public class TableModelImprintsSearch extends TableModelItemsSearch {

    public static final int COLUMN_KEY = 0;
    public static final int COLUMN_NAME = 1;
    public static final int COLUMN_BORN = 2;
    public static final int COLUMN_PUBLISHER = 3;
    public static final int COLUMN_FAVORITE = 4;

    public TableModelImprintsSearch() {
        super(DCSet.getInstance().getItemImprintMap(), new String[]{"Key", "Name", "Birthday", "Publisher", "Favorite"},
                new Boolean[]{false, true, true, false});
        super.COLUMN_FAVORITE = COLUMN_FAVORITE;
        LOGGER = LoggerFactory.getLogger(TableModelImprintsSearch.class.getName());
    }

    @Override
    public Object getValueAt(int row, int column) {
        if (list == null || row > this.list.size() - 1) {
            return null;
        }

        ImprintCls item = (ImprintCls) list.get(row);

        switch (column) {
            case COLUMN_KEY:

                return item.getKey();

            case COLUMN_NAME:

                return item.getName();

            case COLUMN_PUBLISHER:

                return item.getOwner().getPersonAsString();

            case COLUMN_FAVORITE:

                return item.isFavorite();


        }

        return null;
    }

}
