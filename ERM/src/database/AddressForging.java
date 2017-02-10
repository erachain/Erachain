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
	private void set(String address, int height, int previosHeight) 
	{

		if (height > previosHeight) {
			this.set(new Tuple2<String, Integer>(address, height), previosHeight);
		} else {
			int rr = 1;
			rr++;
		}

		this.setLast(address, height);
		
	}	
	public void set(String address, int height) 
	{

		int previosHeight = this.getLast(address);
		if (previosHeight == -1) {
			//previosHeight = height - 1;
		}
		this.set(address, height, previosHeight);
		
	}	

	public void delete(String address, int height) 
	{
		/*
		// test
		if (address.equals("77QnJnSbS9EeGBa2LPZFZKVwjPwzeAxjmy")) {
			// err
			int hh = this.get(address, height);
			hh++;
		}
		*/
		
		if (height < 3) {
			// not delete GENESIS forging data for all accounts
			return;
		}
		Tuple2<String, Integer> key = new Tuple2<String, Integer>(address, height);
		int prevHeight = this.get(key);
		this.delete(key);
		this.setLast(address, prevHeight);
		
	}	
	public Integer getLast(String address) 
	{
		return this.get(new Tuple2<String, Integer>(address, 0));
	}	
	private void setLast(String address, int previosHeight) 
	{
		if (address.equals("77QnJnSbS9EeGBa2LPZFZKVwjPwzeAxjmy")) {
			// err
			int hh = this.getLast(address);
			hh++;
		}
		
		this.set(new Tuple2<String, Integer>(address, 0), previosHeight);
	}	
}
