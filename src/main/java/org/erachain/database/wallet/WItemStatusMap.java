package org.erachain.database.wallet;

import org.erachain.core.item.ItemCls;
import org.erachain.database.serializer.ItemSerializer;
import org.mapdb.BTreeKeySerializer;
import org.mapdb.DB;
import org.mapdb.Fun.Tuple2;
import org.erachain.utils.ObserverMessage;

import java.util.Map;

public class WItemStatusMap extends WItem_Map {

    //static Logger LOGGER = LoggerFactory.getLogger(WItemStatusMap.class.getName());
    static final String NAME = "status";
    static final int TYPE = ItemCls.STATUS_TYPE;


    public WItemStatusMap(DWSet dWSet, DB database) {
        super(dWSet, database,
                TYPE, "item_statuses",
                ObserverMessage.WALLET_RESET_STATUS_TYPE,
                ObserverMessage.WALLET_ADD_STATUS_TYPE,
                ObserverMessage.WALLET_REMOVE_STATUS_TYPE,
                ObserverMessage.WALLET_LIST_STATUS_TYPE
        );
    }

    public WItemStatusMap(WItemStatusMap parent) {
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
	protected Map<Tuple2<String, String>, StatusCls> getMap(DB database) 
	{
		//OPEN MAP
		return database.createTreeMap("status")
				.keySerializer(BTreeKeySerializer.TUPLE2)
				.valueSerializer(new StatusSerializer())
				.counterEnable()
				.makeOrGet();
	}

	@Override
	protected Map<Tuple2<String, String>, StatusCls> getMemoryMap() 
	{
		return new TreeMap<Tuple2<String, String>, StatusCls>(Fun.TUPLE2_COMPARATOR);
	}

	@Override
	protected StatusCls getDefaultValue() 
	{
		return null;
	}
	
	@Override
	protected Map<Integer, Integer> getObservableData() 
	{
		return this.observableData;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List<StatusCls> get(Account account)
	{
		List<StatusCls> statuses = new ArrayList<StatusCls>();
		
		try
		{
			Map<Tuple2<String, String>, StatusCls> accountStatuses = ((BTreeMap) this.map).subMap(
					Fun.t2(account.getAddress(), null),
					Fun.t2(account.getAddress(), Fun.HI()));
			
			//GET ITERATOR
			Iterator<StatusCls> iterator = accountStatuses.values().iterator();
			
			while(iterator.hasNext())
			{
				statuses.add(iterator.next());
			}
		}
		catch(Exception e)
		{
			//ERROR
			LOGGER.error(e.getMessage(),e);
		}
		
		return statuses;
	}
	
	public List<Pair<Account, StatusCls>> get(List<Account> accounts)
	{
		List<Pair<Account, StatusCls>> statuses = new ArrayList<Pair<Account, StatusCls>>();		

		try
		{
			//FOR EACH ACCOUNTS
			synchronized(accounts)
			{
				for(Account account: accounts)
				{
					List<StatusCls> accountStatuses = get(account);
					for(StatusCls status: accountStatuses)
					{
						statuses.add(new Pair<Account, StatusCls>(account, status));
					}
				}
			}
		}
		catch(Exception e)
		{
			//ERROR
			LOGGER.error(e.getMessage(),e);
		}
		
		return statuses;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void delete(Account account)
	{
		//GET ALL POLLS THAT BELONG TO THAT ADDRESS
		Map<Tuple2<String, String>, StatusCls> accountStatuses = ((BTreeMap) this.map).subMap(
				Fun.t2(account.getAddress(), null),
				Fun.t2(account.getAddress(), Fun.HI()));
		
		//DELETE NAMES
		for(Tuple2<String, String> key: accountStatuses.keySet())
		{
			this.delete(key);
		}
	}
	
	public void delete(StatusCls status)
	{
		this.delete(status.getCreator(), status);
	}
	
	public void delete(Account account, StatusCls status) 
	{
		this.delete(new Tuple2<String, String>(account.getAddress(), new String(status.getReference())));	
	}
	
	public void deleteAll(List<Account> accounts)
	{
		for(Account account: accounts)
		{
			this.delete(account);
		}
	}
	
	public boolean add(StatusCls status)
	{
		return this.set(new Tuple2<String, String>(status.getCreator().getAddress(),
				new String(status.getReference())), status);
	}
	
	public void addAll(Map<Account, List<StatusCls>> statuses)
	{
		//FOR EACH ACCOUNT
	    for(Account account: statuses.keySet())
	    {
	    	//FOR EACH TRANSACTION
	    	for(StatusCls status: statuses.get(account))
	    	{
	    		this.add(status);
	    	}
	    }
	}
	*/
}
