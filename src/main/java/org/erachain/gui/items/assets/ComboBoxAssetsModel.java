package org.erachain.gui.items.assets;

import org.erachain.controller.Controller;
import org.erachain.core.item.ItemCls;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.gui.models.FavoriteComboBoxModel;

@SuppressWarnings("serial")
public class ComboBoxAssetsModel extends FavoriteComboBoxModel {

    public ComboBoxAssetsModel() {
        super(ItemCls.ASSET_TYPE);
        setSelectedItem(Controller.getInstance().getItem(item_type, AssetCls.FEE_KEY));
    }

    public void setObservable() {
        this.observable = Controller.getInstance().getWallet().dwSet.getAssetFavoritesSet();
    }

}
