package database.wallet;

import org.mapdb.DB;

import utils.ObserverMessage;

public class FavoriteItemPerson extends FavoriteItem {
	
	// favorites init SET
	public FavoriteItemPerson(WalletDatabase walletDatabase, DB database) 
	{
		super(walletDatabase, database, ObserverMessage.LIST_PERSON_FAVORITES_TYPE, "person", 0);
	}
	
}
