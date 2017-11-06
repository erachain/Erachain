package gui.items.assets;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.DefaultComboBoxModel;

import utils.ObserverMessage;
import controller.Controller;
import core.item.assets.AssetCls;

@SuppressWarnings("serial")
public class AssetsComboBoxModel extends DefaultComboBoxModel<AssetCls> implements Observer {
	Lock lock = new ReentrantLock();
	
	public AssetsComboBoxModel()
	{
		Controller.getInstance().wallet.database.getAssetFavoritesSet().addObserver(this);
	}
	public void deleteObserver(){
		
		Controller.getInstance().wallet.database.getAssetFavoritesSet().deleteObserver(this);
		
	}
	
	@Override
	public void update(Observable o, Object arg) 
	{
		try
		{
			if (lock.tryLock()) {
				try {
					this.syncUpdate(o, arg);
				}
				finally {
					lock.unlock();
				}
			}
			
		}
		catch(Exception e)
		{
			//GUI ERROR
		}
	}
	
	@SuppressWarnings("unchecked")
	public synchronized void syncUpdate(Observable o, Object arg)
	{
		ObserverMessage message = (ObserverMessage) arg;
		
		//CHECK IF LIST UPDATED
		if(message.getType() == ObserverMessage.LIST_ASSET_FAVORITES_TYPE)
		{
			//GET SELECTED ITEM
			AssetCls selected = (AssetCls) this.getSelectedItem();
						
			//EMPTY LIST
			this.removeAllElements();
				
			//INSERT ALL ACCOUNTS
			Set<Long> keys = (Set<Long>) message.getValue();
			List<AssetCls> assets = new ArrayList<AssetCls>();
			for(Long key: keys)
			{				
				if(key==0) continue;

				//GET ASSET
				AssetCls asset = Controller.getInstance().getAsset(key);
				assets.add(asset);
				
				//ADD
				
				this.addElement(asset);
			}
				
			//RESET SELECTED ITEM
			if(this.getIndexOf(selected) != -1)
			{
				for(AssetCls asset: assets)
				{
					if(asset.getKey() == selected.getKey())
					{
						this.setSelectedItem(asset);
						return;
					}
				}
			}
		}
	}
}
