package org.erachain.core.item.statuses;

import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.item.ItemCls;
import org.erachain.datachain.DCSet;
import org.erachain.datachain.Issue_ItemMap;
import org.erachain.datachain.Item_Map;

public abstract class StatusCls extends ItemCls {

    // COMMON STATUSES KEYs
    //public static final Long ALIVE_KEY = 1l; // 1- alive, 2 - dead or as set end_date for ALIVE_KEY
    //public static final Long RANK_KEY = 2l;
    public static final Long RIGHTS_KEY = 1l;
    public static final Long MEMBER_KEY = 2l;
	/*
	public static final Long USER_KEY = 5l;
	public static final Long MAKER_KEY = 6l;
	public static final Long DELEGATE_KEY = 7l;
	public static final Long CERTIFIED_KEY = 8l;
	public static final Long MARRIED_KEY = 9l;
	*/

    public static final int STATUS = 1;
    public static final int TITLE = 2;
    public static final int POSITION = 3;

    public static final int INITIAL_FAVORITES = 10;

    public StatusCls(byte[] typeBytes, PublicKeyAccount owner, String name, byte[] icon, byte[] image, String description) {
        super(typeBytes, owner, name, icon, image, description);
    }

    public StatusCls(int type, PublicKeyAccount owner, String name, byte[] icon, byte[] image, String description, boolean unique) {
        this(new byte[TYPE_LENGTH], owner, name, icon, image, description);
        this.typeBytes[0] = (byte) type;
        this.typeBytes[1] = unique ? (byte) 1 : (byte) 0;

    }

    //GETTERS/SETTERS
    public int getItemTypeInt() {
        return ItemCls.STATUS_TYPE;
    }

    public String getItemTypeStr() {
        return "status";
    }

    public boolean isUnique() {
        return this.typeBytes[1] == (byte) 1;
    }

    // DB
    public Item_Map getDBMap(DCSet db) {
        return db.getItemStatusMap();
    }

    public Issue_ItemMap getDBIssueMap(DCSet db) {
        return db.getIssueStatusMap();
    }

}
