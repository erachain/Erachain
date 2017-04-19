package database;
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
import database.wallet.WalletDatabase;
import utils.ObserverMessage;

//import database.serializer.TransactionSerializer;

// vouched record (BlockNo, RecNo) -> ERM balabce + List of vouchers records
public class VouchRecordMap extends DBMap<Tuple2<Integer, Integer>, Tuple2<BigDecimal, List<Tuple2<Integer, Integer>>>>
{
	private Map<Integer, Integer> observableData = new HashMap<Integer, Integer>();
		
	public VouchRecordMap(DBSet databaseSet, DB database)
	{
		super(databaseSet, database);
		
		this.observableData.put(DBMap.NOTIFY_ADD, ObserverMessage.ADD_VOUCH_TYPE);
		this.observableData.put(DBMap.NOTIFY_REMOVE, ObserverMessage.REMOVE_VOUCH_TYPE);
		this.observableData.put(DBMap.NOTIFY_LIST, ObserverMessage.LIST_VOUCH_TYPE);
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
	
	public boolean set(Tuple2<Integer, Integer>key, Tuple2<BigDecimal, List<Tuple2<Integer, Integer>>> value)
	{
		this.addUses();
		try
		{
			//Controller.getInstance().
			
			 Tuple2<BigDecimal, List<Tuple2<Integer, Integer>>> old = this.map.put(key, value);
			
			if(this.deleted != null)
			{
				this.deleted.remove(key);
			}
			
			//COMMIT
			//if(this.databaseSet != null)
			if(this.parent == null)
			{
				// IT IS NOT FORK
				if(!(this.databaseSet instanceof WalletDatabase
						&& Controller.getInstance().isProcessingWalletSynchronize()))
				{
					this.databaseSet.commit();
				}
			}
		
			//NOTIFY ADD
			if(this.getObservableData().containsKey(NOTIFY_ADD))
			{
				this.setChanged();
				if ( this.getObservableData().get(NOTIFY_ADD).equals( ObserverMessage.ADD_AT_TX_TYPE ) )
				{
					this.notifyObservers(new ObserverMessage(this.getObservableData().get(NOTIFY_ADD), new Tuple2<Tuple2<Integer, Integer>, Tuple2<BigDecimal, List<Tuple2<Integer, Integer>>>>(key,value)));
				}
				else
				{
					this.notifyObservers(new ObserverMessage(this.getObservableData().get(NOTIFY_ADD), new Tuple2<Tuple2<Integer, Integer>, Tuple2<BigDecimal, List<Tuple2<Integer, Integer>>>>(key,value))); //new SortableList<Tuple2<Integer, Integer>,  Tuple2<BigDecimal, List<Tuple2<Integer, Integer>>>>(this)));
				}
			}
			
			//NOTIFY LIST
			if(this.getObservableData().containsKey(NOTIFY_LIST))
			{
				this.setChanged();
				this.notifyObservers(new ObserverMessage(this.getObservableData().get(NOTIFY_LIST), new SortableList<Tuple2<Integer, Integer>,  Tuple2<BigDecimal, List<Tuple2<Integer, Integer>>>>(this)));
			}

			this.outUses();
			return old != null;
		}
		catch(Exception e)
		{
			LOGGER.error(e.getMessage(),e);
		}

		this.outUses();
		return false;
	}
			

}
