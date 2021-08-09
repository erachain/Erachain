package org.erachain.gui.items.imprints;

import org.erachain.controller.Controller;
import org.erachain.core.item.imprints.ImprintCls;
import org.erachain.datachain.DCSet;
import org.erachain.gui.items.SearchItemsTableModel;
import org.slf4j.LoggerFactory;

@SuppressWarnings("serial")
public class ImprintsSearchTableModel extends SearchItemsTableModel {

    public static final int COLUMN_KEY = 0;
    public static final int COLUMN_NAME = 1;
    public static final int COLUMN_BORN = 2;
    public static final int COLUMN_PUBLISHER = 3;
    public static final int COLUMN_FAVORITE = 4;

    public ImprintsSearchTableModel() {
        super(DCSet.getInstance().getItemImprintMap(), new String[]{"Key", "Name", "Birthday", "Publisher", "Favorite"},
                new Boolean[]{false, true, true, false},
                COLUMN_FAVORITE);
        logger = LoggerFactory.getLogger(ImprintsSearchTableModel.class);
    }

    @Override
    protected void updateMap() {
        map = Controller.getInstance().getWallet().dwSet.getTransactionMap();
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

                return item;

            case COLUMN_PUBLISHER:

                return item.getMaker().getPersonAsString();

            case COLUMN_FAVORITE:

                return item.isFavorite();


        }

        return null;
    }

}
