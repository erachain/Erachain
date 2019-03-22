package org.erachain.datachain;

import org.erachain.controller.Controller;
import org.erachain.core.account.Account;
import org.erachain.database.DBMap;
import org.erachain.database.SortableList;
import org.erachain.utils.ObserverMessage;
import org.mapdb.*;
import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple3;
import org.mapdb.Fun.Tuple5;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
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

public class ItemAssetBalanceMap extends DCMap<Tuple2<String, Long>, Tuple5<
        Tuple2<BigDecimal, BigDecimal>, // in OWN - total INCOMED + BALANCE
        Tuple2<BigDecimal, BigDecimal>, // in DEBT
        Tuple2<BigDecimal, BigDecimal>, // in STOCK
        Tuple2<BigDecimal, BigDecimal>, // it DO
        Tuple2<BigDecimal, BigDecimal>  // on HOLD
        >> {

    @SuppressWarnings("rawtypes")
    private BTreeMap assetKeyMap;

    public ItemAssetBalanceMap(DCSet databaseSet, DB database) {
        super(databaseSet, database);

        if (databaseSet.isWithObserver()) {
            this.observableData.put(DBMap.NOTIFY_RESET, ObserverMessage.RESET_BALANCE_TYPE);
            this.observableData.put(DBMap.NOTIFY_LIST, ObserverMessage.LIST_BALANCE_TYPE);
            if (databaseSet.isDynamicGUI()) {
                this.observableData.put(DBMap.NOTIFY_ADD, ObserverMessage.ADD_BALANCE_TYPE);
                this.observableData.put(DBMap.NOTIFY_REMOVE, ObserverMessage.REMOVE_BALANCE_TYPE);
            }
        }
    }

    public ItemAssetBalanceMap(ItemAssetBalanceMap parent) {
        super(parent, null);
    }

    @Override
    protected void createIndexes(DB database) {
    }

    @SuppressWarnings({"unchecked"})
    @Override
    protected Map<Tuple2<String, Long>, Tuple5<
            Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>,
            Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>> getMap(DB database) {
        //OPEN MAP
        BTreeMap<Tuple2<String, Long>, Tuple5<
                Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>,
                Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>> map = database.createTreeMap("balances")
                .keySerializer(BTreeKeySerializer.TUPLE2)
                .counterEnable()
                .makeOrGet();

        if (Controller.getInstance().onlyProtocolIndexing)
            // NOT USE SECONDARY INDEXES
            return map;


        //HAVE/WANT KEY
        this.assetKeyMap = database.createTreeMap("balances_key_asset")
                .comparator(Fun.COMPARATOR)
                .counterEnable()
                .makeOrGet();

        //BIND ASSET KEY
		/*
		Bind.secondaryKey(map, this.assetKeyMap, new Fun.Function2<Tuple3<Long, BigDecimal, String>, Tuple2<String, Long>, BigDecimal>() {
			@Override
			public Tuple3<Long, BigDecimal, String> run(Tuple2<String, Long> key, BigDecimal value) {
				return new Tuple3<Long, BigDecimal, String>(key.b, value.negate(), key.a);
			}
		});*/
        Bind.secondaryKey(map, this.assetKeyMap, new Fun.Function2<Tuple3<Long,
                Tuple5<
                        Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>,
                        Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>,
                String>,
                Tuple2<String, Long>,
                Tuple5<
                        Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>,
                        Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>>
                () {
            @Override
            public Tuple3<Long, Tuple5<
                    Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>,
                    Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>, String>
            run(Tuple2<String, Long> key, Tuple5<
                    Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>,
                    Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>> value) {
                return new Tuple3<Long, Tuple5<
                        Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>,
                        Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>, String>(
                        key.b, new Tuple5<
                        Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>,
                        Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>
                        (new Tuple2<BigDecimal, BigDecimal>(value.a.a.negate(), value.a.b.negate()),
                                new Tuple2<BigDecimal, BigDecimal>(value.b.a.negate(), value.b.b.negate()),
                                new Tuple2<BigDecimal, BigDecimal>(value.c.a.negate(), value.c.b.negate()),
                                new Tuple2<BigDecimal, BigDecimal>(value.d.a.negate(), value.d.b.negate()),
                                new Tuple2<BigDecimal, BigDecimal>(value.e.a.negate(), value.e.b.negate())),
                        key.a);
            }
        });

        //RETURN
        return map;
    }

    @Override
    protected Map<Tuple2<String, Long>, Tuple5<
            Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>,
            Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>> getMemoryMap() {
        return new TreeMap<Tuple2<String, Long>, Tuple5<
                Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>,
                Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>>(Fun.TUPLE2_COMPARATOR);
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
	public void set(String address, BigDecimal value)
	{
		this.set(address, FEE_KEY, value);
	}
	 */

    public void set(String address, long key, Tuple5<
            Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>,
            Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>> value) {
        if (key < 0)
            key = -key;

        this.set(new Tuple2<String, Long>(address, key), value);
    }

	/*
	public BigDecimal get(String address)
	{
		return this.get(address, FEE_KEY);
	}
	 */

    public Tuple5<
            Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>,
            Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>> get(String address, long key) {
        if (key < 0)
            key = -key;


        Tuple5<
                Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>,
                Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>> value = this.get(new Tuple2<String, Long>(address, key));

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
    public SortableList<Tuple2<String, Long>, Tuple5<
            Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>,
            Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>> getBalancesSortableList(long key) {
        if (key < 0)
            key = -key;

        //FILTER ALL KEYS
        Collection<Tuple2<String, Long>> keys = ((BTreeMap<Tuple3, Tuple2<String, Long>>) this.assetKeyMap).subMap(
                Fun.t3(key, null, null),
                Fun.t3(key, Fun.HI(), Fun.HI())).values();

        //RETURN
        return new SortableList<Tuple2<String, Long>, Tuple5<
                Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>,
                Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>>(this, keys);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public SortableList<Tuple2<String, Long>, Tuple5<
            Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>,
            Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>> getBalancesSortableList(Account account) {
        BTreeMap map = (BTreeMap) this.map;

        //FILTER ALL KEYS
        Collection keys = ((BTreeMap<Tuple2, BigDecimal>) map).subMap(
                Fun.t2(account.getAddress(), null),
                Fun.t2(account.getAddress(), Fun.HI())).keySet();

        // TODO - ERROR PARENT not userd!

        //RETURN
        return new SortableList<Tuple2<String, Long>, Tuple5<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>>(this, keys);
    }

}
