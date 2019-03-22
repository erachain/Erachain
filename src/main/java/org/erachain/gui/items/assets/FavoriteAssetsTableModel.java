package org.erachain.gui.items.assets;

import org.erachain.controller.Controller;
import org.erachain.core.item.ItemCls;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.database.SortableList;
import org.erachain.database.wallet.FavoriteItemMapAsset;
import org.erachain.datachain.DCSet;
import org.erachain.gui.items.FavoriteItemModelTable;
import org.erachain.lang.Lang;
import org.erachain.utils.ObserverMessage;

@SuppressWarnings("serial")
public class FavoriteAssetsTableModel extends FavoriteItemModelTable {
    public static final int COLUMN_KEY = 0;
    public static final int COLUMN_NAME = 1;
    public static final int COLUMN_ADDRESS = 2;
    public static final int COLUMN_ASSET_TYPE = 3;
    public static final int COLUMN_AMOUNT = 4;
    public static final int COLUMN_FAVORITE = 5;
    public static final int COLUMN_I_OWNER = 6;

    public FavoriteAssetsTableModel() {
        super( //DCSet.getInstance().getItemAssetMap(),
                null,
                new String[]{"Key", "Name", "Owner", "Type", "Quantity", "Favorite", "I Owner"},
                new Boolean[]{false, true, true, false, false, false, false, false},
                ObserverMessage.RESET_ASSET_FAVORITES_TYPE,
                ObserverMessage.ADD_ASSET_FAVORITES_TYPE,
                ObserverMessage.DELETE_ASSET_FAVORITES_TYPE,
                ObserverMessage.LIST_ASSET_FAVORITES_TYPE,
                COLUMN_FAVORITE);
    }

    @Override
    public Object getValueAt(int row, int column) {
        if (this.list == null || row > this.list.size() - 1) {
            return null;
        }

        AssetCls asset = (AssetCls) this.list.get(row);
        if (asset == null)
            return null;

        switch (column) {
            case COLUMN_KEY:

                return asset.getKey();

            case COLUMN_NAME:

                return asset.viewName();

            case COLUMN_ADDRESS:

                return asset.getOwner().getPersonAsString();

            case COLUMN_ASSET_TYPE:

                return Lang.getInstance().translate(asset.viewAssetType());

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

    public void addObserversThis() {
        if (Controller.getInstance().doesWalletDatabaseExists()) {
            Controller.getInstance().wallet.database.getAssetFavoritesSet().addObserver(this);
        }
    }


    public void removeObserversThis() {
        FavoriteItemMapAsset rrr = Controller.getInstance().wallet.database.getAssetFavoritesSet();

        if (Controller.getInstance().doesWalletDatabaseExists())
            Controller.getInstance().wallet.database.getAssetFavoritesSet().deleteObserver(this);
    }

    public long getMapSize() {
        return favoriteMap.size();
    }

    @Override
    public void getIntervalThis(long startBack, long endBack) {
        this.listSorted = new SortableList<Long, ItemCls>(
                DCSet.getInstance().getItemAssetMap(),
                Controller.getInstance().wallet.database.getAssetFavoritesSet().getFromToKeys(startBack, endBack));

    }

}
