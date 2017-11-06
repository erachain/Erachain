package database.wallet;

import java.util.Map;

import org.mapdb.BTreeKeySerializer;
import org.mapdb.DB;
import org.mapdb.Fun.Tuple2;

import core.item.ItemCls;
import utils.ObserverMessage;
import database.serializer.ItemSerializer;

public class WItemNoteMap extends WItem_Map
{
	
	//static Logger LOGGER = Logger.getLogger(WItemNoteMap.class.getName());
	static final String NAME = "note";
	static final int TYPE = ItemCls.NOTE_TYPE;


	public WItemNoteMap(DWSet dWSet, DB database)
	{
		super(dWSet, database,
				TYPE, "item_notes",
				ObserverMessage.WALLET_RESET_NOTE_TYPE,
				ObserverMessage.WALLET_ADD_NOTE_TYPE,
				ObserverMessage.WALLET_REMOVE_NOTE_TYPE,
				ObserverMessage.WALLET_LIST_NOTE_TYPE
				);
	}

	public WItemNoteMap(WItemNoteMap parent) 
	{
		super(parent);
	}
	
	@Override
	// type+name not initialized yet! - it call as Super in New
	protected Map<Tuple2<String, String>, ItemCls> getMap(DB database) 
	{
		//OPEN MAP
		return database.createTreeMap(NAME)
				.keySerializer(BTreeKeySerializer.TUPLE2)
				.valueSerializer(new ItemSerializer(TYPE))
				.counterEnable()
				.makeOrGet();
	}


	/*
	//@SuppressWarnings({ "unchecked", "rawtypes" })
	protected void createIndexes(DB database)
	{
		//NAME INDEX
	}

	@Override
	protected Map<Tuple2<String, String>, NoteCls> getMap(DB database) 
	{
		//OPEN MAP
		return database.createTreeMap("note")
				.keySerializer(BTreeKeySerializer.TUPLE2)
				.valueSerializer(new NoteSerializer())
				.counterEnable()
				.makeOrGet();
	}

	@Override
	protected Map<Tuple2<String, String>, NoteCls> getMemoryMap() 
	{
		return new TreeMap<Tuple2<String, String>, NoteCls>(Fun.TUPLE2_COMPARATOR);
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

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List<NoteCls> get(Account account)
	{
		List<NoteCls> notes = new ArrayList<NoteCls>();
		
		try
		{
			Map<Tuple2<String, String>, NoteCls> accountNotes = ((BTreeMap) this.map).subMap(
					Fun.t2(account.getAddress(), null),
					Fun.t2(account.getAddress(), Fun.HI()));
			
			//GET ITERATOR
			Iterator<NoteCls> iterator = accountNotes.values().iterator();
			
			while(iterator.hasNext())
			{
				notes.add(iterator.next());
			}
		}
		catch(Exception e)
		{
			//ERROR
			LOGGER.error(e.getMessage(),e);
		}
		
		return notes;
	}
	
	public List<Pair<Account, NoteCls>> get(List<Account> accounts)
	{
		List<Pair<Account, NoteCls>> notes = new ArrayList<Pair<Account, NoteCls>>();		

		try
		{
			//FOR EACH ACCOUNTS
			synchronized(accounts)
			{
				for(Account account: accounts)
				{
					List<NoteCls> accountNotes = get(account);
					for(NoteCls note: accountNotes)
					{
						notes.add(new Pair<Account, NoteCls>(account, note));
					}
				}
			}
		}
		catch(Exception e)
		{
			//ERROR
			LOGGER.error(e.getMessage(),e);
		}
		
		return notes;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void delete(Account account)
	{
		//GET ALL POLLS THAT BELONG TO THAT ADDRESS
		Map<Tuple2<String, String>, NoteCls> accountNotes = ((BTreeMap) this.map).subMap(
				Fun.t2(account.getAddress(), null),
				Fun.t2(account.getAddress(), Fun.HI()));
		
		//DELETE NAMES
		for(Tuple2<String, String> key: accountNotes.keySet())
		{
			this.delete(key);
		}
	}
	
	public void delete(NoteCls note)
	{
		this.delete(note.getCreator(), note);
	}
	
	public void delete(Account account, NoteCls note) 
	{
		this.delete(new Tuple2<String, String>(account.getAddress(), new String(note.getReference())));	
	}
	
	public void deleteAll(List<Account> accounts)
	{
		for(Account account: accounts)
		{
			this.delete(account);
		}
	}
	
	public boolean add(NoteCls note)
	{
		return this.set(new Tuple2<String, String>(note.getCreator().getAddress(),
				new String(note.getReference())), note);
	}
	
	public void addAll(Map<Account, List<NoteCls>> notes)
	{
		//FOR EACH ACCOUNT
	    for(Account account: notes.keySet())
	    {
	    	//FOR EACH TRANSACTION
	    	for(NoteCls note: notes.get(account))
	    	{
	    		this.add(note);
	    	}
	    }
	}
	*/
}
