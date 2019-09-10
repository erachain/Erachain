package org.erachain.datachain;

import org.mapdb.DB;
import org.mapdb.Fun;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.erachain.dbs.rocksDB.utils.ConstantsRocksDB.ROCKS_DB_FOLDER;

public class ItemAssetBalanceRocksMap extends ItemAssetBalanceMap {
    private final String NAME_TABLE = "ITEM_ASSET_BALANCE_TABLE";
    private final String balanceKeyAssetNameIndex = "balances_key_asset";
    private final String balanceAssetKeyNameIndex = "balances_asset_key";
    private List<org.erachain.dbs.rocksDB.indexes.IndexDB> indexes;
    private org.erachain.dbs.rocksDB.integration.DBRocksDBTable rocksDBTable;


    public ItemAssetBalanceRocksMap(DCSet databaseSet, DB database) {
        super(databaseSet, database);
        //init(databaseSet);
    }

    public ItemAssetBalanceRocksMap(ItemAssetBalanceRocksMap parent) {
        super(parent, null);
    }

    @Override
    protected void createIndexes(DB database) {
    }

    @Override
    protected Map<byte[], Fun.Tuple5<
            Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>,
            Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>>>
        getMap(DB database) {
        rocksDBTable = new org.erachain.dbs.rocksDB.integration.DBRocksDBTable<>(
                new org.erachain.dbs.rocksDB.transformation.differentLength.ByteableTuple2StringLong(),
                new org.erachain.dbs.rocksDB.transformation.differentLength.ByteableTuple5Tuples2BigDecimal(), NAME_TABLE, indexes,
                org.erachain.dbs.rocksDB.common.RocksDbSettings.initCustomSettings(7, 64, 32,
                        256, 10,
                        1, 256, 32, false),ROCKS_DB_FOLDER);
        return rocksDBTable.getMap();
    }

}
