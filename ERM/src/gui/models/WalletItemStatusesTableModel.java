package gui.models;
////////
import java.util.Observable;
import java.util.Observer;

import org.mapdb.Fun.Tuple2;

import utils.ObserverMessage;
import controller.Controller;
import core.item.statuses.StatusCls;
import database.DBSet;
import database.SortableList;
import lang.Lang;

@SuppressWarnings("serial")
public class WalletItemStatusesTableModel extends TableModelCls<Tuple2<String, String>, StatusCls> implements Observer
{
	public static final int COLUMN_KEY = 0;
	public static final int COLUMN_NAME = 1;
	public static final int COLUMN_ADDRESS = 2;
	public static final int COLUMN_CONFIRMED = 3;
	public static final int COLUMN_FAVORITE = 4;
	
	private SortableList<Tuple2<String, String>, StatusCls> statuses;
	
	private String[] columnNames = Lang.getInstance().translate(new String[]{"Key", "Name", "Creator", "Confirmed", "Favorite"});
	private Boolean[] column_AutuHeight = new Boolean[]{false,true,true,false,false};
	
	public WalletItemStatusesTableModel()
	{
		Controller.getInstance().addWalletListener(this);
	}
	
	@Override
	public SortableList<Tuple2<String, String>, StatusCls> getSortableList() {
		return this.statuses;
	}
	
	public StatusCls getItem(int row)
	{
		return this.statuses.get(row).getB();
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
		 return this.statuses.size();
	}

	@Override
	public Object getValueAt(int row, int column) 
	{
		if(this.statuses == null || row > this.statuses.size() - 1 )
		{
			return null;
		}
		
		StatusCls status = this.statuses.get(row).getB();
		
		switch(column)
		{
		case COLUMN_KEY:
			
			return status.getKey(DBSet.getInstance());
		
		case COLUMN_NAME:
			
			return status.getName();
		
		case COLUMN_ADDRESS:
			
			return status.getCreator().getPersonAsString();
						
		case COLUMN_CONFIRMED:
			
			return status.isConfirmed();
			
		case COLUMN_FAVORITE:
			
			return status.isFavorite();
			
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
		if(message.getType() == ObserverMessage.LIST_STATUS_TYPE)
		{
			if(this.statuses == null)
			{
				this.statuses = (SortableList<Tuple2<String, String>, StatusCls>) message.getValue();
				this.statuses.registerObserver();
				//this.statuses.sort(PollMap.NAME_INDEX);
			}
			
			this.fireTableDataChanged();
		}
		
		//CHECK IF LIST UPDATED
		if(message.getType() == ObserverMessage.ADD_STATUS_TYPE || message.getType() == ObserverMessage.REMOVE_STATUS_TYPE)
		{
			this.fireTableDataChanged();
		}	
	}
}
