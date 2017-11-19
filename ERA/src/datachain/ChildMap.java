package datachain;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.mapdb.BTreeKeySerializer;
import org.mapdb.DB;

import com.google.common.primitives.UnsignedBytes;

import core.block.Block;
import database.DBSet;

public class ChildMap extends DCMap<byte[], byte[]> 
{
	private Map<Integer, Integer> observableData = new HashMap<Integer, Integer>();
	
	//private BlockMap blockMap;
	
	public ChildMap(DCSet databaseSet, DB database)
	{
		super(databaseSet, database);
	}

	// dbSet - current DBSet for access to others Maps
	public ChildMap(ChildMap parent, DCSet dbSet) 
	{
		super(parent, dbSet);
	}
	
	protected void createIndexes(DB database){}

	@Override
	protected Map<byte[], byte[]> getMap(DB database) 
	{
		//OPEN MAP
		return database.createTreeMap("children")
				.keySerializer(BTreeKeySerializer.BASIC)
				.comparator(UnsignedBytes.lexicographicalComparator())
				.makeOrGet();
	}

	@Override
	protected Map<byte[], byte[]> getMemoryMap() 
	{
		return new TreeMap<byte[], byte[]>(UnsignedBytes.lexicographicalComparator());
	}

	@Override
	protected byte[] getDefaultValue() 
	{
		return null;
	}
	
	@Override
	protected Map<Integer, Integer> getObservableData() 
	{
		return this.observableData;
	}
	
	public Block get(Block parent)
	{
		if(this.contains(parent.getSignature()))
		{
			return this.getDCSet().getBlockMap().get(this.get(parent.getSignature()));
		}
		
		return null;
	}
	
	public void set(Block parent, Block child)
	{
		this.set(parent.getSignature(), child.getSignature());
	}
	public Block getChildBlock(Block parent)
	{
		if(this.map.containsKey(parent.getSignature()))
		{
			byte[] key = map.get(parent.getSignature());
			if (this.getDCSet().getBlockMap().contains(key))	return this.getDCSet().getBlockMap().get(key);
		}
		
		return null;
	}
	
	public byte[] getChildBlock(byte[] parent)
	{
		if(this.contains(parent))
		{
			return this.get(parent);
		}
		
		return null;
	}
}