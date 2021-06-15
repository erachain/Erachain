package org.erachain.gui.items.assets;

import org.erachain.controller.Controller;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.datachain.DCSet;
import org.erachain.gui.items.SearchItemsTableModel;
import org.erachain.lang.Lang;
import org.slf4j.LoggerFactory;

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
        super(DCSet.getInstance().getItemAssetMap(), new String[]{"Key", "Name", "Maker", "Type", "Quantity", "Favorite", "I Maker"},
                new Boolean[]{false, true, true, false, false, false, false, false},
                COLUMN_FAVORITE);

        logger = LoggerFactory.getLogger(ItemAssetsTableModel.class);
    }

    @Override
    public Object getValueAt(int row, int column) {
        if (this.list == null || row > this.list.size() - 1) {
            return null;
        }

        AssetCls asset = (AssetCls) this.getItem(row);
        if (asset == null)
            return "--";

        switch (column) {
            case COLUMN_KEY:

                return asset.getKey();

            case COLUMN_NAME:

                return asset; // use renderer .viewName();

            case COLUMN_ASSET_TYPE:

                return Lang.T(asset.viewAssetType());

            case COLUMN_ADDRESS:

                return asset.getMaker().getPersonAsString();

            case COLUMN_AMOUNT:

                return asset.getQuantity();

            case COLUMN_FAVORITE:

                return asset.isFavorite();

            case COLUMN_I_OWNER:

                if (Controller.getInstance().isAddressIsMine(asset.getMaker().getAddress()))
                    return true;
                return false;
        }

        return null;
    }

}
