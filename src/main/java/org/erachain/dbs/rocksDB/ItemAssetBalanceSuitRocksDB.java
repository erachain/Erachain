package org.erachain.dbs.rocksDB;

import com.google.common.primitives.Longs;
import lombok.extern.slf4j.Slf4j;
import org.erachain.controller.Controller;
import org.erachain.core.BlockChain;
import org.erachain.core.account.Account;
import org.erachain.database.DBASet;
import org.erachain.datachain.ItemAssetBalanceSuit;
import org.erachain.dbs.DBTab;
import org.erachain.dbs.IteratorCloseable;
import org.erachain.dbs.rocksDB.indexes.SimpleIndexDB;
import org.erachain.dbs.rocksDB.indexes.indexByteables.IndexByteableBigDecimal;
import org.erachain.dbs.rocksDB.integration.DBRocksDBTable;
import org.erachain.dbs.rocksDB.integration.DBRocksDBTableDBCommitedAsBath;
import org.erachain.dbs.rocksDB.transformation.ByteableTrivial;
import org.mapdb.DB;
import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple5;
import org.rocksdb.ReadOptions;
import org.rocksdb.WriteOptions;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;

import static org.erachain.dbs.rocksDB.RockDBSetts.ROCK_BIG_DECIMAL_LEN;

