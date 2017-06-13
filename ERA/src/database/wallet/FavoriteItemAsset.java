package database.wallet;

import java.util.List;
import java.util.Observer;

import org.mapdb.DB;

import core.item.assets.AssetCls;
import utils.ObserverMessage;

public class FavoriteItemAsset extends FavoriteItem {

	
	// favorites init SET
	public FavoriteItemAsset(WalletDatabase walletDatabase, DB database) 
	{
		super(walletDatabase, database, ObserverMessage.LIST_ASSET_FAVORITES_TYPE, "asset", AssetCls.INITIAL_FAVORITES);

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
		this.setChanged();
		this.notifyObservers(new ObserverMessage(ObserverMessage.ADD_ASSET_FAVORITES_TYPE, key));
	}
	
	public void delete(Long key)
	{
		this.itemsSet.remove(key);
		this.walletDatabase.commit();
		
		//NOTIFY
		//this.notifyFavorites();
		this.setChanged();
		this.notifyObservers(new ObserverMessage(ObserverMessage.DELETE_ASSET_FAVORITES_TYPE, key));
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
