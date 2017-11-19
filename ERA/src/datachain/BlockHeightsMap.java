package datachain;



import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.mapdb.Atomic;
import org.mapdb.BTreeKeySerializer;
import org.mapdb.DB;
import org.mapdb.Fun.Tuple2;

import core.block.Block;

// BlockNo + 1 -> signature
// 0 - as GENESIS ?
public class BlockHeightsMap extends DCMap<Integer, byte[]> 
{
	protected Map<Integer, Integer> observableData = new HashMap<Integer, Integer>();
		
	protected Atomic.Integer atomicKey;
	protected Integer key;
	
	public BlockHeightsMap(DCSet databaseSet, DB database)
	{
		super(databaseSet, database);
		
		this.atomicKey = database.getAtomicInteger("block_heights_key");
		this.key = this.atomicKey.get();
		
	//	this.observableData.put(DBMap.NOTIFY_ADD, ObserverMessage.ADD_BLOCK_TYPE);
	//	this.observableData.put(DBMap.NOTIFY_REMOVE, ObserverMessage.REMOVE_BLOCK_TYPE);
	//	this.observableData.put(DBMap.NOTIFY_LIST, ObserverMessage.LIST_BLOCK_TYPE);
	}
	
	public BlockHeightsMap(BlockHeightsMap parent) 
	{
		super(parent, null);
		
		this.key = parent.getKey();
	}

	// type+name not initialized yet! - it call as Super in New
	protected Map<Integer, byte[]> getMap(DB database) 
	{		
		//OPEN MAP
		return database.createTreeMap("block_heights")
				.keySerializer(BTreeKeySerializer.BASIC)
				.makeOrGet();
	}
	
	public Integer getKey()
	{
		
		return this.key;
	}

	protected void createIndexes(DB database){}

	@Override
	protected Map<Integer, byte[]> getMemoryMap() 
	{
		return new HashMap<Integer, byte[]>();
	}

	@Override
	protected byte[] getDefaultValue() 
	{
		return null;
	}
	
	@Override
	protected Map<Integer, Integer> getObservableData() 
	{
		return this.observableData;
	}
	
	public Integer add(byte[] signature)
	{
		//INCREMENT ATOMIC KEY IF EXISTS
		if(this.atomicKey != null)
		{
			this.atomicKey.incrementAndGet();
		}
		
		//INCREMENT KEY
		this.key++;
		// TODO - set HEIGHT into BLOCK local
		//signature.setKey(key);
		
		//INSERT WITH NEW KEY
		this.set(this.key, signature);
		
		//RETURN KEY
		return this.key;
	}
	
	
	public byte[] get(Integer key)
	{
		return super.get(key);
	}

	public boolean set(Integer key, byte[] signature)
	{
		super.set(key, signature);
		return true;
	}

	public void delete(Integer key)
	{
		super.delete(key);
		
		//DECREMENT ATOMIC KEY IF EXISTS
		if(this.atomicKey != null)
		{
			this.atomicKey.decrementAndGet();
		}
		
		//DECREMENT KEY
		 this.key = key - 1;
	}
	
	public byte[] getSignByHeight(int  height){
		
		return this.map.get(height);
	}
}