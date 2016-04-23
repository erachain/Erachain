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

public class PersonStatusMap extends DBMap<Tuple2<Long, Long>, Long> 
{
	public static final long ALIVE_KEY = StatusCls.ALIVE_KEY;
	
	private Map<Integer, Integer> observableData = new HashMap<Integer, Integer>();
	
	@SuppressWarnings("rawtypes")
	private BTreeMap statusKeyMap;
	
	public PersonStatusMap(DBSet databaseSet, DB database)
	{
		super(databaseSet, database);
		
		this.observableData.put(DBMap.NOTIFY_ADD, ObserverMessage.ADD_PERSON_STATUS_TYPE);
		this.observableData.put(DBMap.NOTIFY_REMOVE, ObserverMessage.REMOVE_PERSON_STATUS_TYPE);
		//this.observableData.put(DBMap.NOTIFY_LIST, ObserverMessage.LIST_PERSON_STATUSTYPE);
	}

	public PersonStatusMap(PersonStatusMap parent) 
	{
		super(parent);
	}
	
	protected void createIndexes(DB database){}

	@SuppressWarnings({ "unchecked"})
	@Override
	protected Map<Tuple2<Long, Long>, Long> getMap(DB database) 
	{
		//OPEN MAP
		BTreeMap<Tuple2<Long, Long>, Long> map =  database.createTreeMap("person_status")
				.keySerializer(BTreeKeySerializer.TUPLE2)
				.counterEnable()
				.makeOrGet();
		
		//HAVE/WANT KEY
		this.statusKeyMap = database.createTreeMap("person_status_key")
				.comparator(Fun.COMPARATOR)
				.counterEnable()
				.makeOrGet();
		
		//BIND STATUS KEY
		Bind.secondaryKey(map, this.statusKeyMap, new Fun.Function2<Tuple3<Long, Long, Long>, Tuple2<Long, Long>, Long>() {
			@Override
			public Tuple3<Long, Long, Long> run(Tuple2<Long, Long> key, Long value) {
				return new Tuple3<Long, Long, Long>(key.b, -value, key.a);
			}	
		});
		
		//RETURN
		return map;
	}

	@Override
	protected Map<Tuple2<Long, Long>, Long> getMemoryMap() 
	{
		return new TreeMap<Tuple2<Long, Long>, Long>(Fun.TUPLE2_COMPARATOR);
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
		
	public void set(long person, long status, Long value)
	{
		this.set(new Tuple2<Long, Long>(person, status), value);
	}
	public void set(long person, Long value)
	{
		this.set(new Tuple2<Long, Long>(person, ALIVE_KEY), value);
	}
	
	public Long get(long person)
	{
		return this.get(person, ALIVE_KEY);
	}
	
	public Long get(long person, long key)
	{
		return this.get(new Tuple2<Long, Long>(person, key));
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public SortableList<Tuple2<Long, Long>, Long> getBalancesSortableList(long key)
	{
		//FILTER ALL KEYS
		Collection<Tuple2<Long, Long>> keys = ((BTreeMap<Tuple3, Tuple2<Long, Long>>) this.statusKeyMap).subMap(
				Fun.t3(key, null, null),
				Fun.t3(key, Fun.HI(), Fun.HI())).values();
		
		//RETURN
		return new SortableList<Tuple2<Long, Long>, Long>(this, keys);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public SortableList<Tuple2<Long, Long>, Long> getBalancesSortableList(Account account) 
	{
		BTreeMap map = (BTreeMap) this.map;
		
		//FILTER ALL KEYS
		Collection keys = ((BTreeMap<Tuple2, Long>) map).subMap(
				Fun.t2(account.getAddress(), null),
				Fun.t2(account.getAddress(), Fun.HI())).keySet();
		
		//RETURN
		return new SortableList<Tuple2<Long, Long>, Long>(this, keys);
	}
}
