package database;

//import java.math.Long;
import java.util.Collection;
import java.util.List;
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

public class ItemStatusTimeMap extends DBMap<Tuple2<String, Long>, List<Long>> 
{
	public static final long ALIVE_KEY = StatusCls.ALIVE_KEY;
	
	private Map<Integer, Integer> observableData = new HashMap<Integer, Integer>();
	
	@SuppressWarnings("rawtypes")
	private BTreeMap statusKeyMap;
	
	public ItemStatusTimeMap(DBSet databaseSet, DB database)
	{
		super(databaseSet, database);
		
		this.observableData.put(DBMap.NOTIFY_ADD, ObserverMessage.ADD_BALANCE_TYPE);
		this.observableData.put(DBMap.NOTIFY_REMOVE, ObserverMessage.REMOVE_BALANCE_TYPE);
		//this.observableData.put(DBMap.NOTIFY_LIST, ObserverMessage.LIST_BALANCE_TYPE);
	}

	public ItemStatusTimeMap(ItemStatusTimeMap parent) 
	{
		super(parent);
	}
	
	protected void createIndexes(DB database){}

	@SuppressWarnings({ "unchecked"})
	@Override
	protected Map<Tuple2<String, Long>, List<Long>> getMap(DB database) 
	{
		//OPEN MAP
		BTreeMap<Tuple2<String, Long>, List<Long>> map =  database.createTreeMap("status_times")
				.keySerializer(BTreeKeySerializer.TUPLE2)
				.counterEnable()
				.makeOrGet();
		
		//HAVE/WANT KEY
		this.statusKeyMap = database.createTreeMap("times_key_status")
				.comparator(Fun.COMPARATOR)
				.counterEnable()
				.makeOrGet();
		
		/*
		//BIND STATUS KEY
		Bind.secondaryKey(map, this.statusKeyMap, new Fun.Function2<Tuple3<Long, Long, String>, Tuple2<String, Long>, List<Long>>() {
			@Override
			public Tuple3<Long, Long, String> run(Tuple2<String, Long> key, List<Long> value) {
				return new Tuple3<Long, List<Long>, String>(key.b, value, key.a);
			}	
		});
		*/
		
		//RETURN
		return map;
	}

	@Override
	protected Map<Tuple2<String, Long>, List<Long>> getMemoryMap() 
	{
		return new TreeMap<Tuple2<String, Long>, List<Long>>(Fun.TUPLE2_COMPARATOR);
	}

	@Override
	protected List<Long> getDefaultValue() 
	{
		//List<Long> times = Arrays.asList();
		return new ArrayList<Long>();
	}
	
	@Override
	protected Map<Integer, Integer> getObservableData() 
	{
		return this.observableData;
	}
	
	public void set(String address, List<Long> value)
	{
		this.set(address, ALIVE_KEY, value);
	}
	
	public void set(String address, long key, List<Long> value)
	{
		this.set(new Tuple2<String, Long>(address, key), value);
	}
	
	public List<Long> get(String address)
	{
		return this.get(address, ALIVE_KEY);
	}
	
	public List<Long> get(String address, long key)
	{
		return this.get(new Tuple2<String, Long>(address, key));
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public SortableList<Tuple2<String, Long>, List<Long>> getTimesSortableList(long key)
	{
		//FILTER ALL KEYS
		Collection<Tuple2<String, Long>> keys = ((BTreeMap<Tuple3, Tuple2<String, Long>>) this.statusKeyMap).subMap(
				Fun.t3(key, null, null),
				Fun.t3(key, Fun.HI(), Fun.HI())).values();
		
		//RETURN
		return new SortableList<Tuple2<String, Long>, List<Long>>(this, keys);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public SortableList<Tuple2<String, Long>, List<Long>> getTimesSortableList(Account account) 
	{
		BTreeMap map = (BTreeMap) this.map;
		
		//FILTER ALL KEYS
		Collection keys = ((BTreeMap<Tuple2, Long>) map).subMap(
				Fun.t2(account.getAddress(), null),
				Fun.t2(account.getAddress(), Fun.HI())).keySet();
		
		//RETURN
		return new SortableList<Tuple2<String, Long>, List<Long>>(this, keys);
	}
}
