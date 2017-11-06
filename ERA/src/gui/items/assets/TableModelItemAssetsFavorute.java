package gui.items.assets;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import javax.validation.constraints.Null;

import controller.Controller;
import core.item.assets.AssetCls;
import core.item.persons.PersonCls;
import datachain.SortableList;
import gui.models.TableModelCls;
import utils.NumberAsString;
import utils.ObserverMessage;
import lang.Lang;

@SuppressWarnings("serial")
public class TableModelItemAssetsFavorute extends TableModelCls<Long, AssetCls> implements Observer
{
	public static final int COLUMN_KEY = 0;
	public static final int COLUMN_NAME = 1;
	public static final int COLUMN_ADDRESS = 2;
	public static final int COLUMN_MOVABLE = 3;
	public static final int COLUMN_AMOUNT = 4;
	public static final int COLUMN_DIVISIBLE = 5;
	public static final int COLUMN_FAVORITE = 6;
	public static final int COLUMN_I_OWNER = 7;

	private SortableList<Long, AssetCls> assets;
	
	private String[] columnNames = Lang.getInstance().translate(new String[]{"Key", "Name", "Owner", "Movable", "Quantity", "Divisible", "Favorite", "I Owner"});
	private Boolean[] column_AutuHeight = new Boolean[]{false,true,true,false,false,false,false,false};
	private List <AssetCls> persons;
	
	public TableModelItemAssetsFavorute()
	{
		addObservers();
	}
	
	// читаем колонки которые изменяем высоту	   
		public Boolean[] get_Column_AutoHeight(){
			
			return this.column_AutuHeight;
		}
	// устанавливаем колонки которым изменить высоту	
		public void set_get_Column_AutoHeight( Boolean[] arg0){
			this.column_AutuHeight = arg0;	
		}
		
	
	@Override
	public SortableList<Long, AssetCls> getSortableList() 
	{
		return this.assets;
	}
	
	public Class<? extends Object> getColumnClass(int c) {     // set column type
		Object o = getValueAt(0, c);
		return o==null?Null.class:o.getClass();
	    }
	
	public AssetCls getAsset(int row)
	{
		return this.persons.get(row);
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
		return this.persons.size();
		
	}

	@Override
	public Object getValueAt(int row, int column) 
	{
		if(this.persons == null || row > this.persons.size() - 1 )
		{
			return null;
		}
		
		AssetCls asset = this.persons.get(row);
		if (asset == null) return null;
		
		switch(column)
		{
		case COLUMN_KEY:
			
			return asset.getKey();
		
		case COLUMN_NAME:
			
			return asset.getName();
		
		case COLUMN_ADDRESS:
			
			return asset.getOwner().getPersonAsString();
			
		case COLUMN_MOVABLE:
			
			return asset.isMovable();

		case COLUMN_AMOUNT:
			
			return asset.getTotalQuantity();
			
		case COLUMN_DIVISIBLE:
			
			return asset.isDivisible();
			
		case COLUMN_FAVORITE:
			
			return asset.isFavorite();
		
		case COLUMN_I_OWNER:
			
			if (Controller.getInstance().isAddressIsMine(asset.getOwner().getAddress()))
				return true;
			return false;
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
		if(message.getType() == ObserverMessage.LIST_ASSET_FAVORITES_TYPE && persons==null)
		{
			persons = new ArrayList<AssetCls>();
			fill((Set<Long>) message.getValue());
			this.fireTableDataChanged();
			}
		if(message.getType() == ObserverMessage.ADD_ASSET_FAVORITES_TYPE){
			persons.add(  Controller.getInstance().getAsset((long) message.getValue()));
			this.fireTableDataChanged();
			}
		if(message.getType() == ObserverMessage.DELETE_ASSET_FAVORITES_TYPE){
			persons.remove( Controller.getInstance().getAsset((long) message.getValue()));
			this.fireTableDataChanged();
			}
	}
	
	public void fill(Set<Long> set){
		
		//	persons.clear();
				
			for(Long s:set){
				
				if (s == 0) continue;
					persons.add ( Controller.getInstance().getAsset(s));
				
				
			}
			
			
		}
				
	
	
	public void removeObservers() 
	{
		//this.persons.removeObserver();
		Controller.getInstance().wallet.database.getAssetFavoritesSet().addObserver(this);
		//Controller.getInstance().wallet.database.getPersonMap().deleteObserver(this);
	}
	public void addObservers(){
		
		Controller.getInstance().wallet.database.getAssetFavoritesSet().addObserver(this);
	}
}
