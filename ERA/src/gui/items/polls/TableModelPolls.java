package gui.items.polls;

import core.item.polls.PollCls;
import core.item.unions.UnionCls;
import datachain.DCSet;
import gui.items.TableModelItems;
import lang.Lang;

@SuppressWarnings("serial")
public class TableModelPolls extends TableModelItems 
{
	public static final int COLUMN_KEY = 0;
	public static final int COLUMN_NAME = 1;
	public static final int COLUMN_ADDRESS = 2;
	public static final int COLUMN_FAVORITE = 3;

	private String[] columnNames = Lang.getInstance().translate(new String[]{"Key", "Name", "Creator", "Favorite"});
	
	public TableModelPolls()
	{
		super.COLUMN_FAVORITE = COLUMN_FAVORITE;
		db = DCSet.getInstance().getItemPollMap();
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
	public Object getValueAt(int row, int column) 
	{
		if(this.list == null || row > this.list.size() - 1 )
		{
			return null;
		}
		
		PollCls poll = (PollCls) this.list.get(row);
		
		switch(column)
		{
		case COLUMN_KEY:
			
			return poll.getKey();
		
		case COLUMN_NAME:
			
			return poll.viewName();
		
		case COLUMN_ADDRESS:
			
			return poll.getOwner().getPersonAsString();
			
		case COLUMN_FAVORITE:
			
			return poll.isFavorite();

		}
		
		return null;
	}
	
}
