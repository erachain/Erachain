package gui.items.imprints;

import java.util.Observable;
import java.util.Observer;

import javax.validation.constraints.Null;

import controller.Controller;
import core.item.imprints.ImprintCls;
import utils.NumberAsString;
import utils.ObserverMessage;
import database.SortableList;
import gui.models.TableModelCls;
import lang.Lang;

@SuppressWarnings("serial")
public class TableModelImprints extends TableModelCls<Long, ImprintCls> implements Observer
{
	public static final int COLUMN_KEY = 0;
	public static final int COLUMN_NAME = 1;
	public static final int COLUMN_ADDRESS = 2;
	//public static final int COLUMN_AMOUNT = 3;
	//public static final int COLUMN_DIVISIBLE = 4;
	private Boolean[] column_AutuHeight = new Boolean[]{false,true,true};
	private SortableList<Long, ImprintCls> imprints;
	
	private String[] columnNames = Lang.getInstance().translate(new String[]{"Key", "Name", "Owner"});//, "Quantity"});//, "Divisible"});
	
	public TableModelImprints()
	{
		Controller.getInstance().addObserver(this);
	}
	
	@Override
	public SortableList<Long, ImprintCls> getSortableList() 
	{
		return this.imprints;
	}
	
	// читаем колонки которые изменяем высоту	   
		public Boolean[] get_Column_AutoHeight(){
			
			return this.column_AutuHeight;
		}
	// устанавливаем колонки которым изменить высоту	
		public void set_get_Column_AutoHeight( Boolean[] arg0){
			this.column_AutuHeight = arg0;	
		}
		
	
	public Class<? extends Object> getColumnClass(int c) {     // set column type
		Object o = getValueAt(0, c);
		return o==null?Null.class:o.getClass();
	    }
	
	public ImprintCls getImprint(int row)
	{
		return this.imprints.get(row).getB();
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
		return this.imprints.size();
		
	}

	@Override
	public Object getValueAt(int row, int column) 
	{
		if(this.imprints == null || row > this.imprints.size() - 1 )
		{
			return null;
		}
		
		ImprintCls imprint = this.imprints.get(row).getB();
		
		switch(column)
		{
		case COLUMN_KEY:
			
			return imprint.getKey();
		
		case COLUMN_NAME:
			
			return imprint.getName();
		
		case COLUMN_ADDRESS:
			
			return imprint.getOwner().getPersonAsString();
			
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
		if(message.getType() == ObserverMessage.LIST_IMPRINT_TYPE)
		{			
			if(this.imprints == null)
			{
				this.imprints = (SortableList<Long, ImprintCls>) message.getValue();
				this.imprints.addFilterField("name");
				this.imprints.registerObserver();
			}	
			
			this.fireTableDataChanged();
		}
		
		//CHECK IF LIST UPDATED
		if(message.getType() == ObserverMessage.ADD_IMPRINT_TYPE || message.getType() == ObserverMessage.REMOVE_IMPRINT_TYPE)
		{
			this.fireTableDataChanged();
		}
	}
	
	public void removeObservers() 
	{
		this.imprints.removeObserver();
		Controller.getInstance().deleteObserver(this);
	}
}
