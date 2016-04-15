package database.wallet;

import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import org.mapdb.DB;

import utils.ObserverMessage;

public class FavoriteItemAsset extends FavoriteItem {

	
	// favorites init SET
	public FavoriteItemAsset(WalletDatabase walletDatabase, DB database) 
	{
		super(walletDatabase, database, ObserverMessage.LIST_ASSET_FAVORITES_TYPE, "asset", 3);
	}
}
