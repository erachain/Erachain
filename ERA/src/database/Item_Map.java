package database;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mapdb.Atomic;
import org.mapdb.DB;

import core.item.ItemCls;
//import utils.ObserverMessage;
import database.DBSet;

public abstract class Item_Map extends DBMap<Long, ItemCls> 
{

	protected Map<Integer, Integer> observableData = new HashMap<Integer, Integer>();
	
	//protected int type;
	
	protected Atomic.Long atomicKey;
	protected long key;
	
	public Item_Map(DBSet databaseSet, DB database,
			//int type,
			String name,
			int observeAdd,
			int observeRemove,
			int observeList
			)
	{
		super(databaseSet, database);
		
		//this.type = type;
		this.atomicKey = database.getAtomicLong(name +"_key");
		// restore key from dbase
		this.key = this.atomicKey.get();
		
		if (observeAdd !=0 )this.observableData.put(DBMap.NOTIFY_ADD, observeAdd);
		if (observeRemove !=0 )this.observableData.put(DBMap.NOTIFY_REMOVE, observeRemove);
		if (observeList != 0) this.observableData.put(DBMap.NOTIFY_LIST, observeList);
	}

	
	public Item_Map(Item_Map parent) 
	{
		super(parent, null);
		
		this.key = parent.getSize();
	}
	
	public long getSize()
	{	
		return this.key;
	}
	
	public void setSize(long size)
	{	
		//INCREMENT ATOMIC KEY IF EXISTS
		if(this.atomicKey != null)
		{
			this.atomicKey.set(size);
		}
		this.key = size;
	}

	protected void createIndexes(DB database){}

	@Override
	protected Map<Long, ItemCls> getMemoryMap() 
	{
		return new HashMap<Long, ItemCls>();
	}

	@Override
	protected ItemCls getDefaultValue() 
	{
		return null;
	}
	
	@Override
	protected Map<Integer, Integer> getObservableData() 
	{
		return this.observableData;
	}
	
	public long add(ItemCls item)
	{
		//INCREMENT ATOMIC KEY IF EXISTS
		if(this.atomicKey != null)
		{
			this.atomicKey.incrementAndGet();
		}
		
		//INCREMENT KEY
		this.key++;
		item.setKey(key);
		
		//INSERT WITH NEW KEY
		this.set(this.key, item);
		
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
		
		// delete empty KEYS (run to GENESIS inserted keys)
		do {
			//DECREMENT ATOMIC KEY IF EXISTS
			if(this.atomicKey != null)
			{
				this.atomicKey.decrementAndGet();
			}
			
			//DECREMENT KEY
			this.key = key - 1;
		 
		} while ( !super.map.containsKey(key)  || key == 0l );
		 
	}
	
	// get list items in name substring str
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List<ItemCls> get_By_Name(String str)
	{
	List<ItemCls> txs = new ArrayList<>();
		if (str.equals("") || str == null) return null;
		
		for (long i = 0; i< this.getSize(); i++)
		{
			ItemCls item = this.get(i+1);
			if(item.getName().contains(str)) txs.add(item);
		}
		return txs;
	}
}
