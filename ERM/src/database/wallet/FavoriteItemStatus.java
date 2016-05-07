package database.wallet;

import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import org.mapdb.DB;

import core.item.statuses.StatusCls;
import utils.ObserverMessage;

public class FavoriteItemStatus extends FavoriteItem {
	
	// favorites init SET
	public FavoriteItemStatus(WalletDatabase walletDatabase, DB database) 
	{
		super(walletDatabase, database, ObserverMessage.LIST_STATUS_FAVORITES_TYPE, "status", StatusCls.INITIAL_FAVORITES);
	}
	
}
