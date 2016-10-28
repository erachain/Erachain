package database;

import java.math.BigDecimal;
import java.util.HashMap;
//import java.util.List;
import java.util.Map;
//import java.util.TreeMap;
//import java.util.TreeSet;
import java.util.TreeMap;

import org.mapdb.BTreeKeySerializer;
import org.mapdb.DB;
import org.mapdb.Fun.Tuple2;
//import org.mapdb.Fun.Tuple3;
import org.mapdb.Fun.Tuple3;

import com.google.common.primitives.UnsignedBytes;

//import core.account.Account;
import database.DBSet;

// 
// account.address Creditor + account.address Debtor + asset key -> sum
public class Credit_AddressesMap extends DBMap<Tuple3<String, String, Long>, BigDecimal> 
{
	private Map<Integer, Integer> observableData = new HashMap<Integer, Integer>();
	
	
	public Credit_AddressesMap(DBSet databaseSet, DB database)
	{
		super(databaseSet, database);
	}

	public Credit_AddressesMap(Credit_AddressesMap parent) 
	{
		super(parent, null);
	}
	
	protected void createIndexes(DB database){}

	@Override
	protected Map<Tuple3<String, String, Long>, BigDecimal> getMap(DB database) 
	{
		//OPEN MAP
		return database.getTreeMap("credit_debt");
	}

	@Override
	protected Map<Tuple3<String, String, Long>, BigDecimal> getMemoryMap() 
	{
		return new TreeMap<Tuple3<String, String, Long>, BigDecimal>();
	}

	@Override
	protected BigDecimal getDefaultValue() 
	{
		return BigDecimal.ZERO;
	}
	
	@Override
	protected Map<Integer, Integer> getObservableData() 
	{
		return this.observableData;
	}

	public BigDecimal add(Tuple3<String, String, Long> key, BigDecimal amount) 
	{
		BigDecimal summ = this.get(key).add(amount);
		this.set(key, summ);
		return summ;
	}	
	public BigDecimal add(String creditorAddress, String debtorAddress, long key, BigDecimal amount) 
	{
		return this.add(new Tuple3<String, String, Long>(creditorAddress, debtorAddress, key), amount);
	}	

	public BigDecimal sub(Tuple3<String, String, Long> key, BigDecimal amount) 
	{
		BigDecimal summ = this.get(key).subtract(amount);
		this.set(key, summ);
		return summ;
	}	

	public BigDecimal get(String creditorAddress, String debtorAddress, long key) 
	{
		return this.get(new Tuple3<String, String, Long>(creditorAddress, debtorAddress, key));
	}	
	public void delete(String creditorAddress, String debtorAddress, long key) 
	{
		this.delete(new Tuple3<String, String, Long>(creditorAddress, debtorAddress, key));
	}	
}
