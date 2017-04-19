package database.wallet;

import org.mapdb.DB;

import core.item.notes.NoteCls;
import utils.ObserverMessage;

public class FavoriteItemNote extends FavoriteItem {
	
	// favorites init SET
	public FavoriteItemNote(WalletDatabase walletDatabase, DB database) 
	{
		super(walletDatabase, database, ObserverMessage.LIST_NOTE_FAVORITES_TYPE, "note", NoteCls.INITIAL_FAVORITES);
	}
	
}
