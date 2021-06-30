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
import org.mapdb.*;
import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple5;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.NavigableSet;

// TODO SOFT HARD TRUE

@Slf4j
public class ItemAssetBalanceSuitMapDB extends DBMapSuit<byte[], Tuple5<
        Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>,
        Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>>
        implements ItemAssetBalanceSuit {

    static final int ADDR_KEY2_LEN = 10;
    static final byte[] ADDR_KEY2_MIN = new byte[ADDR_KEY2_LEN];
    static final byte[] ADDR_KEY2_MAX = new byte[ADDR_KEY2_LEN];


    @SuppressWarnings("rawtypes")
    protected NavigableSet assetKeySet;
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
                    .makeOrGet();
            map = treeMap;
        }

        if (BlockChain.TEST_DB == 0) {
            // TODO сделать потом отдельную таблицу только для заданного Актива - для ускорения
            // если включены выплаты - то нужно этот индекс тоже делать - хотя можно отдельно по одному Активу только - нужному

            //BIND ASSET KEY
            /// так как основной Индекс не сравниваемы - byte[] то во Вторичном индексе делаем Строку
            // - иначе она не сработает так как тут дерево с поиском
            this.assetKeySet = database.createTreeSet("balances_key_asset_bal_address")
                    .comparator(new Fun.Tuple2Comparator<>(Fun.TUPLE2_COMPARATOR, Fun.BYTE_ARRAY_COMPARATOR))
                    .makeOrGet();

            Bind.secondaryKey(hashMap, this.assetKeySet, new Fun.Function2<Tuple2<Long, BigDecimal>,
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

                    return new Tuple2<Long, BigDecimal>(Longs.fromByteArray(assetKeyBytes), value.a.b.negate());
                }
            });

        }

        if (Controller.getInstance().onlyProtocolIndexing)
            // NOT USE SECONDARY INDEXES
            return;

        this.addressKeySet = database.createTreeSet("balances_address_asset_2_bal")
                .comparator(new Fun.Tuple2Comparator<>(Fun.BYTE_ARRAY_COMPARATOR, Fun.BYTE_ARRAY_COMPARATOR)) // у вторичных ключей всегда там Tuple2
                //.valuesOutsideNodesEnable()
                .makeOrGet();

        Bind.secondaryKey(hashMap, this.addressKeySet, new Fun.Function2<byte[],
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
        return new IndexIterator((NavigableSet) this.assetKeySet.subSet(
                Fun.t2(Fun.t2(assetKey, null), ADDR_KEY2_MIN), Fun.t2(Fun.t2(assetKey, Fun.HI), ADDR_KEY2_MAX)));
    }

    @Override
    public IteratorCloseable<byte[]> accountIterator(Account account) {
        byte[] secondary = new byte[ADDR_KEY2_LEN];
        System.arraycopy(account.getShortAddressBytes(), 0, secondary, 0, ADDR_KEY2_LEN);

        // WRONG - return new IndexIterator(Fun.filter((NavigableSet) this.addressKeyMap2, secondary).iterator());
        return new IndexIterator((NavigableSet) this.addressKeySet.subSet(
                Fun.t2(secondary, ADDR_KEY2_MIN), Fun.t2(secondary, ADDR_KEY2_MAX)));
    }


}
