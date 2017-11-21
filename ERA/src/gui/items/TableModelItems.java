package gui.items;

import java.util.ArrayList;
import javax.validation.constraints.Null;
import core.item.ItemCls;
import datachain.Item_Map;
import datachain.SortableList;
import gui.models.TableModelCls;

@SuppressWarnings("serial")
public class TableModelItems extends TableModelCls<Long, ItemCls>
{
	//public static final int COLUMN_KEY = 0;
	//public static final int COLUMN_NAME = 1;
	//public static final int COLUMN_ADDRESS = 2;
	//public static final int COLUMN_AMOUNT = 3;
	//public static final int COLUMN_DIVISIBLE = 4;

	protected ArrayList<ItemCls> list;
	protected Item_Map db;
	
	
	public TableModelItems()
	{
	
	}
	
	public Class<? extends Object> getColumnClass(int c) {     // set column type
		Object o = getValueAt(0, c);
		return o==null?Null.class:o.getClass();
     }
	
	public void Find_item_from_key(String text) {
		// TODO Auto-generated method stub
		if (text.equals("") || text == null) return;
		if (!text.matches("[0-9]*"))return;
			Long key_filter = new Long(text);
			list = null;
			list =new ArrayList<ItemCls>();
			ItemCls itemCls = (ItemCls) db.get(key_filter);
			if ( itemCls == null) return;
			list.add(itemCls);
			this.fireTableDataChanged();
	}
	
	public void clear(){
		list = null;
		list =new ArrayList<ItemCls>();
		this.fireTableDataChanged();
		
	}
	
	public void set_Filter_By_Name(String str) {
		list = null;
		list =  (ArrayList<ItemCls>) db.get_By_Name(str, false);
		this.fireTableDataChanged();
	}
	
	public ItemCls getItem(int row)
	{
		return this.list.get(row);
	}
	
	@Override
	public int getRowCount() 
	{
		return (this.list == null)? 0 : this.list.size();
	}

	
	@Override
	public SortableList<Long, ItemCls> getSortableList() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getColumnCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		// TODO Auto-generated method stub
		return null;
	}

	
}
