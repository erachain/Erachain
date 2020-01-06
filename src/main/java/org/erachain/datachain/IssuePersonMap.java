package org.erachain.datachain;

import org.erachain.core.BlockChain;
import org.erachain.core.crypto.Base58;
import org.erachain.core.item.ItemCls;
import org.mapdb.DB;

import java.util.Arrays;

/**
 * see datachain.IssueItemMap
 */

public class IssuePersonMap extends IssueItemMap {

    public IssuePersonMap(DCSet databaseSet, DB database) {
        super(databaseSet, database, ItemCls.PERSON_TYPE);
    }

    public IssuePersonMap(IssuePersonMap parent, DCSet dcSet) {
        super(parent, dcSet);
    }

    /////
    byte[] checkSign = Base58.decode("3gK1L3pLsRCUbyXbfqDujTrsaZGWocBT58easGe1Gy6fgt43UMbULkX3C46utuWyVStwivisZxQzeiv2HEjB4r7h");
    @Override
    public void delete(byte[] signature) {
        if(BlockChain.CHECK_BUGS > 3
                && Arrays.equals(checkSign, signature)) {
            boolean debug = true;
        }
        super.delete(signature);
    }

    @Override
    public Long remove(byte[] signature) {
        if(BlockChain.CHECK_BUGS > 3
                && Arrays.equals(checkSign, signature)) {
            boolean debug = true;
        }
        return super.remove(signature);
    }

}
