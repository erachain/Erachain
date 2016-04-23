package database;

import java.util.HashMap;
import java.util.Map;

import org.mapdb.DB;

import core.naming.Name;
//import database.DBSet;
import database.serializer.NameSerializer;

public class AddressPersonMap extends DBMap<String, Long> 
{
	private Map<Integer, Integer> observableData = new HashMap<Integer, Integer>();
	
	public AddressPersonMap(DBSet databaseSet, DB database)
	{
		super(databaseSet, database);
	}

	public AddressPersonMap(AddressPersonMap parent) 
	{
		super(parent);
	}
	
	protected void createIndexes(DB database){}

	@Override
	protected Map<String, Long> getMap(DB database) 
	{
		//OPEN MAP
		return database.createTreeMap("address_person")
				.valueSerializer(new NameSerializer())
				.makeOrGet();
	}

	@Override
	protected Map<String, Long> getMemoryMap() 
	{
		return new HashMap<String, Long>();
	}

	@Override
	protected Long getDefaultValue() 
	{
		return -1L;
	}
	
	@Override
	protected Map<Integer, Integer> getObservableData() 
	{
		return this.observableData;
	}
	
}
