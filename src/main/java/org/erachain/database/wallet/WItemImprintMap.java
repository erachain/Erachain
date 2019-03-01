package org.erachain.database.wallet;

import org.erachain.core.item.ItemCls;
import org.erachain.database.serializer.ItemSerializer;
import org.erachain.utils.ObserverMessage;
import org.mapdb.BTreeKeySerializer;
import org.mapdb.DB;
import org.mapdb.Fun.Tuple2;

import java.util.Map;

public class WItemImprintMap extends WItem_Map {

    //static Logger LOGGER = LoggerFactory.getLogger(WItemImprintMap.class.getName());
    static final String NAME = "imprint";
    static final int TYPE = ItemCls.IMPRINT_TYPE;


    public WItemImprintMap(DWSet dWSet, DB database) {
        super(dWSet, database,
                TYPE, "item_imprints",
                ObserverMessage.WALLET_RESET_IMPRINT_TYPE,
                ObserverMessage.WALLET_ADD_IMPRINT_TYPE,
                ObserverMessage.WALLET_REMOVE_IMPRINT_TYPE,
                ObserverMessage.WALLET_LIST_IMPRINT_TYPE
        );
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
	protected Map<Tuple2<String, String>, ImprintCls> getMap(DB database) 
	{
		//OPEN MAP
		return database.createTreeMap("imprint")
				.keySerializer(BTreeKeySerializer.TUPLE2)
				.valueSerializer(new ImprintSerializer())
				.counterEnable()
				.makeOrGet();
	}

	@Override
	protected Map<Tuple2<String, String>, ImprintCls> getMemoryMap() 
	{
		return new TreeMap<Tuple2<String, String>, ImprintCls>(Fun.TUPLE2_COMPARATOR);
	}

	@Override
	protected ImprintCls getDefaultValue() 
	{
		return null;
	}
	
	@Override
	protected Map<Integer, Integer> getObservableData() 
	{
		return this.observableData;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List<ImprintCls> get(Account account)
	{
		List<ImprintCls> imprints = new ArrayList<ImprintCls>();
		
		try
		{
			Map<Tuple2<String, String>, ImprintCls> accountImprints = ((BTreeMap) this.map).subMap(
					Fun.t2(account.getAddress(), null),
					Fun.t2(account.getAddress(), Fun.HI()));
			
			//GET ITERATOR
			Iterator<ImprintCls> iterator = accountImprints.values().iterator();
			
			while(iterator.hasNext())
			{
				imprints.add(iterator.next());
			}
		}
		catch(Exception e)
		{
			//ERROR
			LOGGER.error(e.getMessage(),e);
		}
		
		return imprints;
	}
	
	public List<Pair<Account, ImprintCls>> get(List<Account> accounts)
	{
		List<Pair<Account, ImprintCls>> imprints = new ArrayList<Pair<Account, ImprintCls>>();		

		try
		{
			//FOR EACH ACCOUNTS
			synchronized(accounts)
			{
				for(Account account: accounts)
				{
					List<ImprintCls> accountImprints = get(account);
					for(ImprintCls imprint: accountImprints)
					{
						imprints.add(new Pair<Account, ImprintCls>(account, imprint));
					}
				}
			}
		}
		catch(Exception e)
		{
			//ERROR
			LOGGER.error(e.getMessage(),e);
		}
		
		return imprints;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void delete(Account account)
	{
		//GET ALL POLLS THAT BELONG TO THAT ADDRESS
		Map<Tuple2<String, String>, ImprintCls> accountImprints = ((BTreeMap) this.map).subMap(
				Fun.t2(account.getAddress(), null),
				Fun.t2(account.getAddress(), Fun.HI()));
		
		//DELETE NAMES
		for(Tuple2<String, String> key: accountImprints.keySet())
		{
			this.delete(key);
		}
	}
	
	public void delete(ImprintCls imprint)
	{
		this.delete(imprint.getCreator(), imprint);
	}
	
	public void delete(Account account, ImprintCls imprint) 
	{
		this.delete(new Tuple2<String, String>(account.getAddress(), new String(imprint.getReference())));	
	}
	
	public void deleteAll(List<Account> accounts)
	{
		for(Account account: accounts)
		{
			this.delete(account);
		}
	}
	
	public boolean add(ImprintCls imprint)
	{
		return this.set(new Tuple2<String, String>(imprint.getCreator().getAddress(),
				new String(imprint.getReference())), imprint);
	}
	
	public void addAll(Map<Account, List<ImprintCls>> imprints)
	{
		//FOR EACH ACCOUNT
	    for(Account account: imprints.keySet())
	    {
	    	//FOR EACH TRANSACTION
	    	for(ImprintCls imprint: imprints.get(account))
	    	{
	    		this.add(imprint);
	    	}
	    }
	}
	*/
}
