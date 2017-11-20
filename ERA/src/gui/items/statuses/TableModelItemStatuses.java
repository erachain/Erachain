package gui.items.statuses;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import javax.validation.constraints.Null;

import controller.Controller;
import core.item.ItemCls;
import core.item.notes.NoteCls;
import core.item.statuses.StatusCls;
import datachain.DCSet;
import datachain.ItemStatusMap;
import datachain.SortableList;
import utils.NumberAsString;
import utils.ObserverMessage;
import gui.models.TableModelCls;
import lang.Lang;

@SuppressWarnings("serial")
public class TableModelItemStatuses extends TableModelCls<Long, StatusCls> implements Observer
{
	public static final int COLUMN_KEY = 0;
	public static final int COLUMN_NAME = 1;
	public static final int COLUMN_ADDRESS = 2;
	public static final int COLUMN_UNIQUE = 3;
	public static final int COLUMN_FAVORITE = 4;

	//private SortableList<Long, StatusCls> statuses;
	
	private String[] columnNames = Lang.getInstance().translate(new String[]{"Key", "Name", "Creator", "Unique", "Favorite"});
	private Boolean[] column_AutuHeight = new Boolean[]{false,true,true,false};
	private ItemStatusMap db;
	private ArrayList<ItemCls> list;
	private Long key_filter;
	private String filter_Name;
	
	public TableModelItemStatuses()
	{
	//	Controller.getInstance().addObserver(this);
		db= DCSet.getInstance().getItemStatusMap();
	}
	
	@Override
	public SortableList<Long, StatusCls> getSortableList() 
	{
		return null;
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
	public StatusCls getStatus(int row)
	{
		return (StatusCls) db.get((long) row);
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
		
		return (list == null)? 0 : list.size();
		
	}

	@Override
	public Object getValueAt(int row, int column) 
	{
		if(list == null || row > list.size() - 1 )
		{
			return null;
		}
		
		StatusCls status = (StatusCls) list.get(row);
		
		switch(column)
		{
		case COLUMN_KEY:
			
			return status.getKey();
		
		case COLUMN_NAME:
			
			return status.getName();
		
		case COLUMN_ADDRESS:
			
			return status.getOwner().getPersonAsString();
			
		case COLUMN_FAVORITE:
			
			return status.isFavorite();
			
		case COLUMN_UNIQUE:
			
			return status.isUnique();

		}
		
		return null;
	}

	@Override
	public void update(Observable o, Object arg) 
	{	
		
	}
	

	
	public void removeObservers() 
	{
		//if(this.statuses != null)this.statuses.removeObserver();
		//Controller.getInstance().deleteObserver(this);
	}
	public void Find_item_from_key(String text) {
		// TODO Auto-generated method stub
		if (text.equals("") || text == null) return;
		if (!text.matches("[0-9]*"))return;
			key_filter = new Long(text);
			list =new ArrayList<ItemCls>();
			// Controller.getInstance().getNote(key_filter);
			StatusCls note = (StatusCls) db.get(key_filter);
			if ( note == null) return;
			list.add(note);
						
			this.fireTableDataChanged();
		
		
	}
	public void clear(){
		list =new ArrayList<ItemCls>();
		this.fireTableDataChanged();
		
	}
	public void set_Filter_By_Name(String str) {
		filter_Name = str;
		list = (ArrayList<ItemCls>) db.get_By_Name(filter_Name, false);
		this.fireTableDataChanged();

	}
}
