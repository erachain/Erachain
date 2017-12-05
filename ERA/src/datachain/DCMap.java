package datachain;
// upd 09/03
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import org.apache.log4j.Logger;
import org.mapdb.BTreeMap;
import org.mapdb.Bind;
import org.mapdb.DB;
import org.mapdb.Fun.Function2;
import org.mapdb.Fun.Tuple2;

import controller.Controller;
import database.DBMap;
import database.wallet.DWSet;
import utils.ObserverMessage;
import utils.Pair;

public abstract class DCMap<T, U> extends Observable {
	
	protected DCMap<T, U> parent;
	protected IDB databaseSet;
	protected Map<T, U> map;
	protected List<T> deleted;
	private Map<Integer, NavigableSet<Tuple2<?, T>>> indexes;
	private boolean worked;
	
	static Logger LOGGER = Logger.getLogger(DCMap.class.getName());

	public DCMap(IDB databaseSet, DB database)
	{
		this.databaseSet = databaseSet;
		
		//OPEN MAP
	    this.map = this.getMap(database);
	    
	    //CREATE INDEXES
	    this.indexes = new HashMap<Integer, NavigableSet<Tuple2<?, T>>>();
	    this.createIndexes(database);
	}
	
	public DCMap(DCMap<T, U> parent, DCSet dcSet)
	{

		this.parent = parent;
		
		this.databaseSet = dcSet;
	    
	    //OPEN MAP
	    this.map = this.getMemoryMap();
	    this.deleted = new ArrayList<T>();
	}

	
	public DCSet getDCSet()
	{		
		return (DCSet) this.databaseSet;
	}

	
	public boolean isWorked()
	{		
		return this.worked;
	}

	protected abstract Map<T, U> getMap(DB database);
	
	protected abstract Map<T, U> getMemoryMap();
	
	protected abstract U getDefaultValue();
	
	protected abstract Map<Integer, Integer> getObservableData();
	
	protected abstract void createIndexes(DB database);
	
	@SuppressWarnings("unchecked")
	protected <V> void createIndex(int index, NavigableSet<?> indexSet, NavigableSet<?> descendingIndexSet, Function2<V, T, U> function) 
	{
		Bind.secondaryKey((BTreeMap<T, U>) this.map, (NavigableSet<Tuple2<V, T>>) indexSet, function);
		this.indexes.put(index, (NavigableSet<Tuple2<?, T>>) indexSet);
		
		Bind.secondaryKey((BTreeMap<T, U>) this.map, (NavigableSet<Tuple2<V, T>>) descendingIndexSet, function);
		this.indexes.put(index + 10000, (NavigableSet<Tuple2<?, T>>) descendingIndexSet);
	}
	
	@SuppressWarnings("unchecked")
	protected <V> void createIndexes(int index, NavigableSet<?> indexSet, NavigableSet<?> descendingIndexSet, Function2<V[], T, U> function) 
	{
		Bind.secondaryKeys((BTreeMap<T, U>) this.map, (NavigableSet<Tuple2<V, T>>) indexSet, function);
		this.indexes.put(index, (NavigableSet<Tuple2<?, T>>) indexSet);
		
		Bind.secondaryKeys((BTreeMap<T, U>) this.map, (NavigableSet<Tuple2<V, T>>) descendingIndexSet, function);
		this.indexes.put(index + 10000, (NavigableSet<Tuple2<?, T>>) descendingIndexSet);
	}
	
	public void addUses()
	{
		worked = true;
		if (this.databaseSet!=null) {
			this.databaseSet.addUses();
		}
	}
	public void outUses()
	{
		worked = false;
		if (this.databaseSet!=null) {
			this.databaseSet.outUses();
		}
	}
	
	public int size() {
		this.addUses();
		int u = this.map.size();
		this.outUses();
		return u;
	}
	