@Slf4j
public class ItemAssetBalanceSuitRocksDB extends DBMapSuit<byte[], Tuple5<
        Tuple2<BigDecimal, BigDecimal>, // in OWN - total INCOMED + BALANCE
        Tuple2<BigDecimal, BigDecimal>, // in DEBT
        Tuple2<BigDecimal, BigDecimal>, // in STOCK
        Tuple2<BigDecimal, BigDecimal>, // it DO
        Tuple2<BigDecimal, BigDecimal>  // on HOLD
        >>
        implements ItemAssetBalanceSuit {

    static final int ADDR_KEY2_LEN = 10;

    private IndexByteableBigDecimal seralizerBigDecimal = new IndexByteableBigDecimal();
    private final String NAME_TABLE = "ITEM_ASSET_BALANCE_TABLE";
    private final String balanceKeyAssetIndexName = "balances_by_asset";
    private SimpleIndexDB<
            byte[],
            Tuple5<
                    Tuple2<BigDecimal, BigDecimal>,
                    Tuple2<BigDecimal, BigDecimal>,
                    Tuple2<BigDecimal, BigDecimal>,
                    Tuple2<BigDecimal, BigDecimal>,
                    Tuple2<BigDecimal, BigDecimal>>,
            byte[]> balanceKeyAssetIndex;

    public ItemAssetBalanceSuitRocksDB(DBASet databaseSet, DB database, DBTab cover) {
        super(databaseSet, database, logger, false, cover);
    }

    @Override
    public void openMap() {

        map = new DBRocksDBTableDBCommitedAsBath<byte[], Tuple5<
                        Tuple2<BigDecimal, BigDecimal>, // in OWN - total INCOMED + BALANCE
                        Tuple2<BigDecimal, BigDecimal>, // in DEBT
                        Tuple2<BigDecimal, BigDecimal>, // in STOCK
                        Tuple2<BigDecimal, BigDecimal>, // it DO
                        Tuple2<BigDecimal, BigDecimal>  // on HOLD
                        >>(
                new ByteableTrivial(),
                new org.erachain.dbs.rocksDB.transformation.differentLength.ByteableTuple5Tuples2BigDecimal(), NAME_TABLE, indexes,
                org.erachain.dbs.rocksDB.common.RocksDbSettings.initCustomSettings(7, 64, 32,
                        256, 128,
                        1, 256, 32, false),
                new WriteOptions().setSync(true).setDisableWAL(false),
                new ReadOptions(),
                databaseSet, sizeEnable);

    }

    @Override
    public void createIndexes() {

        // indexes = new ArrayList<>(); - null - not use SIZE index and counter

        indexes = new ArrayList<>();

        if (BlockChain.TEST_DB == 0) {
            // TODO сделать потом отдельную таблицу только для заданного Актива - для ускорения
            // теперь при мульти выплатах - это протокольная таблица
            // если включены выплаты - то нужно этот индекс тоже делать - хотя можно отдельно по одному Активу только - нужному
            balanceKeyAssetIndex = new SimpleIndexDB<>(balanceKeyAssetIndexName,
                    (key, value) -> {
                        // Address
                        byte[] shortAddress = new byte[20];
                        System.arraycopy(key, 0, shortAddress, 0, 20);
                        // ASSET KEY
                        byte[] assetKeyBytes = new byte[8];
                        System.arraycopy(key, 20, assetKeyBytes, 0, 8);

                        byte[] shiftForSortBuff;
                        shiftForSortBuff = seralizerBigDecimal.toBytes(value.a.b.negate());

                        return org.bouncycastle.util.Arrays.concatenate(
                                assetKeyBytes,
                                shiftForSortBuff
                                //shortAddress - он уже есть в главном ключе
                        );
                    },
                    (result) -> result);
            indexes.add(balanceKeyAssetIndex);
        }

        if (Controller.getInstance().onlyProtocolIndexing)
            // NOT USE SECONDARY INDEXES
            return;

    }

    @Override
    public IteratorCloseable<byte[]> getIteratorByAsset(long assetKey) {
        return map.getIndexIteratorFilter(balanceKeyAssetIndex.getColumnFamilyHandle(),
                Longs.toByteArray(assetKey), false, true);
    }

    // TODO NEED TEST
    @Override
    public IteratorCloseable<byte[]> getIteratorByAsset(long assetKey, BigDecimal fromOwnAmount, boolean descending) {

        byte[] fromKey = new byte[8 + ROCK_BIG_DECIMAL_LEN];
        // ASSET KEY
        System.arraycopy(Longs.toByteArray(assetKey), 0, fromKey, 0, 8);

        byte[] shiftForSortBuff = seralizerBigDecimal.toBytes(fromOwnAmount.negate());

        System.arraycopy(shiftForSortBuff, 0, fromKey, 8, 8);

        return map.getIndexIteratorFilter(balanceKeyAssetIndex.getColumnFamilyHandle(),
                fromKey, descending, true);
    }

    // TODO NEED TEST
    @Override
    public IteratorCloseable<byte[]> getIteratorByAsset(long assetKey, BigDecimal fromOwnAmount, byte[] addressShort, boolean descending) {

        byte[] fromKey = new byte[8 + ROCK_BIG_DECIMAL_LEN + Account.ADDRESS_SHORT_LENGTH];
        // ASSET KEY
        System.arraycopy(Longs.toByteArray(assetKey), 0, fromKey, 0, 8);

        byte[] shiftForSortBuff = seralizerBigDecimal.toBytes(fromOwnAmount.negate());

        System.arraycopy(shiftForSortBuff, 0, fromKey, 8, 8);
        System.arraycopy(addressShort, 0, fromKey, 8 + ROCK_BIG_DECIMAL_LEN, ROCK_BIG_DECIMAL_LEN);

        return map.getIndexIteratorFilter(balanceKeyAssetIndex.getColumnFamilyHandle(),
                fromKey, descending, true);
    }

    @Override
    public IteratorCloseable<byte[]> accountIterator(Account account) {
        byte[] secondary = new byte[ADDR_KEY2_LEN];
        System.arraycopy(account.getShortAddressBytes(), 0, secondary, 0, ADDR_KEY2_LEN);

        return ((DBRocksDBTable) map).getIndexIteratorFilter(secondary, false, false);
    }

    public byte[] getAssetFloorKey(long assetKey, BigDecimal fromAmount) {
        try (IteratorCloseable<byte[]> iterator = getIteratorByAsset(assetKey, fromAmount, false)) {
            if (iterator.hasNext())
                return iterator.next();
        } catch (IOException e) {
            return null;
        }

        return null;
    }

}
