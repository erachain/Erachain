package database;

import java.util.HashMap;
import java.util.Map;

import org.mapdb.DB;

import core.naming.Name;
//import database.DBSet;
import database.serializer.NameSerializer;

// Person contains an addresses
public class PersonAddressMap extends DBMap<Long, String> 
{
	private Map<Integer, Integer> observableData = new HashMap<Integer, Integer>();
	
	public PersonAddressMap(DBSet databaseSet, DB database)
	{
		super(databaseSet, database);
	}

	public PersonAddressMap(PersonAddressMap parent) 
	{
		super(parent);
	}
	
	protected void createIndexes(DB database){}

	@Override
	protected Map<Long, String> getMap(DB database) 
	{
		//OPEN MAP
		return database.createTreeMap("person_address")
				.valueSerializer(new NameSerializer())
				.makeOrGet();
	}

	@Override
	protected Map<Long, String> getMemoryMap() 
	{
		return new HashMap<Long, String>();
	}

	@Override
	protected String getDefaultValue() 
	{
		return null;
	}
	
	@Override
	protected Map<Integer, Integer> getObservableData() 
	{
		return this.observableData;
	}
		
}
