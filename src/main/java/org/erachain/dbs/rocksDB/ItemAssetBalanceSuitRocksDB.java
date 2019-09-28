package org.erachain.dbs.rocksDB;

import com.google.common.primitives.Longs;
import lombok.extern.slf4j.Slf4j;
import org.erachain.controller.Controller;
import org.erachain.core.account.Account;
import org.erachain.database.DBASet;
import org.erachain.datachain.ItemAssetBalanceSuit;
import org.erachain.datachain.ItemAssetBalanceTabImpl;
import org.erachain.dbs.rocksDB.indexes.SimpleIndexDB;
import org.erachain.dbs.rocksDB.indexes.indexByteables.IndexByteableBigDecimal;
import org.erachain.dbs.rocksDB.integration.DBRocksDBTable;
import org.erachain.dbs.rocksDB.transformation.ByteableBigInteger;
import org.erachain.dbs.rocksDB.transformation.ByteableTrivial;
import org.mapdb.DB;
import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple5;
import org.rocksdb.RocksIterator;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Slf4j
public class ItemAssetBalanceSuitRocksDB extends DBMapSuit<byte[], Tuple5<
        Tuple2<BigDecimal, BigDecimal>, // in OWN - total INCOMED + BALANCE
        Tuple2<BigDecimal, BigDecimal>, // in DEBT
        Tuple2<BigDecimal, BigDecimal>, // in STOCK
        Tuple2<BigDecimal, BigDecimal>, // it DO
        Tuple2<BigDecimal, BigDecimal>  // on HOLD
        >>
            implements ItemAssetBalanceSuit {

    private IndexByteableBigDecimal seralizerBigDecimal = new IndexByteableBigDecimal();
    private final String NAME_TABLE = "ITEM_ASSET_BALANCE_TABLE";
    private final String balanceKeyAssetIndexName = "balances_by_asset";
    private final String balanceAddressIndexName = "balances_by_address";
    private SimpleIndexDB<
            byte[],
            Tuple5<
                    Tuple2<BigDecimal, BigDecimal>,
                    Tuple2<BigDecimal, BigDecimal>,
                    Tuple2<BigDecimal, BigDecimal>,
                    Tuple2<BigDecimal, BigDecimal>,
                    Tuple2<BigDecimal, BigDecimal>>,
            byte[]> balanceKeyAssetIndex;

    private SimpleIndexDB<
            byte[],
            Tuple5<
                    Tuple2<BigDecimal, BigDecimal>,
                    Tuple2<BigDecimal, BigDecimal>,
                    Tuple2<BigDecimal, BigDecimal>,
                    Tuple2<BigDecimal, BigDecimal>,
                    Tuple2<BigDecimal, BigDecimal>>,
            byte[]> balanceAddressIndex;

    public ItemAssetBalanceSuitRocksDB(DBASet databaseSet, DB database) {
        super(databaseSet, database, logger, ItemAssetBalanceTabImpl.DEFAULT_VALUE);
    }

    //private final ByteableBigDecimal byteableBigDecimal = new ByteableBigDecimal();
    private final ByteableBigInteger byteableBigInteger = new ByteableBigInteger();

    @Override
    protected void getMap() {

        map = new DBRocksDBTable<byte[], Tuple5<
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
                databaseSet);

    }

    @Override
    public void createIndexes() {

        // indexes = new ArrayList<>(); - null - not use SIZE index and counter

        if (Controller.getInstance().onlyProtocolIndexing)
            // NOT USE SECONDARY INDEXES
            return;

        balanceKeyAssetIndex = new SimpleIndexDB<>(balanceKeyAssetIndexName,
                (key, value) -> {
                    // Address
                    byte[] shortAddress = new byte[20];
                    System.arraycopy(key, 0, shortAddress, 0, 20);
                    // ASSET KEY
                    byte[] assetKeyBytes = new byte[8];
                    System.arraycopy(key, 20, assetKeyBytes, 0, 8);

                    byte[] shiftForSortBuff;
                    shiftForSortBuff = seralizerBigDecimal.toBytes(value.a.b.negate(), null);

                    return org.bouncycastle.util.Arrays.concatenate(
                            assetKeyBytes,
                            shiftForSortBuff
                            //shortAddress - он уже есть в главном ключе
                    );
                },
                (result, key) -> result);

        balanceAddressIndex = new SimpleIndexDB<>(balanceAddressIndexName,
                (key, value) -> {
                    // Address
                    byte[] shortAddress = new byte[20];
                    System.arraycopy(key, 0, shortAddress, 0, 20);
                    // ASSET KEY
                    byte[] assetKeyBytes = new byte[8];
                    System.arraycopy(key, 20, assetKeyBytes, 0, 8);

                    return org.bouncycastle.util.Arrays.concatenate(
                            shortAddress,
                            assetKeyBytes);
                },
                (result, key) -> result); // ByteableTrivial

        indexes = new ArrayList<>();
        indexes.add(balanceKeyAssetIndex);
        indexes.add(balanceAddressIndex);
    }

    // TODO - release it on Iterators

    public List<byte[]> assetKeys_bad(long assetKey) {
        return ((DBRocksDBTable)map).filterAppropriateValuesAsKeys(
                Longs.toByteArray(assetKey),
                balanceKeyAssetIndex.getColumnFamilyHandle());
    }

    public List<byte[]> assetKeys(long assetKey) {
        return ((DBRocksDBTable)map).filterAppropriateValuesAsByteKeys(
                Longs.toByteArray(assetKey),
                balanceKeyAssetIndex.getColumnFamilyHandle());
    }


    @Override
    public Iterator<byte[]> assetIterator(long assetKey) {
        return assetKeys(assetKey).iterator();
    }

    public List<byte[]> accountKeys(Account account) {
        if (false) {
            return ((DBRocksDBTable) map).filterAppropriateValuesAsKeys(
                    account.getShortAddressBytes(),
                    balanceAddressIndex.getColumnFamilyHandle());
        } else {
            RocksIterator iter = ((DBRocksDBTable) map).db.database.newIterator(
                    balanceAddressIndex
                    //balanceKeyAssetIndex
                            .getColumnFamilyHandle());
            List<byte[]> result = new ArrayList<byte[]>();

            for (iter.seek(account.getShortAddressBytes()); iter.isValid() && new String(iter.key())
                    .startsWith(new String(account.getShortAddressBytes())); iter.next()) {
                byte[] key = iter.key();
                byte[] value = iter.value();
                result.add(iter.value());
            }
            return result;

        }
    }

    @Override
    public Iterator<byte[]> accountIterator(Account account) {
        return accountKeys(account).iterator();
    }

}
