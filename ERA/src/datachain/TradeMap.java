package datachain;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
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
import org.mapdb.Fun.Tuple5;

import core.item.assets.Order;
import core.item.assets.Trade;
import database.DBMap;
import utils.ObserverMessage;

/*
 *
 * private BigInteger initiator;
	private BigInteger target;
	private BigDecimal amountHave;
	private BigDecimal amountWant;
	private long timestamp;
 */
@SuppressWarnings("rawtypes")
public class TradeMap extends DCMap<Tuple2<BigInteger, BigInteger>,
Tuple5<BigInteger, BigInteger, BigDecimal, BigDecimal, Long>>
{
	private Map<Integer, Integer> observableData = new HashMap<Integer, Integer>();

	private BTreeMap pairKeyMap;
	private BTreeMap wantKeyMap;
	private BTreeMap haveKeyMap;
	private BTreeMap reverseKeyMap;

	public TradeMap(DCSet databaseSet, DB database)
	{
		super(databaseSet, database);

		if (databaseSet.isWithObserver()) {
			this.observableData.put(DBMap.NOTIFY_RESET, ObserverMessage.RESET_TRADE_TYPE);
			if (databaseSet.isDynamicGUI()) {
				this.observableData.put(DBMap.NOTIFY_ADD, ObserverMessage.ADD_TRADE_TYPE);
				this.observableData.put(DBMap.NOTIFY_REMOVE, ObserverMessage.REMOVE_TRADE_TYPE);
			}
			this.observableData.put(DBMap.NOTIFY_LIST, ObserverMessage.LIST_ORDER_TYPE);
		}
	}

	public TradeMap(TradeMap parent, DCSet dcSet)
	{
		super(parent, dcSet);

	}

	@Override
	protected void createIndexes(DB database){}

	@Override
	protected Map<Tuple2<BigInteger, BigInteger>, Tuple5<BigInteger, BigInteger, BigDecimal, BigDecimal, Long>> getMap(DB database)
	{
		//OPEN MAP
		return this.openMap(database);
	}

	@Override
	protected Map<Tuple2<BigInteger, BigInteger>, Tuple5<BigInteger, BigInteger, BigDecimal, BigDecimal, Long>> getMemoryMap()
	{
		DB database = DBMaker.newMemoryDB().make();

		//OPEN MAP
		return this.openMap(database);
	}

	@SuppressWarnings("unchecked")
	private Map<Tuple2<BigInteger, BigInteger>, Tuple5<BigInteger, BigInteger, BigDecimal, BigDecimal, Long>> openMap(final DB database)
	{
		//OPEN MAP
		BTreeMap<Tuple2<BigInteger, BigInteger>, Tuple5<BigInteger, BigInteger, BigDecimal, BigDecimal, Long>> map = database.createTreeMap("trades")
				//.valueSerializer(new TradeSerializer())
				.makeOrGet();

		//CHECK IF NOT MEMORY DATABASE
		if(parent == null)
		{
			//PAIR KEY
			this.pairKeyMap = database.createTreeMap("trades_key_pair")
					.comparator(Fun.COMPARATOR)
					.makeOrGet();

			//BIND PAIR KEY
			Bind.secondaryKey(map, this.pairKeyMap, new Fun.Function2<Tuple3<String, Long, Tuple2<BigInteger, BigInteger>>, Tuple2<BigInteger, BigInteger>, Tuple5<BigInteger, BigInteger, BigDecimal, BigDecimal, Long>>()
			{
				@Override
				public Tuple3<String, Long, Tuple2<BigInteger, BigInteger>> run(Tuple2<BigInteger, BigInteger> key, Tuple5<BigInteger, BigInteger, BigDecimal, BigDecimal, Long> value)
				{
					Tuple3<Tuple5<BigInteger, String, Long, Boolean, BigDecimal>,
					Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>> order = Order.getOrder(getDCSet(), value.a);
					long have = order.b.a;
					long want = order.c.a;
					String pairKey;
					if(have > want)
					{
						pairKey = have + "/" + want;
					}
					else
					{
						pairKey = want + "/" + have;
					}

					return new Tuple3<String, Long, Tuple2<BigInteger, BigInteger>>(pairKey, Long.MAX_VALUE - value.e, key);
				}
			});

			//
			this.wantKeyMap = database.createTreeMap("trades_key_want")
					.comparator(Fun.COMPARATOR)
					.makeOrGet();

			//BIND
			Bind.secondaryKey(map, this.wantKeyMap, new Fun.Function2<Tuple3<String, Long, Tuple2<BigInteger, BigInteger>>, Tuple2<BigInteger, BigInteger>, Tuple5<BigInteger, BigInteger, BigDecimal, BigDecimal, Long>>()
			{
				@Override
				public Tuple3<String, Long, Tuple2<BigInteger, BigInteger>> run(Tuple2<BigInteger, BigInteger> key, Tuple5<BigInteger, BigInteger, BigDecimal, BigDecimal, Long> value)
				{
					Tuple3<Tuple5<BigInteger, String, Long, Boolean, BigDecimal>,
					Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>> order = Order.getOrder(getDCSet(), value.a);
					long want = order.c.a;

					String wantKey;
					wantKey = String.valueOf(want);

					return new Tuple3<String, Long, Tuple2<BigInteger, BigInteger>>(wantKey, Long.MAX_VALUE - value.e, key);
				}
			});

			//
			this.haveKeyMap = database.createTreeMap("trades_key_have")
					.comparator(Fun.COMPARATOR)
					.makeOrGet();

			//BIND
			Bind.secondaryKey(map, this.haveKeyMap, new Fun.Function2<Tuple3<String, Long, Tuple2<BigInteger, BigInteger>>, Tuple2<BigInteger, BigInteger>, Tuple5<BigInteger, BigInteger, BigDecimal, BigDecimal, Long>>()
			{
				@Override
				public Tuple3<String, Long, Tuple2<BigInteger, BigInteger>> run(Tuple2<BigInteger, BigInteger> key, Tuple5<BigInteger, BigInteger, BigDecimal, BigDecimal, Long> value)
				{
					Tuple3<Tuple5<BigInteger, String, Long, Boolean, BigDecimal>,
					Tuple3<Long, BigDecimal, BigDecimal>, Tuple2<Long, BigDecimal>> order = Order.getOrder(getDCSet(), value.a);
					long have = order.b.a;

					String haveKey;
					haveKey = String.valueOf(have);

					return new Tuple3<String, Long, Tuple2<BigInteger, BigInteger>>(haveKey, Long.MAX_VALUE - value.e, key);
				}
			});

			//REVERSE KEY
			this.reverseKeyMap = database.createTreeMap("trades_key_reverse")
					.comparator(Fun.COMPARATOR)
					.makeOrGet();

			//BIND REVERSE KEY
			Bind.secondaryKey(map, this.reverseKeyMap, new Fun.Function2<Tuple2<BigInteger, BigInteger>, Tuple2<BigInteger, BigInteger>, Tuple5<BigInteger, BigInteger, BigDecimal, BigDecimal, Long>>()
			{
				@Override
				public Tuple2<BigInteger, BigInteger> run(Tuple2<BigInteger, BigInteger> key, Tuple5<BigInteger, BigInteger, BigDecimal, BigDecimal, Long> value)
				{

					return new Tuple2<BigInteger, BigInteger>(key.b, key.a);
				}
			});
			Bind.secondaryKey(map, this.reverseKeyMap, new Fun.Function2<Tuple2<BigInteger, BigInteger>, Tuple2<BigInteger, BigInteger>, Tuple5<BigInteger, BigInteger, BigDecimal, BigDecimal, Long>>()
			{
				@Override
				public Tuple2<BigInteger, BigInteger> run(Tuple2<BigInteger, BigInteger> key, Tuple5<BigInteger, BigInteger, BigDecimal, BigDecimal, Long> value)
				{
					return new Tuple2<BigInteger, BigInteger>(key.a, key.b);
				}
			});
		}

		//RETURN
		return map;
	}

	@Override
	protected Tuple5<BigInteger, BigInteger, BigDecimal, BigDecimal, Long> getDefaultValue()
	{
		return null;
	}

	@Override
	protected Map<Integer, Integer> getObservableData()
	{
		return this.observableData;
	}

	public void add(Tuple5<BigInteger, BigInteger, BigDecimal, BigDecimal, Long> trade)
	{
		this.set(new Tuple2<BigInteger, BigInteger>(trade.a, trade.b), trade);
	}

	@SuppressWarnings( "unchecked" )
	private Collection<Tuple2> getKeys(Order order) {

		Map uncastedMap = this.map;

		//FILTER ALL KEYS
		Collection<Tuple2> keys = ((BTreeMap<Tuple2, Order>) uncastedMap).subMap(
				Fun.t2(order.getId(), null),
				Fun.t2(order.getId(), Fun.HI())).keySet();

		//IF THIS IS A FORK
		if(this.parent != null)
		{
			//GET ALL KEYS FOR FORK
			Collection<Tuple2> forkKeys = ((TradeMap) this.parent).getKeys(order);

			//COMBINE LISTS
			Set<Tuple2> combinedKeys = new TreeSet<Tuple2>(keys);
			combinedKeys.addAll(forkKeys);

			if (this.deleted != null) {
				//DELETE DELETED
				for(Tuple2 deleted: this.deleted)
				{
					combinedKeys.remove(deleted);
				}
			}

			//CONVERT SET BACK TO COLLECTION
			keys = combinedKeys;
		}

		return keys;
	}

	@SuppressWarnings( "unchecked")
	public List<Tuple5<BigInteger, BigInteger, BigDecimal, BigDecimal, Long>> getInitiatedTrades(Order order)
	{
		//FILTER ALL TRADES
		Collection<Tuple2> keys = this.getKeys(order);

		//GET ALL TRADES FOR KEYS
		List<Tuple5<BigInteger, BigInteger, BigDecimal, BigDecimal, Long>> trades = new ArrayList<Tuple5<BigInteger, BigInteger, BigDecimal, BigDecimal, Long>>();
		for(Tuple2 key: keys)
		{
			trades.add(this.get(key));
		}

		//RETURN
		return trades;
	}

	@SuppressWarnings( "unchecked" )
	public SortableList<Tuple2<BigInteger, BigInteger>, Tuple5<BigInteger, BigInteger, BigDecimal, BigDecimal, Long>> getTrades(BigInteger orderID)
	{
		//ADD REVERSE KEYS
		Collection<Tuple2<BigInteger, BigInteger>> keys = ((BTreeMap<Tuple2, Tuple2<BigInteger, BigInteger>>) this.reverseKeyMap).subMap(
				Fun.t2(orderID, null),
				Fun.t2(orderID, Fun.HI())).values();

		//RETURN
		return new SortableList<Tuple2<BigInteger, BigInteger>, Tuple5<BigInteger, BigInteger, BigDecimal, BigDecimal, Long>>(this, keys);
	}

	@SuppressWarnings( "unchecked" )
	public SortableList<Tuple2<BigInteger, BigInteger>, Tuple5<BigInteger, BigInteger, BigDecimal, BigDecimal, Long>> getTradesByOrderID(BigInteger orderID)
	{
		//ADD REVERSE KEYS
		Collection<Tuple2<BigInteger, BigInteger>> keys = ((BTreeMap<Tuple2, Tuple2<BigInteger, BigInteger>>) this.reverseKeyMap).subMap(
				Fun.t2(orderID, null),
				Fun.t2(orderID, Fun.HI())).values();

		//RETURN
		return new SortableList<Tuple2<BigInteger, BigInteger>, Tuple5<BigInteger, BigInteger, BigDecimal, BigDecimal, Long>>(this, keys);
	}


	@SuppressWarnings("unchecked")
	public List<Tuple5<BigInteger, BigInteger, BigDecimal, BigDecimal, Long>> getTrades(long haveWant)
	// get trades for order as HAVE and as WANT
	{
		Map<Tuple2<BigInteger, BigInteger>, Boolean> tradesKeys = new TreeMap<Tuple2<BigInteger, BigInteger>, Boolean>();

		String haveKey = String.valueOf(haveWant);
		Collection<Tuple2<BigInteger, BigInteger>> keys = ((BTreeMap<Tuple3, Tuple2<BigInteger, BigInteger>>) this.haveKeyMap).subMap(
				Fun.t3(haveKey, null, null),
				Fun.t3(haveKey, Fun.HI(), Fun.HI())).values();

		for (Tuple2<BigInteger, BigInteger> key : keys) {
			tradesKeys.put(key, true);
		}

		String wantKey = String.valueOf(haveWant);
		keys = ((BTreeMap<Tuple3, Tuple2<BigInteger, BigInteger>>) this.wantKeyMap).subMap(
				Fun.t3(wantKey, null, null),
				Fun.t3(wantKey, Fun.HI(), Fun.HI())).values();

		for (Tuple2<BigInteger, BigInteger> key : keys) {
			tradesKeys.put(key, true);
		}

		//GET ALL ORDERS FOR KEYS
		List<Tuple5<BigInteger, BigInteger, BigDecimal, BigDecimal, Long>> trades = new ArrayList<Tuple5<BigInteger, BigInteger, BigDecimal, BigDecimal, Long>>();

		for(Map.Entry<Tuple2<BigInteger, BigInteger>, Boolean> tradeKey : tradesKeys.entrySet())
		{
			trades.add(this.get(tradeKey.getKey()));
		}

		//RETURN
		return trades;
	}

	@SuppressWarnings( "unchecked" )
	public SortableList<Tuple2<BigInteger, BigInteger>, Tuple5<BigInteger, BigInteger, BigDecimal, BigDecimal, Long>> getTradesSortableList(long have, long want)
	{
		String pairKey;
		if(have > want)
		{
			pairKey = have + "/" + want;
		}
		else
		{
			pairKey = want + "/" + have;
		}

		//FILTER ALL KEYS
		Collection<Tuple2<BigInteger, BigInteger>> keys = ((BTreeMap<Tuple3, Tuple2<BigInteger, BigInteger>>) this.pairKeyMap).subMap(
				Fun.t3(pairKey, null, null),
				Fun.t3(pairKey, Fun.HI(), Fun.HI())).values();

		//RETURN
		return new SortableList<Tuple2<BigInteger, BigInteger>, Tuple5<BigInteger, BigInteger, BigDecimal, BigDecimal, Long>>(this, keys);
	}

	@SuppressWarnings( "unchecked" )
	public List<Tuple5<BigInteger, BigInteger, BigDecimal, BigDecimal, Long>> getTrades(long have, long want)
	{
		String pairKey;
		if(have > want)
		{
			pairKey = have + "/" + want;
		}
		else
		{
			pairKey = want + "/" + have;
		}

		//FILTER ALL KEYS
		Collection<Tuple2<BigInteger, BigInteger>> keys = ((BTreeMap<Tuple3, Tuple2<BigInteger, BigInteger>>) this.pairKeyMap).subMap(
				Fun.t3(pairKey, null, null),
				Fun.t3(pairKey, Fun.HI(), Fun.HI())).values();

		List<Tuple5<BigInteger, BigInteger, BigDecimal, BigDecimal, Long>> trades = new ArrayList<Tuple5<BigInteger, BigInteger, BigDecimal, BigDecimal, Long>>();

		for (Tuple2<BigInteger, BigInteger> key : keys) {
			trades.add(this.get(key));
		}

		//RETURN
		return trades;
	}
	/**
	 * Get transaction by timestamp
	 * @param have include
	 * @param want wish
	 * @param timestamp is time
	 */
	public List<Tuple5<BigInteger, BigInteger, BigDecimal, BigDecimal, Long>> getTradesByTimestamp(long have, long want, long timestamp)
	{
		String pairKey;
		if (have > want)
			pairKey = have + "/" + want;
		else
			pairKey = want + "/" + have;

		//FILTER ALL KEYS
		Collection<Tuple2<BigInteger, BigInteger>> keys = ((BTreeMap<Tuple3, Tuple2<BigInteger, BigInteger>>) this.pairKeyMap).subMap(
				Fun.t3(pairKey, timestamp, null),
				Fun.t3(pairKey, Fun.HI(), Fun.HI())).values();

		List<Tuple5<BigInteger, BigInteger, BigDecimal, BigDecimal, Long>> trades = new ArrayList<Tuple5<BigInteger, BigInteger, BigDecimal, BigDecimal, Long>>();

		for (Tuple2<BigInteger, BigInteger> key : keys) {
			trades.add(this.get(key));
		}

		//RETURN
		return trades;
	}
	public void delete(Trade trade)
	{
		this.delete(new Tuple2<BigInteger, BigInteger>(trade.getInitiator(), trade.getTarget()));
	}
}
