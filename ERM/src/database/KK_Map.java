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

// key to key_Stack End_Date Map
// in days
public class KK_Map extends DBMap<
			Long, // item1 Key
			TreeMap<Long, // item2 Key
					Stack<Tuple3<
						Long, // end_date
						Integer, // block.getHeight
						byte[] // transaction.getReference
				>>>>
{
	
	private Map<Integer, Integer> observableData = new HashMap<Integer, Integer>();
	private String name;
		
	public KK_Map(DBSet databaseSet, DB database,
			String name, int observerMessage_add, int observerMessage_remove)
	{
		super(databaseSet, database);
		
		this.name = name;
		this.observableData.put(DBMap.NOTIFY_ADD, observerMessage_add);
		this.observableData.put(DBMap.NOTIFY_REMOVE, observerMessage_remove);
		//this.observableData.put(DBMap.NOTIFY_LIST, ObserverMessage.LIST_PERSON_STATUSTYPE);

	}

	public KK_Map(KK_Map parent) 
	{
		super(parent);
	}

	
	protected void createIndexes(DB database){}

	@Override
	protected Map<Long, TreeMap<Long, Stack<Tuple3<Long, Integer, byte[]>>>> getMap(DB database) 
	{
		//OPEN MAP
		BTreeMap<Long, TreeMap<Long, Stack<Tuple3<Long, Integer, byte[]>>>> map =  database.createTreeMap(name)
				.keySerializer(BTreeKeySerializer.BASIC)
				.counterEnable()
				.makeOrGet();
				
		//RETURN
		return map;
	}

	@Override
	protected Map<Long, TreeMap<Long, Stack<Tuple3<Long, Integer, byte[]>>>> getMemoryMap() 
	{
		// HashMap ?
		return new TreeMap<Long, TreeMap<Long, Stack<Tuple3<Long, Integer, byte[]>>>>();
	}

	@Override
	protected TreeMap<Long, Stack<Tuple3<Long, Integer, byte[]>>> getDefaultValue() 
	{
		return new TreeMap<Long, Stack<Tuple3<Long, Integer, byte[]>>>();
	}
	
	@Override
	protected Map<Integer, Integer> getObservableData() 
	{
		return this.observableData;
	}

	public void addItem(Long key, Long itemKey, Tuple3<Long, Integer, byte[]> item)
	{

		TreeMap<Long, Stack<Tuple3<Long, Integer, byte[]>>> value = this.get(key);
		Stack<Tuple3<Long, Integer, byte[]>> stack = value.get(itemKey.intValue());
		if (stack == null) stack = new Stack<Tuple3<Long, Integer, byte[]>>();
		
		stack.add(item);
		value.put(itemKey, stack);
		
		this.set(key, value);
	}
	
	public Tuple3<Long, Integer, byte[]> getItem(Long key, Long itemKey)
	{
		TreeMap<Long, Stack<Tuple3<Long, Integer, byte[]>>> value = this.get(key);
		Stack<Tuple3<Long, Integer, byte[]>> stack = value.get(itemKey.intValue());
		return stack != null? stack.size()> 0? stack.peek(): null : null;
	}
	
	// remove only last item from stack for this key of itemKey
	public void removeItem(Long key, Long itemKey)
	{
		TreeMap<Long, Stack<Tuple3<Long, Integer, byte[]>>> value = this.get(key);
		Stack<Tuple3<Long, Integer, byte[]>> stack = value.get(itemKey.intValue());
		if (stack==null) return;

		stack.pop();
		value.put(itemKey, stack);
		this.set(key, value);
		
	}
	
}
