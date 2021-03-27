package org.erachain.core.item.statements;

import org.erachain.core.BlockChain;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.item.ItemCls;
import org.erachain.core.transaction.Transaction;
import org.erachain.datachain.DCSet;
import org.erachain.datachain.IssueItemMap;
import org.erachain.datachain.ItemMap;

public abstract class StatementCls extends ItemCls {

    public static final int TYPE_KEY = ItemCls.STATEMENT_TYPE;

    public static final int NOTE = 1;

    public static final int INITIAL_FAVORITES = 0;

    public StatementCls(byte[] typeBytes, byte[] appData, PublicKeyAccount maker, String name, byte[] icon, byte[] image, String description) {
        super(typeBytes, appData, maker, name, icon, image, description);
    }

    public StatementCls(int type, byte[] appData, PublicKeyAccount maker, String name, byte[] icon, byte[] image, String description) {
        this(new byte[TYPE_LENGTH], appData, maker, name, icon, image, description);
        typeBytes[0] = (byte) type;

    }

    //GETTERS/SETTERS

    @Override
    public int getItemType() {
        return TYPE_KEY;
    }

    @Override
    public long START_KEY() {
        if (Transaction.parseHeightDBRef(dbRef) > BlockChain.START_KEY_UP)
            return BlockChain.START_KEY_UP_ITEMS;

        return START_KEY_OLD;
    }

    @Override
    public long MIN_START_KEY() {
        if (Transaction.parseHeightDBRef(dbRef) > BlockChain.START_KEY_UP)
            return BlockChain.START_KEY_UP_ITEMS;

        return MIN_START_KEY_OLD;
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
