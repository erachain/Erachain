package database;

import java.util.HashMap;
//import java.util.List;
import java.util.Map;
//import java.util.TreeMap;
//import java.util.TreeSet;

import org.mapdb.DB;
import org.mapdb.Fun.Tuple2;
//import org.mapdb.Fun.Tuple3;

//import core.account.Account;
import database.DBSet;

// 
// account.address + current block.Height ->
//   -> last making blockHeight
// last forged block for ADDRESS -> by height = 0
public class AddressForging extends DBMap<Tuple2<String, Integer>, Integer> 
{
	private Map<Integer, Integer> observableData = new HashMap<Integer, Integer>();
	
	
	public AddressForging(DBSet databaseSet, DB database)
	{
		super(databaseSet, database);
	}

	public AddressForging(AddressForging parent) 
	{
		super(parent, null);
	}
	
	protected void createIndexes(DB database){}

	@Override
	
	protected Map<Tuple2<String, Integer>, Integer> getMap(DB database) 
	{
		//OPEN MAP
		return database.getTreeMap("address_forging");
	}

	@Override
	protected Map<Tuple2<String, Integer>, Integer> getMemoryMap() 
	{
		return new HashMap<Tuple2<String, Integer>, Integer>();
	}

	@Override
	protected Integer getDefaultValue() 
	{
		return -1;
	}
	
	@Override
	protected Map<Integer, Integer> getObservableData() 
	{
		return this.observableData;
	}

	public Integer get(String address, int height) 
	{
		return this.get(new Tuple2<String, Integer>(address, height));
	}	
	public void set(String address, int height, int previosHeight) 
	{
		// TODO some error here 
		if (height > previosHeight)
			this.set(new Tuple2<String, Integer>(address, height), previosHeight);
	}	
	public void delete(String address, int height) 
	{
		this.delete(new Tuple2<String, Integer>(address, height));
	}	
	public Integer getLast(String address) 
	{
		return this.get(new Tuple2<String, Integer>(address, 0));
	}	
	public void setLast(String address, int previosHeight) 
	{
		if ("77tH5ZnvcrSL5a9AordMddVs61x5Pzfj9g" == address) {
			assert(true);
		}
		this.set(new Tuple2<String, Integer>(address, 0), previosHeight);
	}	
}
