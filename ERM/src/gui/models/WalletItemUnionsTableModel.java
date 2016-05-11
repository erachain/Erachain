package gui.models;
////////
import java.util.Observable;
import java.util.Observer;

import org.mapdb.Fun.Tuple2;

import utils.ObserverMessage;
import controller.Controller;
import core.item.unions.UnionCls;
import database.SortableList;
import lang.Lang;

@SuppressWarnings("serial")
public class WalletItemUnionsTableModel extends TableModelCls<Tuple2<String, String>, UnionCls> implements Observer
{
	public static final int COLUMN_KEY = 0;
	public static final int COLUMN_NAME = 1;
	public static final int COLUMN_ADDRESS = 2;
	public static final int COLUMN_CONFIRMED = 3;
	public static final int COLUMN_FAVORITE = 4;
	
	private SortableList<Tuple2<String, String>, UnionCls> unions;
	
	private String[] columnNames = Lang.getInstance().translate(new String[]{"Key", "Name", "Owner", "Confirmed", "Favorite"});
	
	public WalletItemUnionsTableModel()
	{
		Controller.getInstance().addWalletListener(this);
	}
	
	@Override
	public SortableList<Tuple2<String, String>, UnionCls> getSortableList() {
		return this.unions;
	}
	
	public UnionCls getItem(int row)
	{
		return this.unions.get(row).getB();
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
		 return this.unions.size();
	}

	@Override
	public Object getValueAt(int row, int column) 
	{
		if(this.unions == null || row > this.unions.size() - 1 )
		{
			return null;
		}
		
		UnionCls union = this.unions.get(row).getB();
		
		switch(column)
		{
		case COLUMN_KEY:
			
			return union.getKey();
		
		case COLUMN_NAME:
			
			return union.getName();
		
		case COLUMN_ADDRESS:
			
			return union.getCreator().asPerson();
						
		case COLUMN_CONFIRMED:
			
			return union.isConfirmed();
			
		case COLUMN_FAVORITE:
			
			return union.isFavorite();
			
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
		if(message.getType() == ObserverMessage.LIST_UNION_TYPE)
		{
			if(this.unions == null)
			{
				this.unions = (SortableList<Tuple2<String, String>, UnionCls>) message.getValue();
				this.unions.registerObserver();
				//this.unions.sort(PollMap.NAME_INDEX);
			}
			
			this.fireTableDataChanged();
		}
		
		//CHECK IF LIST UPDATED
		if(message.getType() == ObserverMessage.ADD_UNION_TYPE || message.getType() == ObserverMessage.REMOVE_UNION_TYPE)
		{
			this.fireTableDataChanged();
		}	
	}
}
