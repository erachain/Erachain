package database;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.mapdb.BTreeKeySerializer;
import org.mapdb.BTreeMap;
import org.mapdb.Bind;
import org.mapdb.DB;
import org.mapdb.Fun;
import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple3;

import core.account.Account;
import utils.ObserverMessage;
import database.DBSet;

public class BalanceMapHKey extends DBMap<Tuple2<String, byte[]>, BigDecimal> 
{	
	private Map<Integer, Integer> observableData = new HashMap<Integer, Integer>();
	
	@SuppressWarnings("rawtypes")
	private BTreeMap hKeyMap;
	
	public BalanceMapHKey(DBSet databaseSet, DB database)
	{
		super(databaseSet, database);
		
		this.observableData.put(DBMap.NOTIFY_ADD, ObserverMessage.ADD_BALANCE_TYPE);
		this.observableData.put(DBMap.NOTIFY_REMOVE, ObserverMessage.REMOVE_BALANCE_TYPE);
		//this.observableData.put(DBMap.NOTIFY_LIST, ObserverMessage.LIST_BALANCE_TYPE);
	}

	public BalanceMapHKey(BalanceMapHKey parent) 
	{
		super(parent);
	}
	
	protected void createIndexes(DB database){}

	@SuppressWarnings({ "unchecked"})
	@Override
	protected Map<Tuple2<String, byte[]>, BigDecimal> getMap(DB database) 
	{
		//OPEN MAP
		BTreeMap<Tuple2<String, byte[]>, BigDecimal> map = database.createTreeMap("balancesHKey")
				.keySerializer(BTreeKeySerializer.TUPLE2)
				.counterEnable()
				.makeOrGet();

		//HAVE/WANT KEY
		this.hKeyMap = database.createTreeMap("balancesHKey_key")
				.comparator(Fun.COMPARATOR)
				.counterEnable()
				.makeOrGet();
		
		//BIND ASSET KEY
		Bind.secondaryKey(map, this.hKeyMap, new Fun.Function2<Tuple3<byte[], BigDecimal, String>, Tuple2<String, byte[]>, BigDecimal>() {
			@Override
			public Tuple3<byte[], BigDecimal, String> run(Tuple2<String, byte[]> key, BigDecimal value) {
				return new Tuple3<byte[], BigDecimal, String>(key.b, value.negate(), key.a);
			}	
		});
		
		//RETURN
		return map;
	}

	@Override
	protected Map<Tuple2<String, byte[]>, BigDecimal> getMemoryMap() 
	{
		return new TreeMap<Tuple2<String, byte[]>, BigDecimal>(Fun.TUPLE2_COMPARATOR);
	}


	@Override
	protected BigDecimal getDefaultValue() 
	{
		return BigDecimal.ZERO.setScale(8);
	}
	
	@Override
	protected Map<Integer, Integer> getObservableData() 
	{
		return this.observableData;
	}
	
		
	public void set(String address, byte[] hkey, BigDecimal value)
	{
		this.set(new Tuple2<String, byte[]>(address, hkey), value);
	}
		
	public BigDecimal get(String address, byte[] hkey)
	{
		return this.get(new Tuple2<String, byte[]>(address, hkey));
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public SortableList<Tuple2<String, byte[]>, BigDecimal> getBalancesSortableList(byte[] hkey)
	{
		//FILTER ALL HKEYS
		Collection<Tuple2<String, byte[]>> hkeys = ((BTreeMap<Tuple3, Tuple2<String, byte[]>>) this.hKeyMap).subMap(
				Fun.t3(hkey, null, null),
				Fun.t3(hkey, Fun.HI(), Fun.HI())).values();
		
		//RETURN
		return new SortableList<Tuple2<String, byte[]>, BigDecimal>(this, hkeys);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public SortableList<Tuple2<String, byte[]>, BigDecimal> getBalancesSortableList(Account account) 
	{
		BTreeMap map = (BTreeMap) this.map;
		
		//FILTER ALL HKEYS
		Collection hkeys = ((BTreeMap<Tuple2, BigDecimal>) map).subMap(
				Fun.t2(account.getAddress(), null),
				Fun.t2(account.getAddress(), Fun.HI())).keySet();
		
		//RETURN
		return new SortableList<Tuple2<String, byte[]>, BigDecimal>(this, hkeys);
	}
}
