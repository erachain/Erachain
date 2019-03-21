package org.erachain.gui.items.assets;

import org.erachain.controller.Controller;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.database.SortableList;
import org.erachain.datachain.DCSet;
import org.erachain.gui.models.TableModelCls;
import org.erachain.lang.Lang;
import org.erachain.utils.ObserverMessage;

import javax.validation.constraints.Null;
import java.util.*;

@SuppressWarnings("serial")
public class TableModelItemAssetsFavorute extends TableModelCls<Long, AssetCls> implements Observer {
    public static final int COLUMN_KEY = 0;
    public static final int COLUMN_NAME = 1;
    public static final int COLUMN_ADDRESS = 2;
    public static final int COLUMN_ASSET_TYPE = 3;
    public static final int COLUMN_AMOUNT = 4;
    public static final int COLUMN_FAVORITE = 5;
    public static final int COLUMN_I_OWNER = 6;

    private SortableList<Long, AssetCls> assetsSorted;

    private Boolean[] column_AutuHeight = new Boolean[]{false, true, true, false, false, false, false, false};
    private List<AssetCls> assets;

    public TableModelItemAssetsFavorute() {
        super("TableModelItemAssetsFavorute", 1000,
                new String[]{"Key", "Name", "Owner", "Type", "Quantity", "Favorite", "I Owner"});
        super.COLUMN_FAVORITE = COLUMN_FAVORITE;
    }

    // читаем колонки которые изменяем высоту
    public Boolean[] get_Column_AutoHeight() {

        return this.column_AutuHeight;
    }

    // устанавливаем колонки которым изменить высоту
    public void set_get_Column_AutoHeight(Boolean[] arg0) {
        this.column_AutuHeight = arg0;
    }

    @Override
    public SortableList<Long, AssetCls> getSortableList() {
        return this.assetsSorted;
    }

    @Override
    public Class<? extends Object> getColumnClass(int c) {     // set column type
        Object o = getValueAt(0, c);
        return o == null ? Null.class : o.getClass();
    }

    public AssetCls getAsset(int row) {
        return this.assets.get(row);
    }

    @Override
    public int getRowCount() {
        if (this.assets == null)
            return 0;

        return this.assets.size();

    }

    @Override
    public Object getValueAt(int row, int column) {
        if (this.assets == null || row > this.assets.size() - 1) {
            return null;
        }

        AssetCls asset = this.assets.get(row);
        if (asset == null) return null;

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

    @Override
    public void update(Observable o, Object arg) {
        try {
            this.syncUpdate(o, arg);
        } catch (Exception e) {
            //GUI ERROR
        }
    }

    @SuppressWarnings("unchecked")
    public synchronized void syncUpdate(Observable o, Object arg) {
        ObserverMessage message = (ObserverMessage) arg;

        //CHECK IF NEW LIST
        int type = message.getType();
        if (type == ObserverMessage.LIST_ASSET_FAVORITES_TYPE && assets == null) {
            assets = new ArrayList<AssetCls>();
            fill((Set<Long>) message.getValue());
            this.fireTableDataChanged();
        } else if (type == ObserverMessage.ADD_ASSET_FAVORITES_TYPE) {
            assets.add(Controller.getInstance().getAsset((long) message.getValue()));
            this.fireTableDataChanged();
        } else if (type == ObserverMessage.DELETE_ASSET_FAVORITES_TYPE) {
            assets.remove(Controller.getInstance().getAsset((long) message.getValue()));
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

            assets.add(asset);
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

    @Override
    public Object getItem(int k) {
        // TODO Auto-generated method stub
        return this.assets.get(k);
    }
}
