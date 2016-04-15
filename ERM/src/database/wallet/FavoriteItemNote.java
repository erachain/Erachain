package database.wallet;

import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import org.mapdb.DB;

import utils.ObserverMessage;

public class FavoriteItemNote extends FavoriteItem {
	
	// favorites init SET
	public FavoriteItemNote(WalletDatabase walletDatabase, DB database) 
	{
		super(walletDatabase, database, ObserverMessage.LIST_NOTE_FAVORITES_TYPE, "note", 3);
	}
	
}
