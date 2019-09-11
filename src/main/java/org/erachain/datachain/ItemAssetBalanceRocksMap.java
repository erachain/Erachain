package org.erachain.datachain;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Longs;
import org.erachain.controller.Controller;
import org.erachain.core.account.Account;
import org.erachain.core.crypto.Crypto;
import org.erachain.database.SortableList;
import org.erachain.dbs.rocksDB.DCMap;
import org.erachain.dbs.rocksDB.transformation.ByteableTrivial;
import org.mapdb.BTreeMap;
import org.mapdb.Bind;
import org.mapdb.DB;
import org.mapdb.Fun;

import java.io.File;
import java.math.BigDecimal; // org.erachain.dbs.rocksDB.DBMap
import java.nio.file.Paths;
import java.util.Collection;

import static org.erachain.dbs.rocksDB.utils.ConstantsRocksDB.ROCKS_DB_FOLDER;

public class ItemAssetBalanceRocksMap extends DCMap<byte[], Fun.Tuple5<
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

    public ItemAssetBalanceRocksMap(DCSet databaseSet, DB database) {
        super(databaseSet, database);
    }

    public ItemAssetBalanceRocksMap(ItemAssetBalanceRocksMap parent, DCSet dcSet) {
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
        //rocksDBTable = new DBMapDB<>(new HashMap<>());
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
    protected void createIndexes(DB database) {

        //BIND ASSET KEY
        /// так как основной Индекс не сравниваемы - byte[] то во Вторичном индексе делаем Строку
        // - иначе она не сработает так как тут дерево с поиском
        this.assetKeyMap = database.createTreeMap("balances_key_asset_bal_address")
                .comparator(Fun.COMPARATOR)
                //.valuesOutsideNodesEnable()
                .makeOrGet();

        Bind.secondaryKey(hashMap, this.assetKeyMap, new Fun.Function2<Fun.Tuple2<Long, BigDecimal>,
                byte[],
                Fun.Tuple5<
                        Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>,
                        Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>>>
                () {
            @Override
            public Fun.Tuple2<Long, BigDecimal>
            run(byte[] key, Fun.Tuple5<
                    Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>,
                    Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>> value) {

                byte[] assetKeyBytes = new byte[8];
                System.arraycopy(key, 20, assetKeyBytes, 0, 8);

                return new Fun.Tuple2<Long, BigDecimal>(
                        Longs.fromByteArray(assetKeyBytes), value.a.b.negate()
                );
            }
        });

        this.addressKeyMap = database.createTreeMap("balances_address_asset_bal")
                .comparator(Fun.COMPARATOR)
                //.valuesOutsideNodesEnable()
                .makeOrGet();

        Bind.secondaryKey(hashMap, this.addressKeyMap, new Fun.Function2<Fun.Tuple2<String, Long>,
                byte[],
                Fun.Tuple5<
                        Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>,
                        Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>>>
                () {
            @Override
            public Fun.Tuple2<String, Long>
            run(byte[] key, Fun.Tuple5<
                    Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>,
                    Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>> value) {

                // Address
                byte[] shortAddress = new byte[20];
                System.arraycopy(key, 0, shortAddress, 0, 20);
                // ASSET KEY
                byte[] assetKeyBytes = new byte[8];
                System.arraycopy(key, 20, assetKeyBytes, 0, 8);

                return new Fun.Tuple2<String, Long>(
                        Crypto.getInstance().getAddressFromShort(shortAddress),
                        Longs.fromByteArray(assetKeyBytes)
                );
            }
        });

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

	/*
	public BigDecimal get(byte[] address)
	{
		return this.get(address, FEE_KEY);
	}
	 */

    public Fun.Tuple5<
            Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>,
            Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>> get(byte[] address, long key) {
        if (key < 0)
            key = -key;


        Fun.Tuple5<
                Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>,
                Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>> value = this.get(
                Bytes.concat(address, Longs.toByteArray(key)));

		/*
		// TODO for TEST
		// FOR TEST NET
		if (key == Transaction.FEE_KEY &&
				value.a.compareTo(BigDecimal.ONE) < 0) {

			return new Tuple3<BigDecimal, BigDecimal, BigDecimal>(
					BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO);

		}
		 */

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

        //FILTER ALL KEYS
        Collection<byte[]> keys = this.assetKeyMap.subMap(
                Fun.t2(key, null),
                Fun.t2(key, Fun.HI())).values();

        int tt = keys.size();
        //RETURN
        return new SortableList<byte[], Fun.Tuple5<
                Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>,
                Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>>>(this, keys);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public SortableList<byte[], Fun.Tuple5<
            Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>,
            Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>>> getBalancesSortableList(Account account) {

        if (Controller.getInstance().onlyProtocolIndexing)
            return null;

        //FILTER ALL KEYS
        Collection<byte[]> keys = this.addressKeyMap.subMap(
                Fun.t2(account.getAddress(), null),
                Fun.t2(account.getAddress(), Fun.HI())).values();

        int tt = keys.size();
        //RETURN
        return new SortableList<byte[], Fun.Tuple5<Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>,
                Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>>>(this, keys);
    }

    @Override
    public void reset() {
        databaseSet.close();
        File dbFile = new File(Paths.get(ROCKS_DB_FOLDER).toString(), NAME_TABLE);
        dbFile.delete();
    }
}
