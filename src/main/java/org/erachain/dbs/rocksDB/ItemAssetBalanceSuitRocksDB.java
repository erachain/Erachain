package org.erachain.dbs.rocksDB;

import com.google.common.primitives.Longs;
import org.erachain.core.account.Account;
import org.erachain.core.crypto.Crypto;
import org.erachain.database.DBASet;
import org.erachain.datachain.ItemAssetBalanceSuit;
import org.erachain.dbs.mapDB.ItemAssetBalanceSuitMapDB;
import org.erachain.dbs.rocksDB.indexes.SimpleIndexDB;
import org.erachain.dbs.rocksDB.integration.DBRocksDBTable;
import org.erachain.dbs.rocksDB.transformation.ByteableBigDecimal;
import org.erachain.dbs.rocksDB.transformation.ByteableLong;
import org.erachain.dbs.rocksDB.transformation.ByteableString;
import org.erachain.dbs.rocksDB.transformation.ByteableTrivial;
import org.mapdb.BTreeMap;
import org.mapdb.DB;
import org.mapdb.Fun;
import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple5;

import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import static org.erachain.dbs.rocksDB.utils.ConstantsRocksDB.ROCKS_DB_FOLDER;

public class ItemAssetBalanceSuitRocksDB extends DBMapSuit<byte[], Tuple5<
        Tuple2<BigDecimal, BigDecimal>, // in OWN - total INCOMED + BALANCE
        Tuple2<BigDecimal, BigDecimal>, // in DEBT
        Tuple2<BigDecimal, BigDecimal>, // in STOCK
        Tuple2<BigDecimal, BigDecimal>, // it DO
        Tuple2<BigDecimal, BigDecimal>  // on HOLD
        >>
            implements ItemAssetBalanceSuit {

    private final String NAME_TABLE = "ITEM_ASSET_BALANCE_TABLE";
    private final String balanceKeyAssetNameIndex = "balances_key_asset";
    private final String balanceAssetKeyNameIndex = "balances_asset_key";

    public ItemAssetBalanceSuitRocksDB(DBASet databaseSet, DB database) {
        super(databaseSet, database);
    }

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
                        256, 10,
                        1, 256, 32, false), ROCKS_DB_FOLDER);

        databaseSet.addExternalMaps(this);

    }

    @Override
    public void createIndexes() {
        indexes = new ArrayList<>();

        SimpleIndexDB<
                byte[],
                Tuple5<
                        Tuple2<BigDecimal, BigDecimal>,
                        Tuple2<BigDecimal, BigDecimal>,
                        Tuple2<BigDecimal, BigDecimal>,
                        Tuple2<BigDecimal, BigDecimal>,
                        Tuple2<BigDecimal, BigDecimal>>,
                byte[]> indexDBf1f0 = new SimpleIndexDB<>(balanceKeyAssetNameIndex,
                (key, value) -> {
                    // Address
                    byte[] shortAddress = new byte[20];
                    System.arraycopy(key, 0, shortAddress, 0, 20);
                    // ASSET KEY
                    byte[] assetKeyBytes = new byte[8];
                    System.arraycopy(key, 20, assetKeyBytes, 0, 8);

                    return org.bouncycastle.util.Arrays.concatenate(
                            assetKeyBytes,
                            new ByteableBigDecimal().toBytesObject(value.a.b.negate()),
                            shortAddress);
                },
                (result, key) -> result);
        indexes.add(indexDBf1f0);

        SimpleIndexDB<
                byte[],
                Tuple5<
                        Tuple2<BigDecimal, BigDecimal>,
                        Tuple2<BigDecimal, BigDecimal>,
                        Tuple2<BigDecimal, BigDecimal>,
                        Tuple2<BigDecimal, BigDecimal>,
                        Tuple2<BigDecimal, BigDecimal>>,
                byte[] indexDBf0f1 = new SimpleIndexDB<>(balanceAssetKeyNameIndex,
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
        indexes.add(indexDBf0f1);
    }

    @Override
    public Tuple5<
            Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>,
            Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>> getDefaultValue() {
        return DEFAULT_VALUE;
    }

    @Override
    public void reset() {
        databaseSet.close();
        File dbFile = new File(Paths.get(ROCKS_DB_FOLDER).toString(), NAME_TABLE);
        dbFile.delete();
    }


    // TODO - release it

    public Collection<byte[]> assetKeys(long assetKey) {
        Collection<byte[]> keys = new ArrayList<>();
        //FILTER ALL KEYS
        Iterator<byte[]> iterator = assetIterator(assetKey);
        while (iterator.hasNext()) {
            keys.add(iterator.next());
        }
        return keys;
    }

    @Override
    public Iterator<byte[]> assetIterator(long assetKey) {
        return assetKeys(assetKey).iterator();
    }

    public Collection<byte[]> accountKeys(Account account) {
        Collection<byte[]> keys = new ArrayList<>();
        //FILTER ALL KEYS
        Iterator<byte[]> iterator = accountIterator(account);
        while (iterator.hasNext()) {
            keys.add(iterator.next());
        }
        return keys;
    }

    @Override
    public Iterator<byte[]> accountIterator(Account account) {
        //return accountKeys(account).iterator();
    }

}
