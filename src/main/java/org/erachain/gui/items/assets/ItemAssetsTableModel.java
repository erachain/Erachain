package org.erachain.gui.items.assets;

import org.erachain.controller.Controller;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.datachain.DCSet;
import org.erachain.gui.items.SearchItemsTableModel;
import org.erachain.lang.Lang;

@SuppressWarnings("serial")
public class ItemAssetsTableModel extends SearchItemsTableModel {
    public static final int COLUMN_KEY = 0;
    public static final int COLUMN_NAME = 1;
    public static final int COLUMN_ADDRESS = 2;
    public static final int COLUMN_ASSET_TYPE = 3;
    public static final int COLUMN_AMOUNT = 4;
    public static final int COLUMN_FAVORITE = 5;
    public static final int COLUMN_I_OWNER = 6;

    public ItemAssetsTableModel() {
        super(DCSet.getInstance().getItemAssetMap(), new String[]{"Key", "Name", "Owner", "Type", "Quantity", "Favorite", "I Owner"},
                new Boolean[]{false, true, true, false, false, false, false, false},
                COLUMN_FAVORITE);
    }

    @Override
    public Object getValueAt(int row, int column) {
        if (this.list == null || row > this.list.size() - 1) {
            return null;
        }

        AssetCls asset = (AssetCls) this.list.get(row);

        switch (column) {
            case COLUMN_KEY:

                return asset.getKey();

            case COLUMN_NAME:

                return asset.viewName();

            case COLUMN_ASSET_TYPE:

                return Lang.getInstance().translate(asset.viewAssetType());

            case COLUMN_ADDRESS:

                return asset.getOwner().getPersonAsString();

            case COLUMN_AMOUNT:

                return asset.getTotalQuantity(DCSet.getInstance());

            case COLUMN_FAVORITE:

                return asset.isFavorite();

            case COLUMN_I_OWNER:

                if (Controller.getInstance().isAddressIsMine(asset.getOwner().getAddress()))
                    return true;
                return false;
        }

        return null;
    }

}
