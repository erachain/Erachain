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
import org.mapdb.Fun.Tuple4;

import core.item.assets.Order;
import database.DBMap;
import database.serializer.OrderSerializer;
import utils.ObserverMessage;

public class OrderMap extends DCMap<BigInteger, Order>
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
	protected Map<BigInteger, Order> getMap(DB database)
	{
		//OPEN MAP
		return this.openMap(database);
	}

	@Override
	protected Map<BigInteger, Order> getMemoryMap()
	{
		DB database = DBMaker.newMemoryDB().make();

		//OPEN MAP
		return this.openMap(database);
	}

	@SuppressWarnings("unchecked")
	private Map<BigInteger, Order> openMap(DB database)
	{
		//OPEN MAP
		BTreeMap<BigInteger, Order> map = database.createTreeMap("orders")
				.valueSerializer(new OrderSerializer())
				.makeOrGet();

		//HAVE/WANT KEY
		this.haveWantKeyMap = database.createTreeMap("orders_key_have_want")
				.comparator(Fun.COMPARATOR)
				.makeOrGet();

		//BIND HAVE/WANT KEY
		Bind.secondaryKey(map, this.haveWantKeyMap,
				new Fun.Function2<Tuple4<Long, Long, BigDecimal, BigInteger>, BigInteger, Order>() {
			@Override
			public Tuple4<Long, Long, BigDecimal, BigInteger> run(BigInteger key, Order value) {
				return new Tuple4<Long, Long, BigDecimal, BigInteger>(value.getHave(), value.getWant(), value.getPriceCalc(), key);
			}
		});

		// WANT/HAVE KEY
		this.wantHaveKeyMap = database.createTreeMap("orders_key_want_have")
				.comparator(Fun.COMPARATOR)
				.makeOrGet();

		//BIND HAVE/WANT KEY
		Bind.secondaryKey(map, this.wantHaveKeyMap, new Fun.Function2<Tuple4<Long, Long, BigDecimal, BigInteger>, BigInteger, Order>() {
			@Override
			public Tuple4<Long, Long, BigDecimal, BigInteger> run(BigInteger key, Order value) {
				return new Tuple4<Long, Long, BigDecimal, BigInteger>(value.getWant(), value.getHave(), value.getPriceCalc(), key);
			}
		});


		//RETURN
		return map;
	}

	@Override
	protected Order getDefaultValue()
	{
		return null;
	}

	@Override
	protected Map<Integer, Integer> getObservableData()
	{
		return this.observableData;
	}

	public void add(Order order) {

		// this order is NOT executable
		order.setExecutable(true);

		this.set(order.getId(), order);
	}

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
			if (false) { ///////////// OLD VERSION /////////
				//GET ALL KEYS FOR FORK
				Collection<BigInteger> parentKeys = ((OrderMap) this.parent).getSubKeysWithParent(have, want);

				//COMBINE LISTS
				Set<BigInteger> combinedKeys = new TreeSet<BigInteger>(keys);
				combinedKeys.addAll(parentKeys);

				if (this.deleted != null) {
					//DELETE DELETED
					for(BigInteger deleted: this.deleted)
					{
						combinedKeys.remove(deleted);
					}
				}

				//CONVERT SET BACK TO COLLECTION
				keys = combinedKeys;
			} else {

				//GET ALL KEYS FOR FORK
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

	public List<Order> getOrders(long haveWant)
	{
		return getOrders(haveWant, false);
	}

	public List<Order> getOrders(long haveWant, boolean filter)
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
		List<Order> orders = new ArrayList<Order>();

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


		Order order = this.get(key);
		if (order.getAmountHaveLeft().compareTo(BigDecimal.ZERO) <= 0)
			return false;
		BigDecimal price = order.getPriceCalcReverse();
		if (order.getAmountHaveLeft().compareTo(price) < 0)
			return false;

		/*
		BigDecimal thisPrice = order.getPriceCalc();
		boolean isReversePrice = thisPrice.compareTo(BigDecimal.ONE) < 0;
		//if (isReversePrice)
			//return order.getAmountHaveLeft().compareTo(order.getPriceCalc()) >= 0;


		//return BigDecimal.ONE.divide(order.getAmountHaveLeft(), 12,  RoundingMode.HALF_UP )
		//		.compareTo(order.getPriceCalcReverse()) >= 0;
		 */

		return order.isExecutable();

	}

	public List<Order> getOrders(long have, long want, boolean orderReverse)
	{
		//FILTER ALL KEYS
		Collection<BigInteger> keys = this.getSubKeysWithParent(have, want);

		//GET ALL ORDERS FOR KEYS
		List<Order> orders = new ArrayList<Order>();

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

	public SortableList<BigInteger, Order> getOrdersSortableList(long have, long want)
	{
		//RETURN
		return getOrdersSortableList(have, want, false);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public SortableList<BigInteger, Order> getOrdersSortableList(long have, long want, boolean filter)
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
		return new SortableList<BigInteger, Order>(this, keys);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public SortableList<BigInteger, Order> getOrdersHaveSortableList(long have)
	{
		//FILTER ALL KEYS
		Collection<BigInteger> keys = ((BTreeMap<Tuple4, BigInteger>) this.haveWantKeyMap).subMap(
				Fun.t4(have, null, null, null),
				Fun.t4(have, Fun.HI(), Fun.HI(), Fun.HI())).values();

		//RETURN
		return new SortableList<BigInteger, Order>(this, keys);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public SortableList<BigInteger, Order> getOrdersWantSortableList(long want)
	{
		//FILTER ALL KEYS
		Collection<BigInteger> keys = ((BTreeMap<Tuple4, BigInteger>) this.haveWantKeyMap).subMap(
				Fun.t4(null, want, null, null),
				Fun.t4(Fun.HI(), want, Fun.HI(), Fun.HI())).values();

		//RETURN
		return new SortableList<BigInteger, Order>(this, keys);
	}
	@Override
	public Order get(BigInteger key)
	{
		Order order = super.get(key);
		if (order != null )
			order.setExecutable(true);
		else
			LOGGER.error("*** database.OrderMap.get(BigInteger) - key[" + key + "] not found!");


		return order;
	}


	public void delete(Order order)
	{
		this.delete(order.getId());
	}
}
