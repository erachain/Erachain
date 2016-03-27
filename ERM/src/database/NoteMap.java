package database;

import java.util.HashMap;
import java.util.Map;

import org.mapdb.Atomic;
import org.mapdb.DB;

import utils.ObserverMessage;
import database.DBSet;
import database.serializer.NoteSerializer;
import qora.notes.NoteCls;

public class NoteMap extends DBMap<Long, NoteCls> 
{
	private Map<Integer, Integer> observableData = new HashMap<Integer, Integer>();
	
	private Atomic.Long atomicKey;
	private long key;
	
	public NoteMap(DBSet databaseSet, DB database)
	{
		super(databaseSet, database);
		
		this.atomicKey = database.getAtomicLong("notes_key");
		this.key = this.atomicKey.get();
		
		this.observableData.put(DBMap.NOTIFY_ADD, ObserverMessage.ADD_ASSET_TYPE);
		this.observableData.put(DBMap.NOTIFY_REMOVE, ObserverMessage.REMOVE_ASSET_TYPE);
		this.observableData.put(DBMap.NOTIFY_LIST, ObserverMessage.LIST_ASSET_TYPE);
	}

	public NoteMap(NoteMap parent) 
	{
		super(parent);
		
		this.key = this.getKey();
	}
	
	protected long getKey()
	{
		return this.key;
	}
	
	protected void createIndexes(DB database){}

	@Override
	protected Map<Long, NoteCls> getMap(DB database) 
	{
		//OPEN MAP
		return database.createTreeMap("notes")
				.valueSerializer(new NoteSerializer())
				.makeOrGet();
	}

	@Override
	protected Map<Long, NoteCls> getMemoryMap() 
	{
		return new HashMap<Long, NoteCls>();
	}

	@Override
	protected NoteCls getDefaultValue() 
	{
		return null;
	}
	
	@Override
	protected Map<Integer, Integer> getObservableData() 
	{
		return this.observableData;
	}
	
	public long add(NoteCls note)
	{
		//INCREMENT ATOMIC KEY IF EXISTS
		if(this.atomicKey != null)
		{
			this.atomicKey.incrementAndGet();
		}
		
		//INCREMENT KEY
		this.key++;
		
		//INSERT WITH NEW KEY
		this.set(this.key, note);
		
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
