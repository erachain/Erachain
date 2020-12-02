package org.erachain.core.item.statements;

import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.item.ItemCls;
import org.erachain.datachain.DCSet;
import org.erachain.datachain.IssueItemMap;
import org.erachain.datachain.ItemMap;

public abstract class StatementCls extends ItemCls {

    public static final int TYPE_KEY = ItemCls.STATEMENT_TYPE;

    public static final long MIN_START_KEY = 1000L;

    public static final int NOTE = 1;

    public static final int INITIAL_FAVORITES = 0;

    public StatementCls(byte[] typeBytes, PublicKeyAccount owner, String name, byte[] icon, byte[] image, String description) {
        super(typeBytes, owner, name, icon, image, description);
    }

    public StatementCls(int type, PublicKeyAccount owner, String name, byte[] icon, byte[] image, String description) {
        this(new byte[TYPE_LENGTH], owner, name, icon, image, description);
        typeBytes[0] = (byte) type;

    }

    //GETTERS/SETTERS

    @Override
    public int getItemType() {
        return TYPE_KEY;
    }

    @Override
    public long MIN_START_KEY() {
        return MIN_START_KEY;
    }

    public String getItemTypeName() {
        return "statement";
    }

    // DB
    public ItemMap getDBMap(DCSet db) {
        return db.getItemStatementMap();
    }

    public IssueItemMap getDBIssueMap(DCSet db) {
        return db.getIssueStatementMap();
    }

}
