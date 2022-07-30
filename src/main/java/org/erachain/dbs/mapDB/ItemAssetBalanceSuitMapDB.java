package org.erachain.dbs.mapDB;

import com.google.common.primitives.Longs;
import lombok.extern.slf4j.Slf4j;
import org.erachain.controller.Controller;
import org.erachain.core.BlockChain;
import org.erachain.core.account.Account;
import org.erachain.database.DBASet;
import org.erachain.datachain.IndexIterator;
import org.erachain.datachain.ItemAssetBalanceSuit;
import org.erachain.dbs.DBTab;
import org.erachain.dbs.IteratorCloseable;
import org.erachain.dbs.IteratorCloseableImpl;
import org.mapdb.*;
import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple3;
import org.mapdb.Fun.Tuple5;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.NavigableMap;
import java.util.NavigableSet;

// TODO SOFT HARD TRUE

/**
 * key - Bytes.concat(address, Longs.toByteArray(assetKey))
 */
@Slf4j
public class ItemAssetBalanceSuitMapDB extends DBMapSuit<byte[], Tuple5<
        Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>,
        Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>>
        implements ItemAssetBalanceSuit {

    static final int ADDR_KEY2_LEN = 10;
    static final byte[] ADDR_KEY2_MIN = new byte[ADDR_KEY2_LEN];
    static final byte[] ADDR_KEY2_MAX = new byte[ADDR_KEY2_LEN];


    @Deprecated
    protected NavigableSet<Tuple2<Tuple2<Long, BigDecimal>, byte[]>> assetKeySet_deprecated;
    // должно шустрее работать так как не весь первичный ключ берем а только Адрес из него сжатый до 8 байт
    protected NavigableMap<Tuple3<Long, BigDecimal, Long>, byte[]> assetKeyMap;
    // так как основной ключ - HashMap, то для поиска всех активов для заданного счета - нужен TreeMap - но не для -opi ключа
    protected NavigableSet addressKeySet;

    public ItemAssetBalanceSuitMapDB(DBASet databaseSet, DB database, DBTab cover) {
        super(databaseSet, database, logger, false, cover);

        Arrays.fill(ADDR_KEY2_MIN, (byte) -128);
        Arrays.fill(ADDR_KEY2_MAX, (byte) 127);
    }

    @SuppressWarnings({"unchecked"})
    @Override
    public void openMap() {

        //OPEN MAP

        if (true) {
            HTreeMap<byte[], Tuple5<
                    Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>,
                    Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>> hashMap
                    = database.createHashMap("balances")
                    .keySerializer(SerializerBase.BYTE_ARRAY)
                    .hasher(Hasher.BYTE_ARRAY)
                    .makeOrGet();
            map = hashMap;
        } else {

            BTreeMap<byte[], Tuple5<
                    Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>,
                    Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>> treeMap
                    = database.createTreeMap("balances")
                    //.keySerializer(BTreeKeySerializer.TUPLE2)
                    .keySerializer(BTreeKeySerializer.BASIC)
                    //.keySerializer(new BTreeKeySerializer.Tuple2KeySerializer(
                    //        UnsignedBytes.lexicographicalComparator(), // Fun.BYTE_ARRAY_COMPARATOR,
                    //        Serializer.BYTE_ARRAY,
                    //        Serializer.LONG))
                    //.comparator(Fun.TUPLE2_COMPARATOR)
                    .comparator(Fun.BYTE_ARRAY_COMPARATOR)
                    //.comparator(UnsignedBytes.lexicographicalComparator())
                    .makeOrGet();
            map = treeMap;
        }

        if (BlockChain.TEST_DB == 0) {
            // TODO сделать потом отдельную таблицу только для заданного Актива - для ускорения
            // если включены выплаты - то нужно этот индекс тоже делать - хотя можно отдельно по одному Активу только - нужному

            //BIND ASSET KEY

            if (false) {
                /// так как основной Индекс не сравниваемы - byte[] то во Вторичном индексе делаем Строку
                // - иначе она не сработает так как тут дерево с поиском
                this.assetKeySet_deprecated = database.createTreeSet("balances_key_asset_bal_address")
                        .comparator(new Fun.Tuple2Comparator<>(Fun.TUPLE2_COMPARATOR, Fun.BYTE_ARRAY_COMPARATOR))
                        //.comparator(new Fun.Tuple2Comparator<>(Fun.TUPLE2_COMPARATOR, UnsignedBytes.lexicographicalComparator()))
                        .makeOrGet();

                Bind.secondaryKey((Bind.MapWithModificationListener<byte[], Tuple5<
                                Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>,
                                Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>>) map,
                        this.assetKeySet_deprecated, new Fun.Function2<Tuple2<Long, BigDecimal>,
                                byte[],
                                Tuple5<
                                        Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>,
                                        Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>>
                                () {
                            @Override
                            public Tuple2<Long, BigDecimal>
                            run(byte[] key, Tuple5<
                                    Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>,
                                    Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>> value) {

                                // ASSET KEY
                                byte[] assetKeyBytes = new byte[8];
                                System.arraycopy(key, 20, assetKeyBytes, 0, 8);

                                return new Tuple2<Long, BigDecimal>(Longs.fromByteArray(assetKeyBytes), value.a.b);
                            }
                        });
            } else {


                //BIND ASSET KEY
                /// так как основной Индекс не сравниваемы - byte[] то во Вторичном индексе делаем Строку
                // - иначе она не сработает так как тут дерево с поиском
                this.assetKeyMap = database.createTreeMap("balances_key_2_asset_bal_address")
                        .comparator(new Fun.Tuple3Comparator<>(Fun.COMPARATOR, Fun.COMPARATOR, Fun.COMPARATOR))
                        .makeOrGet();

                Bind.secondaryKey((Bind.MapWithModificationListener<byte[], Tuple5<
                                Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>,
                                Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>>) map,
                        this.assetKeyMap, new Fun.Function2<Tuple3<Long, BigDecimal, Long>,
                                byte[],
                                Tuple5<
                                        Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>,
                                        Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>>
                                () {
                            @Override
                            public Tuple3<Long, BigDecimal, Long>
                            run(byte[] key, Tuple5<
                                    Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>,
                                    Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>> value) {

                                // ASSET KEY
                                byte[] assetKeyBytes = new byte[8];
                                // get ASSET KEY and copy it
                                System.arraycopy(key, 20, assetKeyBytes, 0, 8);

                                return new Tuple3<Long, BigDecimal, Long>(Longs.fromByteArray(assetKeyBytes), value.a.b, Longs.fromByteArray(key));
                            }
                        });
            }

        }

        if (Controller.getInstance().onlyProtocolIndexing)
            // NOT USE SECONDARY INDEXES
            return;

        this.addressKeySet = database.createTreeSet("balances_address_asset_2_bal")
                .comparator(new Fun.Tuple2Comparator<>(Fun.BYTE_ARRAY_COMPARATOR, Fun.BYTE_ARRAY_COMPARATOR)) // у вторичных ключей всегда там Tuple2
                //.valuesOutsideNodesEnable()
                .makeOrGet();

        Bind.secondaryKey((Bind.MapWithModificationListener<byte[], Tuple5<
                        Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>,
                        Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>>) map,
                this.addressKeySet, new Fun.Function2<byte[],
                        byte[],
                        Tuple5<
                                Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>,
                                Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>>
                        () {
                    @Override
                    public byte[]
                    run(byte[] key, Tuple5 value) {

                        // first 20 bytes - short address
                        byte[] secondary = new byte[ADDR_KEY2_LEN];
                        System.arraycopy(key, 0, secondary, 0, ADDR_KEY2_LEN);
                        return secondary;

                    }
                });

    }

    @Override
    public IteratorCloseable<byte[]> getIteratorByAsset(long assetKey) {
        return IteratorCloseableImpl.make(this.assetKeyMap.subMap(
                Fun.t3(assetKey, null, Long.MAX_VALUE), Fun.t3(assetKey, Fun.HI(), Long.MAX_VALUE)).values().iterator());
    }

    @Override
    public IteratorCloseable<byte[]> getIteratorByAsset(long assetKey, BigDecimal fromOwnAmount, byte[] addressShort, boolean descending) {
        if (descending) {
            if (fromOwnAmount == null)
                return IteratorCloseableImpl.make(this.assetKeyMap.descendingMap().subMap(
                        Fun.t3(assetKey + 1L, null, Long.MAX_VALUE), true,
                        Fun.t3(assetKey, null, Long.MIN_VALUE), false).values().iterator());
            return IteratorCloseableImpl.make(this.assetKeyMap.descendingMap().subMap(
                    Fun.t3(assetKey, fromOwnAmount, addressShort == null ? Long.MAX_VALUE : Longs.fromByteArray(addressShort)), true,
                    Fun.t3(assetKey, null, Long.MIN_VALUE), false).values().iterator());
        }

        return IteratorCloseableImpl.make(this.assetKeyMap.subMap(
                Fun.t3(assetKey, fromOwnAmount, addressShort == null ? Long.MIN_VALUE : Longs.fromByteArray(addressShort)),
                Fun.t3(assetKey, Fun.HI(), Long.MAX_VALUE)).values().iterator());
    }

    @Override
    public IteratorCloseable<byte[]> accountIterator(Account account) {
        if (addressKeySet == null)
            return null;

        byte[] secondary = new byte[ADDR_KEY2_LEN];
        System.arraycopy(account.getShortAddressBytes(), 0, secondary, 0, ADDR_KEY2_LEN);

        // WRONG - return new IndexIterator(Fun.filter((NavigableSet) this.addressKeyMap2, secondary).iterator());
        return new IndexIterator((NavigableSet) this.addressKeySet.subSet(
                Fun.t2(secondary, ADDR_KEY2_MIN), Fun.t2(secondary, ADDR_KEY2_MAX)));
    }

}
