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
	
	/*
	public long getKey(DBSet db) {
		// TODO if ophran ?
		if (this.key <0) this.key = db.getIssueImprintMap().get(this.reference);
		return this.key;
	}
		
	public boolean isConfirmed(DBSet db) {
		return db.getIssueImprintMap().contains(this.reference);
	}	
	
	public long insertToMap(DBSet db)
	{
		//INSERT INTO DATABASE
		ItemImprintMap dbMap = db.getImprintMap();
		int mapSize = dbMap.size();
		//LOGGER.info("GENESIS MAP SIZE: " + assetMap.size());
		long key = 0l;
		if (mapSize == 0) {
			// initial map set
			dbMap.set(0l, this);
		} else {
			key = dbMap.add(this);
			//this.asset.setKey(key);
		}
		
		//SET ORPHAN DATA
		db.getIssueImprintMap().set(this.reference, key);
		
		return key;
		
	}
	
	public long removeFromMap(DBSet db)
	{
		//DELETE FROM DATABASE
		long key = db.getIssueImprintMap().get(this.reference);
		db.getImprintMap().delete(key);	
				
		//DELETE ORPHAN DATA
		db.getIssueImprintMap().delete(this.reference);
		
		return key;

	}
	
	*/
}
