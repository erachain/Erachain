package org.erachain.gui.models;

import org.erachain.controller.Controller;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.datachain.DCSet;
import org.erachain.lang.Lang;

@SuppressWarnings("serial")
public class WalletItemAssetsTableModel extends WalletTableModel<AssetCls> {
    public static final int COLUMN_KEY = 0;
    public static final int COLUMN_NAME = 1;
    public static final int COLUMN_ADDRESS = 2;
    public static final int COLUMN_ASSET_TYPE = 3;
    public static final int COLUMN_AMOUNT = 4;
    public static final int COLUMN_FAVORITE = 5;

    DCSet dcSet = DCSet.getInstance();

    public WalletItemAssetsTableModel() {
        super(Controller.getInstance().getWallet().dwSet.getAssetMap(),
                new String[]{"Key", "Name", "Maker", "Type", "Quantity", "Favorite"},
                new Boolean[]{false, true, true, false, false, false}, true, COLUMN_FAVORITE);

    }

    @Override
    protected void updateMap() {
        map = Controller.getInstance().getWallet().dwSet.getAssetMap();
    }

    @Override
    public Object getValueAt(int row, int column) {
        if (this.list == null || row > this.list.size() - 1) {
            return null;
        }

        AssetCls asset = this.list.get(row);

        switch (column) {

            case COLUMN_CONFIRMATIONS:
                return asset.getConfirmations(dcSet);

            case COLUMN_KEY:
                return asset.getKey();

            case COLUMN_NAME:
                return asset; // for Icon

            case COLUMN_ADDRESS:
                return asset.getMaker().getPersonAsString();

            case COLUMN_ASSET_TYPE:
                return Lang.T(asset.viewAssetType());

            case COLUMN_AMOUNT:
                return asset.getQuantity();

            case COLUMN_FAVORITE:
                return asset.isFavorite();
        }

        return null;
    }

}
