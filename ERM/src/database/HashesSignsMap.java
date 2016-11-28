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
import org.mapdb.Fun.Tuple3;
//import org.mapdb.Fun.Tuple2;
//import org.mapdb.Fun.Tuple3;
import org.mapdb.Fun.Tuple4;

import com.google.common.primitives.UnsignedBytes;

// hash[byte] -> Stack person + block.height + transaction.seqNo
// Example - database.AddressPersonMap
public class HashesSignsMap extends DBMap<byte[], Stack<Tuple3<
		Long, // person key
		Integer, // block height 
		Integer>>> // transaction index
{
	private Map<Integer, Integer> observableData = new TreeMap<Integer, Integer>(); // icreator -HashMap
	
	public HashesSignsMap(DBSet databaseSet, DB database)
	{
		super(databaseSet, database);
	}

	public HashesSignsMap(HashesSignsMap parent) 
	{
		super(parent, null);
	}
	
	protected void createIndexes(DB database){}

	@Override
	protected Map<byte[], Stack<Tuple3<Long, Integer, Integer>>> getMap(DB database) 
	{
		//OPEN MAP
		return database.createTreeMap("hashes_signs")
				.keySerializer(BTreeKeySerializer.BASIC)
				.comparator(UnsignedBytes.lexicographicalComparator())
				.makeOrGet();
	}

	@Override
	protected Map<byte[], Stack<Tuple3<Long, Integer, Integer>>> getMemoryMap() 
	{
		// HashMap ?
		return new TreeMap<byte[], Stack<Tuple3<Long, Integer, Integer>>>();
	}

	@Override
	protected Stack<Tuple3<Long, Integer, Integer>> getDefaultValue() 
	{
		return new Stack<Tuple3<Long, Integer, Integer>>();
	}
	
	@Override
	protected Map<Integer, Integer> getObservableData() 
	{
		return this.observableData;
	}
	
	///////////////////////////////
	@SuppressWarnings("unchecked")
	public void addItem(byte[] hash, Tuple3<Long, Integer, Integer> item)
	{
		Stack<Tuple3<Long, Integer, Integer>> value = this.get(hash);
		
		Stack<Tuple3<Long, Integer, Integer>> value_new;
		// !!!! NEEED .clone() !!!
		// need for updates only in fork - not in parent DB
		value_new = (Stack<Tuple3<Long, Integer, Integer>>)value.clone();

		value_new.add(item);
		
		this.set(hash, value_new);
		
	}
	
	public Tuple3<Long, Integer, Integer> getItem(byte[] hash)
	{
		Stack<Tuple3<Long, Integer, Integer>> value = this.get(hash);
		return value.size()>0? value.peek(): null;
	}
	
	@SuppressWarnings("unchecked")
	public void removeItem(byte[] hash)
	{
		Stack<Tuple3<Long, Integer, Integer>> value = this.get(hash);
		if (value==null || value.size() == 0) return;

		Stack<Tuple3<Long, Integer, Integer>> value_new;
		// !!!! NEEED .clone() !!!
		// need for updates only in fork - not in parent DB
		value_new = (Stack<Tuple3<Long, Integer, Integer>>)value.clone();

		value_new.pop();
		
		this.set(hash, value_new);
		
	}

}
