package org.erachain.core.item.imprints;


import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.item.ItemCls;
import org.erachain.datachain.DCSet;
import org.erachain.datachain.Issue_ItemMap;
import org.erachain.datachain.Item_Map;

public abstract class ImprintCls extends ItemCls {

    protected static final int IMPRINT = 1;

    public ImprintCls(byte[] typeBytes, PublicKeyAccount owner, String name, byte[] icon, byte[] image, String description) {
        super(typeBytes, owner, name, icon, image, description);

    }

    public ImprintCls(int type, PublicKeyAccount owner, String name, byte[] icon, byte[] image, String description) {
        this(new byte[TYPE_LENGTH], owner, name, icon, image, description);
        this.typeBytes[0] = (byte) type;
    }

    //GETTERS/SETTERS

    public int getItemTypeInt() {
        return ItemCls.IMPRINT_TYPE;
    }

    public String getItemTypeStr() {
        return "imprint";
    }

    // DB
    public Item_Map getDBMap(DCSet db) {
        return db.getItemImprintMap();
    }

    public Issue_ItemMap getDBIssueMap(DCSet db) {
        return db.getIssueImprintMap();
    }

}
