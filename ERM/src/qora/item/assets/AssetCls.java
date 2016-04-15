package qora.item.assets;


// import org.apache.log4j.Logger;

import database.DBSet;
import database.Item_Map;
import database.IssueItemMap;
import qora.account.Account;

import qora.item.ItemCls;


public abstract class AssetCls extends ItemCls {

	public static final int UNIQUE = 1;
	public static final int VENTURE = 2;
	public static final int NAME = 3;
		
	public AssetCls(byte[] typeBytes, Account creator, String name, String description)
	{
		super(typeBytes, creator, name, description);
	}
	public AssetCls(int type, Account creator, String name, String description)
	{
		this(new byte[TYPE_LENGTH], creator, name, description);
		this.typeBytes[0] = (byte)type;
	}
	

	//GETTERS/SETTERS

	public String getItemType() { return "asset"; }
	
	// DB
	public Item_Map getDBMap(DBSet db)
	{
		return db.getAssetMap();
	}
	public IssueItemMap getDBIssueMap(DBSet db)
	{
		return db.getIssueAssetMap();
	}

	public Long getQuantity() {
		return 1l;
	}

	public boolean isDivisible() {
		return true;
	}
	public int getScale() {
		return 8;
	}

	public byte[] toBytes(boolean includeReference)
	{
		return super.toBytes(includeReference);
	}
	
}
