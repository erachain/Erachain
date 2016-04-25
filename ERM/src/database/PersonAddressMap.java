package database;

import java.util.HashMap;
import java.util.Map;
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
			Tuple2<Long, // personKey
				String>, // address
			Stack<Tuple3<Integer, // duration
				Integer, // block.getHeight
				byte[] // transaction.getReference
		>>> 
{
	private Map<Integer, Integer> observableData = new HashMap<Integer, Integer>();
	
	public PersonAddressMap(DBSet databaseSet, DB database)
	{
		super(databaseSet, database);
	}

	public PersonAddressMap(PersonAddressMap parent) 
	{
		super(parent);
	}
	
	protected void createIndexes(DB database){}

	@Override
	protected Map<Tuple2<Long, String>, Stack<Tuple3<Integer, Integer, byte[]>>> getMap(DB database) 
	{
		//OPEN MAP
		return database.createTreeMap("person_address")
				.keySerializer(BTreeKeySerializer.TUPLE2)
				.counterEnable()
				.makeOrGet();
	}

	@Override
	protected Map<Tuple2<Long, String>, Stack<Tuple3<Integer, Integer, byte[]>>> getMemoryMap() 
	{
		return new HashMap<Tuple2<Long, String>, Stack<Tuple3<Integer, Integer, byte[]>>>();
	}

	@Override
	protected Stack<Tuple3<Integer, Integer, byte[]>> getDefaultValue() 
	{
		return new Stack<Tuple3<Integer, Integer, byte[]>>();
	}
	
	@Override
	protected Map<Integer, Integer> getObservableData() 
	{
		return this.observableData;
	}
		
	///////////////////////////////
	public void addItem(Long person, String address, Tuple3<Integer, Integer, byte[]> item)
	{
		Tuple2<Long, String> key = new Tuple2<Long, String>(person, address);
		Stack<Tuple3<Integer, Integer, byte[]>> value = this.get(key);
		
		value.add(item);
		
		this.set(key, value);
	}
	
	public Tuple3<Integer, Integer, byte[]> getItem(Long person, String address)
	{
		Stack<Tuple3<Integer, Integer, byte[]>> value = this.get(new Tuple2<Long, String>(person, address));
		return value.size()> 0? value.peek(): null;
	}
	public void removeItem(Long person, String address)
	{
		Tuple2<Long, String> key = new Tuple2<Long, String>(person, address);
		Stack<Tuple3<Integer, Integer, byte[]>> value = this.get(key);
		if (value==null || value.size() == 0) return;

		value.pop();
		this.set(key, value);
		
	}

}
