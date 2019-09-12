package org.erachain.datachain;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Longs;
import org.erachain.controller.Controller;
import org.erachain.core.account.Account;
import org.erachain.core.crypto.Crypto;
import org.erachain.database.DBMap;
import org.erachain.database.SortableList;
import org.erachain.dbs.mapDB.DCMap;
import org.erachain.utils.ObserverMessage;
import org.mapdb.*;
import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple5;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.TreeMap;

/**
 * Hasher работает неверно! и вообще там 32 битное число 0 INTEGER - чего нифига не хватает!
 *
 * (пока не используется - по идее для бухгалтерских единиц отдельная таблица)
 * Балансы для заданного адреса на данный актив. balances for all account in blockchain<br>
 * <b>Список балансов:</b> имущество, займы, хранение, производство, резерв<br>
 * Каждый баланс: Всего Пришло и Остаток<br><br>
 *
 * <b>Ключ:</b> account.address + asset key<br>
 *
 * <b>Значение:</b> Балансы. in_OWN, in_RENT, on_HOLD = in_USE (TOTAL on HAND)
 *
 */
// TODO SOFT HARD TRUE

public class ItemAssetBalanceMapDBMap extends DCMap<byte[], Tuple5<
        Tuple2<BigDecimal, BigDecimal>, // in OWN - total INCOMED + BALANCE
        Tuple2<BigDecimal, BigDecimal>, // in DEBT
        Tuple2<BigDecimal, BigDecimal>, // in STOCK
        Tuple2<BigDecimal, BigDecimal>, // it DO
        Tuple2<BigDecimal, BigDecimal>  // on HOLD
        >> implements ItemAssetBalanceMap {

    @SuppressWarnings("rawtypes")
    private BTreeMap assetKeyMap;
    private BTreeMap addressKeyMap;

    public ItemAssetBalanceMapDBMap(DCSet databaseSet, DB database) {
        super(databaseSet, database);

        if (databaseSet.isWithObserver()) {
            this.observableData.put(DBMap.NOTIFY_RESET, ObserverMessage.RESET_BALANCE_TYPE);
            this.observableData.put(DBMap.NOTIFY_LIST, ObserverMessage.LIST_BALANCE_TYPE);
            this.observableData.put(DBMap.NOTIFY_ADD, ObserverMessage.ADD_BALANCE_TYPE);
            this.observableData.put(DBMap.NOTIFY_REMOVE, ObserverMessage.REMOVE_BALANCE_TYPE);
        }
    }

    public ItemAssetBalanceMapDBMap(ItemAssetBalanceMapDBMap parent, DCSet dcSet) {
        super(parent, dcSet);
    }

    @Override
    protected void createIndexes(DB database) {
    }

    @SuppressWarnings({"unchecked"})
    @Override
    protected void getMap(DB database) {
        //OPEN MAP
        BTreeMap<byte[], Tuple5<
                Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>,
                Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>> treeMap;
        HTreeMap<byte[], Tuple5<
                Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>,
                Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>> hashMap;

        if (true) {
            hashMap = database.createHashMap("balances")
                    .keySerializer(SerializerBase.BYTE_ARRAY)
                    .hasher(Hasher.BYTE_ARRAY)
                    .counterEnable()
                    .makeOrGet();
            map = hashMap;
        } else {

            treeMap = database.createTreeMap("balances")
                    //.keySerializer(BTreeKeySerializer.TUPLE2)
                    .keySerializer(BTreeKeySerializer.BASIC)
                    //.keySerializer(new BTreeKeySerializer.Tuple2KeySerializer(
                    //        UnsignedBytes.lexicographicalComparator(), // Fun.BYTE_ARRAY_COMPARATOR,
                    //        Serializer.BYTE_ARRAY,
                    //        Serializer.LONG))
                    //.comparator(Fun.TUPLE2_COMPARATOR)
                    .comparator(Fun.BYTE_ARRAY_COMPARATOR)
                    //.comparator(UnsignedBytes.lexicographicalComparator())
                    .counterEnable()
                    .makeOrGet();
            map = treeMap;
        }

        if (Controller.getInstance().onlyProtocolIndexing)
            // NOT USE SECONDARY INDEXES
            return;


        //BIND ASSET KEY
        /// так как основной Индекс не сравниваемы - byte[] то во Вторичном индексе делаем Строку
        // - иначе она не сработает так как тут дерево с поиском
        this.assetKeyMap = database.createTreeMap("balances_key_asset_bal_address")
                .comparator(Fun.COMPARATOR)
                //.valuesOutsideNodesEnable()
                .makeOrGet();

        Bind.secondaryKey(hashMap, this.assetKeyMap, new Fun.Function2<Tuple2<Long, BigDecimal>,
                byte[],
                Tuple5<
                        Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>,
                        Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>>
                () {
            @Override
            public Tuple2<Long, BigDecimal>
            run(byte[] key, Tuple5<
                    Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>,
                    Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>> value) {

                byte[] assetKeyBytes = new byte[8];
                System.arraycopy(key, 20, assetKeyBytes, 0, 8);

                return new Tuple2<Long, BigDecimal>(
                        Longs.fromByteArray(assetKeyBytes), value.a.b.negate()
                    );
            }
        });

        this.addressKeyMap = database.createTreeMap("balances_address_asset_bal")
                .comparator(Fun.COMPARATOR)
                //.valuesOutsideNodesEnable()
                .makeOrGet();

        Bind.secondaryKey(hashMap, this.addressKeyMap, new Fun.Function2<Tuple2<String, Long>,
                byte[],
                Tuple5<
                        Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>,
                        Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>>
                () {
            @Override
            public Tuple2<String, Long>
            run(byte[] key, Tuple5<
                    Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>,
                    Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>> value) {

                // Address
                byte[] shortAddress = new byte[20];
                System.arraycopy(key, 0, shortAddress, 0, 20);
                // ASSET KEY
                byte[] assetKeyBytes = new byte[8];
                System.arraycopy(key, 20, assetKeyBytes, 0, 8);

                return new Tuple2<String, Long>(
                        Crypto.getInstance().getAddressFromShort(shortAddress),
                        Longs.fromByteArray(assetKeyBytes)
                );
            }
        });

    }

    @Override
    protected void getMemoryMap() {
        map = new TreeMap<byte[], Tuple5<
                Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>,
                Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>>(Fun.BYTE_ARRAY_COMPARATOR);
    }

    @Override
    protected Tuple5<
            Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>,
            Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>> getDefaultValue() {
        return new Tuple5<
                Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>,
                Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>
                (new Tuple2<BigDecimal, BigDecimal>(BigDecimal.ZERO, BigDecimal.ZERO),
                        new Tuple2<BigDecimal, BigDecimal>(BigDecimal.ZERO, BigDecimal.ZERO),
                        new Tuple2<BigDecimal, BigDecimal>(BigDecimal.ZERO, BigDecimal.ZERO),
                        new Tuple2<BigDecimal, BigDecimal>(BigDecimal.ZERO, BigDecimal.ZERO),
                        new Tuple2<BigDecimal, BigDecimal>(BigDecimal.ZERO, BigDecimal.ZERO));
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

    public void set(byte[] address, long key, Tuple5<
            Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>,
            Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>> value) {
        if (key < 0)
            key = -key;

        this.set(Bytes.concat(address, Longs.toByteArray(key)), value);
    }

    private Account testAcc = new Account("76ACGgH8c63VrrgEw1wQA4Dno1JuPLTsWe");
    public boolean set(byte[] key, Tuple5<
            Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>,
            Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>> value) {

        boolean test = false;
        if (testAcc.equals(key)) {
            test = true;
        }

        boolean result = super.set(key, value);

        if (test) {
            Tuple5 balance5 = get(key);
        }

        return result;

    }

	/*
	public BigDecimal get(byte[] address)
	{
		return this.get(address, FEE_KEY);
	}
	 */

    public Tuple5<
            Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>,
            Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>> get(byte[] address, long key) {
        if (key < 0)
            key = -key;


        Tuple5<
                Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>,
                Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>> value = this.get(
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
    public SortableList<byte[], Tuple5<
            Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>,
            Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>> getBalancesSortableList(long key) {

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
        return new SortableList<byte[], Tuple5<
                Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>,
                Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>>(this, keys);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public SortableList<byte[], Tuple5<
            Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>,
            Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>> getBalancesSortableList(Account account) {

        if (Controller.getInstance().onlyProtocolIndexing)
            return null;

        //FILTER ALL KEYS
        Collection<byte[]> keys = this.addressKeyMap.subMap(
                Fun.t2(account.getAddress(), null),
                Fun.t2(account.getAddress(), Fun.HI())).values();

        int tt = keys.size();
        //RETURN
        return new SortableList<byte[], Tuple5<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>,
                Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>>(this, keys);
    }

}
