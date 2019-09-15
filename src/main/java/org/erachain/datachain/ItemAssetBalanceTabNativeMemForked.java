package org.erachain.datachain;

import org.erachain.dbs.mapDB.ItemAssetBalanceSuitMapDB;
import org.mapdb.Fun;

import java.util.Collection;

// TODO SOFT HARD TRUE

public class ItemAssetBalanceTabNativeMemForked extends ItemAssetBalanceTabImpl {

    public ItemAssetBalanceTabNativeMemForked(ItemAssetBalanceTab parent, DCSet databaseSet) {
        super(parent, databaseSet);
    }

    @Override
    protected void createIndexes() {
    }

    @SuppressWarnings({"unchecked"})
    @Override
    protected void getMap() {
        map = new org.erachain.dbs.nativeMemMap.nativeMapTreeMap(parent, databaseSet,
                ItemAssetBalanceTab.DEFAULT_VALUE);
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
