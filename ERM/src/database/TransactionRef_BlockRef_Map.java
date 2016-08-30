package database;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.mapdb.BTreeKeySerializer;
import org.mapdb.DB;
//import org.mapdb.Fun.Tuple2;

import com.google.common.primitives.UnsignedBytes;

import core.block.Block;
import core.transaction.Transaction;
import database.DBSet;

// tx.signature (child) -> block.signature (parent)
public class TransactionRef_BlockRef_Map extends DBMap<byte[], byte[]> 
{
	private Map<Integer, Integer> observableData = new HashMap<Integer, Integer>();
	
	//private BlockMap blockMap;
	
	public TransactionRef_BlockRef_Map(DBSet databaseSet, DB database)
	{
		super(databaseSet, database);
		//this.blockMap = databaseSet.getBlockMap();
	}

	public TransactionRef_BlockRef_Map(TransactionRef_BlockRef_Map parent, DBSet dbSet) 
	{
		super(parent, dbSet);
		//this.blockMap = blockMap;
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
	
	public Block getParent(byte[] txSignature)
	{
		
		byte[] bs = this.get(txSignature);
		if(bs == null)
		{
			return null;
		}
		
		DBSet dbSet = (DBSet)this.databaseSet;
		return dbSet.getBlockMap().get(bs);
	}
	
	public void set(Transaction child, Block parent)
	{
		this.set(child.getSignature(), parent.getSignature());
	}
}
