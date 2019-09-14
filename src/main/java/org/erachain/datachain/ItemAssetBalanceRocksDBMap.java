package org.erachain.datachain;

import com.google.common.primitives.Longs;
import org.erachain.core.crypto.Crypto;
import org.erachain.dbs.rocksDB.indexes.IndexDB;
import org.erachain.dbs.rocksDB.indexes.SimpleIndexDB;
import org.erachain.dbs.rocksDB.transformation.ByteableBigDecimal;
import org.erachain.dbs.rocksDB.transformation.ByteableLong;
import org.erachain.dbs.rocksDB.transformation.ByteableString;
import org.erachain.dbs.rocksDB.transformation.ByteableTrivial;
import org.mapdb.DB;
import org.mapdb.Fun;
import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple5;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.erachain.dbs.rocksDB.utils.ConstantsRocksDB.ROCKS_DB_FOLDER;

public class ItemAssetBalanceRocksDBMap extends ItemAssetBalanceMapImpl {

    private final String NAME_TABLE = "ITEM_ASSET_BALANCE_TABLE";
    private final String balanceKeyAssetNameIndex = "balances_key_asset";
    private final String balanceAssetKeyNameIndex = "balances_asset_key";

    public ItemAssetBalanceRocksDBMap(DCSet databaseSet, DB database) {
        super(databaseSet, database);
    }


    @Override
    protected void getMap() {

        List<IndexDB> indexes = new ArrayList<>();

        SimpleIndexDB<
                byte[],
                Tuple5<
                        Tuple2<BigDecimal, BigDecimal>,
                        Tuple2<BigDecimal, BigDecimal>,
                        Tuple2<BigDecimal, BigDecimal>,
                        Tuple2<BigDecimal, BigDecimal>,
                        Tuple2<BigDecimal, BigDecimal>>,
                Fun.Tuple2<Long, BigDecimal>> indexDBf1f0 = new SimpleIndexDB<>(balanceKeyAssetNameIndex,
                (key, value) -> {
                    byte[] assetKeyBytes = new byte[8];
                    System.arraycopy(key, 20, assetKeyBytes, 0, 8);
                    return new Fun.Tuple2<>(Longs.fromByteArray(assetKeyBytes), value.a.b.negate());
                }
                ,
                (result, key) -> org.bouncycastle.util.Arrays.concatenate(
                        new ByteableLong().toBytesObject(result.a),
                        new ByteableBigDecimal().toBytesObject(result.b)
                ));
        indexes.add(indexDBf1f0);

        SimpleIndexDB<
                byte[],
                Tuple5<
                        Tuple2<BigDecimal, BigDecimal>,
                        Tuple2<BigDecimal, BigDecimal>,
                        Tuple2<BigDecimal, BigDecimal>,
                        Tuple2<BigDecimal, BigDecimal>,
                        Tuple2<BigDecimal, BigDecimal>>,
                Fun.Tuple2<String, Long>> indexDBf0f1 = new SimpleIndexDB<>(balanceAssetKeyNameIndex,
                (key, value) -> {
                    // Address
                    byte[] shortAddress = new byte[20];
                    System.arraycopy(key, 0, shortAddress, 0, 20);
                    // ASSET KEY
                    byte[] assetKeyBytes = new byte[8];
                    System.arraycopy(key, 20, assetKeyBytes, 0, 8);

                    return new Fun.Tuple2<String, Long>(
                            Crypto.getInstance().getAddressFromShort(shortAddress),
                            Longs.fromByteArray(assetKeyBytes));
                },
                (result, key) -> org.bouncycastle.util.Arrays.concatenate(
                        new ByteableString().toBytesObject(result.a),
                        new ByteableLong().toBytesObject(result.b)
                ));
        indexes.add(indexDBf0f1);

        map = new org.erachain.dbs.rocksDB.ItemAssetBalanceRocksDBMap(databaseSet, database);
        /*
        <byte[], Fun.Tuple5<
                Fun.Tuple2<BigDecimal, BigDecimal>, // in OWN - total INCOMED + BALANCE
                Fun.Tuple2<BigDecimal, BigDecimal>, // in DEBT
                Fun.Tuple2<BigDecimal, BigDecimal>, // in STOCK
                Fun.Tuple2<BigDecimal, BigDecimal>, // it DO
                Fun.Tuple2<BigDecimal, BigDecimal>  // on HOLD
                >>(
                new ByteableTrivial(),
                new org.erachain.dbs.rocksDB.transformation.differentLength.ByteableTuple5Tuples2BigDecimal(), NAME_TABLE, indexes,
                org.erachain.dbs.rocksDB.common.RocksDbSettings.initCustomSettings(7, 64, 32,
                        256, 10,
                        1, 256, 32, false), ROCKS_DB_FOLDER);

         */

        databaseSet.addExternalMaps(this);

    }

    @Override
    protected void createIndexes() {
    }

}
