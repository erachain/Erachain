package database;

import java.util.Collection;
import java.util.Map;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.Stack;

import org.mapdb.BTreeKeySerializer;
import org.mapdb.BTreeMap;
import org.mapdb.DB;
import org.mapdb.Fun;
//import org.mapdb.Fun.Tuple2;
//import org.mapdb.Fun.Tuple3;
import org.mapdb.Fun.Tuple4;

// address+key -> Stack person + duration + block.height + transaction.reference
// Controller.getInstance().getHeight()
public class AddressPersonMap extends DBMap<String, Stack<Tuple4<
		Long, // person key
		Integer, // duration day
		Integer, // block height 
		byte[]>>> // transaction reference
{
	private Map<Integer, Integer> observableData = new TreeMap<Integer, Integer>(); // icreator -HashMap
	
	public AddressPersonMap(DBSet databaseSet, DB database)
	{
		super(databaseSet, database);
	}

	public AddressPersonMap(AddressPersonMap parent) 
	{
		super(parent);
	}
	
	protected void createIndexes(DB database){}

	@Override
	protected Map<String, Stack<Tuple4<Long, Integer, Integer, byte[]>>> getMap(DB database) 
	{
		//OPEN MAP
		return database.createTreeMap("address_person")
				.keySerializer(BTreeKeySerializer.STRING)
				.counterEnable()
				.makeOrGet();
	}

	@Override
	protected Map<String, Stack<Tuple4<Long, Integer, Integer, byte[]>>> getMemoryMap() 
	{
		// HashMap ?
		return new TreeMap<String, Stack<Tuple4<Long, Integer, Integer, byte[]>>>();
	}

	@Override
	protected Stack<Tuple4<Long, Integer, Integer, byte[]>> getDefaultValue() 
	{
		return new Stack<Tuple4<Long, Integer, Integer, byte[]>>();
	}
	
	@Override
	protected Map<Integer, Integer> getObservableData() 
	{
		return this.observableData;
	}
	
	///////////////////////////////
	public void addItem(String address, Tuple4<Long, Integer, Integer, byte[]> item)
	{
		Stack<Tuple4<Long, Integer, Integer, byte[]>> value = this.get(address);
		
		value.add(item);
		
		this.set(address, value);
	}
	
	public Tuple4<Long, Integer, Integer, byte[]> getItem(String address)
	{
		Stack<Tuple4<Long, Integer, Integer, byte[]>> value = this.get(address);
		return value.size()>0? value.peek(): null;
	}
	public void removeItem(String address)
	{
		Stack<Tuple4<Long, Integer, Integer, byte[]>> value = this.get(address);
		if (value==null || value.size() == 0) return;

		value.pop();
		this.set(address, value);
		
	}

}
