package database;

import java.util.HashMap;
import java.util.Map;

import org.mapdb.DB;
import org.mapdb.Fun.Tuple2;

import core.account.Account;
import database.DBSet;

// seek reference to tx_Parent by address
// account.addres + tx1.timestamp -> <tx2.signature>
public class AddressTime_SignatureMap extends DBMap<Tuple2<String, Long>, byte[]> 
{
	private Map<Integer, Integer> observableData = new HashMap<Integer, Integer>();
	
	public AddressTime_SignatureMap(DBSet databaseSet, DB database)
	{
		super(databaseSet, database);
	}

	public AddressTime_SignatureMap(AddressTime_SignatureMap parent) 
	{
		super(parent);
	}
	
	protected void createIndexes(DB database){}

	@Override
	
	protected Map<Tuple2<String, Long>, byte[]> getMap(DB database) 
	{
		//OPEN MAP
		return database.getTreeMap("references");
	}

	@Override
	protected Map<Tuple2<String, Long>, byte[]> getMemoryMap() 
	{
		return new HashMap<Tuple2<String, Long>, byte[]>();
	}

	@Override
	protected byte[] getDefaultValue() 
	{
		return null;
	}
	
	@Override
	protected Map<Integer, Integer> getObservableData() 
	{
		return this.observableData;
	}

	public byte[] get(Account account, Long timestamp) 
	{
		return this.get(account.getAddress(), timestamp);
	}
	
	public byte[] get(String address, Long timestamp) 
	{
		return this.get(new Tuple2<String, Long>(address, timestamp));
	}
	public byte[] get(String address) 
	{
		return this.get(new Tuple2<String, Long>(address, null));
	}
	
	public void set(Account account, Long timestampRef, byte[] signtureRef)
	{
		this.set(new Tuple2<String, Long>(account.getAddress(), timestampRef), signtureRef);
	}
	public void set(Account account, byte[] signtureRef)
	{
		this.set(new Tuple2<String, Long>(account.getAddress(), null), signtureRef);
	}
	
	public void delete(Account account, Long timestampRef)
	{
		this.delete(new Tuple2<String, Long>(account.getAddress(), timestampRef));
	}
}
