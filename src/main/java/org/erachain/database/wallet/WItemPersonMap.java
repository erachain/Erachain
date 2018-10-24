package org.erachain.database.wallet;

import org.erachain.core.item.ItemCls;
import org.erachain.database.serializer.ItemSerializer;
import org.mapdb.BTreeKeySerializer;
import org.mapdb.DB;
import org.mapdb.Fun.Tuple2;
import org.erachain.utils.ObserverMessage;

import java.util.Map;

public class WItemPersonMap extends WItem_Map {

    //static Logger LOGGER = LoggerFactory.getLogger(WItemPersonMap.class.getName());
    static final String NAME = "person";
    static final int TYPE = ItemCls.PERSON_TYPE;


    public WItemPersonMap(DWSet dWSet, DB database) {
        super(dWSet, database,
                TYPE, "item_persons",
                ObserverMessage.WALLET_RESET_PERSON_TYPE,
                ObserverMessage.WALLET_ADD_PERSON_TYPE,
                ObserverMessage.WALLET_REMOVE_PERSON_TYPE,
                ObserverMessage.WALLET_LIST_PERSON_TYPE
        );
    }

    public WItemPersonMap(WItemPersonMap parent) {
        super(parent);
    }

    @Override
    // type+name not initialized yet! - it call as Super in New
    protected Map<Tuple2<String, String>, ItemCls> getMap(DB database) {
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
	protected Map<Tuple2<String, String>, PersonCls> getMap(DB database) 
	{
		//OPEN MAP
		return database.createTreeMap("person")
				.keySerializer(BTreeKeySerializer.TUPLE2)
				.valueSerializer(new PersonSerializer())
				.counterEnable()
				.makeOrGet();
	}

	@Override
	protected Map<Tuple2<String, String>, PersonCls> getMemoryMap() 
	{
		return new TreeMap<Tuple2<String, String>, PersonCls>(Fun.TUPLE2_COMPARATOR);
	}

	@Override
	protected PersonCls getDefaultValue() 
	{
		return null;
	}
	
	@Override
	protected Map<Integer, Integer> getObservableData() 
	{
		return this.observableData;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List<PersonCls> get(Account account)
	{
		List<PersonCls> persons = new ArrayList<PersonCls>();
		
		try
		{
			Map<Tuple2<String, String>, PersonCls> accountPersons = ((BTreeMap) this.map).subMap(
					Fun.t2(account.getAddress(), null),
					Fun.t2(account.getAddress(), Fun.HI()));
			
			//GET ITERATOR
			Iterator<PersonCls> iterator = accountPersons.values().iterator();
			
			while(iterator.hasNext())
			{
				persons.add(iterator.next());
			}
		}
		catch(Exception e)
		{
			//ERROR
			LOGGER.error(e.getMessage(),e);
		}
		
		return persons;
	}
	
	public List<Pair<Account, PersonCls>> get(List<Account> accounts)
	{
		List<Pair<Account, PersonCls>> persons = new ArrayList<Pair<Account, PersonCls>>();		

		try
		{
			//FOR EACH ACCOUNTS
			synchronized(accounts)
			{
				for(Account account: accounts)
				{
					List<PersonCls> accountPersons = get(account);
					for(PersonCls person: accountPersons)
					{
						persons.add(new Pair<Account, PersonCls>(account, person));
					}
				}
			}
		}
		catch(Exception e)
		{
			//ERROR
			LOGGER.error(e.getMessage(),e);
		}
		
		return persons;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void delete(Account account)
	{
		//GET ALL POLLS THAT BELONG TO THAT ADDRESS
		Map<Tuple2<String, String>, PersonCls> accountPersons = ((BTreeMap) this.map).subMap(
				Fun.t2(account.getAddress(), null),
				Fun.t2(account.getAddress(), Fun.HI()));
		
		//DELETE NAMES
		for(Tuple2<String, String> key: accountPersons.keySet())
		{
			this.delete(key);
		}
	}
	
	public void delete(PersonCls person)
	{
		this.delete(person.getCreator(), person);
	}
	
	public void delete(Account account, PersonCls person) 
	{
		this.delete(new Tuple2<String, String>(account.getAddress(), new String(person.getReference())));	
	}
	
	public void deleteAll(List<Account> accounts)
	{
		for(Account account: accounts)
		{
			this.delete(account);
		}
	}
	
	public boolean add(PersonCls person)
	{
		return this.set(new Tuple2<String, String>(person.getCreator().getAddress(),
				new String(person.getReference())), person);
	}
	
	public void addAll(Map<Account, List<PersonCls>> persons)
	{
		//FOR EACH ACCOUNT
	    for(Account account: persons.keySet())
	    {
	    	//FOR EACH TRANSACTION
	    	for(PersonCls person: persons.get(account))
	    	{
	    		this.add(person);
	    	}
	    }
	}
	*/
}
