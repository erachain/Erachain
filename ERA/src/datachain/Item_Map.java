package datachain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.mapdb.Atomic;
import org.mapdb.DB;

import core.item.ItemCls;
import database.DBMap;
import datachain.DCSet;
import utils.ObserverMessage;
import utils.Pair;

public abstract class Item_Map extends DCMap<Long, ItemCls> 
{

	protected Map<Integer, Integer> observableData = new HashMap<Integer, Integer>();
	
	//protected int type;
	
	protected Atomic.Long atomicKey;
	protected long key;
	
	static Logger LOGGER = Logger.getLogger(Item_Map.class.getName());
	
	public Item_Map(DCSet databaseSet, DB database,
			//int type,
			String name,
			int observeReset, int observeAdd,	int observeRemove,	int observeList
			)
	{
		super(databaseSet, database);
		
		//this.type = type;
		this.atomicKey = database.getAtomicLong(name +"_key");
		// restore key from dbase
		this.key = this.atomicKey.get();
		
		if (databaseSet.isWithObserver()) {
			this.observableData.put(DBMap.NOTIFY_RESET, observeReset);
			if (databaseSet.isDynamicGUI()) {
				this.observableData.put(DBMap.NOTIFY_ADD, observeAdd);
				this.observableData.put(DBMap.NOTIFY_REMOVE, observeRemove);
			}
			this.observableData.put(DBMap.NOTIFY_LIST, observeList);
		}
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
	
	
	public void remove()
	{
		super.delete(key);
		
		if(this.atomicKey != null)
		{
			this.atomicKey.decrementAndGet();
		}
			
		//DECREMENT KEY
		--this.key;
		 
	}
	
	// get list items in name substring str
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List<ItemCls> get_By_Name(String str, boolean caseCharacter)
	{
	List<ItemCls> txs = new ArrayList<>();
		if (str == null || str.length() < 3)
			return null;
		
		Iterator<Pair<Long, ItemCls>> it = this.getList().iterator();
		while (it.hasNext()){
			Pair<Long, ItemCls> a = it.next();
			String s1 =  a.getB().getName() ;
			if (!caseCharacter){
			s1 = s1.toLowerCase();
			str = str.toLowerCase();
			}
				if (s1.contains(str))txs.add(a.getB());
			}
		
		return txs;
	}
}
