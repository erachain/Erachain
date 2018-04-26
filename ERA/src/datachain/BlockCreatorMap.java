package datachain;



import java.util.Map;

import org.mapdb.BTreeKeySerializer;
import org.mapdb.DB;

/*
 * Block Height ->
 *  creator public key 32 bytes
 *  ?+ winValue - long -> 8 bytes
 *
 */
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
	@Override
	protected Map<Integer, byte[]> getMap(DB database)
	{
		//OPEN MAP
		return database.createTreeMap(NAME)
				.keySerializer(BTreeKeySerializer.BASIC)
				.counterEnable()
				.makeOrGet();
	}

}