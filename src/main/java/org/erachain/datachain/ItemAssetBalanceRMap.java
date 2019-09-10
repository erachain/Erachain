package org.erachain.datachain;

import org.erachain.core.account.Account;
import org.erachain.database.SortableList;
import org.erachain.utils.ObserverMessage;
import org.mapdb.DB;
import org.mapdb.Fun;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.erachain.rocksDB.utils.ConstantsRocksDB.ROCKS_DB_FOLDER;

public class ItemAssetBalanceRMap extends ItemAssetBalanceMap {
    private final String NAME_TABLE = "ITEM_ASSET_BALANCE_TABLE";
    private final String balanceKeyAssetNameIndex = "balances_key_asset";
    private final String balanceAssetKeyNameIndex = "balances_asset_key";
    private List<org.erachain.rocksDB.indexes.IndexDB> indexes;
    private org.erachain.rocksDB.integration.DBRocksDBTable rocksDBTable;


    public ItemAssetBalanceRMap(DCSet databaseSet, DB database) {
        super(databaseSet, database);
        //init(databaseSet);
    }

    public ItemAssetBalanceRMap(ItemAssetBalanceRMap parent) {
        super(parent, null);
    }

    @Override
    protected void createIndexes(DB database) {
    }

    @Override
    protected org.erachain.rocksDB.integration.DBRocksDBTable<Fun.Tuple2<String, Long>,
            Fun.Tuple5<
                    Fun.Tuple2<BigDecimal, BigDecimal>,
                    Fun.Tuple2<BigDecimal, BigDecimal>,
                    Fun.Tuple2<BigDecimal, BigDecimal>,
                    Fun.Tuple2<BigDecimal, BigDecimal>,
                    Fun.Tuple2<BigDecimal, BigDecimal>>>
        getMap(DB database) {
        rocksDBTable = new org.erachain.rocksDB.integration.DBRocksDBTable<>(new org.erachain.rocksDB.transformation.differentLength.ByteableTuple2StringLong(), new org.erachain.rocksDB.transformation.differentLength.ByteableTuple5Tuples2BigDecimal(), NAME_TABLE, indexes,
                org.erachain.rocksDB.common.RocksDbSettings.initCustomSettings(7, 64, 32,
                        256, 10,
                        1, 256, 32, false),ROCKS_DB_FOLDER);
        return rocksDBTable;
    }

    @Override
    protected org.erachain.rocksDB.integration.InnerDBTable<Fun.Tuple2<String, Long>,
            Tuple5<
                    Tuple2<BigDecimal, BigDecimal>,
                    Tuple2<BigDecimal, BigDecimal>,
                    Tuple2<BigDecimal, BigDecimal>,
                    Tuple2<BigDecimal, BigDecimal>,
                    Tuple2<BigDecimal, BigDecimal>>>
    getMemoryMap() {
        return new org.erachain.rocksDB.integration.DBMapDB<>(new HashMap<>());
    }


}
