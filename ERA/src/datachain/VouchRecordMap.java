package datachain;
//04/01 +- 

//import java.lang.reflect.Array;
import java.math.BigDecimal;
//import java.util.ArrayList;
//import java.util.Collection;
import java.util.HashMap;
//import java.util.Iterator;
import java.util.List;
import java.util.Map;
//import java.util.NavigableSet;
//import java.util.Set;
//import java.util.TreeSet;

import org.mapdb.BTreeKeySerializer;
import org.mapdb.BTreeMap;
//import org.mapdb.Bind;
import org.mapdb.DB;
import org.mapdb.DBMaker;
//import org.mapdb.Fun;
import org.mapdb.Fun.Tuple2;

import controller.Controller;
import database.DBMap;
import database.wallet.DWSet;
import utils.ObserverMessage;

//import database.serializer.TransactionSerializer;

// vouched record (BlockNo, RecNo) -> ERM balabce + List of vouchers records
public class VouchRecordMap extends DCMap<Tuple2<Integer, Integer>, Tuple2<BigDecimal, List<Tuple2<Integer, Integer>>>>
{
	private Map<Integer, Integer> observableData = new HashMap<Integer, Integer>();
		
	public VouchRecordMap(DCSet databaseSet, DB database)
	{
		super(databaseSet, database);
		
		if (databaseSet.isWithObserver()) {
			this.observableData.put(DBMap.NOTIFY_RESET, ObserverMessage.RESET_VOUCH_TYPE);
			if (databaseSet.isDynamicGUI()) {
				this.observableData.put(DBMap.NOTIFY_ADD, ObserverMessage.ADD_VOUCH_TYPE);
				this.observableData.put(DBMap.NOTIFY_REMOVE, ObserverMessage.REMOVE_VOUCH_TYPE);
			}
			this.observableData.put(DBMap.NOTIFY_LIST, ObserverMessage.LIST_VOUCH_TYPE);
		}
	}

	public VouchRecordMap(VouchRecordMap parent) 
	{
		super(parent, null);
	}
	
	protected void createIndexes(DB database)
	{
	}
	
	//@SuppressWarnings("unchecked")
	private Map<Tuple2<Integer, Integer>, Tuple2<BigDecimal, List<Tuple2<Integer, Integer>>>>  openMap(DB database)
	{
		
		BTreeMap<Tuple2<Integer, Integer>, Tuple2<BigDecimal, List<Tuple2<Integer, Integer>>>> map = 
			database.createTreeMap("vouch_records")
				.keySerializer(BTreeKeySerializer.TUPLE2)
				//.valueSerializer(new TransactionSerializer())
				.makeOrGet();
		return map;
		
	}
				

	@Override
	protected Map<Tuple2<Integer, Integer>, Tuple2<BigDecimal, List<Tuple2<Integer, Integer>>>> getMap(DB database) 
	{
		//OPEN MAP
		return openMap(database);
	}

	@Override
	protected Map<Tuple2<Integer, Integer>, Tuple2<BigDecimal, List<Tuple2<Integer, Integer>>>> getMemoryMap() 
	{
		DB database = DBMaker.newMemoryDB().make();
		
		//OPEN MAP
		return this.getMap(database);	}

	@Override
	protected Tuple2<BigDecimal, List<Tuple2<Integer, Integer>>> getDefaultValue() 
	{
		return null;
	}
	
	@Override
	protected Map<Integer, Integer> getObservableData() 
	{
		return this.observableData;
	}
		
	public Tuple2<BigDecimal, List<Tuple2<Integer, Integer>>> get(Integer height, Integer seq)
	{
		return this.get(new Tuple2<Integer, Integer>(height, seq));
	}

	public void delete(Integer height, Integer seq)
	{
		this.delete(new Tuple2<Integer, Integer>(height, seq));
	}
	
	
	public boolean add(Integer height, Integer seq, Tuple2<BigDecimal, List<Tuple2<Integer, Integer>>> value)
	{
		return this.set(new Tuple2<Integer, Integer>(height, seq), value);
	}
	
}
