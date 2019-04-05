package org.erachain.gui.models;

import org.erachain.controller.Controller;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.database.SortableList;
import org.erachain.datachain.DCSet;
import org.erachain.lang.Lang;
import org.erachain.utils.ObserverMessage;
import org.mapdb.Fun.Tuple2;

import java.util.Observable;
import java.util.Observer;

@SuppressWarnings("serial")
public class WalletItemAssetsTableModel extends SortedListTableModelCls<Tuple2<String, String>, AssetCls> implements Observer {
    public static final int COLUMN_KEY = 0;
    public static final int COLUMN_NAME = 1;
    public static final int COLUMN_ADDRESS = 2;
    public static final int COLUMN_ASSET_TYPE = 3;
    public static final int COLUMN_AMOUNT = 4;
    public static final int COLUMN_CONFIRMED = 5;
    public static final int COLUMN_FAVORITE = 6;

    public WalletItemAssetsTableModel() {
        super(Controller.getInstance().wallet.database.getAssetMap(),
                new String[]{"Key", "Name", "Owner", "Type", "Quantity", "Confirmed", "Favorite"},
                new Boolean[]{false, true, true, false, false, false, false, false}, true);
    }

    @Override
    public Object getValueAt(int row, int column) {
        if (this.listSorted == null || row > this.listSorted.size() - 1) {
            return null;
        }

        try {
            AssetCls asset = this.listSorted.get(row).getB();

            switch (column) {
                case COLUMN_KEY:

                    return asset.getKey(DCSet.getInstance());

                case COLUMN_NAME:

                    return asset.viewName();

                case COLUMN_ADDRESS:

                    return asset.getOwner().getPersonAsString();

                case COLUMN_ASSET_TYPE:

                    return Lang.getInstance().translate(asset.viewAssetType());

                case COLUMN_AMOUNT:

                    return asset.getTotalQuantity(DCSet.getInstance());

                case COLUMN_CONFIRMED:

                    return asset.isConfirmed();

                case COLUMN_FAVORITE:

                    return asset.isFavorite();
            }
        } catch (Exception e) {
            //GUI ERROR
        }


        return null;
    }

    @SuppressWarnings("unchecked")
    public synchronized void syncUpdate(Observable o, Object arg) {
        ObserverMessage message = (ObserverMessage) arg;

        //CHECK IF NEW LIST
        if (message.getType() == ObserverMessage.LIST_ASSET_TYPE || message.getType() == ObserverMessage.WALLET_LIST_ASSET_TYPE) {
            if (this.listSorted == null) {
                this.listSorted = (SortableList<Tuple2<String, String>, AssetCls>) message.getValue();
                //this.assets.registerObserver();
                //this.assets.sort(PollMap.NAME_INDEX);
            }

            this.fireTableDataChanged();
        }

        //CHECK IF LIST UPDATED
        if (message.getType() == ObserverMessage.ADD_ASSET_TYPE || message.getType() == ObserverMessage.REMOVE_ASSET_TYPE
                || message.getType() == ObserverMessage.WALLET_ADD_ASSET_TYPE || message.getType() == ObserverMessage.WALLET_REMOVE_ASSET_TYPE) {
            this.fireTableDataChanged();
        }
    }

    public void addObservers() {

        super.addObservers();

        if (Controller.getInstance().doesWalletDatabaseExists())
            return;

        map.addObserver(this);

    }

    public void deleteObservers() {
        super.deleteObservers();

        if (Controller.getInstance().doesWalletDatabaseExists())
            return;

        map.deleteObserver(this);
    }

}
