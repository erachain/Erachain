package gui.items.statuses;

import java.util.Observable;
import java.util.Observer;

import controller.Controller;
import core.item.statuses.StatusCls;
import utils.NumberAsString;
import utils.ObserverMessage;
import database.SortableList;
import gui.models.TableModelCls;
import lang.Lang;

@SuppressWarnings("serial")
public class TableModelItemStatuses extends TableModelCls<Long, StatusCls> implements Observer
{
	public static final int COLUMN_KEY = 0;
	public static final int COLUMN_NAME = 1;
	public static final int COLUMN_ADDRESS = 2;
	public static final int COLUMN_FAVORITE = 3;

	private SortableList<Long, StatusCls> statuses;
	
	private String[] columnNames = Lang.getInstance().translate(new String[]{"Key", "Name", "Creator", "Favorite"});
	
	public TableModelItemStatuses()
	{
		Controller.getInstance().addObserver(this);
	}
	
	@Override
	public SortableList<Long, StatusCls> getSortableList() 
	{
		return this.statuses;
	}
	
	
	public Class<? extends Object> getColumnClass(int c) {     // set column type
	       return getValueAt(0, c).getClass();
	    }
	
	public StatusCls getStatus(int row)
	{
		return this.statuses.get(row).getB();
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
			
			return status.getKey();
		
		case COLUMN_NAME:
			
			return status.getName();
		
		case COLUMN_ADDRESS:
			
			return status.getCreator().getAddress();
			
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
	//	System.out.println("message:"+message.getType()+"  value:"+ message.getValue());
		//CHECK IF NEW LIST
		if(message.getType() == ObserverMessage.LIST_STATUS_TYPE)
		{			
			if(this.statuses == null)
			{
				this.statuses = (SortableList<Long, StatusCls>) message.getValue();
				this.statuses.addFilterField("name");
				this.statuses.registerObserver();
			}	
			
			this.fireTableDataChanged();
		}
		int a = message.getType();
		//CHECK IF LIST UPDATED
		if(message.getType() == ObserverMessage.ADD_STATUS_TYPE || message.getType() == ObserverMessage.REMOVE_STATUS_TYPE || message.getType() == ObserverMessage.LIST_STATUS_FAVORITES_TYPE )
		{
			
			
			this.statuses = (SortableList<Long, StatusCls>) message.getValue();
			this.fireTableDataChanged();
		}
	}
	
	public void removeObservers() 
	{
		this.statuses.removeObserver();
		Controller.getInstance().deleteObserver(this);
	}
}
