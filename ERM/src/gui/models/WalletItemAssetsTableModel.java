package gui.models;

import java.util.Observable;
import java.util.Observer;

import org.mapdb.Fun.Tuple2;

import utils.ObserverMessage;
import controller.Controller;
import core.item.assets.AssetCls;
import database.DBSet;
import database.SortableList;
import lang.Lang;

@SuppressWarnings("serial")
public class WalletItemAssetsTableModel extends TableModelCls<Tuple2<String, String>, AssetCls> implements Observer
{
	public static final int COLUMN_KEY = 0;
	public static final int COLUMN_NAME = 1;
	public static final int COLUMN_ADDRESS = 2;
	public static final int COLUMN_MOVABLE = 3;
	public static final int COLUMN_AMOUNT = 4;
	public static final int COLUMN_DIVISIBLE = 5;
	public static final int COLUMN_CONFIRMED = 6;
	public static final int COLUMN_FAVORITE = 7;
	
	private SortableList<Tuple2<String, String>, AssetCls> assets;
	
	private String[] columnNames = Lang.getInstance().translate(new String[]{"Key", "Name", "Owner", "Movable", "Quantity", "Divisible", "Confirmed", "Favorite"});
	private Boolean[] column_AutuHeight = new Boolean[]{false,true,true,false,false,false,false,false};
	
	public WalletItemAssetsTableModel()
	{
		Controller.getInstance().addWalletListener(this);
	}
	
	@Override
	public SortableList<Tuple2<String, String>, AssetCls> getSortableList() {
		return this.assets;
	}
	
	public Class<? extends Object> getColumnClass(int c) {     // set column type
		Object o = getValueAt(0, c);
		return o==null?null:o.getClass();
	    }
	
	// читаем колонки которые изменяем высоту	   
	public Boolean[] get_Column_AutoHeight(){
		
		return this.column_AutuHeight;
	}
// устанавливаем колонки которым изменить высоту	
	public void set_get_Column_AutoHeight( Boolean[] arg0){
		this.column_AutuHeight = arg0;	
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

		try
		{
			AssetCls asset = this.assets.get(row).getB();
			
			switch(column)
			{
			case COLUMN_KEY:
				
				return asset.getKey(DBSet.getInstance());
			
			case COLUMN_NAME:
				
				return asset.getName();
			
			case COLUMN_ADDRESS:
				
				return asset.getCreator().getPersonAsString();
				
			case COLUMN_MOVABLE:
				
				return asset.isMovable();
				
			case COLUMN_AMOUNT:
				
				return asset.getQuantity();
				
			case COLUMN_DIVISIBLE:
				
				return asset.isDivisible();
				
			case COLUMN_CONFIRMED:
				
				return asset.isConfirmed();
				
			case COLUMN_FAVORITE:
				
				return asset.isFavorite();
			}
		}
		catch(Exception e)
		{
			//GUI ERROR
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
				this.assets = (SortableList<Tuple2<String, String>, AssetCls>) message.getValue();
				this.assets.registerObserver();
				//this.assets.sort(PollMap.NAME_INDEX);
			}
			
			this.fireTableDataChanged();
		}
		
		//CHECK IF LIST UPDATED
		if(message.getType() == ObserverMessage.ADD_ASSET_TYPE || message.getType() == ObserverMessage.REMOVE_ASSET_TYPE)
		{
			this.fireTableDataChanged();
		}	
	}
}
