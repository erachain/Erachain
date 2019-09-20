package org.erachain.dbs.mapDB;

import org.erachain.core.account.Account;
import org.erachain.database.DBASet;
import org.erachain.datachain.ItemAssetBalanceSuit;
import org.erachain.datachain.ItemAssetBalanceTab;
import org.mapdb.*;
import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple5;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

// TODO SOFT HARD TRUE

public class ItemAssetBalanceSuitMapDBFork extends DBMapSuitFork<byte[], Tuple5<
        Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>,
        Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>>
            implements ItemAssetBalanceSuit {

    static Logger logger = LoggerFactory.getLogger(ItemAssetBalanceSuitMapDBFork.class.getSimpleName());

    public ItemAssetBalanceSuitMapDBFork(ItemAssetBalanceTab parent, DBASet databaseSet) {
        super(parent, databaseSet);
    }

    @SuppressWarnings({"unchecked"})
    @Override
    protected void getMap() {
        //OPEN MAP
        BTreeMap<byte[], Tuple5<
                Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>,
                Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>> treeMap;
        HTreeMap<byte[], Tuple5<
                Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>,
                Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>> hashMap;

        if (true) {
            hashMap = database.createHashMap("balances")
                    .keySerializer(SerializerBase.BYTE_ARRAY)
                    .hasher(Hasher.BYTE_ARRAY)
                    .counterEnable()
                    .makeOrGet();
            map = hashMap;
        } else {

            treeMap = database.createTreeMap("balances")
                    //.keySerializer(BTreeKeySerializer.TUPLE2)
                    .keySerializer(BTreeKeySerializer.BASIC)
                    //.keySerializer(new BTreeKeySerializer.Tuple2KeySerializer(
                    //        UnsignedBytes.lexicographicalComparator(), // Fun.BYTE_ARRAY_COMPARATOR,
                    //        Serializer.BYTE_ARRAY,
                    //        Serializer.LONG))
                    //.comparator(Fun.TUPLE2_COMPARATOR)
                    .comparator(Fun.BYTE_ARRAY_COMPARATOR)
                    //.comparator(UnsignedBytes.lexicographicalComparator())
                    .counterEnable()
                    .makeOrGet();
            map = treeMap;
        }

    }

    @Override
    public Tuple5<
            Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>,
            Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>> getDefaultValue() {
        return DEFAULT_VALUE;
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected void createIndexes() {
    }

    @Override
    // NOT used in FORK
    public Iterator<byte[]> assetIterator(long key) {
        return null;
    }

    @Override
    // NOT used in FORK
    public List<byte[]> assetKeys(long key) {
        return null;
    }

    @Override
    // NOT used in FORK
    public Iterator<byte[]> accountIterator(Account account) {
        return null;
    }
    @Override
    // NOT used in FORK
    public List<byte[]> accountKeys(Account account) {
        return null;
    }

}
