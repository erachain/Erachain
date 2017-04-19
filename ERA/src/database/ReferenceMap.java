package database;

import java.util.HashMap;
import java.util.Map;

import org.mapdb.DB;
import org.mapdb.Fun.Tuple2;

import core.account.Account;
import database.DBSet;

// seek reference to tx_Parent by address
// account.address -> <tx2.parentTimestamp>
public class ReferenceMap extends DBMap<String, Long> 
{
	private Map<Integer, Integer> observableData = new HashMap<Integer, Integer>();
	
	public ReferenceMap(DBSet databaseSet, DB database)
	{
		super(databaseSet, database);
	}

	public ReferenceMap(ReferenceMap parent) 
	{
		super(parent, null);
	}
	
	protected void createIndexes(DB database){}

	@Override
	
	protected Map<String, Long> getMap(DB database) 
	{
		//OPEN MAP
		return database.getTreeMap("references");
	}

	@Override
	protected Map<String, Long> getMemoryMap() 
	{
		return new HashMap<String, Long>();
	}

	@Override
	protected Long getDefaultValue() 
	{
		// NEED for toByte for nit referenced accounts
		return 0l;
	}
	
	@Override
	protected Map<Integer, Integer> getObservableData() 
	{
		return this.observableData;
	}
			
}
