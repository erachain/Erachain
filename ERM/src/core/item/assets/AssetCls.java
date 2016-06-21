package core.item.assets;


import core.account.Account;
import core.item.ItemCls;

// import org.apache.log4j.Logger;

import database.DBSet;
import database.Item_Map;
import database.Issue_ItemMap;
import database.wallet.WalletDatabase;
import database.wallet.FavoriteItem;



public abstract class AssetCls extends ItemCls {

	// CORE KEY
	public static final long ERMO_KEY = 1l;
	public static final String ERMO_ABBREV = "ERM"; // ERMO (main rights units)
	public static final String ERMO_NAME = "ERMO";
	public static final String ERMO_DESCR = "It is the basic unit of Environment Real Management Objects (" + ERMO_NAME + ")";
	// FEE KEY
	public static final long FEE_KEY = 2l;
	public static final String FEE_ABBREV = "CMP"; // COMPU (compute units)
	public static final String FEE_NAME = "COMPU";
	public static final String FEE_DESCR = "It is an drops of computation used for deals (" + FEE_NAME + ")";
	// TRUST KEY
	public static final long TRUST_KEY = 3l;
	public static final String TRUST_ABBREV = "TRU"; // COMPU (compute units)
	public static final String TRUST_NAME = "TRUST";
	public static final String TRUST_DESCR = "It is an drops of the trust (" + TRUST_NAME + ")";
	// REAL KEY
	public static final long REAL_KEY = 4l;
	public static final String REAL_ABBREV = "RIL"; // COMPU (compute units)
	public static final String REAL_NAME = "REAL";
	public static final String REAL_DESCR = "It is an drops of the real (" + REAL_NAME + ")";
	// DEaL KEY
	public static final long DEAL_KEY = 5l;
	public static final String DEAL_ABBREV = "DIL"; // COMPU (compute units)
	public static final String DEAL_NAME = "DEAL";
	public static final String DEAL_DESCR = "It is an drops of the deal (" + DEAL_NAME + ")";
	
	public static final int UNIQUE = 1;
	public static final int VENTURE = 2;
	public static final int NAME = 3;
	
	public static final int INITIAL_FAVORITES = 4;
		
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

	public int getItemTypeInt() { return ItemCls.ASSET_TYPE; }
	public String getItemTypeStr() { return "asset"; }
	
	// DB
	public Item_Map getDBMap(DBSet db)
	{
		return db.getItemAssetMap();
	}
	public Issue_ItemMap getDBIssueMap(DBSet db)
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
