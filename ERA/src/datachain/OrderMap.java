package datachain;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.mapdb.BTreeMap;
import org.mapdb.Bind;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Fun;
import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple3;
import org.mapdb.Fun.Tuple4;
import org.mapdb.Fun.Tuple5;

import core.item.assets.Order;
import database.DBMap;
import utils.ObserverMessage;

/*
 * Tuple5
 * 	private BigInteger id;
	private Account creator;
	protected long timestamp;
	private boolean isExecutable = true;
	private BigDecimal Price;

Tuple3
	private long have;
	private BigDecimal amountHave;
	private BigDecimal fulfilledHave;

Tuple3
	private long want;
	private BigDecimal amountWant;
	private BigDecimal fulfilledWant;
 */

public class OrderMap extends DCMap<BigInteger,
Tuple3<Tuple5<BigInteger, String, Long, Boolean, BigDecimal>,
Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>>>
{
	private Map<Integer, Integer> observableData = new HashMap<Integer, Integer>();

	@SuppressWarnings("rawtypes")
	private BTreeMap haveWantKeyMap;
	@SuppressWarnings("rawtypes")
	private BTreeMap wantHaveKeyMap;

	public OrderMap(DCSet databaseSet, DB database)
	{
		super(databaseSet, database);

		if (databaseSet.isWithObserver()) {
			this.observableData.put(DBMap.NOTIFY_RESET, ObserverMessage.RESET_ORDER_TYPE);
			if (databaseSet.isDynamicGUI()) {
				this.observableData.put(DBMap.NOTIFY_ADD, ObserverMessage.ADD_ORDER_TYPE);
				this.observableData.put(DBMap.NOTIFY_REMOVE, ObserverMessage.REMOVE_ORDER_TYPE);
			}
			this.observableData.put(DBMap.NOTIFY_LIST, ObserverMessage.LIST_ORDER_TYPE);
		}
	}

	public OrderMap(OrderMap parent, DCSet dcSet)
	{
		super(parent, dcSet);

	}

	@Override
	protected void createIndexes(DB database){}

	@Override
	protected Map<BigInteger, Tuple3<Tuple5<BigInteger, String, Long, Boolean, BigDecimal>,
	Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>>> getMap(DB database)
	{
		//OPEN MAP
		return this.openMap(database);
	}

	@Override
	protected Map<BigInteger, Tuple3<Tuple5<BigInteger, String, Long, Boolean, BigDecimal>,
	Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>>> getMemoryMap()
	{
		DB database = DBMaker.newMemoryDB().make();

		//OPEN MAP
		return this.openMap(database);
	}

	@SuppressWarnings("unchecked")
	private Map<BigInteger, Tuple3<Tuple5<BigInteger, String, Long, Boolean, BigDecimal>,
	Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>>> openMap(DB database)
	{
		//OPEN MAP
		BTreeMap<BigInteger, Tuple3<Tuple5<BigInteger, String, Long, Boolean, BigDecimal>,
		Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>>> map = database.createTreeMap("orders")
		//.valueSerializer(new OrderSerializer())
		.makeOrGet();

		//HAVE/WANT KEY
		this.haveWantKeyMap = database.createTreeMap("orders_key_have_want")
				.comparator(Fun.COMPARATOR)
				.makeOrGet();

		//BIND HAVE/WANT KEY
		Bind.secondaryKey(map, this.haveWantKeyMap,
				new Fun.Function2<Tuple4<Long, Long, BigDecimal, BigInteger>, BigInteger,
				Tuple3<Tuple5<BigInteger, String, Long, Boolean, BigDecimal>,
				Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>>>() {
			@Override
			public Tuple4<Long, Long, BigDecimal, BigInteger> run(BigInteger key,
					Tuple3<Tuple5<BigInteger, String, Long, Boolean, BigDecimal>,
					Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>> value) {
				return new Tuple4<Long, Long, BigDecimal, BigInteger>(value.b.a, value.c.a,
						Order.calcPrice(value.b.b, value.c.b), key);
			}
		});

		// WANT/HAVE KEY
		this.wantHaveKeyMap = database.createTreeMap("orders_key_want_have")
				.comparator(Fun.COMPARATOR)
				.makeOrGet();

		//BIND HAVE/WANT KEY
		Bind.secondaryKey(map, this.wantHaveKeyMap, new Fun.Function2<Tuple4<Long, Long, BigDecimal, BigInteger>, BigInteger,
				Tuple3<Tuple5<BigInteger, String, Long, Boolean, BigDecimal>,
				Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>>>() {
			@Override
			public Tuple4<Long, Long, BigDecimal, BigInteger> run(BigInteger key,
					Tuple3<Tuple5<BigInteger, String, Long, Boolean, BigDecimal>,
					Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>> value) {
				return new Tuple4<Long, Long, BigDecimal, BigInteger>(value.c.a, value.b.a,
						Order.calcPrice(value.b.b, value.c.b), key);
			}
		});


		//RETURN
		return map;
	}

	@Override
	protected Tuple3<Tuple5<BigInteger, String, Long, Boolean, BigDecimal>,
	Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>> getDefaultValue()
	{
		return null;
	}

	@Override
	protected Map<Integer, Integer> getObservableData()
	{
		return this.observableData;
	}

	public static Tuple3<Tuple5<BigInteger, String, Long, Boolean, BigDecimal>,
	Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>> setExecutable(Tuple3<Tuple5<BigInteger, String, Long, Boolean, BigDecimal>,
			Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>> order, boolean executable) {
		Tuple3<Tuple5<BigInteger, String, Long, Boolean, BigDecimal>,
		Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>> newOrder =	new Tuple3<Tuple5<BigInteger, String, Long, Boolean, BigDecimal>, Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>>(
				new Tuple5<BigInteger, String, Long, Boolean, BigDecimal>(order.a.a, order.a.b, order.a.c, executable, order.a.e),
				order.b, order.c);
		return newOrder;
	}

	public void add(Tuple3<Tuple5<BigInteger, String, Long, Boolean, BigDecimal>,
			Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>> order) {

		// this order is NOT executable
		this.set(order.a.a, setExecutable(order, true));
	}

	// GET KEYs with FORKED rules
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected Collection<BigInteger> getSubKeysWithParent(long have, long want) {

		//FILTER ALL KEYS
		Collection<BigInteger> keys = ((BTreeMap<Tuple4, BigInteger>) this.haveWantKeyMap).subMap(
				//Fun.t4(have, want, null, null),
				Fun.t4(have, want, null, null),
				Fun.t4(have, want, Fun.HI(), Fun.HI())).values();

		//IF THIS IS A FORK
		if(this.parent != null)
		{

			//GET ALL KEYS FOR FORK in PARENT
			Collection<BigInteger> parentKeys = ((OrderMap) this.parent).getSubKeysWithParent(have, want);

			// REMOVE those who DELETED here
			if (this.deleted != null) {
				//DELETE DELETED
				for(BigInteger deleted: this.deleted)
				{
					parentKeys.remove(deleted);
				}
			}

			//COMBINE LISTS
			Set<BigInteger> combinedKeys = new TreeSet<BigInteger>(keys);
			combinedKeys.addAll(parentKeys);

			//CONVERT SET BACK TO COLLECTION
			keys = combinedKeys;

		}

		return keys;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Collection<BigInteger> getKeysHave(long have) {

		//FILTER ALL KEYS
		Collection<BigInteger> keys = ((BTreeMap<Tuple4, BigInteger>) this.haveWantKeyMap).subMap(
				Fun.t4(have, null, null, null),
				Fun.t4(have, Fun.HI(), Fun.HI(), Fun.HI())).values();

		return keys;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Collection<BigInteger> getKeysWant(long want) {

		//FILTER ALL KEYS
		Collection<BigInteger> keys = ((BTreeMap<Tuple4, BigInteger>) this.wantHaveKeyMap).subMap(
				Fun.t4(want, null, null, null),
				Fun.t4(want, Fun.HI(), Fun.HI(), Fun.HI())).values();

		return keys;
	}

	public List<Tuple3<Tuple5<BigInteger, String, Long, Boolean, BigDecimal>,
	Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>>> getOrders(long haveWant)
	{
		return getOrders(haveWant, false);
	}

	public List<Tuple3<Tuple5<BigInteger, String, Long, Boolean, BigDecimal>,
	Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>>> getOrders(long haveWant, boolean filter)
	{
		Map<BigInteger, Boolean> orderKeys = new TreeMap<BigInteger, Boolean>();

		//FILTER ALL KEYS
		Collection<BigInteger> keys = this.getKeysHave(haveWant);

		for (BigInteger key : keys) {
			orderKeys.put(key, true);
		}

		keys = this.getKeysWant(haveWant);

		for (BigInteger key : keys) {
			orderKeys.put(key, true);
		}

		//GET ALL ORDERS FOR KEYS
		List<Tuple3<Tuple5<BigInteger, String, Long, Boolean, BigDecimal>,
		Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>>> orders = new ArrayList<Tuple3<Tuple5<BigInteger, String, Long, Boolean, BigDecimal>,
		Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>>>();

		for(Map.Entry<BigInteger, Boolean> orderKey : orderKeys.entrySet())
		{
			//Filters orders with unacceptably small amount. These orders have not worked
			if(filter){
				if(isExecutable(getDCSet(), orderKey.getKey()))
					orders.add(this.get(orderKey.getKey()));
			}
			else
			{
				orders.add(this.get(orderKey.getKey()));
			}
		}

		//IF THIS IS A FORK
		if(this.parent != null)
		{
			//RESORT ORDERS
			Collections.sort(orders);
		}

		//RETURN
		return orders;
	}

	public boolean isExecutable(DCSet db, BigInteger key)
	{

		/* OLD
		Order order = this.get(key);

		BigDecimal increment = order.calculateBuyIncrement(order, db);
		BigDecimal amount = order.getAmountHaveLeft();
		amount = amount.subtract(amount.remainder(increment));
		return  (amount.compareTo(BigDecimal.ZERO) > 0);
		} else {

		}
		 */


		Tuple3<Tuple5<BigInteger, String, Long, Boolean, BigDecimal>,
		Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>> order = this.get(key);
		// если произведение остатка
		BigDecimal left = order.b.b.subtract(order.b.c);
		if (left.signum() <= 0
				|| left.multiply(order.a.e).compareTo(BigDecimal.ONE.scaleByPowerOfTen(-order.c.b.scale())) < 0)
			return false;

		return true;

	}

	public List<Tuple3<Tuple5<BigInteger, String, Long, Boolean, BigDecimal>,
	Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>>> getOrders(long have, long want, boolean orderReverse)
	{
		//FILTER ALL KEYS
		Collection<BigInteger> keys = this.getSubKeysWithParent(have, want);

		//GET ALL ORDERS FOR KEYS
		List<Tuple3<Tuple5<BigInteger, String, Long, Boolean, BigDecimal>,
		Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>>> orders = new ArrayList<Tuple3<Tuple5<BigInteger, String, Long, Boolean, BigDecimal>,
		Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>>>();

		if (false && orderReverse) {
			for(BigInteger key: keys)
			{
				orders.add(this.get(key));
			}
		} else {
			for(BigInteger key: keys)
			{
				orders.add(this.get(key));
			}
		}

		//IF THIS IS A FORK
		if(false && this.parent != null)
		{
			//RESORT ORDERS
			Collections.sort(orders);
		}

		//RETURN
		return orders;
	}

	public SortableList<BigInteger, Tuple3<Tuple5<BigInteger, String, Long, Boolean, BigDecimal>,
	Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>>> getOrdersSortableList(long have, long want)
	{
		//RETURN
		return getOrdersSortableList(have, want, false);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public SortableList<BigInteger, Tuple3<Tuple5<BigInteger, String, Long, Boolean, BigDecimal>,
	Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>>> getOrdersSortableList(long have, long want, boolean filter)
	{
		//FILTER ALL KEYS
		Collection<BigInteger> keys;
		if (false) {
			keys = ((BTreeMap<Tuple4, BigInteger>) this.haveWantKeyMap).subMap(
					Fun.t4(have, want, null, null),
					Fun.t4(have, want, Fun.HI(), Fun.HI())).values();
		} else if (false) {
			keys = ((BTreeMap<Tuple4, BigInteger>) this.wantHaveKeyMap).subMap(
					Fun.t4(have, want, null, null),
					Fun.t4(have, want, Fun.HI(), Fun.HI())).values();
		} else if (false) {
			keys = ((BTreeMap<Tuple4, BigInteger>) this.haveWantKeyMap).subMap(
					Fun.t4(want, have, null, null),
					Fun.t4(want, have, Fun.HI(), Fun.HI())).values();
		} else {
			// CORRECT! - haveWantKeyMap LOSES some orders!
			// https://github.com/icreator/Erachain/issues/178
			keys = ((BTreeMap<Tuple4, BigInteger>) this.wantHaveKeyMap).subMap(
					Fun.t4(want, have, null, null),
					Fun.t4(want, have, Fun.HI(), Fun.HI())).values();
		}

		// 3736689355080347897954007846376144397840229205698623203564810984254622835951327166579305402606772203148742040644598072529939453854935677087096743677805573
		//Filters orders with unacceptably small amount. These orders have not worked
		if(filter) {
			List<BigInteger> keys2 = new ArrayList<BigInteger>();

			DCSet db = getDCSet();
			Iterator<BigInteger> iter = keys.iterator();
			while (iter.hasNext()) {
				BigInteger key = iter.next();
				if(isExecutable(db, key))
					keys2.add(key);
			}
			keys = keys2;
		}

		//RETURN
		return new SortableList<BigInteger, Tuple3<Tuple5<BigInteger, String, Long, Boolean, BigDecimal>,
				Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>>>(this, keys);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public SortableList<BigInteger, Tuple3<Tuple5<BigInteger, String, Long, Boolean, BigDecimal>,
	Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>>> getOrdersHaveSortableList(long have)
	{
		//FILTER ALL KEYS
		Collection<BigInteger> keys = ((BTreeMap<Tuple4, BigInteger>) this.haveWantKeyMap).subMap(
				Fun.t4(have, null, null, null),
				Fun.t4(have, Fun.HI(), Fun.HI(), Fun.HI())).values();

		//RETURN
		return new SortableList<BigInteger, Tuple3<Tuple5<BigInteger, String, Long, Boolean, BigDecimal>,
				Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>>>(this, keys);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public SortableList<BigInteger, Tuple3<Tuple5<BigInteger, String, Long, Boolean, BigDecimal>,
	Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>>> getOrdersWantSortableList(long want)
	{
		//FILTER ALL KEYS
		Collection<BigInteger> keys = ((BTreeMap<Tuple4, BigInteger>) this.haveWantKeyMap).subMap(
				Fun.t4(null, want, null, null),
				Fun.t4(Fun.HI(), want, Fun.HI(), Fun.HI())).values();

		//RETURN
		return new SortableList<BigInteger, Tuple3<Tuple5<BigInteger, String, Long, Boolean, BigDecimal>,
				Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>>>(this, keys);
	}
	@Override
	public Tuple3<Tuple5<BigInteger, String, Long, Boolean, BigDecimal>,
	Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>> get(BigInteger key)
	{
		Tuple3<Tuple5<BigInteger, String, Long, Boolean, BigDecimal>,
		Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>> order = super.get(key);
		if (order == null )
			return null;

		return setExecutable(order, true);
	}


	public void delete(Order order)
	{
		this.delete(order.getId());
	}
}
