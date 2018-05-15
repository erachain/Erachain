package gui.items.assets;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.Null;

import controller.Controller;
import core.item.ItemCls;
import core.item.assets.AssetCls;
import datachain.DCSet;
import datachain.ItemAssetMap;
import gui.items.TableModelItems;
import lang.Lang;

@SuppressWarnings("serial")
public class TableModelItemAssets extends TableModelItems
{
	public static final int COLUMN_KEY = 0;
	public static final int COLUMN_NAME = 1;
	public static final int COLUMN_ADDRESS = 2;
	public static final int COLUMN_ASSET_TYPE = 3;
	public static final int COLUMN_AMOUNT = 4;
	public static final int COLUMN_FAVORITE = 5;
	public static final int COLUMN_I_OWNER = 6;

	//private SortableList<Long, AssetCls> assets;

	private String[] columnNames = Lang.getInstance().translate(new String[]{"Key", "Name", "Owner", "Type", "Quantity", "Favorite", "I Owner"});
	private Boolean[] column_AutuHeight = new Boolean[]{false,true,true,false,false,false,false,false};
	private List<ItemCls> list;
	private String filter_Name = "";
	private long key_filter =0;
	private ItemAssetMap db;

	public TableModelItemAssets()
	{
		//Controller.getInstance().addObserver(this);
		super.COLUMN_FAVORITE = COLUMN_FAVORITE;
		db = DCSet.getInstance().getItemAssetMap();
	}
	@Override
	public void set_Filter_By_Name(String str) {
		filter_Name = str;
		list = db.get_By_Name(filter_Name, false);
		this.fireTableDataChanged();

	}
	@Override
	public void clear(){
		list =new ArrayList<ItemCls>();
		this.fireTableDataChanged();

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
	public Class<? extends Object> getColumnClass(int c) {     // set column type
		Object o = getValueAt(0, c);
		return o==null?Null.class:o.getClass();
	}

	public AssetCls getAsset(int row)
	{
		return (AssetCls) this.list.get(row);
	}

	@Override
	public ItemCls getItem(int row)
	{
		return this.list.get(row);
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
		if (this.list == null)
			return 0;
		;
		return this.list.size();

	}

	@Override
	public Object getValueAt(int row, int column)
	{
		if(this.list == null || row > this.list.size() - 1 )
		{
			return null;
		}

		AssetCls asset = (AssetCls) this.list.get(row);

		switch(column)
		{
		case COLUMN_KEY:

			return asset.getKey();

		case COLUMN_NAME:

			return asset.viewName();

		case COLUMN_ASSET_TYPE:

			return Lang.getInstance().translate(asset.viewAssetType());

		case COLUMN_ADDRESS:

			return asset.getOwner().getPersonAsString();

		case COLUMN_AMOUNT:

			return asset.getTotalQuantity(DCSet.getInstance());

		case COLUMN_FAVORITE:

			return asset.isFavorite();

		case COLUMN_I_OWNER:

			if (Controller.getInstance().isAddressIsMine(asset.getOwner().getAddress()))
				return true;
			return false;
		}

		return null;
	}






	@Override
	public void Find_item_from_key(String text) {
		// TODO Auto-generated method stub
		if (text.equals("") || text == null) return;
		if (!text.matches("[0-9]*"))return;
		key_filter = new Long(text);
		list =new ArrayList<ItemCls>();
		AssetCls pers = Controller.getInstance().getAsset(key_filter);
		if ( pers == null) return;
		list.add(pers);
		this.fireTableDataChanged();


	}
}
