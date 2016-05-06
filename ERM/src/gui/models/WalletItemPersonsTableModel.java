package gui.models;
////////
import java.util.Observable;
import java.util.Observer;

import org.mapdb.Fun.Tuple2;

import utils.ObserverMessage;
import controller.Controller;
import core.item.persons.PersonCls;
import database.SortableList;
import lang.Lang;

@SuppressWarnings("serial")
public class WalletItemPersonsTableModel extends TableModelCls<Tuple2<String, String>, PersonCls> implements Observer
{
	public static final int COLUMN_KEY = 0;
	public static final int COLUMN_NAME = 1;
	public static final int COLUMN_ADDRESS = 2;
	public static final int COLUMN_CONFIRMED = 3;
	
	private SortableList<Tuple2<String, String>, PersonCls> persons;
	
	private String[] columnNames = Lang.getInstance().translate(new String[]{"Key", "Name", "Owner", "Confirmed"});
	
	public WalletItemPersonsTableModel()
	{
		Controller.getInstance().addWalletListener(this);
	}
	
	@Override
	public SortableList<Tuple2<String, String>, PersonCls> getSortableList() {
		return this.persons;
	}
	
	//public Class<? extends Object> getColumnClass(int c) {     // set column type
    //    return getValueAt(0, c).getClass();
    //}
	
	public PersonCls getItem(int row)
	{
		return this.persons.get(row).getB();
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
		
		PersonCls person = this.persons.get(row).getB();
		
		switch(column)
		{
		case COLUMN_KEY:
			
			return person.getKey();
		
		case COLUMN_NAME:
			
			return person.getName();
		
		case COLUMN_ADDRESS:
			
			return person.getCreator().getAddress();
						
		case COLUMN_CONFIRMED:
			
			return person.isConfirmed();
			
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
		if(message.getType() == ObserverMessage.LIST_PERSON_TYPE)
		{
			if(this.persons == null)
			{
				this.persons = (SortableList<Tuple2<String, String>, PersonCls>) message.getValue();
				this.persons.registerObserver();
				//this.persons.sort(PollMap.NAME_INDEX);
			}
			
			this.fireTableDataChanged();
		}
		
		//CHECK IF LIST UPDATED
		if(message.getType() == ObserverMessage.ADD_PERSON_TYPE || message.getType() == ObserverMessage.REMOVE_PERSON_TYPE)
		{
			this.fireTableDataChanged();
		}	
	}
}