	public U get(T key)
	{

		if (DCSet.isStoped()) {
			return null;
		}
		
		this.addUses();
		
		try
		{
			if(this.map.containsKey(key))
			{
				U u = this.map.get(key);
				this.outUses();
				return u;
			}
			else
			{
				if(this.deleted == null || !this.deleted.contains(key))
				{
					if(this.parent != null)
					{
						U u = this.parent.get(key);
						this.outUses();
						return u;
					}
				}
			}
			
			U u = this.getDefaultValue();
			this.outUses();
			return u;
		}
		catch(Exception e)
		{
			
			U u = this.getDefaultValue();
			this.outUses();
			return u;
		}			
	}
	
	public Set<T> getKeys()
	{
		
		this.addUses();
		Set<T> u = this.map.keySet();
		this.outUses();
		return u;
	}

	public Collection<U> getValues(int count, boolean descending)
	{
		this.addUses();
		Iterator<T> u;
		
		if(descending)
		{
			u = ((NavigableMap<T, U>) this.map).descendingKeySet().iterator();
		} else { 		
			u = ((NavigableMap<T, U>) this.map).keySet().iterator();
		}

		Collection<U> v = new ArrayList<U>();
		int i=0;
		while (u.hasNext() && i++ < count) {
			v.add(this.map.get(u.next()));
		}
		
		this.outUses();
		return v;
	}

	public Collection<U> getValuesAll()
	{
		this.addUses();
		Collection<U> u = this.map.values();
		this.outUses();
		return u;
	}
	
