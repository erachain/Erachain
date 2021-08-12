package org.erachain.gui.items.assets;

import org.erachain.controller.Controller;
import org.erachain.core.item.ItemCls;
import org.erachain.gui.models.FavoriteComboBoxModel;

@SuppressWarnings("serial")
public class ComboBoxAssetsModel extends FavoriteComboBoxModel {

    public ComboBoxAssetsModel(Long initSelect) {
        super(ItemCls.ASSET_TYPE);
        if (initSelect != null)
            setSelectedItem(Controller.getInstance().getItem(item_type, initSelect));
    }

    public void setObservable() {
        this.observable = Controller.getInstance().getWallet().dwSet.getAssetFavoritesSet();
    }

}
