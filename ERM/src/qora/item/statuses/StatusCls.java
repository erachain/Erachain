package qora.item.statuses;

//import java.math.BigDecimal;
// import org.apache.log4j.Logger;

import database.DBSet;
import database.IssueItemMap;
import database.Item_Map;
import database.ItemStatusMap;
import qora.account.Account;
import qora.item.ItemCls;

public abstract class StatusCls extends ItemCls {

	public static final int STATUS = 1;
	public static final int TITLE = 2;
	
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
	public String getItemType() { return "status"; }

	// DB
	public Item_Map getDBMap(DBSet db)
	{
		return db.getStatusMap();
	}
	public IssueItemMap getDBIssueMap(DBSet db)
	{
		return db.getIssueStatusMap();
	}

}
