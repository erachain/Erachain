package org.erachain.datachain;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Longs;
import org.erachain.controller.Controller;
import org.erachain.core.account.Account;
import org.erachain.core.crypto.Crypto;
import org.erachain.database.SortableList;
import org.erachain.dbs.rocksDB.DCMap;
import org.erachain.dbs.rocksDB.indexes.SimpleIndexDB;
import org.erachain.dbs.rocksDB.integration.DBMapDB;
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
import java.math.BigDecimal; // org.erachain.dbs.rocksDB.DBMap
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import static org.erachain.dbs.rocksDB.utils.ConstantsRocksDB.ROCKS_DB_FOLDER;

public class ItemAssetBalanceRocksDBMap extends DCMap<byte[], Fun.Tuple5<
        Fun.Tuple2<BigDecimal, BigDecimal>, // in OWN - total INCOMED + BALANCE
        Fun.Tuple2<BigDecimal, BigDecimal>, // in DEBT
        Fun.Tuple2<BigDecimal, BigDecimal>, // in STOCK
        Fun.Tuple2<BigDecimal, BigDecimal>, // it DO
        Fun.Tuple2<BigDecimal, BigDecimal>  // on HOLD
        >> implements ItemAssetBalanceMap {
    private final String NAME_TABLE = "ITEM_ASSET_BALANCE_TABLE";
    private final String balanceKeyAssetNameIndex = "balances_key_asset";
    private final String balanceAssetKeyNameIndex = "balances_asset_key";

    @SuppressWarnings("rawtypes")
    private BTreeMap assetKeyMap;
    private BTreeMap addressKeyMap;

    public ItemAssetBalanceRocksDBMap(DCSet databaseSet, DB database) {
        super(databaseSet, database);
    }

    public ItemAssetBalanceRocksDBMap(ItemAssetBalanceRocksDBMap parent, DCSet dcSet) {
        super(parent, dcSet);
    }

    @Override
    protected void getMap(DB database) {

        tableDB = new org.erachain.dbs.rocksDB.integration.DBRocksDBTable<byte[], Fun.Tuple5<
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

        databaseSet.addExternalMaps(this);

    }

    @Override
    protected void getMemoryMap() {
        tableDB = new DBMapDB<>(new HashMap<>());
    }

    @Override
    protected void createIndexes(DB database) {
        indexes = new ArrayList<>();

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
    }

    @Override
    protected Fun.Tuple5<
            Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>,
            Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>> getDefaultValue() {
        return new Fun.Tuple5<
                Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>,
                Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>>
                (new Fun.Tuple2<BigDecimal, BigDecimal>(BigDecimal.ZERO, BigDecimal.ZERO),
                        new Fun.Tuple2<BigDecimal, BigDecimal>(BigDecimal.ZERO, BigDecimal.ZERO),
                        new Fun.Tuple2<BigDecimal, BigDecimal>(BigDecimal.ZERO, BigDecimal.ZERO),
                        new Fun.Tuple2<BigDecimal, BigDecimal>(BigDecimal.ZERO, BigDecimal.ZERO),
                        new Fun.Tuple2<BigDecimal, BigDecimal>(BigDecimal.ZERO, BigDecimal.ZERO));
    }

	/*
	public void set(byte[] address, BigDecimal value)
	{
		this.set(address, FEE_KEY, value);
	}
	 */

    public long getAssetKeyFromKey(byte[] key) {
        // ASSET KEY
        byte[] assetKeyBytes = new byte[8];
        System.arraycopy(key, 20, assetKeyBytes, 0, 8);
        return Longs.fromByteArray(assetKeyBytes);
    }

    public byte[] getShortAccountFromKey(byte[] key) {
        // ASSET KEY
        byte[] shortAddressBytes = new byte[20];
        System.arraycopy(key, 0, shortAddressBytes, 0, 20);
        return shortAddressBytes;

    }

    public void set(byte[] address, long key, Fun.Tuple5<
            Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>,
            Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>> value) {
        if (key < 0)
            key = -key;

        this.set(Bytes.concat(address, Longs.toByteArray(key)), value);
    }

    private Account testAcc = new Account("76ACGgH8c63VrrgEw1wQA4Dno1JuPLTsWe");

    public boolean set(byte[] key, Fun.Tuple5<
            Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>,
            Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>> value) {

        boolean test = false;
        if (testAcc.equals(key)) {
            test = true;
        }

        boolean result = super.set(key, value);

        if (test) {
            Fun.Tuple5 balance5 = get(key);
        }

        return result;

    }

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

    @SuppressWarnings({"unchecked", "rawtypes"})
    public SortableList<byte[], Fun.Tuple5<
            Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>,
            Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>>> getBalancesSortableList(long key) {

        if (Controller.getInstance().onlyProtocolIndexing)
            return null;

        if (key < 0)
            key = -key;

        if (false) {
            //FILTER ALL KEYS
            Collection<byte[]> keys = this.assetKeyMap.subMap(
                    Fun.t2(key, null),
                    Fun.t2(key, Fun.HI())).values();

            int tt = keys.size();
            //RETURN
            return new SortableList<byte[], Fun.Tuple5<
                    Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>,
                    Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>>>(this, keys);
        } else {
            Collection<byte[]> keys = ((DBRocksDBTable)tableDB).filterAppropriateValuesAsKeys(new ByteableLong().toBytesObject(key),
                    ((DBRocksDBTable)tableDB).receiveIndexByName(balanceKeyAssetNameIndex));
            return new SortableList<>(this, keys);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public SortableList<byte[], Fun.Tuple5<
            Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>,
            Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>>> getBalancesSortableList(Account account) {

        if (Controller.getInstance().onlyProtocolIndexing)
            return null;

        if (false) {
            //FILTER ALL KEYS
            Collection<byte[]> keys = this.addressKeyMap.subMap(
                    Fun.t2(account.getAddress(), null),
                    Fun.t2(account.getAddress(), Fun.HI())).values();
            //RETURN
            return new SortableList<byte[], Fun.Tuple5<Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>,
                    Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>>>(this, keys);
        } else {
            Collection<byte[]> keys = ((DBRocksDBTable)tableDB).filterAppropriateValuesAsKeys(new ByteableString().toBytesObject(account.getAddress()),
                    ((DBRocksDBTable)tableDB).receiveIndexByName(balanceAssetKeyNameIndex));
            return new SortableList<>(this, keys);
        }
    }

    @Override
    public void reset() {
        databaseSet.close();
        File dbFile = new File(Paths.get(ROCKS_DB_FOLDER).toString(), NAME_TABLE);
        dbFile.delete();
    }

}
