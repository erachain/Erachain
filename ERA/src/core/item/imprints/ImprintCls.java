package core.item.imprints;


import core.account.PublicKeyAccount;
import core.item.ItemCls;
import datachain.DCSet;
import datachain.Issue_ItemMap;
import datachain.Item_Map;

public abstract class ImprintCls extends ItemCls {
		
	protected static final int IMPRINT = 1;

	public ImprintCls(byte[] typeBytes, PublicKeyAccount owner, String name, byte[] icon, byte[] image, String description)
	{
		super(typeBytes, owner, name, icon, image, description);
		
	}
	public ImprintCls(int type, PublicKeyAccount owner, String name, byte[] icon, byte[] image, String description)
	{
		this(new byte[TYPE_LENGTH], owner, name, icon, image, description);
		this.typeBytes[0] = (byte)type;
	}

	//GETTERS/SETTERS
		
	public int getItemTypeInt() { return ItemCls.IMPRINT_TYPE; }
	public String getItemTypeStr() { return "imprint"; }
	
	// DB
	public Item_Map getDBMap(DCSet db)
	{
		return db.getItemImprintMap();
	}
	public Issue_ItemMap getDBIssueMap(DCSet db)
	{
		return db.getIssueImprintMap();
	}	
	
}
