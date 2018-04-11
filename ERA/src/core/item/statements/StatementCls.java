package core.item.statements;

import core.account.PublicKeyAccount;
import core.item.ItemCls;
import datachain.DCSet;
import datachain.Issue_ItemMap;
import datachain.Item_Map;

public abstract class StatementCls extends ItemCls {

	public static final int NOTE = 1;
	
	public static final int INITIAL_FAVORITES = 0;
	
	public StatementCls(byte[] typeBytes, PublicKeyAccount owner, String name, byte[] icon, byte[] image, String description)
	{
		super(typeBytes, owner, name, icon, image, description);
	}
	public StatementCls(int type, PublicKeyAccount owner, String name, byte[] icon, byte[] image, String description)
	{
		this(new byte[TYPE_LENGTH], owner, name, icon, image, description);
		this.typeBytes[0] = (byte)type;
		
	}

	//GETTERS/SETTERS
	public int getItemTypeInt() { return ItemCls.STATEMENT_TYPE; }
	public String getItemTypeStr() { return "statement"; }

	// DB
	public Item_Map getDBMap(DCSet db)
	{
		return db.getItemStatementMap();
	}
	public Issue_ItemMap getDBIssueMap(DCSet db)
	{
		return db.getIssueStatementMap();
	}

}
