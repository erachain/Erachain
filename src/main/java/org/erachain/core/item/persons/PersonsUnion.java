package org.erachain.core.item.persons;

import org.erachain.core.account.PublicKeyAccount;

public class PersonsUnion extends PersonCls {

    private static final int TYPE_ID = UNION;

    private byte unionType;

    public PersonsUnion(byte[] data, boolean includeReference, int forDeal) throws Exception {
        super(data, includeReference, forDeal);

        //READ TYPE
        unionType = data[parsedPos];
        parsedPos++;

    }

    public PersonsUnion(byte[] appData, PublicKeyAccount maker, String fullName, byte[] icon, byte[] image,
                        String description,
                        byte unionType) {
        super(new byte[]{(byte) TYPE_ID, (byte) 0}, appData, maker, fullName,
                icon, image, description);
        this.unionType = unionType;
    }

    //GETTERS/SETTERS

    public String getItemSubType() {
        return "union";
    }

    public int getMinNameLen() {
        return 3;
    }

}
