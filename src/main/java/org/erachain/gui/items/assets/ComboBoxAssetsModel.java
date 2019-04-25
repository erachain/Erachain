package org.erachain.gui.items.assets;

import org.erachain.controller.Controller;
import org.erachain.core.item.ItemCls;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.database.DBMap;
import org.erachain.database.wallet.FavoriteItemMap;
import org.erachain.gui.models.FavoriteComboBoxModel;

import java.util.*;

@SuppressWarnings("serial")
public class ComboBoxAssetsModel extends FavoriteComboBoxModel {

    public ComboBoxAssetsModel() {
        super(ItemCls.ASSET_TYPE);
    }

    public void setObservable() {
        this.observable = Controller.getInstance().wallet.database.getAssetFavoritesSet();
    }

}
