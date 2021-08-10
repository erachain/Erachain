package org.erachain.gui.models;
////////

import org.erachain.controller.Controller;
import org.erachain.core.item.imprints.ImprintCls;

@SuppressWarnings("serial")
public class WalletItemImprintsTableModel extends WalletTableModel<ImprintCls> {
    public static final int COLUMN_KEY = 0;
    public static final int COLUMN_NAME = 1;
    public static final int COLUMN_ADDRESS = 2;
    public static final int COLUMN_CONFIRMED = 3;
    public static final int COLUMN_FAVORITE = 4;

    public WalletItemImprintsTableModel() {
        super(Controller.getInstance().getWallet().dwSet.getImprintMap(),
                new String[]{"Key", "Name", "Maker", "Confirmed", "Favorite"},
                new Boolean[]{false, true, true, false}, true, COLUMN_FAVORITE);
    }

    @Override
    protected void updateMap() {
        map = Controller.getInstance().getWallet().dwSet.getImprintMap();
    }

    @Override
    public Object getValueAt(int row, int column) {
        if (this.list == null || row > this.list.size() - 1) {
            return null;
        }

        ImprintCls imprint = this.list.get(row);

        switch (column) {
            case COLUMN_KEY:
                return imprint.getKey();

            case COLUMN_NAME:
                return imprint;

            case COLUMN_ADDRESS:
                return imprint.getMaker().getPersonAsString();

            case COLUMN_CONFIRMED:
                return imprint.isConfirmed();

            case COLUMN_FAVORITE:
                return imprint.isFavorite();
        }

        return null;
    }

}
