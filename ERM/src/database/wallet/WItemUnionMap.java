package database.wallet;

import java.util.Map;

import org.mapdb.BTreeKeySerializer;
import org.mapdb.DB;
import org.mapdb.Fun.Tuple2;

import core.item.ItemCls;
import utils.ObserverMessage;
import database.serializer.ItemSerializer;

public class WItemUnionMap extends WItem_Map
{
	
	//static Logger LOGGER = Logger.getLogger(WItemUnionMap.class.getName());
	static final String NAME = "union";
	static final int TYPE = ItemCls.UNION_TYPE;


	public WItemUnionMap(WalletDatabase walletDatabase, DB database)
	{
		super(walletDatabase, database,
				TYPE, "item_unions",
				ObserverMessage.ADD_UNION_TYPE,
				ObserverMessage.REMOVE_UNION_TYPE,
				ObserverMessage.LIST_UNION_TYPE
				);
	}

	public WItemUnionMap(WItemUnionMap parent) 
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
	protected Map<Tuple2<String, String>, UnionCls> getMap(DB database) 
	{
		//OPEN MAP
		return database.createTreeMap("union")
				.keySerializer(BTreeKeySerializer.TUPLE2)
				.valueSerializer(new UnionSerializer())
				.counterEnable()
				.makeOrGet();
	}

	@Override
	protected Map<Tuple2<String, String>, UnionCls> getMemoryMap() 
	{
		return new TreeMap<Tuple2<String, String>, UnionCls>(Fun.TUPLE2_COMPARATOR);
	}

	@Override
	protected UnionCls getDefaultValue() 
	{
		return null;
	}
	
	@Override
	protected Map<Integer, Integer> getObservableData() 
	{
		return this.observableData;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List<UnionCls> get(Account account)
	{
		List<UnionCls> unions = new ArrayList<UnionCls>();
		
		try
		{
			Map<Tuple2<String, String>, UnionCls> accountUnions = ((BTreeMap) this.map).subMap(
					Fun.t2(account.getAddress(), null),
					Fun.t2(account.getAddress(), Fun.HI()));
			
			//GET ITERATOR
			Iterator<UnionCls> iterator = accountUnions.values().iterator();
			
			while(iterator.hasNext())
			{
				unions.add(iterator.next());
			}
		}
		catch(Exception e)
		{
			//ERROR
			LOGGER.error(e.getMessage(),e);
		}
		
		return unions;
	}
	
	public List<Pair<Account, UnionCls>> get(List<Account> accounts)
	{
		List<Pair<Account, UnionCls>> unions = new ArrayList<Pair<Account, UnionCls>>();		

		try
		{
			//FOR EACH ACCOUNTS
			synchronized(accounts)
			{
				for(Account account: accounts)
				{
					List<UnionCls> accountUnions = get(account);
					for(UnionCls union: accountUnions)
					{
						unions.add(new Pair<Account, UnionCls>(account, union));
					}
				}
			}
		}
		catch(Exception e)
		{
			//ERROR
			LOGGER.error(e.getMessage(),e);
		}
		
		return unions;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void delete(Account account)
	{
		//GET ALL POLLS THAT BELONG TO THAT ADDRESS
		Map<Tuple2<String, String>, UnionCls> accountUnions = ((BTreeMap) this.map).subMap(
				Fun.t2(account.getAddress(), null),
				Fun.t2(account.getAddress(), Fun.HI()));
		
		//DELETE NAMES
		for(Tuple2<String, String> key: accountUnions.keySet())
		{
			this.delete(key);
		}
	}
	
	public void delete(UnionCls union)
	{
		this.delete(union.getCreator(), union);
	}
	
	public void delete(Account account, UnionCls union) 
	{
		this.delete(new Tuple2<String, String>(account.getAddress(), new String(union.getReference())));	
	}
	
	public void deleteAll(List<Account> accounts)
	{
		for(Account account: accounts)
		{
			this.delete(account);
		}
	}
	
	public boolean add(UnionCls union)
	{
		return this.set(new Tuple2<String, String>(union.getCreator().getAddress(),
				new String(union.getReference())), union);
	}
	
	public void addAll(Map<Account, List<UnionCls>> unions)
	{
		//FOR EACH ACCOUNT
	    for(Account account: unions.keySet())
	    {
	    	//FOR EACH TRANSACTION
	    	for(UnionCls union: unions.get(account))
	    	{
	    		this.add(union);
	    	}
	    }
	}
	*/
}
