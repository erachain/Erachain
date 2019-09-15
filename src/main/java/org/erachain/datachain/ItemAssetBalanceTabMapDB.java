package org.erachain.datachain;

import org.erachain.dbs.mapDB.ItemAssetBalanceSuitMapDB;
import org.mapdb.DB;
import org.mapdb.Fun;

import java.util.Collection;

// TODO SOFT HARD TRUE

public class ItemAssetBalanceTabMapDB extends ItemAssetBalanceTabImpl {

    public ItemAssetBalanceTabMapDB(DCSet databaseSet, DB database) {
        super(databaseSet, database);

    }

    @Override
    protected void createIndexes() {
    }

    @SuppressWarnings({"unchecked"})
    @Override
    protected void getMap() {
        map = new ItemAssetBalanceSuitMapDB(databaseSet, database);
    }

    public Collection<byte[]> assetKeySubMap(long key) {
        return ((ItemAssetBalanceSuitMapDB)map).assetKeyMap.subMap(
                Fun.t2(key, null),
                Fun.t2(key, Fun.HI())).values();
    }

    public Collection<byte[]> addressKeySubMap(String address) {
        return ((ItemAssetBalanceSuitMapDB)map).addressKeyMap.subMap(
                Fun.t2(address, null),
                Fun.t2(address, Fun.HI())).values();
    }

}
