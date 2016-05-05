package database;

//import java.math.Long;
import java.util.Collection;
import java.util.List;
//import java.math.Long;
import java.util.ArrayList;
import java.util.Stack;
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

// days for ALIVE status
// minutes for others?
public class PersonStatusMap extends DBMap<
			Long, // personKey
			TreeMap<Integer, // statusKey
					Stack<Tuple3<
						Integer, // end_date
						Integer, // block.getHeight
						byte[] // transaction.getReference
				>>>>
{
	public static final Long ALIVE_KEY = StatusCls.ALIVE_KEY;
	
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
	protected Map<Long, TreeMap<Integer, Stack<Tuple3<Integer, Integer, byte[]>>>> getMap(DB database) 
	{
		//OPEN MAP
		BTreeMap<Long, TreeMap<Integer, Stack<Tuple3<Integer, Integer, byte[]>>>> map =  database.createTreeMap("person_status")
				.keySerializer(BTreeKeySerializer.BASIC)
				.counterEnable()
				.makeOrGet();
		
		/*
		//HAVE/WANT KEY
		this.statusKeyMap = database.createTreeMap("person_status_key")
				.comparator(Fun.COMPARATOR)
				.counterEnable()
				.makeOrGet();
		*/
		
		/*
		//BIND STATUS KEY
		Bind.secondaryKey(map, this.statusKeyMap, new Fun.Function2<Tuple3<Long, Integer, Long>, Tuple2<Long, Long>, Integer>() {
			@Override
			public Tuple3<Long, Integer, Long> run(Tuple2<Long, Long> key, Integer value) {
				return new Tuple3<Long, Integer, Long>(key.b, -value, key.a);
			}	
		});
		*/
		
		//RETURN
		return map;
	}

	@Override
	protected Map<Long, TreeMap<Integer, Stack<Tuple3<Integer, Integer, byte[]>>>> getMemoryMap() 
	{
		// HashMap ?
		return new TreeMap<Long, TreeMap<Integer, Stack<Tuple3<Integer, Integer, byte[]>>>>();
	}

	@Override
	protected TreeMap<Integer, Stack<Tuple3<Integer, Integer, byte[]>>> getDefaultValue() 
	{
		return new TreeMap<Integer, Stack<Tuple3<Integer, Integer, byte[]>>>();
	}
	
	@Override
	protected Map<Integer, Integer> getObservableData() 
	{
		return this.observableData;
	}

	public void addItem(Long person, Long status, Tuple3<Integer, Integer, byte[]> item)
	{

		TreeMap<Integer, Stack<Tuple3<Integer, Integer, byte[]>>> value = this.get(person);
		Stack<Tuple3<Integer, Integer, byte[]>> stack = value.get(status.intValue());
		if (stack == null) stack = new Stack<Tuple3<Integer, Integer, byte[]>>();
		
		stack.add(item);
		value.put(status.intValue(), stack);
		
		this.set(person, value);
	}
	public void addItem(Long person, Tuple3<Integer, Integer, byte[]> item)
	{
		addItem(person, ALIVE_KEY, item);
	}
	
	public Tuple3<Integer, Integer, byte[]> getItem(Long person)
	{
		return this.getItem(person, ALIVE_KEY);
	}
	
	public Tuple3<Integer, Integer, byte[]> getItem(Long person, Long status)
	{
		TreeMap<Integer, Stack<Tuple3<Integer, Integer, byte[]>>> value = this.get(person);
		Stack<Tuple3<Integer, Integer, byte[]>> stack = value.get(status.intValue());
		return stack != null? stack.size()> 0? stack.peek(): null : null;
	}
	// remove only last item from stack for this status of person
	public void removeItem(Long person, Long status)
	{
		TreeMap<Integer, Stack<Tuple3<Integer, Integer, byte[]>>> value = this.get(person);
		Stack<Tuple3<Integer, Integer, byte[]>> stack = value.get(status.intValue());
		if (stack==null) return;

		stack.pop();
		value.put(status.intValue(), stack);
		this.set(person, value);
		
	}
	public void removeItem(Long person)
	{
		this.removeItem(person, ALIVE_KEY);
	}
	
	/*
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public SortableList<Tuple2<Long, Stack<Tuple2<byte[], Integer>>> getBalancesSortableList(Long key)
	{
		//FILTER ALL KEYS
		Collection<Tuple2<Long, Long>> keys = ((BTreeMap<Tuple3, Tuple2<Long, Long>>) this.statusKeyMap).subMap(
				Fun.t3(key, null, null),
				Fun.t3(key, Fun.HI(), Fun.HI())).values();
		
		//RETURN
		return new SortableList<Tuple2<Long, Long>, Stack<Tuple2<byte[], Integer>>>(this, keys);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public SortableList<Tuple2<Long, Long>, Stack<Tuple2<byte[], Integer>>> getBalancesSortableList(Account account) 
	{
		BTreeMap map = (BTreeMap) this.map;
		
		//FILTER ALL KEYS
		Collection keys = ((BTreeMap<Tuple2, Long>) map).subMap(
				Fun.t2(account.getAddress(), null),
				Fun.t2(account.getAddress(), Fun.HI())).keySet();
		
		//RETURN
		return new SortableList<Tuple2<Long, Long>, Stack<Tuple2<byte[], Integer>>>(this, keys);
	}
	*/
}
