package database;

import java.util.HashMap;
import java.util.Map;

import org.mapdb.Atomic;
import org.mapdb.BTreeKeySerializer;
import org.mapdb.DB;


import database.DBSet;
import utils.ObserverMessage;

// BlockNo -> signature
public class BlockHeightsMap extends DBMap<Long, byte[]> 
{
	protected Map<Integer, Integer> observableData = new HashMap<Integer, Integer>();
		
	protected Atomic.Long atomicKey;
	protected long key;
	
	public BlockHeightsMap(DBSet databaseSet, DB database)
	{
		super(databaseSet, database);
		
		this.atomicKey = database.getAtomicLong("block_heights_key");
		this.key = this.atomicKey.get();
		
		this.observableData.put(DBMap.NOTIFY_ADD, ObserverMessage.ADD_BLOCK_TYPE);
		this.observableData.put(DBMap.NOTIFY_REMOVE, ObserverMessage.REMOVE_BLOCK_TYPE);
		this.observableData.put(DBMap.NOTIFY_LIST, ObserverMessage.LIST_BLOCK_TYPE);
	}
	
	public BlockHeightsMap(BlockHeightsMap parent) 
	{
		super(parent, null);
		
		this.key = parent.getKey();
	}

	// type+name not initialized yet! - it call as Super in New
	protected Map<Long, byte[]> getMap(DB database) 
	{		
		//OPEN MAP
		return database.createTreeMap("block_heights")
				.keySerializer(BTreeKeySerializer.BASIC)
				.makeOrGet();
	}
	
	public long getKey()
	{
		
		return this.key;
	}

	protected void createIndexes(DB database){}

	@Override
	protected Map<Long, byte[]> getMemoryMap() 
	{
		return new HashMap<Long, byte[]>();
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
	
	public long add(byte[] signature)
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
	
	
	public void delete(long key)
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
}
