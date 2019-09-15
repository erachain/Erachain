package org.erachain.datachain;

import org.erachain.dbs.mapDB.ItemAssetBalanceSuitMapDBForked;
import org.erachain.dbs.mapDB.ItemAssetBalanceSuitMapDB;
import org.mapdb.Fun;

import java.util.Collection;

// TODO SOFT HARD TRUE

public class ItemAssetBalanceTabMapDBForked extends ItemAssetBalanceTabImpl {

    public ItemAssetBalanceTabMapDBForked(ItemAssetBalanceTab parent, DCSet databaseSet) {
        super(parent, databaseSet);

    }

    @Override
    protected void createIndexes() {
    }

    @SuppressWarnings({"unchecked"})
    @Override
    protected void getMap() {
        map = new ItemAssetBalanceSuitMapDBForked((ItemAssetBalanceTab)parent, databaseSet);
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
