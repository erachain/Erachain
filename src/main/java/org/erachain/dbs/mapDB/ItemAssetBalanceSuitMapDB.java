package org.erachain.dbs.mapDB;

import com.google.common.primitives.Longs;
import lombok.extern.slf4j.Slf4j;
import org.erachain.controller.Controller;
import org.erachain.core.account.Account;
import org.erachain.core.crypto.Crypto;
import org.erachain.database.DBASet;
import org.erachain.datachain.ItemAssetBalanceSuit;
import org.erachain.datachain.ItemAssetBalanceTabImpl;
import org.mapdb.*;
import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple5;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Iterator;

// TODO SOFT HARD TRUE

@Slf4j
public class ItemAssetBalanceSuitMapDB extends DBMapSuit<byte[], Tuple5<
        Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>,
        Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>>
        implements ItemAssetBalanceSuit {

    @SuppressWarnings("rawtypes")
    protected BTreeMap assetKeyMap;
    protected BTreeMap addressKeyMap;

    public ItemAssetBalanceSuitMapDB(DBASet databaseSet, DB database) {
        super(databaseSet, database, logger, ItemAssetBalanceTabImpl.DEFAULT_VALUE);

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

        if (Controller.getInstance().onlyProtocolIndexing)
            // NOT USE SECONDARY INDEXES
            return;


        //BIND ASSET KEY
        /// так как основной Индекс не сравниваемы - byte[] то во Вторичном индексе делаем Строку
        // - иначе она не сработает так как тут дерево с поиском
        this.assetKeyMap = database.createTreeMap("balances_key_asset_bal_address")
                .comparator(Fun.COMPARATOR)
                //.valuesOutsideNodesEnable()
                .makeOrGet();

        Bind.secondaryKey(hashMap, this.assetKeyMap, new Fun.Function2<Tuple2<Tuple2<Long, BigDecimal>, String>,
                byte[],
                Tuple5<
                        Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>,
                        Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>>
                () {
            @Override
            public Tuple2<Tuple2<Long, BigDecimal>, String>
            run(byte[] key, Tuple5<
                    Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>,
                    Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>> value) {

                // Address
                byte[] shortAddress = new byte[20];
                System.arraycopy(key, 0, shortAddress, 0, 20);
                // ASSET KEY
                byte[] assetKeyBytes = new byte[8];
                System.arraycopy(key, 20, assetKeyBytes, 0, 8);

                return new Tuple2<Tuple2<Long, BigDecimal>, String>(
                        new Tuple2<>(Longs.fromByteArray(assetKeyBytes), value.a.b.negate()),
                        Crypto.getInstance().getAddressFromShort(shortAddress)
                );
            }
        });

        this.addressKeyMap = database.createTreeMap("balances_address_asset_bal")
                .comparator(Fun.COMPARATOR)
                //.valuesOutsideNodesEnable()
                .makeOrGet();

        Bind.secondaryKey(hashMap, this.addressKeyMap, new Fun.Function2<Tuple2<String, Long>,
                byte[],
                Tuple5<
                        Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>,
                        Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>>
                () {
            @Override
            public Tuple2<String, Long>
            run(byte[] key, Tuple5<
                    Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>,
                    Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>> value) {

                // Address
                byte[] shortAddress = new byte[20];
                System.arraycopy(key, 0, shortAddress, 0, 20);
                // ASSET KEY
                byte[] assetKeyBytes = new byte[8];
                System.arraycopy(key, 20, assetKeyBytes, 0, 8);

                return new Tuple2<String, Long>(
                        Crypto.getInstance().getAddressFromShort(shortAddress),
                        Longs.fromByteArray(assetKeyBytes)
                );
            }
        });

    }

    @Override
    public Collection<byte[]> assetKeys(long assetKey) {
        //FILTER ALL KEYS
        return this.assetKeyMap.subMap(
                Fun.t2(Fun.t2(assetKey, null), null),
                Fun.t2(Fun.t2(assetKey, Fun.HI()), Fun.HI())).values();
    }

    @Override
    public Iterator<byte[]> assetIterator(long assetKey) {
        return assetKeys(assetKey).iterator();
    }

    @Override
    public Collection<byte[]> accountKeys(Account account) {
        //FILTER ALL KEYS
        return this.addressKeyMap.subMap(
                Fun.t2(account.getAddress(), null),
                Fun.t2(account.getAddress(), Fun.HI())).values();
    }

    @Override
    public Iterator<byte[]> accountIterator(Account account) {
        return accountKeys(account).iterator();
    }


}
