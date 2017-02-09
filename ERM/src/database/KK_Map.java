package database;

import java.util.Stack;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.mapdb.BTreeKeySerializer;
import org.mapdb.BTreeMap;
import org.mapdb.DB;
import org.mapdb.Fun.Tuple5;

//import core.block.Block;
//import core.item.statuses.StatusCls;
//import utils.ObserverMessage;
import database.DBSet;

// key to key_Stack for End_Date Map
// in days
public class KK_Map extends DBMap<
			Long, // item1 Key
			TreeMap<Long, // item2 Key
				Stack<Tuple5<
					Long, // beg_date
					Long, // end_date

					byte[], // any additional data
					
					Integer, // block.getHeight() -> db.getBlockMap(db.getHeightMap().getBlockByHeight(index))
					Integer // block.getTransaction(transaction.getSignature()) -> block.getTransaction(index)
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
		super(parent, null);
	}

	
	protected void createIndexes(DB database){}

	@Override
	protected Map<Long, TreeMap<Long, Stack<Tuple5<Long, Long, byte[], Integer, Integer>>>> getMap(DB database) 
	{
		//OPEN MAP
		BTreeMap<Long, TreeMap<Long, Stack<Tuple5<Long, Long, byte[], Integer, Integer>>>> map =  database.createTreeMap(name)
				.keySerializer(BTreeKeySerializer.BASIC)
				.counterEnable()
				.makeOrGet();
				
		//RETURN
		return map;
	}

	@Override
	protected Map<Long, TreeMap<Long, Stack<Tuple5<Long, Long, byte[], Integer, Integer>>>> getMemoryMap() 
	{
		// HashMap ?
		return new TreeMap<Long, TreeMap<Long, Stack<Tuple5<Long, Long, byte[], Integer, Integer>>>>();
	}

	@Override
	protected TreeMap<Long, Stack<Tuple5<Long, Long, byte[], Integer, Integer>>> getDefaultValue() 
	{
		return new TreeMap<Long, Stack<Tuple5<Long, Long, byte[], Integer, Integer>>>();
	}
	
	@Override
	protected Map<Integer, Integer> getObservableData() 
	{
		return this.observableData;
	}

	@SuppressWarnings("unchecked")
	public void putItem(Long key, Long itemKey, Tuple5<Long, Long, byte[], Integer, Integer> item)
	{

		TreeMap<Long, Stack<Tuple5<Long, Long, byte[], Integer, Integer>>> value = this.get(key);
		
		TreeMap<Long, Stack<Tuple5<Long, Long, byte[], Integer, Integer>>> value_new;
		if (this.parent == null)
			value_new = value;
		else {
			// !!!! NEEED .clone() !!!
			// need for updates only in fork - not in parent DB
			value_new = (TreeMap<Long, Stack<Tuple5<Long, Long, byte[], Integer, Integer>>>)value.clone();
		}

		Stack<Tuple5<Long, Long, byte[], Integer, Integer>> stack = value_new.get(itemKey);
		if (stack == null) {
			stack = new Stack<Tuple5<Long, Long, byte[], Integer, Integer>>();
			stack.push(item);
			value_new.put(itemKey, stack);
		} else {
			if (this.parent == null) {
				stack.push(item);
				value_new.put(itemKey, stack);
			} else {
				Stack<Tuple5<Long, Long, byte[], Integer, Integer>> stack_new;
				stack_new = (Stack<Tuple5<Long, Long, byte[], Integer, Integer>>)stack.clone();
				if (item.a == null || item.b == null) {
					// item has NULL values id dates - reset it by last values
					Long valA;
					Long valB;
					Tuple5<Long, Long, byte[], Integer, Integer> lastItem = stack_new.peek();
					if (item.a == null) {
						// if input item Begin Date = null - take date from stack (last value)
						valA = lastItem.a;
					} else {
						valA = item.a;					
					}
					if (item.b == null) {
						// if input item End Date = null - take date from stack (last value)
						valB = lastItem.b;
					} else {
						valB = item.b;					
					}
					stack_new.push(new Tuple5<Long, Long, byte[], Integer, Integer>(valA, valB, item.c, item.d, item.e));
				} else {
					stack_new.push(item);
				}
				value_new.put(itemKey, stack_new);
			}
		}

		
		this.set(key, value_new);
	}

	// NOT UPDATE UNIQUE STATUS FOR ITEM - ADD NEW STATUS FOR ITEM + DATA
	@SuppressWarnings("unchecked")
	public void addItem(Long key, Long itemKey, Tuple5<Long, Long, byte[], Integer, Integer> item)
	{

		TreeMap<Long, Stack<Tuple5<Long, Long, byte[], Integer, Integer>>> value = this.get(key);
		
		TreeMap<Long, Stack<Tuple5<Long, Long, byte[], Integer, Integer>>> value_new;
		if (this.parent == null)
			value_new = value;
		else {
			// !!!! NEEED .clone() !!!
			// need for updates only in fork - not in parent DB
			value_new = (TreeMap<Long, Stack<Tuple5<Long, Long, byte[], Integer, Integer>>>)value.clone();
		}

		Stack<Tuple5<Long, Long, byte[], Integer, Integer>> stack = value_new.get(itemKey);
		if (stack == null) {
			stack = new Stack<Tuple5<Long, Long, byte[], Integer, Integer>>();			
			stack.push(item);
			value_new.put(itemKey, stack);
		} else {
			if (this.parent == null) {
				stack.push(item);
				value_new.put(itemKey, stack);
			} else {
				Stack<Tuple5<Long, Long, byte[], Integer, Integer>> stack_new;
				stack_new = (Stack<Tuple5<Long, Long, byte[], Integer, Integer>>)stack.clone();
				stack_new.push(item);
				value_new.put(itemKey, stack_new);
			}
		}
		
		this.set(key, value_new);
	}

	public Tuple5<Long, Long, byte[], Integer, Integer> getItem(Long key, Long itemKey)
	{
		TreeMap<Long, Stack<Tuple5<Long, Long, byte[], Integer, Integer>>> value = this.get(key);
		Stack<Tuple5<Long, Long, byte[], Integer, Integer>> stack = value.get(itemKey);
		return stack != null? stack.size()> 0? stack.peek(): null : null;
	}
	public Stack<Tuple5<Long, Long, byte[], Integer, Integer>> getStack(Long key, Long itemKey)
	{
		TreeMap<Long, Stack<Tuple5<Long, Long, byte[], Integer, Integer>>> value = this.get(key);
		return value.get(itemKey);
	}
	
	// remove only last item from stack for this key of itemKey
	@SuppressWarnings("unchecked")
	public void removeItem(Long key, Long itemKey)
	{
		TreeMap<Long, Stack<Tuple5<Long, Long, byte[], Integer, Integer>>> value = this.get(key);
		
		TreeMap<Long, Stack<Tuple5<Long, Long, byte[], Integer, Integer>>> value_new;
		if (this.parent == null)
			value_new = value;
		else {
			value_new = (TreeMap<Long, Stack<Tuple5<Long, Long, byte[], Integer, Integer>>>)value.clone();
		}

		Stack<Tuple5<Long, Long, byte[], Integer, Integer>> stack = value_new.get(itemKey);
		if (stack==null || stack.size() == 0)
			return;

		if (this.parent == null) {
			stack.pop();
			value_new.put(itemKey, stack);
		} else {
			Stack<Tuple5<Long, Long, byte[], Integer, Integer>> stack_new;
			stack_new = (Stack<Tuple5<Long, Long, byte[], Integer, Integer>>)stack.clone();
			stack_new.pop();
			value_new.put(itemKey, stack_new);
		}

		this.set(key, value_new);
	}
}
