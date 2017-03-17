package database;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
//import java.util.List;
import java.util.Map;
//import java.util.TreeMap;
//import java.util.TreeSet;
import java.util.TreeMap;

import org.mapdb.BTreeKeySerializer;
import org.mapdb.BTreeMap;
import org.mapdb.DB;
import org.mapdb.Fun;
import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple3;

import com.google.common.primitives.SignedBytes;
import com.google.common.primitives.UnsignedBytes;

import core.transaction.Transaction;
//import core.account.Account;
import database.DBSet;
import network.Peer;

// 
// account.address Creditor + asset key + account.address Debtor -> sum + Int Int (Block + seeqNo trunsaction
public class Credit_AddressesMap extends DBMap<Tuple3<String, Long, String>, BigDecimal> 
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
	protected Map<Tuple3<String, Long, String>, BigDecimal> getMap(DB database) 
	{
		//OPEN MAP
		return database.createTreeMap("credit_debt")
    		.keySerializer(BTreeKeySerializer.TUPLE3)
    		//.comparator(UnsignedBytes.lexicographicalComparator())
			//.comparator(Fun.COMPARATOR)
			//.comparator(Fun.COMPARABLE_ARRAY_COMPARATOR)
            //.comparator(SignedBytes.lexicographicalComparator())
			.makeOrGet();
	}

	@Override
	protected Map<Tuple3<String, Long, String>, BigDecimal> getMemoryMap() 
	{
		return new TreeMap<Tuple3<String, Long, String>, BigDecimal>();
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

	public BigDecimal add(Tuple3<String, Long, String> key, BigDecimal amount) 
	{
		BigDecimal summ = this.get(key).add(amount);
		this.set(key, summ);
		return summ;
	}	
	public BigDecimal add(String creditorAddress, long key, String debtorAddress, BigDecimal amount) 
	{
		return this.add(new Tuple3<String, Long, String>(creditorAddress, key, debtorAddress), amount);
	}	

	public BigDecimal sub(Tuple3<String, Long, String> key, BigDecimal amount) 
	{
		BigDecimal summ = this.get(key).subtract(amount);
		this.set(key, summ);
		return summ;
	}	

	public BigDecimal get(String creditorAddress, long key, String debtorAddress) 
	{
		return this.get(new Tuple3<String, Long, String>(creditorAddress, key, debtorAddress));
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List<Tuple2<Tuple3<String, Long, String>, BigDecimal>> getList(String creditorAddress, long key)
	{	
		BTreeMap map = (BTreeMap) this.map;
		//GET ALL TRANSACTIONS THAT BELONG TO THAT ADDRESS
		Collection<Tuple3> keys = ((BTreeMap<Tuple3, Transaction>) map).subMap(
				Fun.t3(creditorAddress, key, null),
				Fun.t3(creditorAddress, key, Fun.HI())).keySet();
		
		//DELETE TRANSACTIONS
		List<Tuple2<Tuple3<String, Long, String>, BigDecimal>> result = new ArrayList<Tuple2<Tuple3<String, Long, String>, BigDecimal>>();

		for(Tuple3<String, Long, String> keyMap: keys)
		{
			result.add(new Tuple2<Tuple3<String, Long, String>, BigDecimal>(keyMap, this.get(keyMap)));
		}
		
		return result;
	}

	public void delete(String creditorAddress, long key, String debtorAddress) 
	{
		this.delete(new Tuple3<String, Long, String>(creditorAddress, key, debtorAddress));
	}	
}
