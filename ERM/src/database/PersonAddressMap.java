package database;

import java.util.Map;
//import java.util.HashMap;
import java.util.TreeMap;
import java.util.Stack;

import org.mapdb.BTreeKeySerializer;
import org.mapdb.DB;
import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple3;

import core.naming.Name;
//import database.DBSet;
import database.serializer.NameSerializer;

// Person contains an addresses
public class PersonAddressMap extends DBMap<
				Long, // personKey
				TreeMap<
					String, // address
					Stack<Tuple3<Integer, // end_date
						Integer, // block.getHeight
						Integer // transaction index
		>>>>
{
	private Map<Integer, Integer> observableData = new TreeMap<Integer, Integer>(); // hashMap ?
	
	public PersonAddressMap(DBSet databaseSet, DB database)
	{
		super(databaseSet, database);
	}

	public PersonAddressMap(PersonAddressMap parent) 
	{
		super(parent, null);
	}
	
	protected void createIndexes(DB database){}

	@Override
	protected Map<Long, TreeMap<String, Stack<Tuple3<Integer, Integer, Integer>>>> getMap(DB database) 
	{
		//OPEN MAP
		return database.createTreeMap("person_address")
				.keySerializer(BTreeKeySerializer.BASIC)
				.counterEnable()
				.makeOrGet();
	}

	@Override
	protected Map<Long, TreeMap<String, Stack<Tuple3<Integer, Integer, Integer>>>> getMemoryMap() 
	{
		return new TreeMap<Long, TreeMap<String, Stack<Tuple3<Integer, Integer, Integer>>>>();
	}

	@Override
	protected TreeMap<String, Stack<Tuple3<Integer, Integer, Integer>>> getDefaultValue() 
	{
		return new TreeMap<String, Stack<Tuple3<Integer, Integer, Integer>>>();
	}
	
	@Override
	protected Map<Integer, Integer> getObservableData() 
	{
		return this.observableData;
	}
		
	///////////////////////////////
	public void addItem(Long person, String address, Tuple3<Integer, Integer, Integer> item)
	{
		TreeMap<String, Stack<Tuple3<Integer, Integer, Integer>>> value = this.get(person);
		
		TreeMap<String, Stack<Tuple3<Integer, Integer, Integer>>> value_new;
		if (false && this.parent == null)
			value_new = value;
		else {
			// !!!! NEEED .clone() !!!
			// need for updates only in fork - not in parent DB
			value_new = (TreeMap<String, Stack<Tuple3<Integer, Integer, Integer>>>)value.clone();
		}

		Stack<Tuple3<Integer, Integer, Integer>> stack = value_new.get(address);
		if (stack == null)
			stack = new Stack<Tuple3<Integer, Integer, Integer>>();
		
		stack.push(item);

		value_new.put(address, stack);
		
		this.set(person, value_new);
		
	}
	
	// GET ALL ITEMS
	public TreeMap<String, Stack<Tuple3<Integer, Integer, Integer>>> getItems(Long person)
	{
		TreeMap<String, Stack<Tuple3<Integer, Integer, Integer>>> tree = this.get(person);
		return tree;
	}

	public Tuple3<Integer, Integer, Integer> getItem(Long person, String address)
	{
		TreeMap<String, Stack<Tuple3<Integer, Integer, Integer>>> tree = this.get(person);
		Stack<Tuple3<Integer, Integer, Integer>> stack = tree.get(address);
		if (stack == null) return null;
		return stack.size()> 0? stack.peek(): null;
	}
	
	public void removeItem(Long person, String address)
	{
		TreeMap<String, Stack<Tuple3<Integer, Integer, Integer>>> value = this.get(person);
		
		TreeMap<String, Stack<Tuple3<Integer, Integer, Integer>>> value_new;
		if (false && this.parent == null)
			value_new = value;
		else {
			// !!!! NEEED .clone() !!!
			// need for updates only in fork - not in parent DB
			value_new = (TreeMap<String, Stack<Tuple3<Integer, Integer, Integer>>>)value.clone();
		}

		Stack<Tuple3<Integer, Integer, Integer>> stack = value_new.get(address);
		if (stack==null || stack.size() == 0) return;

		stack.pop();

		value_new.put(address, stack);
		
		this.set(person, value_new);
		
	}

}
