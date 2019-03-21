package org.erachain.gui.items.assets;

import org.erachain.controller.Controller;
import org.erachain.core.item.ItemCls;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.database.SortableList;
import org.erachain.datachain.DCSet;
import org.erachain.gui.items.FavoriteItemModelTable;
import org.erachain.lang.Lang;
import org.erachain.utils.ObserverMessage;

import java.util.*;

@SuppressWarnings("serial")
public class FavoriteItemAssetsTableModel extends FavoriteItemModelTable<Long, AssetCls> {
    public static final int COLUMN_KEY = 0;
    public static final int COLUMN_NAME = 1;
    public static final int COLUMN_ADDRESS = 2;
    public static final int COLUMN_ASSET_TYPE = 3;
    public static final int COLUMN_AMOUNT = 4;
    public static final int COLUMN_FAVORITE = 5;
    public static final int COLUMN_I_OWNER = 6;


    public FavoriteItemAssetsTableModel() {
        super(ItemCls.ASSET_TYPE, new String[]{"Key", "Name", "Owner", "Type", "Quantity", "Favorite", "I Owner"},
                new Boolean[]{false, true, true, false, false, false, false, false});
        super.COLUMN_FAVORITE = COLUMN_FAVORITE;
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

    @SuppressWarnings("unchecked")
    public synchronized void syncUpdate(Observable o, Object arg) {
        ObserverMessage message = (ObserverMessage) arg;

        //CHECK IF NEW LIST
        int type = message.getType();
        if (type == ObserverMessage.LIST_ASSET_FAVORITES_TYPE && list == null) {
            list = new ArrayList<ItemCls>();
            fill((Set<Long>) message.getValue());
            this.fireTableDataChanged();
        } else if (type == ObserverMessage.ADD_ASSET_FAVORITES_TYPE) {
            list.add(Controller.getInstance().getAsset((long) message.getValue()));
            this.fireTableDataChanged();
        } else if (type == ObserverMessage.DELETE_ASSET_FAVORITES_TYPE) {
            list.remove(Controller.getInstance().getAsset((long) message.getValue()));
            this.fireTableDataChanged();
        }
    }

    public void fill(Set<Long> set) {
        AssetCls asset;
        for (Long s : set) {
            if (s < 1)
                continue;

            asset = Controller.getInstance().getAsset(s);
            if (asset == null)
                continue;

            list.add(asset);
        }
    }

    public void addObserversThis() {
        if (Controller.getInstance().doesWalletDatabaseExists()) {
            Controller.getInstance().wallet.database.getAssetFavoritesSet().addObserver(this);
        }
    }

    public void removeObserversThis() {
        if (Controller.getInstance().doesWalletDatabaseExists())
            Controller.getInstance().wallet.database.getAssetFavoritesSet().deleteObserver(this);
    }

}
