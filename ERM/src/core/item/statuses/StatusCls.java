package core.item.statuses;

import core.account.Account;
import core.item.ItemCls;

//import java.math.BigDecimal;
// import org.apache.log4j.Logger;

import database.DBSet;
import database.Issue_ItemMap;
import database.Item_Map;
//import database.ItemStatusMap;

public abstract class StatusCls extends ItemCls {

	// PERSON KEY
	public static final Long AA_KEY = 1l;	
	public static final Long BB_KEY = 2l;	
	public static final Long CC_KEY = 3l;	
	public static final Long DD_KEY = 4l;	
	public static final Long EE_KEY = 5l;	
	public static final Long FF_KEY = 6l;	
	public static final Long GG_KEY = 7l;	
	public static final Long HH_KEY = 8l;	
	
	public static final Long ALIVE_KEY = 9l;
	public static final Long DEAD_KEY = 10l;

	public static final Long CITIZEN_KEY = 11l;
	public static final Long MEMBER_KEY = 12l;
	public static final Long SPOUSE_KEY = 13l;

	public static final Long GENERAL_KEY = 14l;
	public static final Long MAJOR_KEY = 15l;
	public static final Long MINOR_KEY = 16l;
	public static final Long ADMIN_KEY = 17l;
	public static final Long MANAGER_KEY = 18l;
	public static final Long WORKER_KEY = 19l;
	public static final Long CREATOR_KEY = 20l;
	public static final Long PRESIDENT_KEY = 21l;
	public static final Long DIRECTOR_KEY = 22l;
	public static final Long SENATOR_KEY = 23l;
	public static final Long DEPUTATE_KEY = 24l;
	public static final Long OBSERVER_KEY = 25l;

	public static final Long CERTIFIED_KEY = 26l;
	public static final Long CONFIRMED_KEY = 27l;
	public static final Long EXPIRED_KEY = 28l;

	public static final int STATUS = 1;
	public static final int TITLE = 2;
	public static final int POSITION = 3;
	
	public static final int INITIAL_FAVORITES = 2;

	public StatusCls(byte[] typeBytes, Account creator, String name, String description)
	{
		super(typeBytes, creator, name, description);
	}
	public StatusCls(int type, Account creator, String name, String description)
	{
		this(new byte[TYPE_LENGTH], creator, name, description);
		this.typeBytes[0] = (byte)type;
	}

	//GETTERS/SETTERS
	public int getItemTypeInt() { return ItemCls.STATUS_TYPE; }
	public String getItemTypeStr() { return "status"; }

	// DB
	public Item_Map getDBMap(DBSet db)
	{
		return db.getItemStatusMap();
	}
	public Issue_ItemMap getDBIssueMap(DBSet db)
	{
		return db.getIssueStatusMap();
	}

}
