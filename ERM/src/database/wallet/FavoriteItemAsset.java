package database.wallet;

import org.mapdb.DB;

import core.item.assets.AssetCls;
import utils.ObserverMessage;

public class FavoriteItemAsset extends FavoriteItem {

	
	// favorites init SET
	public FavoriteItemAsset(WalletDatabase walletDatabase, DB database) 
	{
		super(walletDatabase, database, ObserverMessage.LIST_ASSET_FAVORITES_TYPE, "asset", AssetCls.INITIAL_FAVORITES);

	}
}
