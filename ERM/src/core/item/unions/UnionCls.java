package qora.item.unions;

//import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
 import org.apache.log4j.Logger;

import org.json.simple.JSONObject;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;

import controller.Controller;
import database.DBSet;
import database.IssueItemMap;
import database.Item_Map;
import database.ItemUnionMap;
import qora.account.Account;
import qora.crypto.Base58;
import qora.crypto.Crypto;
import qora.item.ItemCls;

public abstract class UnionCls extends ItemCls{

	public static final int UNION = 1;
	
	public UnionCls(byte[] typeBytes, Account creator, String name, String description)
	{
		super(typeBytes, creator, name, description);
	}
	public UnionCls(int type, Account creator, String name, String description)
	{
		this(new byte[TYPE_LENGTH], creator, name, description);
		this.typeBytes[0] = (byte)type;
	}

	//GETTERS/SETTERS
	public String getItemType() { return "union"; }
	
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
