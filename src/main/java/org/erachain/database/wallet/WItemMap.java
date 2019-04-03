package org.erachain.database.wallet;

import org.erachain.core.account.Account;
import org.erachain.core.item.ItemCls;
import org.erachain.database.DBMap;
import org.erachain.database.serializer.ItemSerializer;
import org.erachain.utils.Pair;
import org.mapdb.BTreeKeySerializer;
import org.mapdb.BTreeMap;
import org.mapdb.DB;
import org.mapdb.Fun;
import org.mapdb.Fun.Tuple2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

// TODO reference as TIMESTAMP of transaction
public class WItemMap extends DBMap<Tuple2<String, String>, ItemCls> {

    public static final int NAME_INDEX = 1;
    public static final int CREATOR_INDEX = 2;
    static Logger LOGGER = LoggerFactory.getLogger(WItemMap.class.getName());
    protected int type;
    protected String name;

    public WItemMap(DWSet dWSet, DB database, int type, String name,
                    int observeReset,
                    int observeAdd,
                    int observeRemove,
                    int observeList
    ) {
        super(dWSet, database);

        this.type = type;
        this.name = name;

        if (databaseSet.isWithObserver()) {
            this.observableData.put(DBMap.NOTIFY_RESET, observeReset);
            this.observableData.put(DBMap.NOTIFY_LIST, observeList);
            this.observableData.put(DBMap.NOTIFY_ADD, observeAdd);
            this.observableData.put(DBMap.NOTIFY_REMOVE, observeRemove);
        }
    }

    //@SuppressWarnings({ "unchecked", "rawtypes" })
    protected void createIndexes(DB database) {
        //NAME INDEX
		/*NavigableSet<Tuple2<String, Tuple2<String, String>>> nameIndex = database.createTreeSet("polls_index_name")
				.comparator(Fun.COMPARATOR)
				.makeOrGet();
		
		NavigableSet<Tuple2<String, Tuple2<String, String>>> descendingNameIndex = database.createTreeSet("polls_index_name_descending")
				.comparator(new ReverseComparator(Fun.COMPARATOR))
				.makeOrGet();
		
		createIndex(NAME_INDEX, nameIndex, descendingNameIndex, new Fun.Function2<String, Tuple2<String, String>, Poll>() {
		   	@Override
		    public String run(Tuple2<String, String> key, Poll value) {
		   		return value.getName();
		    }
		});
		
		//CREATOR INDEX
		NavigableSet<Tuple2<String, Tuple2<String, String>>> creatorIndex = database.createTreeSet("polls_index_creator")
				.comparator(Fun.COMPARATOR)
				.makeOrGet();
		
		NavigableSet<Tuple2<String, Tuple2<String, String>>> descendingCreatorIndex = database.createTreeSet("polls_index_creator_descending")
				.comparator(new ReverseComparator(Fun.COMPARATOR))
				.makeOrGet();
		
		createIndex(CREATOR_INDEX, creatorIndex, descendingCreatorIndex, new Fun.Function2<String, Tuple2<String, String>, Poll>() {
		   	@Override
		    public String run(Tuple2<String, String> key, Poll poll) {
		   		return key.a;
		    }
		});*/
    }

    @Override
    protected Map<Tuple2<String, String>, ItemCls> getMap(DB database) {
        //OPEN MAP
        return database.createTreeMap(this.name)
                .keySerializer(BTreeKeySerializer.TUPLE2)
                .valueSerializer(new ItemSerializer(this.type))
                .counterEnable()
                .makeOrGet();
    }

    @Override
    protected Map<Tuple2<String, String>, ItemCls> getMemoryMap() {
        return new TreeMap<Tuple2<String, String>, ItemCls>(Fun.TUPLE2_COMPARATOR);
    }

    @Override
    protected ItemCls getDefaultValue() {
        return null;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public List<ItemCls> get(Account account) {
        List<ItemCls> items = new ArrayList<ItemCls>();

        try {
            Map<Tuple2<String, String>, ItemCls> accountItems = ((BTreeMap) this.map).subMap(
                    Fun.t2(account.getAddress(), null),
                    Fun.t2(account.getAddress(), Fun.HI()));

            //GET ITERATOR
            Iterator<ItemCls> iterator = accountItems.values().iterator();

            while (iterator.hasNext()) {
                items.add(iterator.next());
            }
        } catch (Exception e) {
            //ERROR
            LOGGER.error(e.getMessage(), e);
        }

        return items;
    }

    public List<Pair<Account, ItemCls>> get(List<Account> accounts) {
        List<Pair<Account, ItemCls>> items = new ArrayList<Pair<Account, ItemCls>>();

        try {
            //FOR EACH ACCOUNTS
            synchronized (accounts) {
                for (Account account : accounts) {
                    List<ItemCls> accountItems = get(account);
                    for (ItemCls item : accountItems) {
                        items.add(new Pair<Account, ItemCls>(account, item));
                    }
                }
            }
        } catch (Exception e) {
            //ERROR
            LOGGER.error(e.getMessage(), e);
        }

        return items;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public void delete(Account account) {
        //GET ALL POLLS THAT BELONG TO THAT ADDRESS
        Map<Tuple2<String, String>, ItemCls> accountItems = ((BTreeMap) this.map).subMap(
                Fun.t2(account.getAddress(), null),
                Fun.t2(account.getAddress(), Fun.HI()));

        //DELETE NAMES
        for (Tuple2<String, String> key : accountItems.keySet()) {
            this.delete(key);
        }
    }

    public void delete(String address, byte[] reference) {
        this.delete(new Tuple2<String, String>(address, new String(reference)));
    }
	
	/*
	public void deleteAll1(List<Account> accounts)
	{
		for(Account account: accounts)
		{
			this.delete(account);
		}
	}
	*/

    public boolean add(String address, byte[] reference, ItemCls item) {
        return this.set(new Tuple2<String, String>(address, new String(reference)), item);
    }
	
	/*
	public void addAll1(Map<Account, List<ItemCls>> items)
	{
		//FOR EACH ACCOUNT
	    for(Account account: items.keySet())
	    {
	    	//FOR EACH TRANSACTION
	    	for(ItemCls item: items.get(account))
	    	{
	    		this.add(item);
	    	}
	    }
	}
    */
}
