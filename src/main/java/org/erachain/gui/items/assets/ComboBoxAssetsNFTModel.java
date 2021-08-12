package org.erachain.gui.items.assets;

import org.erachain.controller.Controller;
import org.erachain.core.item.ItemCls;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.item.assets.AssetUniqueSeriesCopy;

@SuppressWarnings("serial")
public class ComboBoxAssetsNFTModel extends ComboBoxAssetsModel {

    public ComboBoxAssetsNFTModel() {
        super(null);
    }

    public void setObservable() {
        this.observable = Controller.getInstance().getWallet().dwSet.getAssetFavoritesSet();
    }

    @Override
    protected void addElementFiltered(Object o) {
        ItemCls item = getElementByEvent((Long) o);
        if (!filter(item))
            return;

        items.add(item);
        super.addElement(item);
    }

    @Override
    public boolean filter(ItemCls item) {
        return item != null && ((AssetCls) item).getAssetType() == AssetCls.AS_NON_FUNGIBLE && !(item instanceof AssetUniqueSeriesCopy);
    }

}
