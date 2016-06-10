package core.item.imprints;


import core.account.Account;
import core.item.ItemCls;
//import database.DBMap;
import database.DBSet;
import database.Item_Map;
//import database.wallet.FavoriteItemImprint;
import database.Issue_ItemMap;

public abstract class ImprintCls extends ItemCls {
		
	protected static final int IMPRINT = 1;

	public ImprintCls(byte[] typeBytes, Account creator, String name, String description)
	{
		super(typeBytes, creator, name, description);
		
	}
	public ImprintCls(int type, Account creator, String name, String description)
	{
		this(new byte[TYPE_LENGTH], creator, name, description);
		this.typeBytes[0] = (byte)type;
	}

	//GETTERS/SETTERS
		
	public int getItemTypeInt() { return ItemCls.IMPRINT_TYPE; }
	public String getItemTypeStr() { return "imprint"; }
	
	// DB
	public Item_Map getDBMap(DBSet db)
	{
		return db.getItemImprintMap();
	}
	public Issue_ItemMap getDBIssueMap(DBSet db)
	{
		return db.getIssueImprintMap();
	}	
	
}
