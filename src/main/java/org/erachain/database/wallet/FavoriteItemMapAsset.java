package org.erachain.database.wallet;

import org.erachain.core.item.assets.AssetCls;
import org.erachain.settings.Settings;
import org.erachain.utils.ObserverMessage;
import org.mapdb.DB;

public class FavoriteItemMapAsset extends FavoriteItemMap {

    // favorites init SET
    public FavoriteItemMapAsset(DWSet dWSet, DB database) {
        super(dWSet, database, ObserverMessage.LIST_ASSET_FAVORITES_TYPE, "asset", AssetCls.INITIAL_FAVORITES);

        if (Settings.getInstance().isTestnet()) {
            add(1077L);
            add(1078L);
            add(1079L);

        } else {
            add(12L);
            add(95L);
            add(94L);
            add(92L);
        }
    }

}
