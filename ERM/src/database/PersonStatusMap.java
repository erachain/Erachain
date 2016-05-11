package database;

import java.util.Stack;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.mapdb.BTreeKeySerializer;
import org.mapdb.BTreeMap;
import org.mapdb.DB;
import org.mapdb.Fun.Tuple3;

import core.item.statuses.StatusCls;
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

	@Override
	protected Map<Long, TreeMap<Integer, Stack<Tuple3<Integer, Integer, byte[]>>>> getMap(DB database) 
	{
		//OPEN MAP
		BTreeMap<Long, TreeMap<Integer, Stack<Tuple3<Integer, Integer, byte[]>>>> map =  database.createTreeMap("person_status")
				.keySerializer(BTreeKeySerializer.BASIC)
				.counterEnable()
				.makeOrGet();
				
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
	
}
