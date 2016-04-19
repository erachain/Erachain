package gui.items.persons;

import java.util.Observable;
import java.util.Observer;

import controller.Controller;
import core.item.persons.PersonCls;
import utils.NumberAsString;
import utils.ObserverMessage;
import database.SortableList;
import gui.models.TableModelCls;
import lang.Lang;

@SuppressWarnings("serial")
public class TableModelPersons extends TableModelCls<Long, PersonCls> implements Observer
{
	public static final int COLUMN_KEY = 0;
	public static final int COLUMN_NAME = 1;
	public static final int COLUMN_ADDRESS = 2;
	//public static final int COLUMN_AMOUNT = 3;
	//public static final int COLUMN_DIVISIBLE = 4;

	private SortableList<Long, PersonCls> persons;
	
	private String[] columnNames = Lang.getInstance().translate(new String[]{"Key", "Name", "Owner", "Quantity", "Divisible"});
	
	public TableModelPersons()
	{
		Controller.getInstance().addObserver(this);
	}
	
	@Override
	public SortableList<Long, PersonCls> getSortableList() 
	{
		return this.persons;
	}
	
	public PersonCls getPerson(int row)
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
				this.persons = (SortableList<Long, PersonCls>) message.getValue();
				this.persons.addFilterField("name");
				this.persons.registerObserver();
			}	
			
			this.fireTableDataChanged();
		}
		
		//CHECK IF LIST UPDATED
		if(message.getType() == ObserverMessage.ADD_PERSON_TYPE || message.getType() == ObserverMessage.REMOVE_PERSON_TYPE)
		{
			this.fireTableDataChanged();
		}
	}
	
	public void removeObservers() 
	{
		this.persons.removeObserver();
		Controller.getInstance().deleteObserver(this);
	}
}
