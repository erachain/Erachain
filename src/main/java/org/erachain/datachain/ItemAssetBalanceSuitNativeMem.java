package org.erachain.datachain;

import org.mapdb.Fun;

import java.util.Collection;

// TODO SOFT HARD TRUE

public class ItemAssetBalanceSuitNativeMem extends ItemAssetBalanceMapImpl {

    public ItemAssetBalanceSuitNativeMem(ItemAssetBalanceMap parent, DCSet databaseSet) {
        super(parent, databaseSet);
    }

    @Override
    protected void createIndexes() {
    }

    @SuppressWarnings({"unchecked"})
    @Override
    protected void getMap() {
        map = new org.erachain.dbs.nativeMemMap.nativeMapTreeMap(parent, databaseSet);
    }

    public Collection<byte[]> assetKeySubMap(long key) {
        return ((org.erachain.dbs.mapDB.ItemAssetBalanceMapDBMap)map).assetKeyMap.subMap(
                Fun.t2(key, null),
                Fun.t2(key, Fun.HI())).values();
    }

    public Collection<byte[]> addressKeySubMap(String address) {
        return ((org.erachain.dbs.mapDB.ItemAssetBalanceMapDBMap)map).addressKeyMap.subMap(
                Fun.t2(address, null),
                Fun.t2(address, Fun.HI())).values();
    }

}
