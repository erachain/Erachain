package org.erachain.core.transaction;

import org.erachain.core.item.ItemCls;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.item.assets.Order;

public interface Orderable {

    Order getOrderFromDb();

    AssetCls getHaveAsset();

    AssetCls getWantAsset();
}
