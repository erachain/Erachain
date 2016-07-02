package database;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.mapdb.BTreeMap;
import org.mapdb.DB;
import org.mapdb.Fun;
import org.mapdb.Fun.Tuple2;

import core.account.Account;
import core.account.PublicKeyAccount;
import database.DBSet;

/// !!!! not used now

// seek notes by address
// account.addres + note.key -> <tx.block + tx.seq>
public class AddressNoteMap extends DBMap<Tuple2<String, Long>, Tuple2<Integer, Integer>> 
{
	private Map<Integer, Integer> observableData = new HashMap<Integer, Integer>();
	
	public AddressNoteMap(DBSet databaseSet, DB database)
	{
		super(databaseSet, database);
	}

	public AddressNoteMap(AddressNoteMap parent) 
	{
		super(parent);
	}
	
	protected void createIndexes(DB database){}

	@Override
	
	protected Map<Tuple2<String, Long>, Tuple2<Integer, Integer>> getMap(DB database) 
	{
		//OPEN MAP
		return database.getTreeMap("address_note");
	}

	@Override
	protected Map<Tuple2<String, Long>, Tuple2<Integer, Integer>> getMemoryMap() 
	{
		return new HashMap<Tuple2<String, Long>, Tuple2<Integer, Integer>>();
	}

	@Override
	protected Tuple2<Integer, Integer> getDefaultValue() 
	{
		return null;
	}
	
	@Override
	protected Map<Integer, Integer> getObservableData() 
	{
		return this.observableData;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List<Tuple2<Integer, Integer>> get(Account account)
	{
		List<Tuple2<Integer, Integer>> items = new ArrayList<Tuple2<Integer, Integer>>();
		
		try
		{
			Map<Tuple2<String, String>, Tuple2<Integer, Integer>> accountItems = ((BTreeMap) this.map).subMap(
					Fun.t2(account.getAddress(), null),
					Fun.t2(account.getAddress(), Fun.HI()));
			
			//GET ITERATOR
			Iterator<Tuple2<Integer, Integer>> iterator = accountItems.values().iterator();
			
			while(iterator.hasNext())
			{
				items.add(iterator.next());
			}
		}
		catch(Exception e)
		{
			//ERROR
			LOGGER.error(e.getMessage(),e);
		}
		
		return items;
	}

	public Tuple2<Integer, Integer> get(Account account, Long noteKey) 
	{
		return this.get(account.getAddress(), noteKey);
	}
	
	public Tuple2<Integer, Integer> get(String address, Long noteKey) 
	{
		return this.get(new Tuple2<String, Long>(address, noteKey));
	}
	public Tuple2<Integer, Integer> get(String address) 
	{
		return this.get(new Tuple2<String, Long>(address, null));
	}
	
	public void set(Account account, Long timestampRef, Tuple2<Integer, Integer> recordRef)
	{
		this.set(new Tuple2<String, Long>(account.getAddress(), timestampRef), recordRef);
	}
	public void set(Account account, Tuple2<Integer, Integer> recordRef)
	{
		this.set(new Tuple2<String, Long>(account.getAddress(), null), recordRef);
	}
	
	// not need for chain
	public void delete(PublicKeyAccount account)
	{

		Map<Tuple2<String, Long>, Tuple2<Integer, Integer>> keys = ((BTreeMap) this.map).subMap(
		//BTreeMap keys = ((BTreeMap) this.assetsBalanceMap).subMap(
				Fun.t2(account.getAddress(), null),
				Fun.t2(account.getAddress(), Fun.HI()));
		
		//DELETE KEYS
		for(Tuple2<String, Long> key: keys.keySet())
		{
			this.delete(key);
		}

	}
	
	public void delete(Account account, Long recordRef)
	{
		this.delete(new Tuple2<String, Long>(account.getAddress(), recordRef));
	}
}
