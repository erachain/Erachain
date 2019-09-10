package org.erachain.datachain;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Longs;
import org.erachain.dbs.rocksDB.DCMap;
import org.mapdb.DB;
import org.mapdb.Fun;

import java.io.File;
import java.math.BigDecimal; // org.erachain.dbs.rocksDB.DBMap
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import static org.erachain.dbs.rocksDB.utils.ConstantsRocksDB.ROCKS_DB_FOLDER;

public class ItemAssetBalanceRocksMap extends org.erachain.dbs.rocksDB.DCMap<byte[], Fun.Tuple5<
        Fun.Tuple2<BigDecimal, BigDecimal>, // in OWN - total INCOMED + BALANCE
        Fun.Tuple2<BigDecimal, BigDecimal>, // in DEBT
        Fun.Tuple2<BigDecimal, BigDecimal>, // in STOCK
        Fun.Tuple2<BigDecimal, BigDecimal>, // it DO
        Fun.Tuple2<BigDecimal, BigDecimal>  // on HOLD
        >> implements ItemAssetBalanceMap {
    private final String NAME_TABLE = "ITEM_ASSET_BALANCE_TABLE";
    private final String balanceKeyAssetNameIndex = "balances_key_asset";
    private final String balanceAssetKeyNameIndex = "balances_asset_key";
    private List<org.erachain.dbs.rocksDB.indexes.IndexDB> indexes;
    private org.erachain.dbs.rocksDB.integration.DBRocksDBTable rocksDBTable;


    public ItemAssetBalanceRocksMap(DCSet databaseSet, DB database) {
        super(databaseSet, database);
    }

    public ItemAssetBalanceRocksMap(ItemAssetBalanceRocksMap parent) {
        super(parent, null);
    }

    @Override
    protected void getMap(DB database) {

        rocksDBTable = new org.erachain.dbs.rocksDB.integration.DBRocksDBTable<>(
                new org.erachain.dbs.rocksDB.transformation.differentLength.ByteableTuple2StringLong(),
                new org.erachain.dbs.rocksDB.transformation.differentLength.ByteableTuple5Tuples2BigDecimal(), NAME_TABLE, indexes,
                org.erachain.dbs.rocksDB.common.RocksDbSettings.initCustomSettings(7, 64, 32,
                        256, 10,
                        1, 256, 32, false),ROCKS_DB_FOLDER);

        databaseSet.addExternalMaps(this);

    }

    /*
    public Fun.Tuple5<
            Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>,
            Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>> get(byte[] address, long key) {
        if (key < 0)
            key = -key;


        Fun.Tuple5<
                Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>,
                Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>> value = this.get(
                Bytes.concat(address, Longs.toByteArray(key)));

        return value;
    }
    */

    @Override
    public void reset() {
        databaseSet.close();
        File dbFile = new File(Paths.get(ROCKS_DB_FOLDER).toString(), NAME_TABLE);
        dbFile.delete();
    }
}
