package gui.models;

import java.util.Observable;
import java.util.Observer;

import org.apache.log4j.Logger;
import org.mapdb.Fun.Tuple2;

import utils.ObserverMessage;
import controller.Controller;
import core.item.ItemCls;
import database.SortableList;
import lang.Lang;

@SuppressWarnings("serial")
public abstract class WalletItem_TableModel extends TableModelCls<Tuple2<String, String>, ItemCls> implements Observer
{
	static Logger LOGGER = Logger.getLogger(WalletItem_TableModel.class.getName());

	public static final int COLUMN_KEY = 0;
	public static final int COLUMN_NAME = 1;
	public static final int COLUMN_ADDRESS = 2;
	public static final int COLUMN_CONFIRMED = 3;
	
	protected SortableList<Tuple2<String, String>, ItemCls> items;
	
	protected String[] columnNames = Lang.getInstance().translate(new String[]{"Key", "Name", "Owner", "Confirmed"});
	
	protected int observer_add;
	protected int observer_remove;
	protected int observer_list;
	
	public WalletItem_TableModel(int observer_add, int observer_remove, int observer_list)
	{
		Controller.getInstance().addWalletListener(this);
		this.observer_add = observer_add;
		this.observer_remove = observer_remove;
		this.observer_list = observer_list;
		this.fireTableDataChanged();
	}
	
	@Override
	public SortableList<Tuple2<String, String>, ItemCls> getSortableList() {
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
			
			return item.getCreator().getAddress();
						
		case COLUMN_CONFIRMED:
			
			return item.isConfirmed();
			
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
			LOGGER.error(Lang.getInstance().translate("GUI update ERROR") ,e);
		}
	}
	
	//@SuppressWarnings("unchecked")
	public abstract void syncUpdate(Observable o, Object arg);
	/*
	@SuppressWarnings("unchecked")
	public synchronized void syncUpdate(Observable o, Object arg)
	{
		ObserverMessage message = (ObserverMessage) arg;
		
		//CHECK IF NEW LIST
		if(message.getType() == this.observer_list)
		{
			if(this.items == null)
			{
				this.items = (SortableList<Tuple2<String, String>, ItemCls>) message.getValue();
				this.items.registerObserver();
				//this.items.sort(PollMap.NAME_INDEX);
			}
			
			this.fireTableDataChanged();
		}
		
		//CHECK IF LIST UPDATED
		if(message.getType() == this.observer_add || message.getType() == this.observer_remove)
		{
			this.fireTableDataChanged();
		}	
	}
	*/
}
