package database;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.mapdb.BTreeMap;
import org.mapdb.Bind;

//import java.util.HashMap;
//import java.util.Map;

//import org.mapdb.Atomic;
import org.mapdb.DB;
import org.mapdb.Fun;
import org.mapdb.Fun.Function2;
import org.mapdb.Fun.Tuple2;

import core.account.Account;
import core.item.ItemCls;
import core.item.persons.PersonCls;
import core.transaction.Transaction;
import utils.ObserverMessage;
import utils.Pair;
import utils.ReverseComparator;
import database.DBSet;
//import database.serializer.PersonSerializer;
import database.serializer.ItemSerializer;

public class ItemPersonMap extends Item_Map
{

	static final String NAME = "item_persons";
	static final int TYPE = ItemCls.PERSON_TYPE;
	private NavigableSet person_Name_Index;
	private  BTreeMap<Long, ItemCls> person_Map;
	public static final int PERSON_NAME_INDEX = 1;
	private NavigableSet<Tuple2<String, Long>> name_Index;
	private NavigableSet<Tuple2<String, Long>> name_descending_Index;

	public ItemPersonMap(DBSet databaseSet, DB database)
	{
		super(databaseSet, database,
				//TYPE,
				NAME,
				ObserverMessage.ADD_PERSON_TYPE,
				ObserverMessage.REMOVE_PERSON_TYPE,
				ObserverMessage.LIST_PERSON_TYPE
				);
			}


	public ItemPersonMap(Item_Map parent) 
	{
		super(parent);
	}


	
	
	
	
	
	// type+name not initialized yet! - it call as Super in New
	@SuppressWarnings("unchecked")
	protected Map<Long, ItemCls> getMap(DB database) 
	{
		
		//OPEN MAP
		person_Map = database.createTreeMap(NAME)
				.valueSerializer(new ItemSerializer(TYPE))
				//.valueSerializer(new PersonSerializer())
				.makeOrGet();
		
		 // open name index
		 this.person_Name_Index = database.createTreeSet("person_name_index")
					.comparator(Fun.COMPARATOR)
					.makeOrGet();
		// create index	
			Bind.secondaryKey(person_Map, this.person_Name_Index, new Fun.Function2<String, Long, ItemCls>(){
				@Override
				public String run(Long a, ItemCls b) {
					// TODO Auto-generated method stub
					PersonCls person = (PersonCls)b;
					return person.getName();
				}
			});
			
			
	
			
		 return person_Map;	
	}
	
	
	
	@SuppressWarnings("unchecked")
	protected void createIndexes(DB database)
	{
		//NAME INDEX
		 name_Index = database.createTreeSet("pp")
				.comparator(Fun.COMPARATOR)
				.makeOrGet();
				
		name_descending_Index = database.createTreeSet("ppd")
				.comparator(new ReverseComparator(Fun.COMPARATOR))
				.makeOrGet();	
		
		createIndex(PERSON_NAME_INDEX, name_Index, name_descending_Index, new Fun.Function2<String, Long, ItemCls>(){
			@Override
			public String run(Long a, ItemCls b) {
				// TODO Auto-generated method stub
				PersonCls person = (PersonCls)b;
				return person.getName();
			}
		});
		
	}
	
	public NavigableSet<Tuple2<String, Long>> get_Name_Index(){
		
		return name_Index;
		
	}
	
	public NavigableSet<Tuple2<String, Long>> name_descending_Index(){
		
		return name_descending_Index;
		
	}
	
	
	@SuppressWarnings("rawtypes")
	public PersonCls get_Indexes(String str){
		PersonCls sss = null;
		 Iterator<Tuple2<String, Long>> ssi = this.name_Index.iterator();	
		
		 Pattern pat = Pattern.compile(".*" + str + ".*");
		 while (ssi.hasNext()){
			 Tuple2<String, Long> as = ssi.next(); 
			 Matcher matcher = pat.matcher(as.a);
				if(matcher.find())
				{
					sss = (PersonCls) this.get(as.b);
					sss=sss;
				}
		
		 
		 }
			
		
		
		return sss;
		
		
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List<PersonCls> getPerson_By_Name(String str)
	{
	//	Iterable keys = Fun.filter(this.person_Name_Index, str);
	//	Iterator iter = keys.iterator();
		Iterator <Tuple2<String,Long>> iter = this.person_Name_Index.iterator();
		List<PersonCls> txs = new ArrayList<>();
		if (str.equals("") || str == null) return null;
		
		while ( iter.hasNext() )
		{
			Tuple2<String, Long> iterator_Step = iter.next();
			
			
			if(iterator_Step.a.contains(str))	txs.add((PersonCls)this.person_Map.get(iterator_Step.b));
			
		}
		return txs;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List<PersonCls> getPersonsAll()
	{
	//	Iterable keys = Fun.filter(this.person_Name_Index, str);
	//	Iterator iter = keys.iterator();
	//	Iterator <Tuple2<String,Long>> iter = this.person_Name_Index.iterator();
		List<PersonCls> txs = new ArrayList<>();
		
		Iterator<Long> iter = this.getIterator(0,false);
		while ( iter.hasNext() )
		{
			 Long iterator_Step = iter.next();
			
			
		txs.add((PersonCls)this.person_Map.get(iterator_Step));
			
		}
		
		
		
		
		return txs;
	}
	/*
	protected long getKey()
	{
		return this.key;
	}
	
	protected void createIndexes(DB database){}

	@Override
	protected Map<Long, PersonCls> getMap(DB database) 
	{
		//OPEN MAP
		return database.createTreeMap("item_persons")
				.valueSerializer(new PersonSerializer())
				.makeOrGet();
	}

	@Override
	protected Map<Long, PersonCls> getMemoryMap() 
	{
		return new HashMap<Long, PersonCls>();
	}

	@Override
	protected PersonCls getDefaultValue() 
	{
		return null;
	}
	
	@Override
	protected Map<Integer, Integer> getObservableData() 
	{
		return this.observableData;
	}
	
	public long add(PersonCls person)
	{
		//INCREMENT ATOMIC KEY IF EXISTS
		if(this.atomicKey != null)
		{
			this.atomicKey.incrementAndGet();
		}
		
		//INCREMENT KEY
		this.key++;
		
		//INSERT WITH NEW KEY
		this.set(this.key, person);
		
		//RETURN KEY
		return this.key;
	}
	
	
	public void delete(long key)
	{
		super.delete(key);
		
		//DECREMENT ATOMIC KEY IF EXISTS
		if(this.atomicKey != null)
		{
			this.atomicKey.decrementAndGet();
		}
		
		//DECREMENT KEY
		 this.key = key - 1;
	}
	*/
}
