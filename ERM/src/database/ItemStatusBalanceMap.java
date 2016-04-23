package database;

//import java.math.Long;
import java.util.Collection;
import java.util.List;
//import java.math.Long;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.mapdb.BTreeKeySerializer;
import org.mapdb.BTreeMap;
import org.mapdb.Bind;
import org.mapdb.DB;
import org.mapdb.Fun;
import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple3;

import core.account.Account;
import core.item.statuses.StatusCls;
import core.transaction.Transaction;
import utils.ObserverMessage;
import database.DBSet;

public class ItemStatusBalanceMap extends DBMap<Tuple2<String, Long>, Long> 
{
	public static final long ALIVE_KEY = StatusCls.ALIVE_KEY;
	
	private Map<Integer, Integer> observableData = new HashMap<Integer, Integer>();
	
	@SuppressWarnings("rawtypes")
	private BTreeMap statusKeyMap;
	
	public ItemStatusBalanceMap(DBSet databaseSet, DB database)
	{
		super(databaseSet, database);
		
		this.observableData.put(DBMap.NOTIFY_ADD, ObserverMessage.ADD_BALANCE_TYPE);
		this.observableData.put(DBMap.NOTIFY_REMOVE, ObserverMessage.REMOVE_BALANCE_TYPE);
		//this.observableData.put(DBMap.NOTIFY_LIST, ObserverMessage.LIST_BALANCE_TYPE);
	}

	public ItemStatusBalanceMap(ItemStatusBalanceMap parent) 
	{
		super(parent);
	}
	
	protected void createIndexes(DB database){}

	@SuppressWarnings({ "unchecked"})
	@Override
	protected Map<Tuple2<String, Long>, Long> getMap(DB database) 
	{
		//OPEN MAP
		BTreeMap<Tuple2<String, Long>, Long> map =  database.createTreeMap("balances_status")
				.keySerializer(BTreeKeySerializer.TUPLE2)
				.counterEnable()
				.makeOrGet();
		
		//HAVE/WANT KEY
		this.statusKeyMap = database.createTreeMap("balances_key_status")
				.comparator(Fun.COMPARATOR)
				.counterEnable()
				.makeOrGet();
		
		//BIND STATUS KEY
		Bind.secondaryKey(map, this.statusKeyMap, new Fun.Function2<Tuple3<Long, Long, String>, Tuple2<String, Long>, Long>() {
			@Override
			public Tuple3<Long, Long, String> run(Tuple2<String, Long> key, Long value) {
				return new Tuple3<Long, Long, String>(key.b, -value, key.a);
			}	
		});
		
		//RETURN
		return map;
	}

	@Override
	protected Map<Tuple2<String, Long>, Long> getMemoryMap() 
	{
		return new TreeMap<Tuple2<String, Long>, Long>(Fun.TUPLE2_COMPARATOR);
	}

	@Override
	protected Long getDefaultValue() 
	{
		return -1L;
	}
	
	@Override
	protected Map<Integer, Integer> getObservableData() 
	{
		return this.observableData;
	}
	
	public void set(String address, Long value)
	{
		this.set(address, ALIVE_KEY, value);
	}
	
	public void set(String address, long key, Long value)
	{
		this.set(new Tuple2<String, Long>(address, key), value);
	}
	
	public Long get(String address)
	{
		return this.get(address, ALIVE_KEY);
	}
	
	public Long get(String address, long key)
	{
		return this.get(new Tuple2<String, Long>(address, key));
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public SortableList<Tuple2<String, Long>, Long> getBalancesSortableList(long key)
	{
		//FILTER ALL KEYS
		Collection<Tuple2<String, Long>> keys = ((BTreeMap<Tuple3, Tuple2<String, Long>>) this.statusKeyMap).subMap(
				Fun.t3(key, null, null),
				Fun.t3(key, Fun.HI(), Fun.HI())).values();
		
		//RETURN
		return new SortableList<Tuple2<String, Long>, Long>(this, keys);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public SortableList<Tuple2<String, Long>, Long> getBalancesSortableList(Account account) 
	{
		BTreeMap map = (BTreeMap) this.map;
		
		//FILTER ALL KEYS
		Collection keys = ((BTreeMap<Tuple2, Long>) map).subMap(
				Fun.t2(account.getAddress(), null),
				Fun.t2(account.getAddress(), Fun.HI())).keySet();
		
		//RETURN
		return new SortableList<Tuple2<String, Long>, Long>(this, keys);
	}
}
