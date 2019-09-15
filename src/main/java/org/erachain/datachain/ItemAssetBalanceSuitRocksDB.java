package org.erachain.datachain;

import com.google.common.primitives.Longs;
import org.erachain.core.crypto.Crypto;
import org.erachain.dbs.rocksDB.indexes.IndexDB;
import org.erachain.dbs.rocksDB.indexes.SimpleIndexDB;
import org.erachain.dbs.rocksDB.transformation.ByteableBigDecimal;
import org.erachain.dbs.rocksDB.transformation.ByteableLong;
import org.erachain.dbs.rocksDB.transformation.ByteableString;
import org.mapdb.DB;
import org.mapdb.Fun;
import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple5;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ItemAssetBalanceSuitRocksDB extends ItemAssetBalanceMapImpl {

    private final String NAME_TABLE = "ITEM_ASSET_BALANCE_TABLE";
    private final String balanceKeyAssetNameIndex = "balances_key_asset";
    private final String balanceAssetKeyNameIndex = "balances_asset_key";

    public ItemAssetBalanceSuitRocksDB(DCSet databaseSet, DB database) {
        super(databaseSet, database);
    }

    @Override
    Collection<byte[]> assetKeySubMap(long key) {
        return null;
    }

    @Override
    Collection<byte[]> addressKeySubMap(String address) {
        return null;
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

        ///map = new org.erachain.dbs.mapDB.ItemAssetBalanceMapDBMap(databaseSet, database);
        map = new org.erachain.dbs.rocksDB.ItemAssetBalanceRocksDBMap(databaseSet, database);

    }

    @Override
    protected void createIndexes() {
    }

}
