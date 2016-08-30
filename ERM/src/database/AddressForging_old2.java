package database;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
//import java.util.TreeMap;
import java.util.TreeSet;

import org.mapdb.DB;
import org.mapdb.Fun.Tuple3;

import core.account.Account;
import database.DBSet;

// 
// account.address + current block.Height ->
//   -> last making blockHeight
// в общем запоминаем послений блок который был сгенерирован эти аккаунтом
// баланс который был на этом счету в момент генерации блока и список адресов куда были переведдены монеты после генреации
// так чтобы на те адрес создать тоже такую же запись начальную 
public class AddressForging_old2 extends DBMap<String, Tuple3<Integer, Integer, TreeSet<String>>> 
{
	private Map<Integer, Integer> observableData = new HashMap<Integer, Integer>();
	
	public AddressForging_old2(DBSet databaseSet, DB database)
	{
		super(databaseSet, database);
	}

	public AddressForging_old2(AddressForging_old2 parent) 
	{
		super(parent, null);
	}
	
	protected void createIndexes(DB database){}

	@Override
	
	protected Map<String, Tuple3<Integer, Integer, TreeSet<String>>> getMap(DB database) 
	{
		//OPEN MAP
		return database.getTreeMap("address_forging");
	}

	@Override
	protected Map<String, Tuple3<Integer, Integer, TreeSet<String>>> getMemoryMap() 
	{
		return new HashMap<String, Tuple3<Integer, Integer, TreeSet<String>>>();
	}

	@Override
	protected Tuple3<Integer, Integer, TreeSet<String>> getDefaultValue() 
	{
		return new Tuple3<Integer, Integer, TreeSet<String>>(0, 0, new TreeSet<String>());
	}
	
	@Override
	protected Map<Integer, Integer> getObservableData() 
	{
		return this.observableData;
	}

	public Tuple3<Integer, Integer, TreeSet<String>> get(Account account) 
	{
		return this.get(account.getAddress());
	}	
}
