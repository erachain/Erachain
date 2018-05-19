package datachain;

import java.util.Map;

import org.mapdb.BTreeKeySerializer;
import org.mapdb.DB;

import com.google.common.primitives.UnsignedBytes;

public class IssuePollMap extends Issue_ItemMap
{
	
	public IssuePollMap(DCSet databaseSet, DB database)
	{
		super(databaseSet, database);
	}

	public IssuePollMap(IssuePollMap parent) 
	{
		super(parent);
	}
	
	@Override
	protected Map<byte[], Long> getMap(DB database) 
	{
		//OPEN MAP
		return database.createTreeMap("poll_OrphanData")
				.keySerializer(BTreeKeySerializer.BASIC)
				.comparator(UnsignedBytes.lexicographicalComparator())
				.counterEnable()
				.makeOrGet();
	}

}
