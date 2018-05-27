package database.wallet;

import java.util.List;
import java.util.Observer;

import org.mapdb.DB;

import core.item.polls.PollCls;
import utils.ObserverMessage;

public class FavoriteItemPoll extends FavoriteItem {
	
	// favorites init SET
	public FavoriteItemPoll(DWSet dWSet, DB database) 
	{
		super(dWSet, database, ObserverMessage.WALLET_LIST_POLL_FAVORITES_TYPE, "poll", PollCls.INITIAL_FAVORITES);
	}
	public void replace(List<Long> keys)
	{
		this.itemsSet.clear();
		this.itemsSet.addAll(keys);
		this.dWSet.commit();
		
		//NOTIFY
		this.notifyFavorites();
	}
	
	public void add(Long key)
	{
		this.itemsSet.add(key);
		this.dWSet.commit();
		
		//NOTIFY
		this.setChanged();
		this.notifyObservers(new ObserverMessage(ObserverMessage.WALLET_ADD_POLL_FAVORITES_TYPE, key));
	}
	
	public void delete(Long key)
	{
		this.itemsSet.remove(key);
		this.dWSet.commit();
		
		//NOTIFY
		//this.notifyFavorites();
		this.setChanged();
		this.notifyObservers(new ObserverMessage(ObserverMessage.WALLET_DELETE_POLL_FAVORITE_TYPE, key));
	}
	
	@Override
	public void addObserver(Observer o) 
	{
		//ADD OBSERVER
		super.addObserver(o);	
		
		//NOTIFY LIST
		this.notifyFavorites();
	}
	
	protected void notifyFavorites()
	{
		this.setChanged();
		this.notifyObservers(new ObserverMessage(this.observer_favorites, this.itemsSet));
	}
	
	
}
