package gui.items.assets;

import java.util.Observable;
import java.util.Observer;

import controller.Controller;
import core.item.assets.AssetCls;
import database.SortableList;
import gui.models.TableModelCls;
import utils.NumberAsString;
import utils.ObserverMessage;
import lang.Lang;

@SuppressWarnings("serial")
public class TableModelItemAssets extends TableModelCls<Long, AssetCls> implements Observer
{
	public static final int COLUMN_KEY = 0;
	public static final int COLUMN_NAME = 1;
	public static final int COLUMN_ADDRESS = 2;
	public static final int COLUMN_AMOUNT = 3;
	public static final int COLUMN_DIVISIBLE = 4;
	public static final int COLUMN_FAVORITE = 5;

	private SortableList<Long, AssetCls> assets;
	
	private String[] columnNames = Lang.getInstance().translate(new String[]{"Key", "Name", "Owner", "Quantity", "Divisible", "Favorite"});
	
	public TableModelItemAssets()
	{
		Controller.getInstance().addObserver(this);
	}
	
	@Override
	public SortableList<Long, AssetCls> getSortableList() 
	{
		return this.assets;
	}
	
	public AssetCls getAsset(int row)
	{
		return this.assets.get(row).getB();
	}
	
	@Override
	public int getColumnCount() 
	{
		return this.columnNames.length;
	}
	
	@Override
	public String getColumnName(int index) 
	{
		return this.columnNames[index];
	}

	@Override
	public int getRowCount() 
	{
		return this.assets.size();
		
	}

	@Override
	public Object getValueAt(int row, int column) 
	{
		if(this.assets == null || row > this.assets.size() - 1 )
		{
			return null;
		}
		
		AssetCls asset = this.assets.get(row).getB();
		
		switch(column)
		{
		case COLUMN_KEY:
			
			return asset.getKey();
		
		case COLUMN_NAME:
			
			return asset.getName();
		
		case COLUMN_ADDRESS:
			
			return asset.getCreator().asPerson();
			
		case COLUMN_AMOUNT:
			
			return NumberAsString.getInstance().numberAsString(asset.getQuantity());
			
		case COLUMN_DIVISIBLE:
			
			return asset.isDivisible();
			
		case COLUMN_FAVORITE:
			
			return asset.isFavorite();

		}
		
		return null;
	}

	@Override
	public void update(Observable o, Object arg) 
	{	
		try
		{
			this.syncUpdate(o, arg);
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
		
		//CHECK IF NEW LIST
		if(message.getType() == ObserverMessage.LIST_ASSET_TYPE)
		{			
			if(this.assets == null)
			{
				this.assets = (SortableList<Long, AssetCls>) message.getValue();
				this.assets.addFilterField("name");
				this.assets.registerObserver();
			}	
			
			this.fireTableDataChanged();
		}
		
		//CHECK IF LIST UPDATED
		if(message.getType() == ObserverMessage.ADD_ASSET_TYPE || message.getType() == ObserverMessage.REMOVE_ASSET_TYPE)
		{
			this.fireTableDataChanged();
		}
	}
	
	public void removeObservers() 
	{
		this.assets.removeObserver();
		Controller.getInstance().deleteObserver(this);
	}
}
