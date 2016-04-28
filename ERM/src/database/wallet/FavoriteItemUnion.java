package database.wallet;

import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import org.mapdb.DB;

import utils.ObserverMessage;

public class FavoriteItemUnion extends FavoriteItem {
	
	// favorites init SET
	public FavoriteItemUnion(WalletDatabase walletDatabase, DB database) 
	{
		super(walletDatabase, database, ObserverMessage.LIST_UNION_FAVORITES_TYPE, "union", 3);
	}
	
}
