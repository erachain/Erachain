package gui.items.notes;

import java.util.Observable;
import java.util.Observer;

import javax.validation.constraints.Null;

import controller.Controller;
import core.item.notes.NoteCls;
import datachain.SortableList;
import utils.NumberAsString;
import utils.ObserverMessage;
import gui.models.TableModelCls;
import lang.Lang;

@SuppressWarnings("serial")
public class TableModelNotes extends TableModelCls<Long, NoteCls> implements Observer
{
	public static final int COLUMN_KEY = 0;
	public static final int COLUMN_NAME = 1;
	public static final int COLUMN_ADDRESS = 2;
	public static final int COLUMN_FAVORITE = 3;

	private SortableList<Long, NoteCls> notes;
	
	private String[] columnNames = Lang.getInstance().translate(new String[]{"Key", "Name", "Creator", "Favorite"});
	private Boolean[] column_AutuHeight = new Boolean[]{false,true,true,false};
	
	public TableModelNotes()
	{
		Controller.getInstance().addObserver(this);
	}
	
	@Override
	public SortableList<Long, NoteCls> getSortableList() 
	{
		return this.notes;
	}
	
	public Class<? extends Object> getColumnClass(int c) {     // set column type
		Object o = getValueAt(0, c);
		return o==null?Null.class:o.getClass();
	    }
	
	// читаем колонки которые изменяем высоту	   
		public Boolean[] get_Column_AutoHeight(){
			
			return this.column_AutuHeight;
		}
	// устанавливаем колонки которым изменить высоту	
		public void set_get_Column_AutoHeight( Boolean[] arg0){
			this.column_AutuHeight = arg0;	
		}
		
	
	
	public NoteCls getNote(int row)
	{
		return this.notes.get(row).getB();
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
		return (this.notes == null)? 0 : this.notes.size();
	}

	@Override
	public Object getValueAt(int row, int column) 
	{
		if(this.notes == null || row > this.notes.size() - 1 )
		{
			return null;
		}
		
		NoteCls note = this.notes.get(row).getB();
		
		switch(column)
		{
		case COLUMN_KEY:
			
			return note.getKey();
		
		case COLUMN_NAME:
			
			return note.getName();
		
		case COLUMN_ADDRESS:
			
			return note.getOwner().getPersonAsString();

		case COLUMN_FAVORITE:
			
			return note.isFavorite();
			
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
			if(this.notes == null)
			{
				this.notes = (SortableList<Long, NoteCls>) message.getValue();
				this.notes.addFilterField("name");
				this.notes.registerObserver();
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
		this.notes.removeObserver();
		Controller.getInstance().deleteObserver(this);
	}
}
