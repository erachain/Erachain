package core.item.notes;


import core.account.PublicKeyAccount;
import core.item.ItemCls;
//import database.DBMap;
import database.DBSet;
import database.Item_Map;
//import database.wallet.FavoriteItemNote;
import database.Issue_ItemMap;

public abstract class NoteCls extends ItemCls {
	
	// PERS KEY
	public static final long EMPTY_KEY = 1l;
	public static final long LICENSE_KEY = 2l;
	public static final long MARRIAGE_KEY = 3l;
	public static final long UNMARRIAGE_KEY = 4l;
	public static final long HIRING_KEY = 5l;
	public static final long UNHIRING_KEY = 6l;

	protected static final int NOTE = 1;
	protected static final int SAMPLE = 2;
	protected static final int PAPER = 3;

	public static final int INITIAL_FAVORITES = 0;

	public NoteCls(byte[] typeBytes, PublicKeyAccount owner, String name, byte[] icon, byte[] image, String description)
	{
		super(typeBytes, owner, name, icon, image, description);
		
	}
	public NoteCls(int type, PublicKeyAccount owner, String name, byte[] icon, byte[] image, String description)
	{
		this(new byte[TYPE_LENGTH], owner, name, icon, image, description);
		this.typeBytes[0] = (byte)type;
	}

	//GETTERS/SETTERS
		
	public int getItemTypeInt() { return ItemCls.NOTE_TYPE; }
	public String getItemTypeStr() { return "note"; }
	
	// DB
	public Item_Map getDBMap(DBSet db)
	{
		return db.getItemNoteMap();
	}
	public Issue_ItemMap getDBIssueMap(DBSet db)
	{
		return db.getIssueNoteMap();
	}	
	
}
