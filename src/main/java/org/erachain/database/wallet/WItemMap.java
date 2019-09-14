package org.erachain.database.wallet;

import com.google.common.primitives.Longs;
import org.erachain.core.account.Account;
import org.erachain.core.item.ItemCls;
import org.erachain.database.AutoKeyDBMap;
import org.erachain.dbs.DBMap;
import org.erachain.database.serializer.LongItemSerializer;
import org.erachain.utils.Pair;
import org.mapdb.*;
import org.mapdb.Fun.Tuple2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

// TODO reference as TIMESTAMP of transaction

/**
 * key: Address + refDB</br>
 * Value: autoIncrement + Object
 */
public class WItemMap extends AutoKeyDBMap<Tuple2<Long, Long>, Tuple2<Long, ItemCls>> {

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

        // не создаем КАрту и Индексы так как не известно пока ИМЯ и ТИП
        super(dWSet);

        this.type = type;
        this.name = name;

        // Теперь задаем БАЗУ
        this.database = database;

        // ИМЯ и ТИП заданы, создаем карту и ИНдексы
        getMap();

        makeAutoKey(database, (Bind.MapWithModificationListener)map, name + "_wak");

        this.createIndexes();

        if (databaseSet.isWithObserver()) {
            observableData = new HashMap<Integer, Integer>(8, 1);
            this.observableData.put(DBMap.NOTIFY_RESET, observeReset);
            this.observableData.put(DBMap.NOTIFY_LIST, observeList);
            this.observableData.put(DBMap.NOTIFY_ADD, observeAdd);
            this.observableData.put(DBMap.NOTIFY_REMOVE, observeRemove);
        }
    }

    //@SuppressWarnings({ "unchecked", "rawtypes" })
    protected void createIndexes() {
    }

    @Override
    protected void getMap() {
        //OPEN MAP
        if (this.name == null)
            return;

        map = database.createTreeMap(this.name)
                .keySerializer(BTreeKeySerializer.TUPLE2)
                .valueSerializer(new LongItemSerializer(this.type))
                .counterEnable()
                .makeOrGet();
    }

    @Override
    protected void getMemoryMap() {
        map = new TreeMap<Tuple2<Long, Long>, Tuple2<Long, ItemCls>>(Fun.TUPLE2_COMPARATOR);
    }

    @Override
    protected Tuple2<Long, ItemCls> getDefaultValue() {
        return null;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public List<Tuple2<Long, ItemCls>> get(Account account) {
        List<Tuple2<Long, ItemCls>> items = new ArrayList<Tuple2<Long, ItemCls>>();

        try {
            Map<Tuple2<Long, Long>, Tuple2<Long, ItemCls>> accountItems = ((BTreeMap) this.map).subMap(
                    Fun.t2(Longs.fromByteArray(account.getShortAddressBytes()), null),
                    Fun.t2(Longs.fromByteArray(account.getShortAddressBytes()), Fun.HI()));

            //GET ITERATOR
            Iterator<Tuple2<Long, ItemCls>> iterator = accountItems.values().iterator();

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
                    List<Tuple2<Long, ItemCls>> accountItems = get(account);
                    for (Tuple2<Long, ItemCls> item : accountItems) {
                        items.add(new Pair<Account, ItemCls>(account, item.b));
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
        Map<Tuple2<Long, Long>, Tuple2<Long, ItemCls>> accountItems = ((BTreeMap) this.map).subMap(
                Fun.t2(Longs.fromByteArray(account.getShortAddressBytes()), null),
                Fun.t2(Longs.fromByteArray(account.getShortAddressBytes()), Fun.HI()));

        //DELETE NAMES
        for (Tuple2<Long, Long> key : accountItems.keySet()) {
            this.delete(key);
        }
    }

    public void delete(Account account, long refDB) {
        this.delete(new Tuple2<Long, Long>(Longs.fromByteArray(account.getShortAddressBytes()),
                refDB));
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

    public boolean add(Account account, long refDB, ItemCls item) {
        return this.set(new Tuple2<Long, Long>(Longs.fromByteArray(account.getShortAddressBytes()),
                refDB), new Tuple2<Long, ItemCls>(null, item));
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
