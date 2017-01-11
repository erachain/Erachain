package gui.items;

import java.util.Observable;
import java.util.Observer;

import controller.Controller;
import core.item.ItemCls;
//import utils.NumberAsString;
import utils.ObserverMessage;
import database.SortableList;
import gui.models.TableModelCls;
import lang.Lang;

@SuppressWarnings("serial")
public class TableModelItems extends TableModelCls<Long, ItemCls> implements Observer
{
	public static final int COLUMN_KEY = 0;
	public static final int COLUMN_NAME = 1;
	public static final int COLUMN_ADDRESS = 2;
	//public static final int COLUMN_AMOUNT = 3;
	//public static final int COLUMN_DIVISIBLE = 4;

	private SortableList<Long, ItemCls> items;
	
	private String[] columnNames = Lang.getInstance().translate(new String[]{"Key", "Name", "Owner", "Quantity", "Divisible"});
	
	public TableModelItems()
	{
		Controller.getInstance().addObserver(this);
	}
	
	@Override
	public SortableList<Long, ItemCls> getSortableList() 
	{
		return this.items;
	}
	
	public ItemCls getItem(int row)
	{
		return this.items.get(row).getB();
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
		return this.items.size();
		
	}

	@Override
	public Object getValueAt(int row, int column) 
	{
		if(this.items == null || row > this.items.size() - 1 )
		{
			return null;
		}
		
		ItemCls item = this.items.get(row).getB();
		
		switch(column)
		{
		case COLUMN_KEY:
			
			return item.getKey();
		
		case COLUMN_NAME:
			
			return item.getName();
		
		case COLUMN_ADDRESS:
			
			return item.getCreator().getPersonAsString();
			
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
		if(message.getType() == ObserverMessage.LIST_NOTE_TYPE)
		{			
			if(this.items == null)
			{
				this.items = (SortableList<Long, ItemCls>) message.getValue();
				this.items.addFilterField("name");
				this.items.registerObserver();
			}	
			
			this.fireTableDataChanged();
		}
		
		//CHECK IF LIST UPDATED
		if(message.getType() == ObserverMessage.ADD_NOTE_TYPE || message.getType() == ObserverMessage.REMOVE_NOTE_TYPE)
		{
			this.fireTableDataChanged();
		}
	}
	
	public void removeObservers() 
	{
		this.items.removeObserver();
		Controller.getInstance().deleteObserver(this);
	}
}
