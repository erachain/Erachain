package database.wallet;

import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import org.mapdb.DB;

import utils.ObserverMessage;

public class FavoriteItem extends Observable {

	protected WalletDatabase walletDatabase;
	protected Set<Long> itemsSet;
	
	protected int observer_favorites;

	// favorites init SET
	public FavoriteItem(WalletDatabase walletDatabase, DB database, int observer_favorites,
			String treeSet, int initialAdd) 
	{
		this.walletDatabase = walletDatabase;
		this.observer_favorites = observer_favorites;
		
		//OPEN MAP
		this.itemsSet = database.getTreeSet(treeSet + "Favorites");

		for (long i = 0; i < initialAdd; i++)
		{
			//CHECK IF CONTAINS ITEM
			if(!this.itemsSet.contains(i))
			{
				this.add(i);
			} else {
				break;
			}
		}
	}
	
	public void replace(List<Long> keys)
	{
		this.itemsSet.clear();
		this.itemsSet.addAll(keys);
		this.walletDatabase.commit();
		
		//NOTIFY
		this.notifyFavorites();
	}
	
	public void add(Long key)
	{
		this.itemsSet.add(key);
		this.walletDatabase.commit();
		
		//NOTIFY
		this.notifyFavorites();
	}
	
	public void delete(Long key)
	{
		this.itemsSet.remove(key);
		this.walletDatabase.commit();
		
		//NOTIFY
		this.notifyFavorites();
	}
	
	public boolean contains(Long key)
	{
		return this.itemsSet.contains(key);
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
