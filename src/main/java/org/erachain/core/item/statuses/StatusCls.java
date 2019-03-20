package org.erachain.core.item.statuses;

import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.item.ItemCls;
import org.erachain.datachain.DCSet;
import org.erachain.datachain.IssueItemMap;
import org.erachain.datachain.ItemMap;

public abstract class StatusCls extends ItemCls {

    public static final Long RIGHTS_KEY = 1l;
    public static final Long MEMBER_KEY = 2l;
    public static final int STATUS = 1;
    public static final int TITLE = 2;
    public static final int POSITION = 3;

    public static final int INITIAL_FAVORITES = 10;

    public StatusCls(byte[] typeBytes, PublicKeyAccount owner, String name, byte[] icon, byte[] image, String description) {
        super(typeBytes, owner, name, icon, image, description);
    }

    public StatusCls(int type, PublicKeyAccount owner, String name, byte[] icon, byte[] image, String description, boolean unique) {
        this(new byte[TYPE_LENGTH], owner, name, icon, image, description);
        typeBytes[0] = (byte) type;
        typeBytes[1] = unique ? (byte) 1 : (byte) 0;

    }

    //GETTERS/SETTERS
    public int getItemTypeInt() {
        return ItemCls.STATUS_TYPE;
    }

    public String getItemTypeStr() {
        return "status";
    }

    public boolean isUnique() {
        return typeBytes[1] == (byte) 1;
    }

    // DB
    public ItemMap getDBMap(DCSet db) {
        return db.getItemStatusMap();
    }

    public IssueItemMap getDBIssueMap(DCSet db) {
        return db.getIssueStatusMap();
    }

}
