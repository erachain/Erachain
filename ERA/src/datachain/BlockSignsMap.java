package datachain;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.mapdb.BTreeKeySerializer;
import org.mapdb.BTreeMap;
import org.mapdb.Bind;
import org.mapdb.DB;
import org.mapdb.Fun;
import org.mapdb.Atomic;
import org.mapdb.Atomic.Var;
import org.mapdb.Fun.Tuple2;

import com.google.common.primitives.UnsignedBytes;

import controller.Controller;
import core.BlockChain;
import core.block.Block;

// block.signature -> Height, Weight
public class BlockSignsMap extends DCMap<byte[], Tuple2<Integer, Long>> 
{
	private Map<Integer, Integer> observableData = new HashMap<Integer, Integer>();
		
	// for saving in DB
	private Atomic.Long fullWeightVar;
	private Long fullWeight = 0L;
	private int startedInForkHeight = 0;
	
	public BlockSignsMap(DCSet databaseSet, DB database)
	{
		super(databaseSet, database);
		
		this.fullWeightVar = database.getAtomicLong("fullWeight");
		this.fullWeight = this.fullWeightVar.get();
		if (this.fullWeight == null)
			this.fullWeight = 0L;
		
		//startedInForkHeight = 0;
	}

	public BlockSignsMap(BlockSignsMap parent, DCSet dcSet) 
	{
		super(parent, dcSet);
		this.fullWeight = parent.getFullWeight();
		//startedInForkHeight
	}

	protected void createIndexes(DB database){}

	@Override
	protected Map<byte[], Tuple2<Integer, Long>> getMap(DB database) 
	{
		//OPEN MAP
		return database.createTreeMap("height")
			.keySerializer(BTreeKeySerializer.BASIC)
			.comparator(UnsignedBytes.lexicographicalComparator())
			.makeOrGet();
	}

	@Override
	protected Map<byte[], Tuple2<Integer, Long>> getMemoryMap() 
	{
		return new TreeMap<byte[], Tuple2<Integer, Long>>(UnsignedBytes.lexicographicalComparator());
	}

	@Override
	protected Tuple2<Integer, Long> getDefaultValue() 
	{
		return new Tuple2<Integer, Long>(-1,-1L);
	}
	
	public Long getFullWeight() {
		return this.fullWeight;
	}
	
	/*
	public void setFullWeight(long value) {
		
		fullWeight = value;
		if(this.fullWeightVar != null)
		{
			this.fullWeightVar.set(fullWeight);
		}
	}
	*/
	
	public int getStartedInForkHeight() {
		return this.startedInForkHeight;
	}
	
	
	@Override
	protected Map<Integer, Integer> getObservableData() 
	{
		return this.observableData;
	}
	
	public Tuple2<Integer, Long> get(Block block)
	{
		return this.get(block.getSignature());
	}
	
	public Block getBlock(byte[] signature)
	{
		Tuple2<Integer, Long> key = this.get(signature);
		if (key != null && key.a > 0)
			return this.getDCSet().getBlockMap().get(key.a);
		
		return null;
	}

	public Integer getHeight(Block block)
	{
		if (this.contains(block.getSignature()))
			return this.get(block.getSignature()).a;
		return -1;
	}
	public Integer getHeight(byte[] signature)
	{
		if (this.contains(signature)) {
			Tuple2<Integer, Long> o = this.get(signature);
			if (o != null)
				return this.get(signature).a;
		}
		return -1;
	}
	public Long getWeight(Block block)
	{
		if (this.contains(block.getSignature()))
			return this.get(block.getSignature()).b;
		return 0L;
	}
	
	public void recalcWeightFull(DCSet dcSet) {
		
		long weightFull = 0l;
		Iterator<byte[]> iterator = this.getIterator(0, true);
		while (iterator.hasNext()) {
			byte[] key = iterator.next();
			Tuple2<Integer, Long> hw = this.get(key);
			weightFull += hw.b;
		}
						
		fullWeight = weightFull;
		this.fullWeightVar.set(fullWeight);
		
	}

	public boolean set(byte[] key, int height, long weight)
	{
		if (this.contains(key)) {
			// sub old value from FULL
			Tuple2<Integer, Long> value_old = this.get(key);
			fullWeight -= value_old.b;
		}
		
		if (startedInForkHeight == 0 && this.parent != null) {
			startedInForkHeight = height;
		}
		
		fullWeight += weight;
		
		if(this.fullWeightVar != null)
		{
			this.fullWeightVar.set(fullWeight);
		}

		return super.set(key, new Tuple2<Integer, Long>(height, weight));
	}

	public void delete(byte[] key)
	{
		if (this.contains(key)) {
			// sub old value from FULL
			Tuple2<Integer, Long> value_old = this.get(key);
			fullWeight -= value_old.b;

			if(this.fullWeightVar != null)
			{
				this.fullWeightVar.set(fullWeight);
			}
			
			super.delete(key);
		}
		
	}
}