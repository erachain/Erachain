package datachain;



import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.mapdb.Atomic;
import org.mapdb.BTreeKeySerializer;
import org.mapdb.DB;
import org.mapdb.Fun.Tuple2;

import core.block.Block;
import core.item.ItemCls;
import database.serializer.ItemSerializer;

// Block Height -> creator
public class BlockCreatorMap extends AutoIntegerByte 
{

	static final String NAME = "block_creators";

	public BlockCreatorMap(DCSet databaseSet, DB database)
	{
		super(databaseSet, database, NAME);		
	}
	
	public BlockCreatorMap(BlockCreatorMap parent) 
	{
		super(parent);
	}

	// type+name not initialized yet! - it call as Super in New
	protected Map<Integer, byte[]> getMap(DB database) 
	{
		//OPEN MAP
		return database.createTreeMap(NAME)
				.keySerializer(BTreeKeySerializer.BASIC)
				.makeOrGet();
	}
	
}