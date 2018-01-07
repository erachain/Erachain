package gui.items.templates;

import java.util.ArrayList;
import javax.validation.constraints.Null;
import core.item.ItemCls;
import core.item.templates.TemplateCls;
import datachain.DCSet;
import datachain.ItemTemplateMap;
import datachain.SortableList;
import gui.items.TableModelItems;
import gui.models.TableModelCls;
import lang.Lang;

@SuppressWarnings("serial")
public class TableModelNotes extends TableModelItems
{
	public static final int COLUMN_KEY = 0;
	public static final int COLUMN_NAME = 1;
	public static final int COLUMN_ADDRESS = 2;
	public static final int COLUMN_FAVORITE = 3;
	private String[] columnNames = Lang.getInstance().translate(new String[]{"Key", "Name", "Creator", "Favorite"});
	private Boolean[] column_AutuHeight = new Boolean[]{false,true,true,false};
	private Long key_filter;
	private ArrayList<ItemCls> list;
	private String filter_Name;
	private ItemTemplateMap db;
	
	public TableModelNotes()
	{
		super.COLUMN_FAVORITE = COLUMN_FAVORITE;
		db= DCSet.getInstance().getItemTemplateMap();
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
		@Override
		public ItemCls getItem(int row)
		{
			return this.list.get(row);
		}	
	
	
	public TemplateCls getNote(int row)
	{
		return (TemplateCls) list.get(row);
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
		if(this.list == null || row > this.list.size() - 1 )
		{
			return null;
		}
		
		TemplateCls note = (TemplateCls) list.get(row);
		
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

	
	
	public void Find_item_from_key(String text) {
		// TODO Auto-generated method stub
		if (text.equals("") || text == null) return;
		if (!text.matches("[0-9]*"))return;
			key_filter = new Long(text);
			list =new ArrayList<ItemCls>();
			// Controller.getInstance().getNote(key_filter);
			 TemplateCls note = (TemplateCls) db.get(key_filter);
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
