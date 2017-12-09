package datachain;

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
import core.item.assets.AssetCls;
import core.transaction.Transaction;
import database.DBMap;
import datachain.DCSet;
import utils.ObserverMessage;

// balances for all account in blockchain
// TODO SOFT HARD TRUE
// -> in_OWN, in_RENT, on_HOLD = in_USE (TOTAL on HAND) 
public class ItemAssetBalanceMap extends DCMap<Tuple2<String, Long>, Tuple3<BigDecimal, BigDecimal, BigDecimal>> 
{
	//public static final long FEE_KEY = AssetCls.DILE_KEY;
	
	private Map<Integer, Integer> observableData = new HashMap<Integer, Integer>();
	
	@SuppressWarnings("rawtypes")
	private BTreeMap assetKeyMap;
	
	public ItemAssetBalanceMap(DCSet databaseSet, DB database)
	{
		super(databaseSet, database);

		if (databaseSet.isWithObserver()) {
			this.observableData.put(DBMap.NOTIFY_RESET, ObserverMessage.RESET_BALANCE_TYPE);	
			if (databaseSet.isDynamicGUI()) {
				this.observableData.put(DBMap.NOTIFY_ADD, ObserverMessage.ADD_BALANCE_TYPE);
				this.observableData.put(DBMap.NOTIFY_REMOVE, ObserverMessage.REMOVE_BALANCE_TYPE);
			}
			this.observableData.put(DBMap.NOTIFY_LIST, ObserverMessage.LIST_BALANCE_TYPE);
		}
	}

	public ItemAssetBalanceMap(ItemAssetBalanceMap parent) 
	{
		super(parent, null);
	}
	
	protected void createIndexes(DB database){}

	@SuppressWarnings({ "unchecked"})
	@Override
	protected Map<Tuple2<String, Long>, Tuple3<BigDecimal, BigDecimal, BigDecimal>> getMap(DB database) 
	{
		//OPEN MAP
		BTreeMap<Tuple2<String, Long>, Tuple3<BigDecimal, BigDecimal, BigDecimal>> map =  database.createTreeMap("balances")
				.keySerializer(BTreeKeySerializer.TUPLE2)
				.counterEnable()
				.makeOrGet();
		
		//HAVE/WANT KEY
		this.assetKeyMap = database.createTreeMap("balances_key_asset")
				.comparator(Fun.COMPARATOR)
				.counterEnable()
				.makeOrGet();
		
		//BIND ASSET KEY
		/*
		Bind.secondaryKey(map, this.assetKeyMap, new Fun.Function2<Tuple3<Long, BigDecimal, String>, Tuple2<String, Long>, BigDecimal>() {
			@Override
			public Tuple3<Long, BigDecimal, String> run(Tuple2<String, Long> key, BigDecimal value) {
				return new Tuple3<Long, BigDecimal, String>(key.b, value.negate(), key.a);
			}	
		});*/
		Bind.secondaryKey(map, this.assetKeyMap, new Fun.Function2<Tuple3<Long, Tuple3<BigDecimal, BigDecimal, BigDecimal>, String>,
				Tuple2<String, Long>, Tuple3<BigDecimal, BigDecimal, BigDecimal>>() {
			@Override
			public Tuple3<Long, Tuple3<BigDecimal, BigDecimal, BigDecimal>, String> run(Tuple2<String, Long> key, Tuple3<BigDecimal, BigDecimal, BigDecimal> value) {
				return new Tuple3<Long, Tuple3<BigDecimal, BigDecimal, BigDecimal>, String>(
						key.b, new Tuple3<BigDecimal, BigDecimal, BigDecimal>(value.a.negate(), value.b.negate(), value.c.negate()), key.a);
			}	
		});
		
		//RETURN
		return map;
	}

	@Override
	protected Map<Tuple2<String, Long>, Tuple3<BigDecimal, BigDecimal, BigDecimal>> getMemoryMap() 
	{
		return new TreeMap<Tuple2<String, Long>, Tuple3<BigDecimal, BigDecimal, BigDecimal>>(Fun.TUPLE2_COMPARATOR);
	}

	@Override
	protected Tuple3<BigDecimal, BigDecimal, BigDecimal> getDefaultValue() 
	{
		return new Tuple3<BigDecimal, BigDecimal, BigDecimal>(BigDecimal.ZERO.setScale(8), BigDecimal.ZERO.setScale(8), BigDecimal.ZERO.setScale(8));
	}
	
	@Override
	protected Map<Integer, Integer> getObservableData() 
	{
		return this.observableData;
	}
	
	/*
	public void set(String address, BigDecimal value)
	{
		this.set(address, FEE_KEY, value);
	}
	*/
	
	public void set(String address, long key, Tuple3<BigDecimal, BigDecimal, BigDecimal> value)
	{
		if (key < 0)
			key = -key;

		this.set(new Tuple2<String, Long>(address, key), value);
	}
	
	/*
	public BigDecimal get(String address)
	{
		return this.get(address, FEE_KEY);
	}
	*/
	
	public Tuple3<BigDecimal, BigDecimal, BigDecimal> get(String address, long key)
	{
		if (key < 0)
			key = -key;
		
		
		Tuple3<BigDecimal, BigDecimal, BigDecimal> value = this.get(new Tuple2<String, Long>(address, key));

		/*
		// TODO for TEST
		// FOR TEST NET
		if (key == Transaction.FEE_KEY &&
				value.a.compareTo(BigDecimal.ONE.setScale(8)) < 0) {
					
			return new Tuple3<BigDecimal, BigDecimal, BigDecimal>(
					BigDecimal.ONE.setScale(8), BigDecimal.ZERO.setScale(8), BigDecimal.ZERO.setScale(8));
			
		}
		*/

		return value;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public SortableList<Tuple2<String, Long>, Tuple3<BigDecimal, BigDecimal, BigDecimal>> getBalancesSortableList(long key)
	{
		if (key < 0)
			key = -key;
		
		//FILTER ALL KEYS
		Collection<Tuple2<String, Long>> keys = ((BTreeMap<Tuple3, Tuple2<String, Long>>) this.assetKeyMap).subMap(
				Fun.t3(key, null, null),
				Fun.t3(key, Fun.HI(), Fun.HI())).values();
		
		//RETURN
		return new SortableList<Tuple2<String, Long>, Tuple3<BigDecimal, BigDecimal, BigDecimal>>(this, keys);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public SortableList<Tuple2<String, Long>, Tuple3<BigDecimal, BigDecimal, BigDecimal>> getBalancesSortableList(Account account) 
	{
		BTreeMap map = (BTreeMap) this.map;
		
		//FILTER ALL KEYS
		Collection keys = ((BTreeMap<Tuple2, BigDecimal>) map).subMap(
				Fun.t2(account.getAddress(), null),
				Fun.t2(account.getAddress(), Fun.HI())).keySet();
		
		// TODO - ERROR PARENT not userd!
		
		//RETURN
		return new SortableList<Tuple2<String, Long>, Tuple3<BigDecimal, BigDecimal, BigDecimal>>(this, keys);
	}
}
