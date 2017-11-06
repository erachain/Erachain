package database.wallet;

import org.mapdb.DB;

import core.item.statuses.StatusCls;
import utils.ObserverMessage;

public class FavoriteItemStatus extends FavoriteItem {
	
	// favorites init SET
	public FavoriteItemStatus(DWSet dWSet, DB database) 
	{
		super(dWSet, database, ObserverMessage.LIST_STATUS_FAVORITES_TYPE, "status", StatusCls.INITIAL_FAVORITES);
	}
	
}