	public boolean set(T key, U value)
	{
		if (DCSet.isStoped()) {
			return false;
		}
		
		this.addUses();
		try
		{
			
			U old = this.map.put(key, value);
			
			if(this.deleted != null)
			{
				this.deleted.remove(key);
			}
			
			//COMMIT and NOTIFY if not FORKED
			if(this.parent == null)
			{
				// IT IS NOT FORK
				if(!(this.databaseSet instanceof DWSet
						&& Controller.getInstance().isProcessingWalletSynchronize()))
				{
					// TODO 
					/////this.databaseSet.commit();
				}

				if (this.parent == null) {
					//NOTIFY ADD
					if(this.getObservableData().containsKey(DBMap.NOTIFY_ADD) && !DCSet.isStoped())
					{
						this.setChanged();
						if ( this.getObservableData().get(DBMap.NOTIFY_ADD).equals( ObserverMessage.ADD_AT_TX_TYPE)
								|| this.getObservableData().get(DBMap.NOTIFY_ADD).equals( ObserverMessage.ADD_UNC_TRANSACTION_TYPE ))
						{
							this.notifyObservers(new ObserverMessage(this.getObservableData().get(DBMap.NOTIFY_ADD),
									new Pair<T,U>(key,value)));
						}
						else
						{
							this.notifyObservers(new ObserverMessage(this.getObservableData().get(DBMap.NOTIFY_ADD), value));
						}
					}
					
					if(this.getObservableData().containsKey(DBMap.NOTIFY_COUNT))
					{
						this.setChanged();
						this.notifyObservers(new ObserverMessage(this.getObservableData().get(DBMap.NOTIFY_COUNT), this.map.size()));
					}
				}
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
	
	public void delete(T key) 
	{
		
		if (DCSet.isStoped()) {
			return;
		}

		this.addUses();

		//try
		if (true)
		{
			//REMOVE
			if(this.map.containsKey(key))
			{
				U value = this.map.remove(key);
				
				//NOTIFY REMOVE
				if(this.parent == null) {
					if (this.getObservableData().containsKey(DBMap.NOTIFY_REMOVE))
					{
						this.setChanged();
						if (this.getObservableData().get(DBMap.NOTIFY_REMOVE).equals( ObserverMessage.REMOVE_AT_TX ))
						{
							this.notifyObservers(new ObserverMessage(this.getObservableData().get(DBMap.NOTIFY_REMOVE),
									new Pair<T,U>(key,value)));
						}
						else
						{
							this.notifyObservers(new ObserverMessage(this.getObservableData().get(DBMap.NOTIFY_REMOVE), value));
						}
					}
					
					if(this.getObservableData().containsKey(DBMap.NOTIFY_COUNT))
					{
						this.setChanged();
						this.notifyObservers(new ObserverMessage(this.getObservableData().get(DBMap.NOTIFY_COUNT), this.map.size()));
					}
				}
			}
			
			if(this.deleted != null)
			{
				this.deleted.add(key);
			}
			
			//COMMIT
			if(this.parent == null)
			{
				// IT IS NOT FORK
				if(!(this.databaseSet instanceof DWSet
						&& Controller.getInstance().isProcessingWalletSynchronize()))
				{
					/////this.databaseSet.commit();
				}
			}
		}
		//catch(Exception e)
		else
		{
			//LOGGER.error(e.getMessage(),e);
		}
		
		this.outUses();

	}
	
	public boolean contains(T key)
	{
		
		if (DCSet.isStoped()) {
			return false;
		}
		
		this.addUses();

		if(this.map.containsKey(key))
		{
			this.outUses();
			return true;
		}
		else
		{
			if(this.deleted == null || !this.deleted.contains(key))
			{
				if(this.parent != null)
				{
					boolean u = this.parent.contains(key);
					
					this.outUses();
					return u;
				}
			}
		}
		
		this.outUses();
		return false;
	}
	
	@Override
	public void addObserver(Observer o) 
	{

		// NOT for FORK
		if(this.parent != null)
			return;

		this.addUses();

		//ADD OBSERVER
		super.addObserver(o);	
		
		//NOTIFY LIST FORK
		if(this.getObservableData().containsKey(DBMap.NOTIFY_LIST))
		{
			//CREATE LIST
			SortableList<T, U> list = new SortableList<T, U>(this);
			
			//UPDATE
			o.update(null, new ObserverMessage(this.getObservableData().get(DBMap.NOTIFY_LIST), list));
		}
		
		if(this.getObservableData().containsKey(DBMap.NOTIFY_COUNT))
		{
			this.setChanged();
			this.notifyObservers(new ObserverMessage(this.getObservableData().get(DBMap.NOTIFY_COUNT), this.map.size()));
		}
		
		this.outUses();
	}
	
	public Iterator<T> getIterator(int index, boolean descending)
	{
		this.addUses();

		if(index == DBMap.DEFAULT_INDEX)
		{
			if(descending)
			{
				Iterator<T> u = ((NavigableMap<T, U>) this.map).descendingKeySet().iterator();
				this.outUses();
				return u;
			}
			
			Iterator<T> u = ((NavigableMap<T, U>) this.map).keySet().iterator();
			this.outUses();
			return u;
		}
		else
		{
			if(descending)
			{
				index += 10000;
			}
			
			IndexIterator<T> u = new IndexIterator<T>(this.indexes.get(index));
			this.outUses();
			return u;
		}
	}

	public SortableList<T, U> getList() 
	{
		this.addUses();
		SortableList<T, U> u = new SortableList<T, U>(this);
		this.outUses();
		return u;
	}
	
	public SortableList<T, U> getParentList()
	{
		this.addUses();

		if (this.parent!=null)
		{
			SortableList<T, U> u = new SortableList<T, U>(this.parent);
			this.outUses();
			return u;
		}
		this.outUses();
		return null;
	}
	
	public void reset() 
	{
		this.addUses();

		//RESET MAP
		this.map.clear();
		
		//RESET INDEXES
		for(Set<Tuple2<?, T>> set: this.indexes.values())
		{
			set.clear();
		}
		
		//NOTIFY LIST
		if(this.getObservableData().containsKey(DBMap.NOTIFY_RESET))
		{
			//CREATE LIST
			/////SortableList<T, U> list = new SortableList<T, U>(this);
			
			//UPDATE
			this.setChanged();
			this.notifyObservers(new ObserverMessage(this.getObservableData().get(DBMap.NOTIFY_RESET), null));
		}

		if(this.getObservableData().containsKey(DBMap.NOTIFY_COUNT))
		{
			this.setChanged();
			this.notifyObservers(new ObserverMessage(this.getObservableData().get(DBMap.NOTIFY_COUNT), 0));
		}

		this.outUses();
	}
}
